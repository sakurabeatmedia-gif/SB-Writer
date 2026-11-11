package com.example.ui.screens.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BookEntity
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.BookCard
import com.example.ui.components.PresetCoversList
import com.example.ui.theme.SbCrimson
import com.example.ui.theme.SbGold
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val activeBooks by viewModel.activeBooks.collectAsState()
    val archivedBooks by viewModel.archivedBooks.collectAsState()
    val favoriteBooks by viewModel.favoriteBooks.collectAsState()
    val allFolders by viewModel.allFolders.collectAsState()
    val globalWords by viewModel.globalWords.collectAsState()
    val viewMode by viewModel.libraryViewMode.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Semua") } // Semua, Buku, Folder, Favorit, Arsip
    var selectedFolderId by remember { mutableStateOf<Long?>(null) }

    // Dialog state
    var showCreateBookDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var editingBook by remember { mutableStateOf<BookEntity?>(null) }

    val filterTabs = listOf("Semua", "Buku", "Folder", "Favorit", "Arsip")

    // Filter buku
    val displayedBooks = when (selectedTab) {
        "Favorit" -> favoriteBooks
        "Arsip" -> archivedBooks
        else -> {
            if (selectedFolderId != null) {
                activeBooks.filter { it.folderId == selectedFolderId }
            } else {
                activeBooks
            }
        }
    }.filter {
        if (searchQuery.isBlank()) true
        else it.title.contains(searchQuery, ignoreCase = true) ||
                it.synopsis.contains(searchQuery, ignoreCase = true) ||
                it.tags.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        modifier = modifier.testTag("library_screen"),
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* sudah di Library */ },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Perpustakaan") },
                    label = { Text("Perpustakaan") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.Notes) },
                    icon = { Icon(Icons.Default.NoteAdd, contentDescription = "Catatan") },
                    label = { Text("Catatan") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.Stats) },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Statistik") },
                    label = { Text("Statistik") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.Settings) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                    label = { Text("Pengaturan") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateBookDialog = true },
                containerColor = SbCrimson,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("create_book_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Buat Buku Baru", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header: Logo SB_Media, Judul & Tampilan Grid/List
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_sb_logo),
                        contentDescription = "SB_Media Logo",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SB Writer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "SB_Media Novel Studio",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                viewModel.prefs.setViewMode(if (viewMode == "grid") "list" else "grid")
                            }
                        },
                        modifier = Modifier.testTag("toggle_view_mode_button")
                    ) {
                        Icon(
                            imageVector = if (viewMode == "grid") Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Ganti Tampilan"
                        )
                    }
                }
            }

            // Bilah Pencarian Cepat
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari judul, tag, atau sinopsis...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus Pencarian")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("library_search_field")
            )

            // Ringkasan Statistik Singkat
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Naskah", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${activeBooks.size} Buku Aktif", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column {
                        Text("Akumulasi Kata", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$globalWords Kata", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SbGold)
                    }
                    Column {
                        Text("Folder", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${allFolders.size} Proyek", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // Tab Filter Kategori
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterTabs) { tab ->
                    FilterChip(
                        selected = (selectedTab == tab && selectedFolderId == null),
                        onClick = {
                            selectedTab = tab
                            selectedFolderId = null
                        },
                        label = { Text(tab, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SbCrimson,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                // Tampilkan chip folder proyek jika ada
                items(allFolders) { folder ->
                    FilterChip(
                        selected = (selectedFolderId == folder.id),
                        onClick = {
                            selectedFolderId = if (selectedFolderId == folder.id) null else folder.id
                        },
                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        label = { Text(folder.name, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SbGold,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            // Tombol Cepat Tambah Folder
            if (selectedTab == "Folder") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { showCreateFolderDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Folder Baru", fontSize = 12.sp)
                    }
                }
            }

            // Konten Daftar Buku / Novel
            if (displayedBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Tidak ada buku yang cocok" else "Belum ada novel di bagian ini",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showCreateBookDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SbCrimson)
                        ) {
                            Text("Tulis Novel Baru Sekarang")
                        }
                    }
                }
            } else {
                if (viewMode == "grid") {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(displayedBooks, key = { it.id }) { book ->
                            BookCard(
                                book = book,
                                isGridView = true,
                                onClick = { viewModel.navigateTo(Screen.BookDetail(book.id)) },
                                onPinToggle = { scope.launch { viewModel.repository.togglePinBook(book) } },
                                onFavoriteToggle = { scope.launch { viewModel.repository.toggleFavoriteBook(book) } },
                                onArchiveToggle = { scope.launch { viewModel.repository.toggleArchiveBook(book) } },
                                onDuplicate = { scope.launch { viewModel.repository.duplicateBook(book.id) } },
                                onEdit = { editingBook = book },
                                onDelete = { scope.launch { viewModel.repository.softDeleteBook(book.id) } }
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(displayedBooks, key = { it.id }) { book ->
                            BookCard(
                                book = book,
                                isGridView = false,
                                onClick = { viewModel.navigateTo(Screen.BookDetail(book.id)) },
                                onPinToggle = { scope.launch { viewModel.repository.togglePinBook(book) } },
                                onFavoriteToggle = { scope.launch { viewModel.repository.toggleFavoriteBook(book) } },
                                onArchiveToggle = { scope.launch { viewModel.repository.toggleArchiveBook(book) } },
                                onDuplicate = { scope.launch { viewModel.repository.duplicateBook(book.id) } },
                                onEdit = { editingBook = book },
                                onDelete = { scope.launch { viewModel.repository.softDeleteBook(book.id) } }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog Buat Buku Baru
    if (showCreateBookDialog) {
        CreateBookDialog(
            folders = allFolders,
            onDismiss = { showCreateBookDialog = false },
            onCreate = { title, author, genre, synopsis, folderId, targetWords, preset ->
                scope.launch {
                    val newBookId = viewModel.repository.createBook(
                        title = title,
                        author = author,
                        genre = genre,
                        synopsis = synopsis,
                        folderId = folderId,
                        coverPreset = preset
                    )
                    showCreateBookDialog = false
                    viewModel.navigateTo(Screen.BookDetail(newBookId))
                }
            }
        )
    }

    // Dialog Edit Detail Buku
    if (editingBook != null) {
        EditBookDetailsDialog(
            book = editingBook!!,
            folders = allFolders,
            onDismiss = { editingBook = null },
            onSave = { updated ->
                scope.launch {
                    viewModel.repository.updateBook(updated)
                    editingBook = null
                }
            }
        )
    }

    // Dialog Buat Folder Baru
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onCreate = { name ->
                scope.launch {
                    viewModel.repository.createFolder(name)
                    showCreateFolderDialog = false
                }
            }
        )
    }
}

@Composable
fun CreateBookDialog(
    folders: List<com.example.data.model.FolderEntity>,
    onDismiss: () -> Unit,
    onCreate: (title: String, author: String, genre: String, synopsis: String, folderId: Long?, targetWords: Int, preset: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("SB_Media Author") }
    var genre by remember { mutableStateOf("Novel") }
    var synopsis by remember { mutableStateOf("") }
    var selectedFolderId by remember { mutableStateOf<Long?>(null) }
    var selectedPreset by remember { mutableStateOf("sb_dark") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tulis Buku / Novel Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Novel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_book_title_field")
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Nama Penulis") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Genre (Misal: Fantasi, Misteri, Roman)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = synopsis,
                    onValueChange = { synopsis = it },
                    label = { Text("Sinopsis Singkat") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Pilih Sampul Awal
                Text("Sampul Artistik Awal:", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PresetCoversList) { preset ->
                        FilterChip(
                            selected = (selectedPreset == preset.id),
                            onClick = { selectedPreset = preset.id },
                            label = { Text(preset.name, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, author, genre, synopsis, selectedFolderId, 50000, selectedPreset) },
                colors = ButtonDefaults.buttonColors(containerColor = SbCrimson),
                modifier = Modifier.testTag("confirm_create_book_button")
            ) {
                Text("Buat Novel")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun EditBookDetailsDialog(
    book: BookEntity,
    folders: List<com.example.data.model.FolderEntity>,
    onDismiss: () -> Unit,
    onSave: (BookEntity) -> Unit
) {
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var genre by remember { mutableStateOf(book.genre) }
    var synopsis by remember { mutableStateOf(book.synopsis) }
    var tags by remember { mutableStateOf(book.tags) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Detail Novel", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Novel") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Penulis") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Genre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tag (pisahkan dengan koma)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = synopsis,
                    onValueChange = { synopsis = it },
                    label = { Text("Sinopsis") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(book.copy(title = title, author = author, genre = genre, synopsis = synopsis, tags = tags))
                },
                colors = ButtonDefaults.buttonColors(containerColor = SbCrimson)
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Folder / Proyek Baru") },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Nama Folder") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (folderName.isNotBlank()) onCreate(folderName) },
                colors = ButtonDefaults.buttonColors(containerColor = SbCrimson)
            ) {
                Text("Buat Folder")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
