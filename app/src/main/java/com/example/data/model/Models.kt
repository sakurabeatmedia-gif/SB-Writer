package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas Folder / Proyek untuk mengelompokkan buku & karya.
 */
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#E53935",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Entitas Buku / Novel utama.
 */
@Entity(
    tableName = "books",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("folderId")]
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String = "SB_Media Author",
    val synopsis: String = "",
    val folderId: Long? = null,
    val coverPath: String? = null,
    val coverPreset: String? = "sb_dark",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val tags: String = "",
    val genre: String = "Novel",
    val targetWordCount: Int = 50000,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Entitas Bab / Chapter dalam buku.
 */
@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId")]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val volumeName: String? = null,
    val title: String,
    val content: String = "",
    val orderIndex: Int = 0,
    val wordCount: Int = 0,
    val charCount: Int = 0,
    val lastCursorPosition: Int = 0,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Riwayat revisi dan snapshot bab.
 */
@Entity(
    tableName = "chapter_revisions",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chapterId")]
)
data class ChapterRevisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val title: String,
    val content: String,
    val wordCount: Int,
    val note: String = "Snapshot Otomatis",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Catatan cepat, jurnal ide, karakter, atau daftar tugas.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long? = null,
    val title: String,
    val content: String = "",
    val category: String = "Catatan Cepat", // Ide, Karakter, Plot, Jurnal, Tugas
    val tags: String = "",
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val isCompleted: Boolean = false, // untuk to-do list
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Log progres penulisan harian untuk grafik statistik riil.
 */
@Entity(tableName = "daily_writing_logs")
data class DailyWritingLogEntity(
    @PrimaryKey val dateString: String, // Format: YYYY-MM-DD
    val wordsAdded: Int = 0,
    val minutesSpent: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
