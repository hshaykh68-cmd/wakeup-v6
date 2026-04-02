package com.wakeup.app.presentation.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BedtimeOff
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.data.local.entity.SleepSession
import com.wakeup.app.data.local.entity.SleepStats
import java.time.format.DateTimeFormatter

/**
 * Main Sleep Screen for Pillar 3 Sleep Ecosystem.
 * Shows Smart Wake controls, sleep tracking, and analytics.
 */
@Composable
fun SleepScreen(
    viewModel: SleepViewModel = hiltViewModel()
) {
    val sessions by viewModel.sleepSessions.collectAsState()
    val recentSession by viewModel.recentSession.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val stats by viewModel.sleepStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            SleepHeader()
        }

        // Active Sleep Session Card
        if (activeSession != null) {
            item {
                ActiveSleepCard(
                    session = activeSession!!,
                    onEndSession = { viewModel.endSleepSession() }
                )
            }
        }

        // Last Night Summary
        if (recentSession != null && recentSession != activeSession) {
            item {
                LastNightCard(session = recentSession!!)
            }
        }

        // Sleep Stats Overview
        if (stats != null) {
            item {
                SleepStatsCard(stats = stats!!)
            }
        }

        // Weekly Trend
        if (stats?.weeklyTrend?.isNotEmpty() == true) {
            item {
                WeeklyTrendCard(weeklyData = stats!!.weeklyTrend)
            }
        }

        // Smart Wake Info Card
        item {
            SmartWakeInfoCard()
        }

        // Recent Sessions List
        if (sessions.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(sessions.take(5)) { session ->
                SessionListItem(
                    session = session,
                    isActive = session.id == activeSession?.id,
                    formatDuration = viewModel::formatDuration
                )
            }
        }

        // Loading state
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WakeUpColors.iosBlue)
                }
            }
        }
    }
}

@Composable
private fun SleepHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.NightsStay,
            contentDescription = null,
            tint = WakeUpColors.iosPurple,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sleep",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Track your sleep and wake up refreshed",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActiveSleepCard(
    session: SleepSession,
    onEndSession: () -> Unit
) {
    val startTime = session.getStartDateTime()
    val formatter = DateTimeFormatter.ofPattern("h:mm a")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = WakeUpColors.iosPurple.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = null,
                    tint = WakeUpColors.iosPurple
                )
                Text(
                    text = "Sleeping Now",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WakeUpColors.iosPurple
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Started at ${startTime.format(formatter)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onEndSession,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WakeUpColors.iosPurple
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.BedtimeOff,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("End Sleep Session")
            }
        }
    }
}

@Composable
private fun LastNightCard(session: SleepSession) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    val dateStr = session.getStartDateTime().format(dateFormatter)
    val startTimeStr = session.getStartDateTime().format(timeFormatter)
    val endTimeStr = session.getEndDateTime()?.format(timeFormatter) ?: "--"

    val hours = session.sleepDurationMinutes / 60
    val minutes = session.sleepDurationMinutes % 60
    val durationStr = "${hours}h ${minutes}m"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = WakeUpColors.iosBlue.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last Night",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WakeUpColors.iosBlue
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = durationStr,
                    label = "Duration",
                    icon = Icons.Default.DateRange
                )
                StatItem(
                    value = "${session.sleepQualityScore.toInt()}",
                    label = "Quality",
                    icon = Icons.Default.TrendingUp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time range
            Text(
                text = "$startTimeStr - $endTimeStr",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Smart Wake indicator
            if (session.smartWakeUsed) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = if (session.wokeInSmartWindow) WakeUpColors.iosGreen else WakeUpColors.iosOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (session.wokeInSmartWindow) "Smart Wake ✓" else "Smart Wake (late)",
                        fontSize = 12.sp,
                        color = if (session.wokeInSmartWindow) WakeUpColors.iosGreen else WakeUpColors.iosOrange
                    )
                }
            }
        }
    }
}

@Composable
private fun SleepStatsCard(stats: SleepStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Sleep Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val avgHours = (stats.averageDurationMinutes / 60).toInt()
                val avgMins = (stats.averageDurationMinutes % 60).toInt()

                StatItem(
                    value = "${avgHours}h ${avgMins}m",
                    label = "Avg Duration",
                    icon = Icons.Default.DateRange
                )
                StatItem(
                    value = "${stats.averageQualityScore.toInt()}",
                    label = "Avg Quality",
                    icon = Icons.Default.TrendingUp
                )
                StatItem(
                    value = "${stats.averageEfficiency.toInt()}%",
                    label = "Efficiency",
                    icon = Icons.Default.MonitorHeart
                )
            }

            // Smart Wake stats
            if (stats.totalSmartWakeUses > 0) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = WakeUpColors.iosBlue
                        )
                        Text(
                            text = "Smart Wake Success",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Text(
                        text = "${stats.smartWakeSuccessRate.toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (stats.smartWakeSuccessRate >= 70) WakeUpColors.iosGreen else WakeUpColors.iosOrange
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { stats.smartWakeSuccessRate / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (stats.smartWakeSuccessRate >= 70) WakeUpColors.iosGreen else WakeUpColors.iosOrange,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun WeeklyTrendCard(weeklyData: List<com.wakeup.app.data.local.entity.DailySleepData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Last 7 Days",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bar chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { day ->
                    val dayStr = day.date.format(java.time.format.DateTimeFormatter.ofPattern("EEE"))
                    val barHeight = (day.durationMinutes / 600f).coerceIn(0.05f, 1f)  // Max 10 hours
                    val qualityColor = when {
                        day.qualityScore >= 80 -> WakeUpColors.iosGreen
                        day.qualityScore >= 60 -> WakeUpColors.iosBlue
                        day.qualityScore > 0 -> WakeUpColors.iosOrange
                        else -> Color.White.copy(alpha = 0.2f)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Bar
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height((barHeight * 80).dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(qualityColor)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Day label
                        Text(
                            text = dayStr,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartWakeInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = WakeUpColors.iosGreen.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = WakeUpColors.iosGreen,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Smart Wake",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = WakeUpColors.iosGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enable in alarm settings to wake during light sleep",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SessionListItem(
    session: SleepSession,
    isActive: Boolean,
    formatDuration: (Int) -> String
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")
    val dateStr = session.getStartDateTime().format(dateFormatter)
    val durationStr = formatDuration(session.sleepDurationMinutes)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) WakeUpColors.iosPurple.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Bedtime else Icons.Default.NightsStay,
                    contentDescription = null,
                    tint = if (isActive) WakeUpColors.iosPurple else WakeUpColors.iosBlue,
                    modifier = Modifier.size(24.dp)
                )

                Column {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    if (session.smartWakeUsed) {
                        Text(
                            text = if (session.wokeInSmartWindow) "Smart Wake ✓" else "Smart Wake",
                            fontSize = 12.sp,
                            color = if (session.wokeInSmartWindow) WakeUpColors.iosGreen else WakeUpColors.iosOrange
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = durationStr,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = "Quality: ${session.sleepQualityScore.toInt()}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WakeUpColors.iosBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}
