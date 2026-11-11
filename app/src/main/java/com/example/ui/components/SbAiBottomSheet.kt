package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.SbAiPersona
import com.example.ui.MainViewModel
import com.example.ui.theme.SbCrimson
import com.example.ui.theme.SbGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SbAiBottomSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val persona by viewModel.aiPersona.collectAsState()
    val messages by viewModel.aiMessages.collectAsState()
    val isGenerating by viewModel.isAiGenerating.collectAsState()
    val lastSuggestion by viewModel.lastAiSuggestion.collectAsState()

    val chapterTitle by viewModel.editorChapterTitle.collectAsState()
    val selectedText by viewModel.editorSelectedText.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxHeight(0.92f)
            .testTag("sb_ai_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header: Judul, Persona Selector, & Tombol Tutup
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SbCrimson, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SB_AI Assistant",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "SB_Media Creative Engine",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.clearAiChatHistory() },
                        modifier = Modifier.testTag("clear_ai_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Reset Chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_ai_sheet_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }
            }

            // Pemilih Gaya Bicara (Persona)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SbAiPersona.values().forEach { p ->
                    FilterChip(
                        selected = (persona == p),
                        onClick = { viewModel.setAiPersona(p) },
                        label = { Text(p.displayName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SbCrimson,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Indikator Konteks Layar / Dokumen Aktif
            if (chapterTitle.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = SbGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedText.isNotBlank()) {
                                "Membaca teks terpilih (${selectedText.length} karakter) di $chapterTitle"
                            } else {
                                "Konteks internal: $chapterTitle"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            // Quick Tools Bar (Pill Cepat)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val quickActions = listOf(
                    "Lanjutkan Cerita" to "Lanjutkan naskah cerita ini dengan mempertahankan gaya tutur yang konsisten, tensi yang mengalir, dan detail emosi yang kuat.",
                    "Sunting Naskah" to "Sunting penggalan naskah ini agar lebih hidup, puitis namun lugas, menerapkan prinsip 'show don't tell', dan perbaiki ritme kalimat.",
                    "Perbaiki Dialog" to "Perbaiki dan tajamkan dialog antar karakter agar terdengar alami, berbobot, dan mencerminkan emosi serta subteks masing-masing tokoh.",
                    "Kembangkan Ide" to "Berikan 3 alternatif kejutan cerita (plot twist) atau pengembangan alur berikutnya yang logis namun tak tertebak.",
                    "Bangun Karakter" to "Bantu kembangkan profil karakter, motivasi terdalam, rahasia masa lalu, dan konflik psikologisnya.",
                    "Cek Konsistensi" to "Analisis apakah adegan ini memiliki kelemahan logika (plot hole), ritme terburu-buru, atau kontradiksi.",
                    "Ringkas Bab" to "Buat ringkasan penting dari bab ini untuk panduan penulisan bab selanjutnya.",
                    "Atasi Kebuntuan" to "Saya mengalami kebuntuan ide. Berikan pemicu adegan baru yang mendadak mengubah dinamika cerita."
                )

                items(quickActions) { (label, actionPrompt) ->
                    OutlinedButton(
                        onClick = { viewModel.sendPromptToAi(actionPrompt, isEditorialAction = true) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = label, fontSize = 12.sp)
                    }
                }
            }

            // Daftar Pesan Chat
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.role == "user"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            color = if (isUser) SbCrimson else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth(if (msg.content.length > 80) 0.95f else 0.85f)
                                .border(
                                    width = if (isUser) 0.dp else 1.dp,
                                    color = if (isUser) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                if (!isUser) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    ) {
                                        Text(
                                            text = "SB_AI (${persona.displayName})",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SbGold
                                        )
                                    }
                                }
                                Text(
                                    text = msg.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp,
                                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                if (isGenerating) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = SbCrimson
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "SB_AI sedang meracik naskah...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Panel Tindakan Naskah (Ketika AI menghasilkan teks suntingan/lanjutan)
            if (lastSuggestion != null && !isGenerating) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Terapkan Hasil Suntingan ke Naskah?",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SbGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.applyAiTextReplace(lastSuggestion!!) },
                                colors = ButtonDefaults.buttonColors(containerColor = SbCrimson),
                                modifier = Modifier.weight(1f).testTag("ai_replace_text_button")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ganti Teks", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.applyAiTextInsert(lastSuggestion!!) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier.weight(1f).testTag("ai_insert_text_button")
                            ) {
                                Icon(Icons.Default.Input, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sisipkan", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.copyToClipboard(lastSuggestion!!) },
                                modifier = Modifier.testTag("ai_copy_text_button")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Salin", modifier = Modifier.size(16.dp))
                            }

                            OutlinedButton(
                                onClick = { viewModel.dismissAiSuggestion() },
                                modifier = Modifier.testTag("ai_dismiss_button")
                            ) {
                                Text("Batal", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputPrompt,
                        onValueChange = { inputPrompt = it },
                        placeholder = { Text("Tanyakan apa saja pada SB_AI...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sb_ai_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SbCrimson,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputPrompt.isNotBlank() && !isGenerating) {
                                val text = inputPrompt
                                inputPrompt = ""
                                viewModel.sendPromptToAi(text)
                            }
                        },
                        enabled = inputPrompt.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (inputPrompt.isNotBlank() && !isGenerating) SbCrimson else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            )
                            .testTag("sb_ai_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Kirim",
                            tint = if (inputPrompt.isNotBlank() && !isGenerating) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
