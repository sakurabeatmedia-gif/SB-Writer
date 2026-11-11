package com.example.ui.screens.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.SbAiPersona
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.theme.SbCrimson
import com.example.ui.theme.SbGold
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentTheme by viewModel.themeMode.collectAsState()
    val dailyGoal by viewModel.dailyWordGoal.collectAsState()
    val pinEnabled by viewModel.pinEnabled.collectAsState()
    val aiPersona by viewModel.aiPersona.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = modifier.testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan SB Writer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profil SB_Media Production
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_sb_logo),
                            contentDescription = "SB_Media Logo",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "SB_Media Production",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "MUSIC • NOVEL • STORY • CREATIVE MEDIA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SbGold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "SB Writer v1.0 • Edisi Penulis Profesional",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Tampilan & Tema
            item {
                SettingsSectionCard(title = "Tampilan & Tema Aplikasi", icon = Icons.Default.Palette) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Tema Warna Studio:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "dark" to "Gelap (OLED)",
                                "light" to "Terang (Gading)",
                                "sepia" to "Sepia (Kertas)"
                            ).forEach { (id, label) ->
                                FilterChip(
                                    selected = (currentTheme == id),
                                    onClick = { scope.launch { viewModel.prefs.setTheme(id) } },
                                    label = { Text(label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Target Kata Harian
            item {
                SettingsSectionCard(title = "Target Kata Harian", icon = Icons.Default.Speed) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Target Menulis per Hari:")
                            Text("$dailyGoal kata", fontWeight = FontWeight.Bold, color = SbCrimson)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(500, 1000, 1500, 2000).forEach { goal ->
                                FilterChip(
                                    selected = (dailyGoal == goal),
                                    onClick = { scope.launch { viewModel.prefs.setDailyGoal(goal) } },
                                    label = { Text("$goal", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Keamanan & Privasi (PIN Naskah)
            item {
                SettingsSectionCard(title = "Keamanan & Penguncian PIN", icon = Icons.Default.Security) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Kunci Naskah dengan PIN", fontWeight = FontWeight.Medium)
                                Text(
                                    text = if (pinEnabled) "PIN aktif melindungi aplikasi" else "Tidak ada proteksi PIN",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = pinEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        showPinDialog = true
                                    } else {
                                        scope.launch {
                                            viewModel.prefs.setPin("")
                                        }
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SbCrimson)
                            )
                        }

                        if (pinEnabled) {
                            OutlinedButton(
                                onClick = { showPinDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ubah 4 Digit PIN", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Pengaturan SB_AI
            item {
                SettingsSectionCard(title = "Asisten SB_AI (SB_Media)", icon = Icons.Default.AutoAwesome) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Gaya Bicara Bawaan:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SbAiPersona.values().forEach { p ->
                                FilterChip(
                                    selected = (aiPersona == p),
                                    onClick = { viewModel.setAiPersona(p) },
                                    label = { Text(p.displayName, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.updateAiIconPosition(280f, 550f)
                                Toast.makeText(context, "Posisi ikon SB_AI dikembalikan ke posisi awal!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Posisi Tombol Melayang SB_AI", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.clearAiChatHistory()
                                Toast.makeText(context, "Riwayat percakapan SB_AI telah dibersihkan!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Bersihkan Riwayat Percakapan", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Cadangan, Tempat Sampah & Ekspor
            item {
                SettingsSectionCard(title = "Cadangan Data & Tempat Sampah", icon = Icons.Default.Backup) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val backupJson = viewModel.repository.createFullBackupJson()
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_SUBJECT, "Backup SB Writer - ${System.currentTimeMillis()}")
                                        putExtra(Intent.EXTRA_TEXT, backupJson)
                                        type = "application/json"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Simpan / Kirim Cadangan"))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SbCrimson),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ekspor Cadangan Lengkap (.JSON)")
                        }

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.Trash) },
                            modifier = Modifier.fillMaxWidth().testTag("open_trash_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka Tempat Sampah", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Dialog Set PIN
    if (showPinDialog) {
        SetPinDialog(
            onDismiss = { showPinDialog = false },
            onSetPin = { newPin ->
                scope.launch {
                    viewModel.prefs.setPin(newPin)
                    showPinDialog = false
                    Toast.makeText(context, "PIN keamanan berhasil diaktifkan!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = SbCrimson, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SetPinDialog(
    onDismiss: () -> Unit,
    onSetPin: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atur 4 Digit PIN Keamanan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Masukkan 4 digit angka PIN untuk mengunci akses ke SB Writer:")
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            pin = it
                        }
                    },
                    label = { Text("4 Digit Angka") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (pin.length == 4) onSetPin(pin) },
                enabled = pin.length == 4,
                colors = ButtonDefaults.buttonColors(containerColor = SbCrimson)
            ) {
                Text("Aktifkan PIN")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
