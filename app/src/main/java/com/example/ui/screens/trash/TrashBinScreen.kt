package com.example.ui.screens.trash

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.SbCrimson
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashBinScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val deletedBooks by viewModel.deletedBooks.collectAsState()
    val deletedNotes by viewModel.deletedNotes.collectAsState()

    var showEmptyTrashConfirm by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateBack()
    }

    val totalItems = deletedBooks.size + deletedNotes.size

    Scaffold(
        modifier = modifier.testTag("trash_bin_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Tempat Sampah ($totalItems)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (totalItems > 0) {
                        IconButton(onClick = { showEmptyTrashConfirm = true }) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Kosongkan Sampah", tint = SbCrimson)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        if (totalItems == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tempat sampah kosong",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Buku dan catatan yang dihapus akan ditampung di sini.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Buku yang Dihapus
                if (deletedBooks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Buku / Novel yang Dihapus:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(deletedBooks, key = { "b_${it.id}" }) { book ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = SbCrimson)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(book.title, fontWeight = FontWeight.Bold)
                                    Text("Penulis: ${book.author}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(
                                    onClick = { scope.launch { viewModel.repository.restoreBook(book.id) } }
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = "Pulihkan")
                                }
                                IconButton(
                                    onClick = { scope.launch { viewModel.repository.hardDeleteBook(book.id) } }
                                ) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = "Hapus Permanen", tint = SbCrimson)
                                }
                            }
                        }
                    }
                }

                // Catatan yang Dihapus
                if (deletedNotes.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Catatan yang Dihapus:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(deletedNotes, key = { "n_${it.id}" }) { note ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Note, contentDescription = null, tint = SbCrimson)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(note.title, fontWeight = FontWeight.Bold)
                                    Text(note.content.take(50), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(
                                    onClick = { scope.launch { viewModel.repository.restoreNote(note.id) } }
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = "Pulihkan")
                                }
                                IconButton(
                                    onClick = { scope.launch { viewModel.repository.hardDeleteNote(note.id) } }
                                ) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = "Hapus Permanen", tint = SbCrimson)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEmptyTrashConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashConfirm = false },
            title = { Text("Kosongkan Tempat Sampah?") },
            text = { Text("Semua buku dan catatan di tempat sampah akan dihapus secara permanen dan tidak dapat dipulihkan kembali.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.repository.emptyTrash()
                            showEmptyTrashConfirm = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SbCrimson)
                ) {
                    Text("Hapus Permanen")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEmptyTrashConfirm = false }) { Text("Batal") }
            }
        )
    }
}
