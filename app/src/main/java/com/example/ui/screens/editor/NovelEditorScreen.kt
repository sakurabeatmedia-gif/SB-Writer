package com.example.ui.screens.editor

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterRevisionEntity
import com.example.data.repository.WriterRepository
import com.example.ui.MainViewModel
import com.example.ui.theme.SbCrimson
import com.example.ui.theme.SbGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelEditorScreen(
    chapterId: Long,
    bookId: Long,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(chapterId) {
        viewModel.loadChapterForEditor(chapterId, bookId)
    }

    val title by viewModel.editorChapterTitle.collectAsState()
    val content by viewModel.editorContent.collectAsState()
    val isAutoSaving by viewModel.isAutoSaving.collectAsState()
    val isFocusMode by viewModel.isFocusMode.collectAsState()

    // Pengaturan Tampilan & Tipografi
    val appTheme by viewModel.themeMode.collectAsState()
    val fontFamilyChoice by viewModel.fontFamily.collectAsState()
    val fontSizeChoice by viewModel.fontSize.collectAsState()
    val lineSpacingChoice by viewModel.lineSpacing.collectAsState()
    val marginDpChoice by viewModel.marginDp.collectAsState()
    val textAlignChoice by viewModel.textAlign.collectAsState()

    // State Bilah Cari & Ganti
    var showFindReplace by remember { mutableStateOf(false) }
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var currentMatchIndex by remember { mutableIntStateOf(0) }

    // Bottom Sheets
    var showTypographySheet by remember { mutableStateOf(false) }
    var showRevisionsSheet by remember { mutableStateOf(false) }
    var showChapterNavSheet by remember { mutableStateOf(false) }
    var showExportChapterDialog by remember { mutableStateOf(false) }

    // Debounce Auto-save
    LaunchedEffect(content, title) {
        delay(1200)
        viewModel.triggerAutoSave()
    }

    BackHandler {
        viewModel.triggerAutoSave()
        viewModel.navigateBack()
    }

    val wordCount = WriterRepository.countWords(content)
    val charCount = content.length
    val readingTimeMinutes = (wordCount / 200).coerceAtLeast(1)

    val currentFont = when (fontFamilyChoice) {
        "sans" -> FontFamily.SansSerif
        "mono" -> FontFamily.Monospace
        else -> FontFamily.Serif
    }

    val currentAlign = when (textAlignChoice) {
        "center" -> TextAlign.Center
        "justify" -> TextAlign.Justify
        else -> TextAlign.Start
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .testTag("novel_editor_screen"),
        topBar = {
            if (!isFocusMode) {
                TopAppBar(
                    title = {
                        Column {
                            BasicTextField(
                                value = title,
                                onValueChange = { viewModel.editorChapterTitle.value = it },
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("editor_title_input")
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isAutoSaving) {
                                    Text(
                                        text = "Menyimpan...",
                                        fontSize = 11.sp,
                                        color = SbGold
                                    )
                                } else {
                                    Text(
                                        text = "Tersimpan otomatis • $wordCount kata",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.triggerAutoSave()
                            viewModel.navigateBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.isFocusMode.value = true },
                            modifier = Modifier.testTag("enter_focus_mode_button")
                        ) {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = "Mode Fokus")
                        }
                        IconButton(onClick = { showFindReplace = !showFindReplace }) {
                            Icon(Icons.Default.FindReplace, contentDescription = "Cari & Ganti")
                        }
                        IconButton(onClick = { showTypographySheet = true }) {
                            Icon(Icons.Default.TextFields, contentDescription = "Pengaturan Tipografi")
                        }
                        IconButton(onClick = { showRevisionsSheet = true }) {
                            Icon(Icons.Default.History, contentDescription = "Riwayat Revisi")
                        }
                        IconButton(onClick = { showExportChapterDialog = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Ekspor Bab")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        },
        bottomBar = {
            if (!isFocusMode) {
                // Strip Alat Cepat di Bawah Editor
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.performUndo() },
                                modifier = Modifier.size(38.dp).testTag("editor_undo_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                onClick = { viewModel.performRedo() },
                                modifier = Modifier.size(38.dp).testTag("editor_redo_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                onClick = {
                                    // Sisipkan tanda petik dialog novel “ ”
                                    val current = content
                                    val newContent = "$current\n\n“...”"
                                    viewModel.pushUndoState(newContent)
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.FormatQuote, contentDescription = "Sisipkan Kutipan Dialog", modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                onClick = {
                                    // Indentasi paragraf
                                    val newContent = "$content\n    "
                                    viewModel.pushUndoState(newContent)
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Text("Indent", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SbCrimson)
                            }
                        }

                        // Statistik Mini
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "$wordCount kata | ~$readingTimeMinutes mnt",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { viewModel.openAiSheet() },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Buka SB_AI", tint = SbCrimson, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Bilah Cari & Ganti (Jika aktif)
                AnimatedVisibility(visible = showFindReplace) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = findText,
                                    onValueChange = {
                                        findText = it
                                        currentMatchIndex = 0
                                    },
                                    placeholder = { Text("Cari kata dalam bab...") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("find_input_field")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val matchesCount = if (findText.isNotBlank()) {
                                    Regex(Regex.escape(findText), RegexOption.IGNORE_CASE).findAll(content).count()
                                } else 0
                                Text(
                                    text = "$matchesCount cocok",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(onClick = { showFindReplace = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = replaceText,
                                    onValueChange = { replaceText = it },
                                    placeholder = { Text("Ganti dengan...") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("replace_input_field")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        if (findText.isNotBlank()) {
                                            val newContent = content.replaceFirst(findText, replaceText, ignoreCase = true)
                                            viewModel.pushUndoState(newContent)
                                            Toast.makeText(context, "1 teks diganti", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SbCrimson),
                                    modifier = Modifier.testTag("replace_one_button")
                                ) {
                                    Text("Ganti", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                OutlinedButton(
                                    onClick = {
                                        if (findText.isNotBlank()) {
                                            val regex = Regex(Regex.escape(findText), RegexOption.IGNORE_CASE)
                                            val newContent = regex.replace(content, replaceText)
                                            viewModel.pushUndoState(newContent)
                                            Toast.makeText(context, "Semua teks diganti", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("replace_all_button")
                                ) {
                                    Text("Semua", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Kanvas Penulisan Naskah Utama
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = marginDpChoice.dp, vertical = 12.dp)
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        BasicTextField(
                            value = content,
                            onValueChange = { newText ->
                                viewModel.pushUndoState(newText)
                            },
                            textStyle = TextStyle(
                                fontFamily = currentFont,
                                fontSize = fontSizeChoice.sp,
                                lineHeight = (fontSizeChoice * lineSpacingChoice).sp,
                                textAlign = currentAlign,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            cursorBrush = SolidColor(SbCrimson),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((content.lines().size * fontSizeChoice * lineSpacingChoice * 2f).coerceAtLeast(600f).dp)
                                .testTag("novel_editor_canvas"),
                            decorationBox = { innerTextField ->
                                if (content.isEmpty()) {
                                    Text(
                                        text = "Mulai menulis naskah bab ini... Alirkan ide cerita dan biarkan kata-kata mengalir bebas tanpa hambatan.",
                                        style = TextStyle(
                                            fontFamily = currentFont,
                                            fontSize = fontSizeChoice.sp,
                                            lineHeight = (fontSizeChoice * lineSpacingChoice).sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                        Spacer(modifier = Modifier.height(200.dp))
                    }
                }
            }

            // Tombol Keluar dari Mode Fokus (Muncul mengambang minimalis)
            if (isFocusMode) {
                Surface(
                    onClick = { viewModel.isFocusMode.value = false },
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .testTag("exit_focus_mode_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = "Keluar Fokus", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Keluar Fokus", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Bottom Sheet Pengaturan Tipografi & Tema
    if (showTypographySheet) {
        TypographySettingsBottomSheet(
            viewModel = viewModel,
            currentFont = fontFamilyChoice,
            currentSize = fontSizeChoice,
            currentSpacing = lineSpacingChoice,
            currentMargin = marginDpChoice,
            currentAlign = textAlignChoice,
            currentTheme = appTheme,
            onDismiss = { showTypographySheet = false }
        )
    }

    // Bottom Sheet Riwayat Revisi
    if (showRevisionsSheet) {
        RevisionsBottomSheet(
            chapterId = chapterId,
            bookId = bookId,
            viewModel = viewModel,
            onDismiss = { showRevisionsSheet = false }
        )
    }

    // Dialog Ekspor Bab
    if (showExportChapterDialog) {
        AlertDialog(
            onDismissRequest = { showExportChapterDialog = false },
            title = { Text("Ekspor Bab Ini") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Pilih format untuk mengekspor atau membagikan bab '$title':")
                    Button(
                        onClick = {
                            val text = "$title\n\n$content"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_SUBJECT, title)
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Bagikan Bab"))
                            showExportChapterDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Format Teks (.TXT)", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = {
                            val text = "# $title\n\n$content"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_SUBJECT, title)
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Bagikan Markdown"))
                            showExportChapterDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Format Markdown (.MD)", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showExportChapterDialog = false }) { Text("Tutup") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypographySettingsBottomSheet(
    viewModel: MainViewModel,
    currentFont: String,
    currentSize: Float,
    currentSpacing: Float,
    currentMargin: Int,
    currentAlign: String,
    currentTheme: String,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Pengaturan Tipografi & Tampilan Editor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Tema Tampilan (Gelap, Terang, Sepia)
            Text("Tema Editor:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "dark" to "Gelap (OLED)",
                    "light" to "Terang (Gading)",
                    "sepia" to "Sepia (Kertas Kuno)"
                ).forEach { (id, label) ->
                    FilterChip(
                        selected = (currentTheme == id),
                        onClick = { scope.launch { viewModel.prefs.setTheme(id) } },
                        label = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Jenis Huruf (Font Family)
            Text("Jenis Huruf:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "serif" to "Serif (Buku Klasik)",
                    "sans" to "Sans-Serif (Modern)",
                    "mono" to "Monospace (Mesin Tik)"
                ).forEach { (id, label) ->
                    FilterChip(
                        selected = (currentFont == id),
                        onClick = { scope.launch { viewModel.prefs.setFontFamily(id) } },
                        label = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ukuran Huruf (Font Size)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ukuran Huruf:", style = MaterialTheme.typography.labelMedium)
                Text("${currentSize.toInt()} sp", fontWeight = FontWeight.Bold, color = SbCrimson)
            }
            Slider(
                value = currentSize,
                onValueChange = { scope.launch { viewModel.prefs.setFontSize(it) } },
                valueRange = 14f..28f,
                steps = 7
            )

            // Jarak Baris (Line Spacing)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Jarak Antar Baris:", style = MaterialTheme.typography.labelMedium)
                Text("${String.format(Locale.US, "%.1f", currentSpacing)}x", fontWeight = FontWeight.Bold, color = SbGold)
            }
            Slider(
                value = currentSpacing,
                onValueChange = { scope.launch { viewModel.prefs.setLineSpacing(it) } },
                valueRange = 1.2f..2.2f,
                steps = 4
            )

            // Perataan Paragraf
            Text("Perataan Paragraf:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "left" to "Rata Kiri",
                    "justify" to "Rata Kanan-Kiri",
                    "center" to "Tengah"
                ).forEach { (id, label) ->
                    FilterChip(
                        selected = (currentAlign == id),
                        onClick = { scope.launch { viewModel.prefs.setTextAlign(id) } },
                        label = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SbCrimson),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Selesai")
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionsBottomSheet(
    chapterId: Long,
    bookId: Long,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val revisions by viewModel.repository.getRevisions(chapterId).collectAsState(initial = emptyList())
    var snapshotNote by remember { mutableStateOf("") }
    var selectedRevisionForRestore by remember { mutableStateOf<ChapterRevisionEntity?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Riwayat Revisi & Versi Bab",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Simpan Snapshot Manual
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = snapshotNote,
                    onValueChange = { snapshotNote = it },
                    placeholder = { Text("Catatan versi (misal: Selesai adegan aksi)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val note = snapshotNote.ifBlank { "Snapshot Manual Penulis" }
                        snapshotNote = ""
                        scope.launch {
                            viewModel.repository.createChapterSnapshot(chapterId, note)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SbCrimson)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Snapshot")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Daftar Snapshot Versi:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (revisions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada riwayat revisi yang tersimpan untuk bab ini.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(revisions) { rev ->
                        val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(rev.timestamp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = rev.note,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "${rev.wordCount} kata",
                                        fontSize = 11.sp,
                                        color = SbGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = dateStr,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = rev.content.take(160) + if (rev.content.length > 160) "..." else "",
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { selectedRevisionForRestore = rev },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Pulihkan Versi Ini", fontSize = 12.sp, color = SbCrimson)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Konfirmasi Pemulihan Revisi
    if (selectedRevisionForRestore != null) {
        val rev = selectedRevisionForRestore!!
        AlertDialog(
            onDismissRequest = { selectedRevisionForRestore = null },
            title = { Text("Pulihkan Naskah?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Isi naskah aktif bab ini akan digantikan dengan versi snapshot '${rev.note}' (${rev.wordCount} kata). Salinan draf saat ini akan disimpan otomatis.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.repository.restoreChapterRevision(rev)
                            viewModel.loadChapterForEditor(chapterId, bookId)
                            selectedRevisionForRestore = null
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SbCrimson)
                ) {
                    Text("Ya, Pulihkan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedRevisionForRestore = null }) { Text("Batal") }
            }
        )
    }
}
