package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.ChapterRevisionEntity
import com.example.data.model.DailyWritingLogEntity
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        ChapterRevisionEntity::class,
        NoteEntity::class,
        FolderEntity::class,
        DailyWritingLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sb_writer_db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database.appDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: AppDao) {
            // Folder Bawaan
            val folderId = dao.insertFolder(
                FolderEntity(
                    name = "Karya Utama",
                    colorHex = "#E53935",
                    description = "Proyek novel utama SB_Media"
                )
            )

            // Buku Pertama
            val bookId = dao.insertBook(
                BookEntity(
                    title = "Bayang-bayang Kota Senja",
                    author = "SB_Media Production",
                    synopsis = "Sebuah novel thriller psikologis tentang detektif yang mengungkap misteri di balik gelombang frekuensi radio tua.",
                    folderId = folderId,
                    coverPreset = "sb_dark",
                    isPinned = true,
                    genre = "Mystery / Thriller",
                    tags = "Misteri, Thriller, Senja, Urban",
                    targetWordCount = 60000
                )
            )

            // Bab 1
            dao.insertChapter(
                ChapterEntity(
                    bookId = bookId,
                    title = "Bab 1: Frekuensi yang Hilang",
                    orderIndex = 1,
                    volumeName = "Bagian Satu",
                    content = """Malam di sudut kota itu tidak pernah benar-benar tidur. Hujan gerimis menyisakan genangan di aspal yang memantulkan kerlip lampu neon usang bertuliskan SB_MEDIA.

Reyhan menghela napas panjang, menatap pemancar radio analog yang berdebu di hadapannya. Jarum penunjuk gelombang tiba-tiba bergetar tanpa disentuh. Suara statis merayap keluar dari pengeras suara kuno.

"Jika kamu mendengar ini... berarti gerbangnya telah terbuka."

Sebuah bisikan lirih, dingin, namun terasa sangat dekat—seolah si pembicara berdiri persis di balik tengkuknya. Reyhan tersentak, mencengkeram tepi meja kayu. Catatan tua di hadapannya terbuka di halaman yang sama: tanggal yang sama dengan malam ini, sepuluh tahun yang lalu.""",
                    wordCount = 98,
                    charCount = 692
                )
            )

            // Bab 2
            dao.insertChapter(
                ChapterEntity(
                    bookId = bookId,
                    title = "Bab 2: Catatan di Balik Dinding",
                    orderIndex = 2,
                    volumeName = "Bagian Satu",
                    content = """Setiap penulis memiliki rahasia yang terkubur di antara draf pertama dan naskah akhir. Bagi Reyhan, rahasia itu bukan sekadar fiksi.

Ia menyalakan rokoknya yang ketiga malam ini. Asap kelabu meliuk ke langit-langit kamar sempitnya. Di dinding kayu lapuk, peta kota telah dipenuhi benang merah yang saling silang. Di pusat benang-benang itu, satu nama terpahat tebal: Proyek Sandekala.

Langkah kaki terdengar dari lorong luar. Lambat, berirama, dan berhenti tepat di depan pintu nomor 404.""",
                    wordCount = 74,
                    charCount = 496
                )
            )

            // Catatan pembuka
            dao.insertNote(
                NoteEntity(
                    bookId = bookId,
                    title = "Profil Karakter: Reyhan Bramantyo",
                    category = "Karakter",
                    content = "Usia 32 tahun. Mantan jurnalis investigasi yang beralih menjadi penyiar radio independen. Memiliki trauma masa lalu terhadap suara frekuensi 104.4 MHz.",
                    tags = "Tokoh Utama, Protagonis",
                    isPinned = true
                )
            )

            dao.insertNote(
                NoteEntity(
                    title = "Selamat Datang di SB Writer!",
                    category = "Catatan Cepat",
                    content = "SB Writer dirancang khusus untuk novelis dan penulis profesional oleh SB_Media Production.\n\nTips Singkat:\n- Geser ikon SB_AI untuk memindahkan posisinya.\n- Ketuk SB_AI untuk meminta bantuan penulisan, ide, atau suntingan naskah.\n- Buka Editor untuk menulis bebas distraksi dengan auto-save dan riwayat revisi.",
                    tags = "Tutorial, SB_Media",
                    isPinned = true,
                    isFavorite = true
                )
            )

            // Log awal
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            dao.insertOrUpdateDailyLog(
                DailyWritingLogEntity(
                    dateString = today,
                    wordsAdded = 172,
                    minutesSpent = 15
                )
            )
        }
    }
}
