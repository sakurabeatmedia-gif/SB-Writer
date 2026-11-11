package com.example.ui.screens.stats

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyWritingLogEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.SbCrimson
import com.example.ui.theme.SbGold
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val globalWords by viewModel.globalWords.collectAsState()
    val globalChapters by viewModel.globalChapters.collectAsState()
    val activeBooks by viewModel.activeBooks.collectAsState()
    val dailyGoal by viewModel.dailyWordGoal.collectAsState()
    val recentLogs by viewModel.recentDailyLogs.collectAsState()

    BackHandler {
        viewModel.navigateBack()
    }

    // Hitung kata yang ditulis hari ini
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayLog = recentLogs.find { it.dateString == todayDateStr }
    val todayWords = todayLog?.wordsAdded ?: 0
    val todayProgress = (todayWords.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f)

    // Siapkan 7 hari terakhir untuk grafik batang riil
    val weekDays = rememberLast7Days(recentLogs)

    // Hitung streak beruntun
    val streakDays = calculateStreak(recentLogs)

    val readingTimeHours = (globalWords / (200 * 60)).coerceAtLeast(0)
    val readingTimeMinutes = ((globalWords / 200) % 60).coerceAtLeast(1)

    Scaffold(
        modifier = modifier.testTag("stats_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Statistik & Progres Menulis", fontWeight = FontWeight.Bold) },
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
            // Target Kata Hari Ini
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("daily_target_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(SbCrimson.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = SbCrimson, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Target Kata Hari Ini", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("Target: $dailyGoal kata", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = "${(todayProgress * 100).toInt()}%",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = SbCrimson
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { todayProgress },
                            color = SbCrimson,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$todayWords kata tertulis hari ini",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (todayWords >= dailyGoal) {
                                Text(
                                    text = "Target Tercapai!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SbGold
                                )
                            }
                        }
                    }
                }
            }

            // Streak Menulis & Penghargaan
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Streak Menulis", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$streakDays Hari", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = SbGold, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Level Penulis", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = when {
                                        globalWords > 50000 -> "Novelis Senior"
                                        globalWords > 20000 -> "Penulis Mahir"
                                        globalWords > 5000 -> "Penulis Aktif"
                                        else -> "Penulis Pemula"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Ringkasan Keseluruhan (Grid 2x2)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ringkasan Total Perpustakaan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatMetricCard(
                            title = "Total Kata Naskah",
                            value = "$globalWords",
                            subtitle = "Semua bab aktif",
                            icon = Icons.Default.EditNote,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            title = "Total Bab",
                            value = "$globalChapters Bab",
                            subtitle = "Dalam ${activeBooks.size} buku",
                            icon = Icons.Default.AutoStories,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatMetricCard(
                            title = "Estimasi Waktu Baca",
                            value = if (readingTimeHours > 0) "${readingTimeHours}j ${readingTimeMinutes}m" else "${readingTimeMinutes} mnt",
                            subtitle = "Kecepatan 200 kata/mnt",
                            icon = Icons.Default.Speed,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            title = "Buku / Karya",
                            value = "${activeBooks.size} Proyek",
                            subtitle = "Aktif di studio",
                            icon = Icons.Default.CalendarMonth,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Grafik Batang Mingguan (Real Data Visualizer)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("weekly_chart_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Aktivitas Menulis 7 Hari Terakhir",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Data Asli",
                                fontSize = 11.sp,
                                color = SbGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Grafik kata yang ditambahkan per hari",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Custom Canvas Bar Chart
                        WeeklyBarChart(
                            days = weekDays,
                            dailyGoal = dailyGoal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                }
            }
        }
    }
}

data class DayStat(
    val dayLabel: String, // "Sen", "Sel", dst.
    val dateString: String,
    val words: Int,
    val isToday: Boolean
)

@Composable
fun rememberLast7Days(logs: List<DailyWritingLogEntity>): List<DayStat> {
    val calendar = Calendar.getInstance()
    val dayFormat = SimpleDateFormat("EEE", Locale("id", "ID"))
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = dateFormat.format(Date())

    val list = mutableListOf<DayStat>()
    for (i in 6 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -i)
        val dStr = dateFormat.format(cal.time)
        val dLabel = dayFormat.format(cal.time)
        val log = logs.find { it.dateString == dStr }
        list.add(
            DayStat(
                dayLabel = dLabel,
                dateString = dStr,
                words = log?.wordsAdded ?: 0,
                isToday = (dStr == todayStr)
            )
        )
    }
    return list
}

fun calculateStreak(logs: List<DailyWritingLogEntity>): Int {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = Calendar.getInstance()
    var streak = 0

    for (i in 0..60) {
        val checkCal = Calendar.getInstance()
        checkCal.add(Calendar.DAY_OF_YEAR, -i)
        val dStr = dateFormat.format(checkCal.time)
        val log = logs.find { it.dateString == dStr }
        if (log != null && log.wordsAdded > 0) {
            streak++
        } else if (i == 0) {
            // Jika hari ini belum menulis, jangan patahkan streak kemarin
            continue
        } else {
            break
        }
    }
    return streak
}

@Composable
fun WeeklyBarChart(
    days: List<DayStat>,
    dailyGoal: Int,
    modifier: Modifier = Modifier
) {
    val maxWords = (days.maxOfOrNull { it.words } ?: dailyGoal).coerceAtLeast(dailyGoal).coerceAtLeast(100)
    val barColor = SbCrimson
    val todayBarColor = SbGold
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val bottomPadding = 30f
        val chartHeight = height - bottomPadding
        val barCount = days.size
        val barWidth = (width / barCount) * 0.45f
        val slotWidth = width / barCount

        // Gambar garis acuan target harian
        val goalY = chartHeight - ((dailyGoal.toFloat() / maxWords.toFloat()) * chartHeight)
        drawLine(
            color = gridColor,
            start = Offset(0f, goalY),
            end = Offset(width, goalY),
            strokeWidth = 2f
        )

        days.forEachIndexed { index, day ->
            val centerX = (index * slotWidth) + (slotWidth / 2)
            val barHeight = ((day.words.toFloat() / maxWords.toFloat()) * chartHeight).coerceAtLeast(8f)
            val topY = chartHeight - barHeight

            // Batang Bar
            drawRoundRect(
                color = if (day.isToday) todayBarColor else barColor,
                topLeft = Offset(centerX - (barWidth / 2), topY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
    }

    // Label hari di bawah
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        days.forEach { day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = day.dayLabel,
                    fontSize = 11.sp,
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (day.isToday) SbGold else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${day.words}",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = SbCrimson, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
