package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.SbAiContext
import com.example.ai.SbAiPersona
import com.example.ai.SbAiService
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.ChapterRevisionEntity
import com.example.data.model.DailyWritingLogEntity
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.data.repository.WriterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Library : Screen("library")
    data class BookDetail(val bookId: Long) : Screen("book_detail")
    data class Editor(val chapterId: Long, val bookId: Long) : Screen("editor")
    object Notes : Screen("notes")
    object Stats : Screen("stats")
    object Settings : Screen("settings")
    object Trash : Screen("trash")
}

data class AiMessage(
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isProseSuggestion: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = WriterRepository(application, db.appDao())
    val prefs = PreferencesManager(application)
    private var aiService = SbAiService()

    // Navigation Stack
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Library))
    val currentScreen: StateFlow<Screen> = _screenStack.combine(_screenStack) { stack, _ ->
        stack.lastOrNull() ?: Screen.Library
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Screen.Library)

    fun navigateTo(screen: Screen) {
        _screenStack.value = _screenStack.value + screen
    }

    fun navigateBack(): Boolean {
        if (_screenStack.value.size > 1) {
            _screenStack.value = _screenStack.value.dropLast(1)
            return true
        }
        return false
    }

    // App Theme & Preferences
    val themeMode: StateFlow<String> = prefs.themeFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "dark")
    val fontFamily: StateFlow<String> = prefs.fontFamilyFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "serif")
    val fontSize: StateFlow<Float> = prefs.fontSizeFlow.stateIn(viewModelScope, SharingStarted.Eagerly, 17f)
    val lineSpacing: StateFlow<Float> = prefs.lineSpacingFlow.stateIn(viewModelScope, SharingStarted.Eagerly, 1.5f)
    val marginDp: StateFlow<Int> = prefs.marginDpFlow.stateIn(viewModelScope, SharingStarted.Eagerly, 16)
    val textAlign: StateFlow<String> = prefs.textAlignFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "left")
    val dailyWordGoal: StateFlow<Int> = prefs.dailyGoalFlow.stateIn(viewModelScope, SharingStarted.Eagerly, 1000)
    val libraryViewMode: StateFlow<String> = prefs.viewModeFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "grid")

    // Security PIN Lock
    val pinEnabled: StateFlow<Boolean> = prefs.pinEnabledFlow.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val pinHash: StateFlow<String> = prefs.pinHashFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    private val _isAppUnlocked = MutableStateFlow(false)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    fun unlockApp(pinEntered: String): Boolean {
        if (pinEntered == pinHash.value) {
            _isAppUnlocked.value = true
            return true
        }
        return false
    }

    fun lockApp() {
        if (pinEnabled.value) {
            _isAppUnlocked.value = false
        }
    }

    // Library Data
    val activeBooks: StateFlow<List<BookEntity>> = repository.activeBooks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val archivedBooks: StateFlow<List<BookEntity>> = repository.archivedBooks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val favoriteBooks: StateFlow<List<BookEntity>> = repository.favoriteBooks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val deletedBooks: StateFlow<List<BookEntity>> = repository.deletedBooks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allFolders: StateFlow<List<FolderEntity>> = repository.allFolders.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val activeNotes: StateFlow<List<NoteEntity>> = repository.activeNotes.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val deletedNotes: StateFlow<List<NoteEntity>> = repository.deletedNotes.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recentDailyLogs: StateFlow<List<DailyWritingLogEntity>> = repository.recentDailyLogs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val globalWords: StateFlow<Int> = repository.globalWordCount.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val globalChapters: StateFlow<Int> = repository.globalChapterCount.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // Library Filter & Search
    val searchQuery = MutableStateFlow("")
    val selectedFilterTab = MutableStateFlow("Semua") // "Semua", "Buku", "Folder", "Catatan", "Favorit", "Arsip"

    // Active Book / Chapter
    val activeBookId = MutableStateFlow<Long?>(null)
    val activeChapterId = MutableStateFlow<Long?>(null)

    // Editor Live Text & Selection State
    val editorChapterTitle = MutableStateFlow("")
    val editorContent = MutableStateFlow("")
    val editorCursorPosition = MutableStateFlow(0)
    val editorSelectedText = MutableStateFlow("")
    val isAutoSaving = MutableStateFlow(false)
    val isFocusMode = MutableStateFlow(false)

    // Editor Undo / Redo Stacks
    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()

    fun pushUndoState(newContent: String) {
        if (undoStack.isEmpty() || undoStack.last() != editorContent.value) {
            undoStack.add(editorContent.value)
            if (undoStack.size > 50) undoStack.removeAt(0)
            redoStack.clear()
        }
        editorContent.value = newContent
    }

    fun performUndo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.size - 1)
            redoStack.add(editorContent.value)
            editorContent.value = previous
            triggerAutoSave()
        }
    }

    fun performRedo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.size - 1)
            undoStack.add(editorContent.value)
            editorContent.value = next
            triggerAutoSave()
        }
    }

    // SB_AI State
    val isAiSheetVisible = MutableStateFlow(false)
    val aiPersona = MutableStateFlow(SbAiPersona.EDITOR)
    val aiIconX = MutableStateFlow(280f)
    val aiIconY = MutableStateFlow(550f)
    val aiMessages = MutableStateFlow<List<AiMessage>>(listOf(
        AiMessage("model", "Halo! Saya SB_AI, asisten AI dari SB_Media Production. Apa yang sedang kamu tulis hari ini?")
    ))
    val isAiGenerating = MutableStateFlow(false)
    val lastAiSuggestion = MutableStateFlow<String?>(null)

    init {
        // Inisialisasi posisi ikon SB_AI dari DataStore
        viewModelScope.launch {
            val savedX = prefs.aiIconXFlow.first()
            val savedY = prefs.aiIconYFlow.first()
            if (savedX >= 0f && savedY >= 0f) {
                aiIconX.value = savedX
                aiIconY.value = savedY
            }
            val savedPersona = prefs.aiPersonaFlow.first()
            aiPersona.value = when (savedPersona) {
                "lucu" -> SbAiPersona.LUCU
                "profesional" -> SbAiPersona.PROFESIONAL
                else -> SbAiPersona.EDITOR
            }
            // Load custom keys if any
            val k1 = prefs.customApiKey1Flow.first()
            val k2 = prefs.customApiKey2Flow.first()
            aiService = SbAiService(k1, k2)
        }
    }

    fun updateAiIconPosition(x: Float, y: Float) {
        aiIconX.value = x
        aiIconY.value = y
        viewModelScope.launch {
            prefs.setAiIconPosition(x, y)
        }
    }

    fun setAiPersona(persona: SbAiPersona) {
        aiPersona.value = persona
        viewModelScope.launch {
            prefs.setAiPersona(persona.id)
        }
    }

    fun openAiSheet() {
        isAiSheetVisible.value = true
    }

    fun closeAiSheet() {
        isAiSheetVisible.value = false
    }

    fun triggerAutoSave() {
        val chId = activeChapterId.value ?: return
        viewModelScope.launch {
            isAutoSaving.value = true
            repository.saveChapterContent(
                chapterId = chId,
                content = editorContent.value,
                cursorPosition = editorCursorPosition.value,
                title = editorChapterTitle.value.ifBlank { null }
            )
            kotlinx.coroutines.delay(400)
            isAutoSaving.value = false
        }
    }

    fun loadChapterForEditor(chapterId: Long, bookId: Long) {
        activeChapterId.value = chapterId
        activeBookId.value = bookId
        undoStack.clear()
        redoStack.clear()
        viewModelScope.launch {
            val ch = repository.getChapter(chapterId)
            if (ch != null) {
                editorChapterTitle.value = ch.title
                editorContent.value = ch.content
                editorCursorPosition.value = ch.lastCursorPosition
            }
        }
    }

    // SB_AI Interaction
    fun sendPromptToAi(userPrompt: String, isEditorialAction: Boolean = false) {
        if (userPrompt.isBlank() || isAiGenerating.value) return

        val userMsg = AiMessage("user", userPrompt)
        aiMessages.value = aiMessages.value + userMsg
        isAiGenerating.value = true
        lastAiSuggestion.value = null

        viewModelScope.launch {
            // Bangun konteks internal dokumen aktif secara otomatis tanpa screenshot
            val currentBook = activeBookId.value?.let { repository.getBook(it) }
            val context = SbAiContext(
                bookTitle = currentBook?.title,
                chapterTitle = editorChapterTitle.value.ifBlank { null },
                selectedText = editorSelectedText.value.ifBlank { null },
                chapterSnippet = editorContent.value.ifBlank { null },
                totalWords = WriterRepository.countWords(editorContent.value)
            )

            val history = aiMessages.value.dropLast(1).map { it.role to it.content }
            val result = aiService.generateResponse(userPrompt, aiPersona.value, context, history)

            isAiGenerating.value = false
            result.onSuccess { responseText ->
                val modelMsg = AiMessage("model", responseText, isProseSuggestion = isEditorialAction || isLikelyProse(responseText))
                aiMessages.value = aiMessages.value + modelMsg
                if (modelMsg.isProseSuggestion) {
                    lastAiSuggestion.value = responseText
                }
            }.onFailure { error ->
                aiMessages.value = aiMessages.value + AiMessage(
                    "model",
                    "Maaf, SB_AI mengalami kendala: ${error.localizedMessage ?: "Gagal memproses naskah."}"
                )
            }
        }
    }

    private fun isLikelyProse(text: String): Boolean {
        // Tentukan apakah respons AI merupakan teks cerita/suntingan yang bisa langsung disisipkan/diganti
        return text.length > 50 && !text.contains("?") && (text.contains(".") || text.contains("\""))
    }

    // Tombol Tindakan SB_AI
    fun applyAiTextReplace(newText: String) {
        pushUndoState(newText)
        triggerAutoSave()
        Toast.makeText(getApplication(), "Teks naskah berhasil diperbarui!", Toast.LENGTH_SHORT).show()
        lastAiSuggestion.value = null
    }

    fun applyAiTextInsert(newText: String) {
        val current = editorContent.value
        val pos = editorCursorPosition.value.coerceIn(0, current.length)
        val updated = current.substring(0, pos) + "\n\n" + newText + "\n\n" + current.substring(pos)
        pushUndoState(updated)
        triggerAutoSave()
        Toast.makeText(getApplication(), "Teks disisipkan ke naskah!", Toast.LENGTH_SHORT).show()
        lastAiSuggestion.value = null
    }

    fun copyToClipboard(text: String) {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("SB Writer AI", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(getApplication(), "Disalin ke papan klip!", Toast.LENGTH_SHORT).show()
    }

    fun dismissAiSuggestion() {
        lastAiSuggestion.value = null
    }

    fun clearAiChatHistory() {
        aiMessages.value = listOf(
            AiMessage("model", "Halo! Percakapan telah direset. Saya SB_AI dari SB_Media Production siap membantu karya tulisanmu.")
        )
        lastAiSuggestion.value = null
    }
}
