package com.wakeup.app.presentation.stats

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.core.chart.line.LineSpec
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.domain.model.StatsTimeRange
import com.wakeup.app.domain.model.WakeHistory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredStats = viewModel.getFilteredStats()

    val hasData = uiState.filteredHistory.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Progress",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Time Range Toggle
            val onRangeSelected = remember { { range: StatsTimeRange -> viewModel.setTimeRange(range) } }
            TimeRangeToggle(
                selectedRange = uiState.selectedTimeRange,
                onRangeSelected = onRangeSelected
            )

            if (uiState.isLoading) {
                ShimmerStatsLoading()
            } else if (!hasData) {
                EmptyStatsState()
            } else {
                // Main Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Wake-ups",
                        value = "${filteredStats.streakInfo.totalWakeUps}",
                        subtitle = "in ${uiState.selectedTimeRange.displayName.lowercase()}",
                        icon = Icons.Default.CheckCircle,
                        iconColor = WakeUpColors.iosGreen,
                        backgroundColor = WakeUpColors.iosGreen.copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Success Rate",
                        value = "${filteredStats.successRate.toInt()}%",
                        subtitle = "missions",
                        icon = Icons.Default.TrendingUp,
                        iconColor = WakeUpColors.iosBlue,
                        backgroundColor = WakeUpColors.iosBlue.copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Dynamic Chart based on time range
                when (uiState.selectedTimeRange) {
                    StatsTimeRange.WEEKLY -> {
                        WeeklyStreakChart(
                            weeklySuccess = generateWeeklyData(uiState.filteredHistory)
                        )
                    }
                    StatsTimeRange.MONTHLY -> {
                        MonthlyTrendChart(
                            history = uiState.filteredHistory
                        )
                    }
                    StatsTimeRange.ALL_TIME -> {
                        AllTimeChart(
                            history = uiState.filteredHistory
                        )
                    }
                }

                // Success Rate Donut Chart
                SuccessRateDonutChart(successRate = filteredStats.successRate)

                // Wake-up Time Line Chart
                if (uiState.filteredHistory.isNotEmpty()) {
                    WakeUpTimeChart(
                        wakeHistory = uiState.filteredHistory,
                        timeRange = uiState.selectedTimeRange
                    )
                }

                // Totals
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Best Streak",
                        value = "${uiState.stats.streakInfo.bestStreak}",
                        subtitle = "days",
                        icon = Icons.Default.EmojiEvents,
                        iconColor = WakeUpColors.iosYellow,
                        backgroundColor = WakeUpColors.iosYellow.copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Missions",
                        value = "${filteredStats.totalMissionsCompleted}",
                        subtitle = "completed",
                        icon = Icons.Default.MilitaryTech,
                        iconColor = WakeUpColors.iosPurple,
                        backgroundColor = WakeUpColors.iosPurple.copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Time Range Toggle Component - Weekly / Monthly / All-time
 */
@Composable
private fun TimeRangeToggle(
    selectedRange: StatsTimeRange,
    onRangeSelected: (StatsTimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    val ranges = StatsTimeRange.values()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ranges.forEach { range ->
                val isSelected = range == selectedRange
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = if (isSelected) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        WakeUpColors.iosBlue,
                                        WakeUpColors.iosPurple
                                    )
                                )
                            } else null
                        )
                        .clickable { onRangeSelected(range) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = range.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * Shimmer loading effect for stats screen
 */
@Composable
private fun ShimmerStatsLoading() {
    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.1f),
        Color.White.copy(alpha = 0.2f),
        Color.White.copy(alpha = 0.1f)
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Skeleton stat cards row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerCard(brush = brush, modifier = Modifier.weight(1f), height = 120.dp)
            ShimmerCard(brush = brush, modifier = Modifier.weight(1f), height = 120.dp)
        }
        
        // Skeleton chart
        ShimmerCard(brush = brush, modifier = Modifier.fillMaxWidth(), height = 200.dp)
        
        // Skeleton donut chart
        ShimmerCard(brush = brush, modifier = Modifier.fillMaxWidth(), height = 250.dp)
        
        // Skeleton stat cards row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerCard(brush = brush, modifier = Modifier.weight(1f), height = 120.dp)
            ShimmerCard(brush = brush, modifier = Modifier.weight(1f), height = 120.dp)
        }
    }
}

@Composable
private fun ShimmerCard(brush: Brush, modifier: Modifier = Modifier, height: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(20.dp))
            .background(brush)
    )
}

/**
 * Generate weekly success data from history
 */
private fun generateWeeklyData(history: List<WakeHistory>): List<Boolean> {
    val today = LocalDate.now()
    return (6 downTo 0).map { daysAgo ->
        val date = today.minusDays(daysAgo.toLong())
        history.any { it.alarmTime.toLocalDate() == date && it.success }
    }
}

@Composable
private fun EmptyStatsState() {
    val infiniteTransition = rememberInfiniteTransition(label = "float")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WakeUpColors.iosPurple.copy(alpha = 0.2f),
                        WakeUpColors.iosPink.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Sleeping icon animation
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(
                        color = WakeUpColors.iosPurple.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NightsStay,
                    contentDescription = "No data",
                    modifier = Modifier.size(64.dp),
                    tint = WakeUpColors.iosPurple
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No wake data yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Complete your first mission to see your stats!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Placeholder chart preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    repeat(7) { index ->
                        val height = 20.dp + (index * 8).dp
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(height)
                                .background(
                                    color = WakeUpColors.iosPurple.copy(
                                        alpha = 0.3f + (index * 0.1f)
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyStreakChart(weeklySuccess: List<Boolean>) {
    val dayLabels = DayOfWeek.values()
        .map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2) }

    val chartEntryModel = entryModelOf(
        weeklySuccess.mapIndexed { index, success ->
            FloatEntry(index.toFloat(), if (success) 1f else 0f)
        }
    )

    val axisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        dayLabels.getOrNull(value.toInt()) ?: ""
    }

    ChartCard(title = "Weekly Streak") {
        Chart(
            chart = columnChart(
                columns = weeklySuccess.map { isSuccess ->
                    lineComponent(
                        color = if (isSuccess) WakeUpColors.iosGreen else WakeUpColors.iosGray.copy(
                            alpha = 0.3f
                        ),
                        thickness = 24.dp,
                        shape = Shapes.roundedCornerShape(50)
                    )
                },
                axisValuesOverrider = AxisValuesOverrider.fixed(
                    minY = 0f,
                    maxY = 1.2f
                )
            ),
            chartModelProducer = ChartEntryModelProducer(),
            startAxis = startAxis(
                valueFormatter = { _, _ -> "" },
                tickLength = 0.dp,
                guideline = null
            ),
            bottomAxis = bottomAxis(
                valueFormatter = axisValueFormatter,
                tickLength = 0.dp,
                guideline = null
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(WakeUpColors.iosGreen, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Success",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(WakeUpColors.iosGray.copy(alpha = 0.5f), CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Missed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Monthly trend chart showing daily success/miss over the last 30 days
 */
@Composable
private fun MonthlyTrendChart(history: List<WakeHistory>) {
    val today = LocalDate.now()
    val daysInMonth = (29 downTo 0).map { daysAgo ->
        today.minusDays(daysAgo.toLong())
    }

    val chartEntryModel = entryModelOf(
        daysInMonth.mapIndexed { index, date ->
            val hasSuccess = history.any { it.alarmTime.toLocalDate() == date && it.success }
            val hasAttempt = history.any { it.alarmTime.toLocalDate() == date }
            val value = when {
                hasSuccess -> 1f
                hasAttempt -> 0f
                else -> 0.3f // No data - gray
            }
            FloatEntry(index.toFloat(), value)
        }
    )

    val axisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val dayIndex = value.toInt()
        if (dayIndex % 5 == 0) { // Show every 5th day
            daysInMonth.getOrNull(dayIndex)?.dayOfMonth?.toString() ?: ""
        } else ""
    }

    ChartCard(title = "30-Day Trend") {
        Text(
            text = "Last 30 days",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Chart(
            chart = columnChart(
                columns = daysInMonth.map { date ->
                    val dayHistory = history.filter { it.alarmTime.toLocalDate() == date }
                    val hasSuccess = dayHistory.any { it.success }
                    val hasAttempt = dayHistory.isNotEmpty()

                    val color = when {
                        hasSuccess -> WakeUpColors.iosGreen
                        hasAttempt -> WakeUpColors.iosRed.copy(alpha = 0.6f)
                        else -> WakeUpColors.iosGray.copy(alpha = 0.2f)
                    }

                    lineComponent(
                        color = color,
                        thickness = 8.dp,
                        shape = Shapes.roundedCornerShape(50)
                    )
                },
                axisValuesOverrider = AxisValuesOverrider.fixed(
                    minY = 0f,
                    maxY = 1.2f
                ),
                spacing = 2.dp
            ),
            chartModelProducer = ChartEntryModelProducer(),
            startAxis = startAxis(
                valueFormatter = { _, _ -> "" },
                tickLength = 0.dp,
                guideline = null
            ),
            bottomAxis = bottomAxis(
                valueFormatter = axisValueFormatter,
                tickLength = 0.dp,
                guideline = null
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )
    }
}

/**
 * All-time chart showing monthly aggregated stats
 */
@Composable
private fun AllTimeChart(history: List<WakeHistory>) {
    if (history.isEmpty()) return

    // Group by month and calculate success rate for each month
    val monthlyData = history.groupBy {
        it.alarmTime.toLocalDate().withDayOfMonth(1)
    }.mapValues { (_, entries) ->
        val total = entries.size
        val success = entries.count { it.success }
        success.toFloat() / total * 100
    }.toSortedMap()

    val chartEntryModel = entryModelOf(
        monthlyData.values.mapIndexed { index, rate ->
            FloatEntry(index.toFloat(), rate)
        }
    )

    val axisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        monthlyData.keys.toList().getOrNull(value.toInt())?.let { date ->
            date.format(java.time.format.DateTimeFormatter.ofPattern("MMM yy"))
        } ?: ""
    }

    ChartCard(title = "Monthly Success Rate") {
        Text(
            text = "${monthlyData.size} months of progress",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Chart(
            chart = lineChart(
                lines = listOf(
                    LineSpec(
                        lineColor = WakeUpColors.iosPurple,
                        lineThickness = 3.dp,
                        pointSize = 6.dp,
                        pointColor = WakeUpColors.iosGreen
                    )
                ),
                axisValuesOverrider = AxisValuesOverrider.fixed(
                    minY = 0f,
                    maxY = 100f
                )
            ),
            chartModelProducer = ChartEntryModelProducer(),
            startAxis = startAxis(
                valueFormatter = { value, _ ->
                    "${value.toInt()}%"
                },
                tickLength = 4.dp,
                guideline = lineComponent(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
            ),
            bottomAxis = bottomAxis(
                valueFormatter = axisValueFormatter,
                tickLength = 0.dp,
                guideline = null
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
    }
}

/**
 * Reusable chart card wrapper
 */
@Composable
private fun ChartCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.06f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun SuccessRateDonutChart(successRate: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.06f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Success Rate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                )

                // Animated progress arc using gradient
                val progressColor = when {
                    successRate >= 80f -> WakeUpColors.iosGreen
                    successRate >= 50f -> WakeUpColors.iosOrange
                    else -> WakeUpColors.iosRed
                }

                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulse by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulse)
                        .padding(8.dp)
                        .background(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    progressColor.copy(alpha = 0.9f),
                                    progressColor.copy(alpha = successRate / 150f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Inner circle with text
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${successRate.toInt()}%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = progressColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when {
                    successRate >= 90f -> "Excellent! You're a wake-up champion!"
                    successRate >= 70f -> "Great job! Keep building that consistency!"
                    successRate >= 50f -> "You're doing well, aim higher!"
                    else -> "Keep trying, you can do better!"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun WakeUpTimeChart(
    wakeHistory: List<WakeHistory>,
    timeRange: StatsTimeRange
) {
    val entries = when (timeRange) {
        StatsTimeRange.WEEKLY -> {
            // Last 7 days
            wakeHistory.takeLast(7).mapIndexed { index, history ->
                val hour = history.wakeTime?.hour?.toFloat() ?: 0f
                val minute = history.wakeTime?.minute?.toFloat() ?: 0f
                FloatEntry(index.toFloat(), hour + minute / 60f)
            }
        }
        StatsTimeRange.MONTHLY -> {
            // Last 14 days (spaced out for readability)
            wakeHistory.takeLast(14).mapIndexed { index, history ->
                val hour = history.wakeTime?.hour?.toFloat() ?: 0f
                val minute = history.wakeTime?.minute?.toFloat() ?: 0f
                FloatEntry(index.toFloat(), hour + minute / 60f)
            }
        }
        StatsTimeRange.ALL_TIME -> {
            // Sample every nth entry for readability based on total size
            val sampleSize = minOf(wakeHistory.size, 20)
            val step = wakeHistory.size / sampleSize
            wakeHistory.filterIndexed { index, _ -> index % step == 0 }
                .take(sampleSize)
                .mapIndexed { index, history ->
                    val hour = history.wakeTime?.hour?.toFloat() ?: 0f
                    val minute = history.wakeTime?.minute?.toFloat() ?: 0f
                    FloatEntry(index.toFloat(), hour + minute / 60f)
                }
        }
    }

    val chartEntryModel = entryModelOf(entries)

    val axisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        when (timeRange) {
            StatsTimeRange.WEEKLY -> "Day ${value.toInt() + 1}"
            StatsTimeRange.MONTHLY -> if (value.toInt() % 3 == 0) "D${value.toInt() + 1}" else ""
            StatsTimeRange.ALL_TIME -> ""
        }
    }

    val subtitleText = when (timeRange) {
        StatsTimeRange.WEEKLY -> "Last 7 days"
        StatsTimeRange.MONTHLY -> "Last 14 days"
        StatsTimeRange.ALL_TIME -> "All time trend"
    }

    ChartCard(title = "Wake-up Times") {
        Text(
            text = subtitleText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Chart(
            chart = lineChart(
                lines = listOf(
                    LineSpec(
                        lineColor = WakeUpColors.iosBlue,
                        lineThickness = 3.dp,
                        pointSize = if (timeRange == StatsTimeRange.ALL_TIME) 4.dp else 8.dp,
                        pointColor = WakeUpColors.iosBlue
                    )
                ),
                axisValuesOverrider = AxisValuesOverrider.fixed(
                    minY = 5f,
                    maxY = 12f
                )
            ),
            chartModelProducer = ChartEntryModelProducer(),
            startAxis = startAxis(
                valueFormatter = { value, _ ->
                    val hour = value.toInt()
                    val amPm = if (hour >= 12) "PM" else "AM"
                    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                    "$displayHour $amPm"
                },
                tickLength = 4.dp,
                guideline = lineComponent(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
            ),
            bottomAxis = bottomAxis(
                valueFormatter = axisValueFormatter,
                tickLength = 0.dp,
                guideline = null
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.06f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
