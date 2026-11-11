package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.FloatingSbAiButton
import com.example.ui.components.PinLockScreen
import com.example.ui.components.SbAiBottomSheet
import com.example.ui.screens.bookdetail.BookDetailScreen
import com.example.ui.screens.editor.NovelEditorScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.notes.NotesScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.stats.StatsScreen
import com.example.ui.screens.trash.TrashBinScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val themeMode by mainViewModel.themeMode.collectAsState()
            val pinEnabled by mainViewModel.pinEnabled.collectAsState()
            val isUnlocked by mainViewModel.isAppUnlocked.collectAsState()
            val currentScreen by mainViewModel.currentScreen.collectAsState()
            val isAiSheetVisible by mainViewModel.isAiSheetVisible.collectAsState()

            MyApplicationTheme(themeMode = themeMode) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (pinEnabled && !isUnlocked) {
                        PinLockScreen(viewModel = mainViewModel)
                    } else {
                        // Layar Utama Berdasarkan Stack Navigasi
                        when (val screen = currentScreen) {
                            is Screen.Library -> LibraryScreen(viewModel = mainViewModel)
                            is Screen.BookDetail -> BookDetailScreen(bookId = screen.bookId, viewModel = mainViewModel)
                            is Screen.Editor -> NovelEditorScreen(
                                chapterId = screen.chapterId,
                                bookId = screen.bookId,
                                viewModel = mainViewModel
                            )
                            is Screen.Notes -> NotesScreen(viewModel = mainViewModel)
                            is Screen.Stats -> StatsScreen(viewModel = mainViewModel)
                            is Screen.Settings -> SettingsScreen(viewModel = mainViewModel)
                            is Screen.Trash -> TrashBinScreen(viewModel = mainViewModel)
                        }

                        // Tombol Mengambang SB_AI (Bebas Digeser)
                        FloatingSbAiButton(viewModel = mainViewModel)

                        // Bottom Sheet Percakapan Asisten SB_AI
                        if (isAiSheetVisible) {
                            SbAiBottomSheet(
                                viewModel = mainViewModel,
                                onDismiss = { mainViewModel.closeAiSheet() }
                            )
                        }
                    }
                }
            }
        }
    }
}
