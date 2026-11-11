package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.BookEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.SbCrimson
import com.example.ui.theme.SbGold
import kotlinx.coroutines.launch
import java.io.File

data class PresetCover(val id: String, val name: String, val colors: List<Color>)

val PresetCoversList = listOf(
    PresetCover("sb_dark", "SB Obsidian", listOf(Color(0xFF0F0F12), Color(0xFF282830))),
    PresetCover("sb_crimson", "Crimson Flame", listOf(Color(0xFF7F0000), Color(0xFFE53935))),
    PresetCover("sb_gold", "Royal Gold", listOf(Color(0xFF3E2723), Color(0xFFD4AF37))),
    PresetCover("sb_emerald", "Emerald Forest", listOf(Color(0xFF0A2E1E), Color(0xFF1E824C))),
    PresetCover("sb_midnight", "Midnight Cyber", listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF415A77))),
    PresetCover("sb_sepia", "Vintage Sepia", listOf(Color(0xFF4E342E), Color(0xFF8D6E63)))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverPickerBottomSheet(
    book: BookEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPreset by remember { mutableStateOf(book.coverPreset ?: "sb_dark") }
    var scaleMode by remember { mutableStateOf(ContentScale.Crop) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("cover_picker_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pengaturan Sampul Novel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pratinjau Sampul (Rasio 2:3 Standar Buku)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                ) {
                    if (selectedUri != null) {
                        AsyncImage(
                            model = selectedUri,
                            contentDescription = "Pratinjau Sampul Baru",
                            contentScale = scaleMode,
                            modifier = Modifier.matchParentSize()
                        )
                    } else if (!book.coverPath.isNullOrBlank() && File(book.coverPath).exists()) {
                        AsyncImage(
                            model = File(book.coverPath),
                            contentDescription = "Sampul Saat Ini",
                            contentScale = scaleMode,
                            modifier = Modifier.matchParentSize()
                        )
                    } else {
                        // Preset Gradient
                        val preset = PresetCoversList.find { it.id == selectedPreset } ?: PresetCoversList.first()
                        Box(
                            modifier = Modifier
                                .matchParentSize()
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
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = book.title,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }

                // Pengaturan Penyesuaian & Tombol Aksi
                Column(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SbCrimson),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pick_cover_from_gallery_button")
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pilih dari Galeri", fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Opsi Pemotongan / Posisi (Crop vs Fit)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = (scaleMode == ContentScale.Crop),
                            onClick = { scaleMode = ContentScale.Crop },
                            label = { Text("Penuh (Crop)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = (scaleMode == ContentScale.Fit),
                            onClick = { scaleMode = ContentScale.Fit },
                            label = { Text("Pas (Fit)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (book.coverPath != null || selectedUri != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    viewModel.repository.removeCover(book.id)
                                    selectedUri = null
                                    Toast.makeText(context, "Sampul foto dihapus", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SbCrimson),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hapus Foto Sampul", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Koleksi Sampul Bawaan SB_Media (Pilihan Tambahan)
            Text(
                text = "Atau Gunakan Sampul Artistik SB_Media:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(PresetCoversList) { preset ->
                    val isSelected = (selectedPreset == preset.id && selectedUri == null)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                selectedPreset = preset.id
                                selectedUri = null
                                scope.launch {
                                    viewModel.repository.setCoverPreset(book.id, preset.id)
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 50.dp, height = 75.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Brush.verticalGradient(preset.colors))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) SbGold else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.name,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tombol Simpan Permanen
            Button(
                onClick = {
                    scope.launch {
                        if (selectedUri != null) {
                            val savedPath = viewModel.repository.saveCoverFromUri(book.id, selectedUri!!)
                            if (savedPath != null) {
                                Toast.makeText(context, "Sampul buku berhasil disimpan permanen!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Gagal menyimpan sampul", Toast.LENGTH_SHORT).show()
                            }
                        }
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SbCrimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_permanent_cover_button")
            ) {
                Text("Simpan Sampul", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
