package com.example.ui.screens.bookdetail

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.CoverPickerBottomSheet
import com.example.ui.components.PresetCoversList
import com.example.ui.theme.SbCrimson
import com.example.ui.theme.SbGold
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Long,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val book by viewModel.repository.observeBook(bookId).collectAsState(initial = null)
    val chapters by viewModel.repository.getChapters(bookId).collectAsState(initial = emptyList())

    var showCoverPicker by remember { mutableStateOf(false) }
    var showCreateChapterDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var chapterToRename by remember { mutableStateOf<ChapterEntity?>(null) }

    BackHandler {
        viewModel.navigateBack()
    }

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Memuat naskah...")
        }
        return
    }

    val currentBook = book!!
    val totalWords = chapters.sumOf { it.wordCount }
    val progress = (totalWords.toFloat() / currentBook.targetWordCount.toFloat()).coerceIn(0f, 1f)
    val preset = PresetCoversList.find { it.id == currentBook.coverPreset } ?: PresetCoversList.first()

    Scaffold(
        modifier = modifier.testTag("book_detail_screen"),
        topBar = {
            TopAppBar(
                title = { Text(currentBook.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }, modifier = Modifier.testTag("export_book_button")) {
                        Icon(Icons.Default.Share, contentDescription = "Ekspor Novel")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateChapterDialog = true },
                containerColor = SbCrimson,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_chapter_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Bab", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header Info Buku & Sampul
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Kotak Sampul dengan Tombol Kamera/Ganti Sampul
                        Box(
                            modifier = Modifier
                                .width(105.dp)
                                .aspectRatio(2f / 3f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable { showCoverPicker = true }
                                .testTag("book_cover_clickable")
                        ) {
                            if (!currentBook.coverPath.isNullOrBlank() && File(currentBook.coverPath).exists()) {
                                AsyncImage(
                                    model = File(currentBook.coverPath),
                                    contentDescription = "Sampul Novel",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.verticalGradient(preset.colors))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_sb_logo),
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = currentBook.title,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }

                            // Ikon Ganti Sampul
                            Surface(
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = CircleShape,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Ganti Sampul",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentBook.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Penulis: ${currentBook.author}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Genre: ${currentBook.genre}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SbGold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Progres Target Kata
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$totalWords / ${currentBook.targetWordCount} kata",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SbCrimson
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                color = SbCrimson,
                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${chapters.size} Bab tersusun",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Sinopsis Buku (jika ada)
            if (currentBook.synopsis.isNotBlank()) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sinopsis Novel",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentBook.synopsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Bagian Header Daftar Bab
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Bab & Bagian",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { showCreateChapterDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Bab", fontSize = 12.sp)
                    }
                }
            }

            // Item-item Bab
            if (chapters.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Belum ada bab. Ketuk tombol + untuk mulai menulis bab pertama.")
                        }
                    }
                }
            } else {
                itemsIndexed(chapters, key = { _, ch -> ch.id }) { index, ch ->
                    ChapterRowItem(
                        chapter = ch,
                        index = index + 1,
                        onClick = {
                            viewModel.navigateTo(Screen.Editor(ch.id, currentBook.id))
                        },
                        onRename = { chapterToRename = ch },
                        onDelete = {
                            scope.launch {
                                viewModel.repository.deleteChapter(ch.id)
                            }
                        }
                    )
                }
            }
        }
    }

    // Bottom Sheet Pengatur Sampul Buku
    if (showCoverPicker) {
        CoverPickerBottomSheet(
            book = currentBook,
            viewModel = viewModel,
            onDismiss = { showCoverPicker = false }
        )
    }

    // Dialog Tambah Bab Baru
    if (showCreateChapterDialog) {
        CreateChapterDialog(
            defaultOrder = chapters.size + 1,
            onDismiss = { showCreateChapterDialog = false },
            onCreate = { title, volume ->
                scope.launch {
                    val newChId = viewModel.repository.createChapter(currentBook.id, title, volume)
                    showCreateChapterDialog = false
                    viewModel.navigateTo(Screen.Editor(newChId, currentBook.id))
                }
            }
        )
    }

    // Dialog Rename Bab
    if (chapterToRename != null) {
        RenameChapterDialog(
            chapter = chapterToRename!!,
            onDismiss = { chapterToRename = null },
            onRename = { newTitle ->
                scope.launch {
                    viewModel.repository.saveChapterContent(
                        chapterId = chapterToRename!!.id,
                        content = chapterToRename!!.content,
                        cursorPosition = chapterToRename!!.lastCursorPosition,
                        title = newTitle
                    )
                    chapterToRename = null
                }
            }
        )
    }

    // Dialog Ekspor Novel Lengkap
    if (showExportDialog) {
        ExportBookDialog(
            book = currentBook,
            onDismiss = { showExportDialog = false },
            onExport = { format ->
                scope.launch {
                    val exportText = when (format) {
                        "md" -> viewModel.repository.exportBookAsMarkdown(currentBook.id)
                        "html" -> viewModel.repository.exportBookAsHtml(currentBook.id)
                        else -> viewModel.repository.exportBookAsText(currentBook.id)
                    }

                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_SUBJECT, currentBook.title)
                        putExtra(Intent.EXTRA_TEXT, exportText)
                        type = if (format == "html") "text/html" else "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Bagikan Naskah: ${currentBook.title}")
                    context.startActivity(shareIntent)
                    showExportDialog = false
                }
            }
        )
    }
}

@Composable
fun ChapterRowItem(
    chapter: ChapterEntity,
    index: Int,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() }
            .testTag("chapter_row_${chapter.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(SbCrimson.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    fontWeight = FontWeight.Bold,
                    color = SbCrimson,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (!chapter.volumeName.isNullOrBlank()) {
                    Text(
                        text = chapter.volumeName,
                        fontSize = 10.sp,
                        color = SbGold,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(chapter.updatedAt))
                Text(
                    text = "${chapter.wordCount} kata • ${chapter.charCount} karakter • $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu Bab")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Ubah Judul Bab") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = { onRename(); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus Bab", color = SbCrimson) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = SbCrimson) },
                        onClick = { onDelete(); showMenu = false }
                    )
                }
            }
        }
    }
}

@Composable
fun CreateChapterDialog(
    defaultOrder: Int,
    onDismiss: () -> Unit,
    onCreate: (title: String, volume: String?) -> Unit
) {
    var title by remember { mutableStateOf("Bab $defaultOrder") }
    var volume by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Bab Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Bab") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_chapter_title_input")
                )
                OutlinedTextField(
                    value = volume,
                    onValueChange = { volume = it },
                    label = { Text("Bagian / Volume (Opsional)") },
                    singleLine = true,
                    placeholder = { Text("Misal: Bagian 1: Permulaan") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onCreate(title, volume.ifBlank { null }) },
                colors = ButtonDefaults.buttonColors(containerColor = SbCrimson),
                modifier = Modifier.testTag("confirm_add_chapter_button")
            ) {
                Text("Mulai Menulis")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun RenameChapterDialog(
    chapter: ChapterEntity,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var newTitle by remember { mutableStateOf(chapter.title) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Judul Bab") },
        text = {
            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text("Judul Baru") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (newTitle.isNotBlank()) onRename(newTitle) },
                colors = ButtonDefaults.buttonColors(containerColor = SbCrimson)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun ExportBookDialog(
    book: BookEntity,
    onDismiss: () -> Unit,
    onExport: (format: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ekspor Naskah Novel", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Pilih format dokumen untuk membagikan atau menyimpan seluruh isi naskah '${book.title}':",
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = { onExport("txt") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dokumen Teks Murni (.TXT)", color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = { onExport("md") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Format Markdown (.MD)", color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = { onExport("html") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("HTML / Naskah Siap Cetak (DOCX/PDF)", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}
