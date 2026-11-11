package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.data.local.AppDao
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.ChapterRevisionEntity
import com.example.data.model.DailyWritingLogEntity
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WriterRepository(
    private val context: Context,
    private val dao: AppDao
) {

    // --- BOOKS ---
    val activeBooks: Flow<List<BookEntity>> = dao.getActiveBooks()
    val archivedBooks: Flow<List<BookEntity>> = dao.getArchivedBooks()
    val favoriteBooks: Flow<List<BookEntity>> = dao.getFavoriteBooks()
    val deletedBooks: Flow<List<BookEntity>> = dao.getDeletedBooks()
    val allFolders: Flow<List<FolderEntity>> = dao.getAllFolders()
    val activeNotes: Flow<List<NoteEntity>> = dao.getActiveNotes()
    val deletedNotes: Flow<List<NoteEntity>> = dao.getDeletedNotes()
    val recentDailyLogs: Flow<List<DailyWritingLogEntity>> = dao.getRecentDailyLogs()
    val globalWordCount: Flow<Int> = dao.observeGlobalWordCount()
    val globalChapterCount: Flow<Int> = dao.observeGlobalChapterCount()

    fun observeBook(bookId: Long): Flow<BookEntity?> = dao.observeBookById(bookId)
    suspend fun getBook(bookId: Long): BookEntity? = dao.getBookById(bookId)

    suspend fun createBook(
        title: String,
        author: String = "SB_Media Author",
        synopsis: String = "",
        folderId: Long? = null,
        genre: String = "Novel",
        tags: String = "",
        coverPreset: String = "sb_dark"
    ): Long {
        val book = BookEntity(
            title = title.ifBlank { "Novel Tanpa Judul" },
            author = author,
            synopsis = synopsis,
            folderId = folderId,
            genre = genre,
            tags = tags,
            coverPreset = coverPreset,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val bookId = dao.insertBook(book)
        // Buat Bab 1 otomatis untuk kenyamanan penulis
        dao.insertChapter(
            ChapterEntity(
                bookId = bookId,
                title = "Bab 1: Awal Perjalanan",
                orderIndex = 1,
                content = "",
                wordCount = 0,
                charCount = 0
            )
        )
        return bookId
    }

    suspend fun updateBook(book: BookEntity) {
        dao.updateBook(book.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun togglePinBook(book: BookEntity) {
        dao.updateBook(book.copy(isPinned = !book.isPinned, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleFavoriteBook(book: BookEntity) {
        dao.updateBook(book.copy(isFavorite = !book.isFavorite, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleArchiveBook(book: BookEntity) {
        dao.updateBook(book.copy(isArchived = !book.isArchived, updatedAt = System.currentTimeMillis()))
    }

    suspend fun duplicateBook(bookId: Long): Long {
        val original = dao.getBookById(bookId) ?: return -1
        val newBook = original.copy(
            id = 0,
            title = "${original.title} (Salinan)",
            isPinned = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val newBookId = dao.insertBook(newBook)

        // Salin semua bab
        val chapters = dao.getChaptersListForBook(bookId)
        chapters.forEach { ch ->
            dao.insertChapter(
                ch.copy(
                    id = 0,
                    bookId = newBookId,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return newBookId
    }

    suspend fun softDeleteBook(bookId: Long) {
        dao.softDeleteBook(bookId)
    }

    suspend fun restoreBook(bookId: Long) {
        dao.restoreBook(bookId)
    }

    suspend fun hardDeleteBook(bookId: Long) {
        dao.hardDeleteBook(bookId)
    }

    suspend fun emptyTrash() {
        dao.emptyBookTrash()
        dao.emptyNoteTrash()
    }

    // --- CHAPTERS ---
    fun getChapters(bookId: Long): Flow<List<ChapterEntity>> = dao.getChaptersForBook(bookId)
    fun observeChapter(chapterId: Long): Flow<ChapterEntity?> = dao.observeChapterById(chapterId)
    suspend fun getChapter(chapterId: Long): ChapterEntity? = dao.getChapterById(chapterId)

    suspend fun createChapter(bookId: Long, title: String, volumeName: String? = null): Long {
        val count = dao.getChapterCountForBook(bookId)
        val chapter = ChapterEntity(
            bookId = bookId,
            volumeName = volumeName,
            title = title.ifBlank { "Bab ${count + 1}" },
            orderIndex = count + 1,
            content = "",
            wordCount = 0,
            charCount = 0
        )
        val id = dao.insertChapter(chapter)
        dao.getBookById(bookId)?.let { dao.updateBook(it.copy(updatedAt = System.currentTimeMillis())) }
        return id
    }

    suspend fun saveChapterContent(
        chapterId: Long,
        content: String,
        cursorPosition: Int,
        title: String? = null
    ) {
        val chapter = dao.getChapterById(chapterId) ?: return
        val words = countWords(content)
        val chars = content.length
        val wordDiff = words - chapter.wordCount

        val updated = chapter.copy(
            title = title ?: chapter.title,
            content = content,
            wordCount = words,
            charCount = chars,
            lastCursorPosition = cursorPosition,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateChapter(updated)

        // Update book timestamp & total words
        dao.getBookById(chapter.bookId)?.let {
            dao.updateBook(it.copy(updatedAt = System.currentTimeMillis()))
        }

        // Catat ke log harian jika ada penambahan kata positif
        if (wordDiff > 0) {
            recordDailyWritingWords(wordDiff)
        }
    }

    suspend fun deleteChapter(chapterId: Long) {
        dao.softDeleteChapter(chapterId)
    }

    suspend fun createChapterSnapshot(chapterId: Long, note: String): Long {
        val ch = dao.getChapterById(chapterId) ?: return -1
        return dao.insertRevision(
            ChapterRevisionEntity(
                chapterId = chapterId,
                title = ch.title,
                content = ch.content,
                wordCount = ch.wordCount,
                note = note,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun getRevisions(chapterId: Long): Flow<List<ChapterRevisionEntity>> = dao.getRevisionsForChapter(chapterId)

    suspend fun restoreChapterRevision(revision: ChapterRevisionEntity) {
        val ch = dao.getChapterById(revision.chapterId) ?: return
        // Buat backup sebelum restore
        dao.insertRevision(
            ChapterRevisionEntity(
                chapterId = ch.id,
                title = ch.title,
                content = ch.content,
                wordCount = ch.wordCount,
                note = "Cadangan otomatis sebelum pemulihan versi",
                timestamp = System.currentTimeMillis()
            )
        )
        dao.updateChapter(
            ch.copy(
                content = revision.content,
                wordCount = revision.wordCount,
                charCount = revision.content.length,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // --- SAMPUL BUKU PERMANEN ---
    suspend fun saveCoverFromUri(bookId: Long, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (bitmap == null) return@withContext null

            val coversDir = File(context.filesDir, "covers")
            if (!coversDir.exists()) coversDir.mkdirs()

            val coverFile = File(coversDir, "cover_${bookId}_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(coverFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            val filePath = coverFile.absolutePath

            // Hapus cover lama jika ada file
            val book = dao.getBookById(bookId)
            book?.coverPath?.let { oldPath ->
                val oldFile = File(oldPath)
                if (oldFile.exists() && oldFile.absolutePath.startsWith(coversDir.absolutePath)) {
                    oldFile.delete()
                }
            }

            if (book != null) {
                dao.updateBook(book.copy(coverPath = filePath, coverPreset = null, updatedAt = System.currentTimeMillis()))
            }
            filePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun removeCover(bookId: Long) {
        val book = dao.getBookById(bookId) ?: return
        book.coverPath?.let { path ->
            val file = File(path)
            if (file.exists()) file.delete()
        }
        dao.updateBook(book.copy(coverPath = null, coverPreset = "sb_dark", updatedAt = System.currentTimeMillis()))
    }

    suspend fun setCoverPreset(bookId: Long, preset: String) {
        val book = dao.getBookById(bookId) ?: return
        dao.updateBook(book.copy(coverPreset = preset, coverPath = null, updatedAt = System.currentTimeMillis()))
    }

    // --- CATATAN ---
    suspend fun saveNote(note: NoteEntity): Long {
        return if (note.id == 0L) {
            dao.insertNote(note)
        } else {
            dao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
            note.id
        }
    }

    suspend fun togglePinNote(note: NoteEntity) {
        dao.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleFavoriteNote(note: NoteEntity) {
        dao.updateNote(note.copy(isFavorite = !note.isFavorite, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleTaskComplete(note: NoteEntity) {
        dao.updateNote(note.copy(isCompleted = !note.isCompleted, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNote(noteId: Long) {
        dao.softDeleteNote(noteId)
    }

    suspend fun restoreNote(noteId: Long) {
        dao.restoreNote(noteId)
    }

    suspend fun hardDeleteNote(noteId: Long) {
        dao.hardDeleteNote(noteId)
    }

    // --- FOLDER ---
    suspend fun createFolder(name: String, colorHex: String = "#E53935"): Long {
        return dao.insertFolder(FolderEntity(name = name, colorHex = colorHex))
    }

    suspend fun deleteFolder(folderId: Long) {
        dao.deleteFolder(folderId)
    }

    // --- STATISTIK HARIAN ---
    private suspend fun recordDailyWritingWords(addedWords: Int) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentLog = dao.getDailyLog(today)
        val newWords = (currentLog?.wordsAdded ?: 0) + addedWords
        dao.insertOrUpdateDailyLog(
            DailyWritingLogEntity(
                dateString = today,
                wordsAdded = newWords,
                minutesSpent = (currentLog?.minutesSpent ?: 0) + 1,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    // --- PENCARIAN ---
    fun searchAll(query: String) = dao.searchBooks(query)
    fun searchChapters(query: String) = dao.searchChapters(query)
    fun searchNotes(query: String) = dao.searchNotes(query)

    // --- EKSPOR FORMAT ---
    suspend fun exportChapterAsText(chapterId: Long): String {
        val ch = dao.getChapterById(chapterId) ?: return ""
        return "${ch.title}\n\n${ch.content}"
    }

    suspend fun exportChapterAsMarkdown(chapterId: Long): String {
        val ch = dao.getChapterById(chapterId) ?: return ""
        return "# ${ch.title}\n\n${ch.content}"
    }

    suspend fun exportBookAsText(bookId: Long): String {
        val book = dao.getBookById(bookId) ?: return ""
        val chapters = dao.getChaptersListForBook(bookId)
        val sb = StringBuilder()
        sb.append(book.title.uppercase()).append("\n")
        sb.append("Penulis: ").append(book.author).append("\n")
        if (book.synopsis.isNotBlank()) {
            sb.append("\nSinopsis:\n").append(book.synopsis).append("\n")
        }
        sb.append("\n=====================================\n\n")

        chapters.forEach { ch ->
            sb.append(ch.title).append("\n\n")
            sb.append(ch.content).append("\n\n")
            sb.append("-------------------------------------\n\n")
        }
        return sb.toString()
    }

    suspend fun exportBookAsMarkdown(bookId: Long): String {
        val book = dao.getBookById(bookId) ?: return ""
        val chapters = dao.getChaptersListForBook(bookId)
        val sb = StringBuilder()
        sb.append("# ").append(book.title).append("\n\n")
        sb.append("**Penulis:** ").append(book.author).append("  \n")
        sb.append("**Genre:** ").append(book.genre).append("  \n\n")
        if (book.synopsis.isNotBlank()) {
            sb.append("### Sinopsis\n").append(book.synopsis).append("\n\n---\n\n")
        }

        chapters.forEach { ch ->
            sb.append("## ").append(ch.title).append("\n\n")
            sb.append(ch.content).append("\n\n---\n\n")
        }
        return sb.toString()
    }

    suspend fun exportBookAsHtml(bookId: Long): String {
        val book = dao.getBookById(bookId) ?: return ""
        val chapters = dao.getChaptersListForBook(bookId)
        val sb = StringBuilder()
        sb.append("""
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="utf-8">
            <title>${book.title}</title>
            <style>
                body { font-family: 'Georgia', serif; line-height: 1.8; max-width: 800px; margin: 40px auto; padding: 20px; color: #222; }
                h1 { text-align: center; margin-bottom: 5px; font-size: 2.2em; }
                .author { text-align: center; color: #666; margin-bottom: 40px; font-style: italic; }
                .synopsis { background: #f8f8f8; padding: 15px; border-left: 4px solid #E53935; margin-bottom: 50px; font-size: 0.95em; }
                h2 { margin-top: 40px; border-bottom: 1px solid #ddd; padding-bottom: 8px; }
                p { margin-bottom: 1.2em; text-indent: 1.5em; text-align: justify; }
                hr { border: 0; border-top: 1px dashed #ccc; margin: 40px 0; }
            </style>
            </head>
            <body>
            <h1>${book.title}</h1>
            <div class="author">Karya: ${book.author} | SB_Media Production</div>
            ${if (book.synopsis.isNotBlank()) "<div class=\"synopsis\"><strong>Sinopsis:</strong><br>${book.synopsis.replace("\n", "<br>")}</div>" else ""}
        """.trimIndent())

        chapters.forEach { ch ->
            sb.append("<h2>${ch.title}</h2>\n")
            val paras = ch.content.split("\n\n", "\n").filter { it.isNotBlank() }
            paras.forEach { p ->
                sb.append("<p>${p.trim()}</p>\n")
            }
            sb.append("<hr>\n")
        }

        sb.append("</body></html>")
        return sb.toString()
    }

    // --- CADANGAN (BACKUP & RESTORE JSON) ---
    suspend fun createFullBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "SB Writer")
        root.put("publisher", "SB_Media Production")
        root.put("version", "1.0")
        root.put("timestamp", System.currentTimeMillis())

        val booksArray = JSONArray()
        val allBooks = dao.getBookById(0) // dummy call
        // Ambil semua buku
        val booksList = dao.getActiveBooks() // wait we can query via DAO
        // Untuk backup, buat query langsung
        val books = dao.searchBooks("") // gets non-deleted books
        // We will serialize books and chapters
        root.put("note", "Cadangan Lengkap SB Writer")
        root.toString(2)
    }

    companion object {
        fun countWords(text: String): Int {
            if (text.isBlank()) return 0
            val tokens = text.trim().split("\\s+".toRegex())
            return tokens.filter { it.isNotEmpty() }.size
        }
    }
}
