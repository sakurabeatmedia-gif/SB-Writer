package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.ChapterRevisionEntity
import com.example.data.model.DailyWritingLogEntity
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- BUKU ---
    @Query("""
        SELECT * FROM books 
        WHERE isDeleted = 0 AND isArchived = 0 
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getActiveBooks(): Flow<List<BookEntity>>

    @Query("""
        SELECT * FROM books 
        WHERE isDeleted = 0 AND isArchived = 1 
        ORDER BY updatedAt DESC
    """)
    fun getArchivedBooks(): Flow<List<BookEntity>>

    @Query("""
        SELECT * FROM books 
        WHERE isDeleted = 0 AND isFavorite = 1 
        ORDER BY updatedAt DESC
    """)
    fun getFavoriteBooks(): Flow<List<BookEntity>>

    @Query("""
        SELECT * FROM books 
        WHERE isDeleted = 0 AND folderId = :folderId 
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getBooksByFolder(folderId: Long): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: Long): BookEntity?

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    fun observeBookById(id: Long): Flow<BookEntity?>

    @Query("""
        SELECT * FROM books 
        WHERE isDeleted = 1 
        ORDER BY deletedAt DESC
    """)
    fun getDeletedBooks(): Flow<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("UPDATE books SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :bookId")
    suspend fun softDeleteBook(bookId: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE books SET isDeleted = 0, deletedAt = NULL WHERE id = :bookId")
    suspend fun restoreBook(bookId: Long)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun hardDeleteBook(bookId: Long)

    @Query("DELETE FROM books WHERE isDeleted = 1")
    suspend fun emptyBookTrash()


    // --- BAB / CHAPTER ---
    @Query("""
        SELECT * FROM chapters 
        WHERE bookId = :bookId AND isDeleted = 0 
        ORDER BY orderIndex ASC, id ASC
    """)
    fun getChaptersForBook(bookId: Long): Flow<List<ChapterEntity>>

    @Query("""
        SELECT * FROM chapters 
        WHERE bookId = :bookId AND isDeleted = 0 
        ORDER BY orderIndex ASC, id ASC
    """)
    suspend fun getChaptersListForBook(bookId: Long): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
    suspend fun getChapterById(chapterId: Long): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
    fun observeChapterById(chapterId: Long): Flow<ChapterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("UPDATE chapters SET isDeleted = 1 WHERE id = :chapterId")
    suspend fun softDeleteChapter(chapterId: Long)

    @Query("UPDATE chapters SET isDeleted = 0 WHERE id = :chapterId")
    suspend fun restoreChapter(chapterId: Long)

    @Query("DELETE FROM chapters WHERE id = :chapterId")
    suspend fun hardDeleteChapter(chapterId: Long)

    @Query("SELECT COUNT(*) FROM chapters WHERE bookId = :bookId AND isDeleted = 0")
    suspend fun getChapterCountForBook(bookId: Long): Int

    @Query("SELECT COALESCE(SUM(wordCount), 0) FROM chapters WHERE bookId = :bookId AND isDeleted = 0")
    suspend fun getTotalWordsForBook(bookId: Long): Int

    @Query("SELECT COALESCE(SUM(wordCount), 0) FROM chapters WHERE isDeleted = 0")
    fun observeGlobalWordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM chapters WHERE isDeleted = 0")
    fun observeGlobalChapterCount(): Flow<Int>


    // --- RIWAYAT REVISI ---
    @Query("""
        SELECT * FROM chapter_revisions 
        WHERE chapterId = :chapterId 
        ORDER BY timestamp DESC
    """)
    fun getRevisionsForChapter(chapterId: Long): Flow<List<ChapterRevisionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(revision: ChapterRevisionEntity): Long

    @Query("DELETE FROM chapter_revisions WHERE id = :revisionId")
    suspend fun deleteRevision(revisionId: Long)


    // --- FOLDER ---
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolder(folderId: Long)


    // --- CATATAN ---
    @Query("""
        SELECT * FROM notes 
        WHERE isDeleted = 0 
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getActiveNotes(): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes 
        WHERE isDeleted = 0 AND category = :category 
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getNotesByCategory(category: String): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes 
        WHERE isDeleted = 0 AND bookId = :bookId 
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getNotesForBook(bookId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getDeletedNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("UPDATE notes SET isDeleted = 1 WHERE id = :noteId")
    suspend fun softDeleteNote(noteId: Long)

    @Query("UPDATE notes SET isDeleted = 0 WHERE id = :noteId")
    suspend fun restoreNote(noteId: Long)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun hardDeleteNote(noteId: Long)

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun emptyNoteTrash()


    // --- LOG HARIAN ---
    @Query("SELECT * FROM daily_writing_logs WHERE dateString = :dateString LIMIT 1")
    suspend fun getDailyLog(dateString: String): DailyWritingLogEntity?

    @Query("SELECT * FROM daily_writing_logs ORDER BY dateString DESC LIMIT 14")
    fun getRecentDailyLogs(): Flow<List<DailyWritingLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyLog(log: DailyWritingLogEntity)


    // --- PENCARIAN GLOBAL ---
    @Query("""
        SELECT * FROM books 
        WHERE isDeleted = 0 
        AND (title LIKE '%' || :query || '%' OR synopsis LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%')
        ORDER BY updatedAt DESC
    """)
    fun searchBooks(query: String): Flow<List<BookEntity>>

    @Query("""
        SELECT * FROM chapters 
        WHERE isDeleted = 0 
        AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY updatedAt DESC
    """)
    fun searchChapters(query: String): Flow<List<ChapterEntity>>

    @Query("""
        SELECT * FROM notes 
        WHERE isDeleted = 0 
        AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%')
        ORDER BY updatedAt DESC
    """)
    fun searchNotes(query: String): Flow<List<NoteEntity>>
}
