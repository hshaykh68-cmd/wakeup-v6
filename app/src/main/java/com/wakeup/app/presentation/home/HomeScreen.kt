package com.wakeup.app.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.domain.model.Alarm
import com.wakeup.app.presentation.home.components.*
import com.wakeup.app.presentation.home.modifiers.*
import kotlinx.coroutines.launch

/**
 * Home Screen - "Illegal Level Glass UI"
 * Complete redesign with:
 * - Aurora background with 5 animated color blobs
 * - HyperGlass cards with heavy blur + edge highlights + inner shadows
 * - Physics-based interactions (melt + spring + glow burst)
 * - Liquid touch ripple effects
 * - 5-layer depth system with parallax
 * - Moving light highlights + chromatic aberration
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onCreateAlarm: () -> Unit,
    onNavigateToAlarms: () -> Unit,
    onNavigateToSleepSounds: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val parallaxState = rememberParallaxScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Update parallax scroll state
    LaunchedEffect(scrollState.value) {
        parallaxState.scrollOffset = -scrollState.value.toFloat()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 0: Aurora Background (farthest)
        DepthLayer(
            config = DepthLayers.Background,
            scrollState = parallaxState,
            modifier = Modifier.fillMaxSize()
        ) {
            AuroraBackground(modifier = Modifier.fillMaxSize())
        }

        // Layer 1: Floating Blurred Blobs (mid-depth)
        DepthLayer(
            config = DepthLayers.FloatingBlobs,
            scrollState = parallaxState,
            modifier = Modifier.fillMaxSize()
        ) {
            // Additional floating blur elements
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(60.dp)
                    .drawBehind {
                        drawCircle(
                            color = WakeUpColors.iosPurple.copy(alpha = 0.3f),
                            radius = size.minDimension * 0.4f,
                            center = Offset(size.width * 0.2f, size.height * 0.3f)
                        )
                        drawCircle(
                            color = WakeUpColors.iosBlue.copy(alpha = 0.25f),
                            radius = size.minDimension * 0.35f,
                            center = Offset(size.width * 0.8f, size.height * 0.7f)
                        )
                    }
            )
        }

        // Layer 2-4: Content with depth
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with floating effect
            DepthLayer(
                config = DepthLayers.FloatingContent,
                scrollState = parallaxState,
                modifier = Modifier.fillMaxWidth()
            ) {
                HomeHeader(
                    streak = uiState.currentStreak,
                    greeting = uiState.greeting
                )
            }

            // Next Alarm Card with full glass effects
            DepthLayer(
                config = DepthLayers.GlassCards,
                scrollState = parallaxState,
                modifier = Modifier.fillMaxWidth()
            ) {
                NextAlarmHyperCard(
                    nextAlarm = uiState.nextAlarm,
                    onNavigateToAlarms = onNavigateToAlarms
                )
            }

            // Streak Card
            DepthLayer(
                config = DepthLayers.GlassCards,
                scrollState = parallaxState,
                modifier = Modifier.fillMaxWidth()
            ) {
                StreakHyperCard(streak = uiState.currentStreak)
            }

            // Quick Stats Row
            DepthLayer(
                config = DepthLayers.GlassCards,
                scrollState = parallaxState,
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickStatsHyperRow(
                    totalWakeUps = uiState.totalWakeUps,
                    successRate = uiState.successRate
                )
            }

            // Sleep Sounds Card
            DepthLayer(
                config = DepthLayers.GlassCards,
                scrollState = parallaxState,
                modifier = Modifier.fillMaxWidth()
            ) {
                SleepSoundsHyperCard(onClick = onNavigateToSleepSounds)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Floating Action Button with glass effect + glow
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            DepthLayer(
                config = DepthLayers.GlowOverlay,
                scrollState = parallaxState,
                modifier = Modifier.padding(24.dp)
            ) {
                GlassFAB(onClick = onCreateAlarm)
            }
        }

        // Glow overlay layer (closest)
        DepthLayer(
            config = DepthLayers.GlowOverlay,
            scrollState = parallaxState,
            modifier = Modifier.fillMaxSize()
        ) {
            // Global moving highlight overlay
            MovingHighlight(
                modifier = Modifier.fillMaxSize(),
                highlightWidth = 0.4f,
                duration = 8000,
                intensity = 0.08f
            )
        }
    }
}

@Composable
private fun HomeHeader(streak: Int, greeting: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "header")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "header_glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Background glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .drawBehind {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                WakeUpColors.iosBlue.copy(alpha = glowAlpha * 0.5f),
                                WakeUpColors.iosPurple.copy(alpha = glowAlpha * 0.3f),
                                Color.Transparent
                            )
                        )
                    )
                }
                .blur(40.dp)
        )

        // Content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Let's win the morning",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // Streak badge with breathing glow
            Box(
                modifier = Modifier
                    .breathingGlow(
                        color = WakeUpColors.iosOrange,
                        minAlpha = 0.15f,
                        maxAlpha = 0.35f
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                WakeUpColors.iosOrange.copy(alpha = 0.25f),
                                WakeUpColors.iosOrange.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = WakeUpColors.iosOrange.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = WakeUpColors.iosOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "$streak",
                        style = MaterialTheme.typography.titleLarge,
                        color = WakeUpColors.iosOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NextAlarmHyperCard(
    nextAlarm: Alarm?,
    onNavigateToAlarms: () -> Unit
) {
    AlarmHyperGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .physicsGlassInteraction { onNavigateToAlarms() },
        isActive = nextAlarm != null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            if (nextAlarm != null) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Next Alarm",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = nextAlarm.formattedTime(),
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = nextAlarm.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    if (nextAlarm.repeatDays.isNotEmpty()) {
                        Text(
                            text = com.wakeup.app.core.util.DateTimeUtil.formatRepeatDays(nextAlarm.repeatDays),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No alarms set",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Tap to create your first alarm",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakHyperCard(streak: Int) {
    HyperGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        blurAmount = 25.dp,
        gradientColors = listOf(
            WakeUpColors.iosOrange.copy(alpha = 0.12f),
            Color.White.copy(alpha = 0.06f),
            Color.White.copy(alpha = 0.03f)
        ),
        glowColor = WakeUpColors.iosOrange.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Current Streak",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = "$streak days",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Animated streak icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .breathingGlow(
                        color = WakeUpColors.iosOrange,
                        minAlpha = 0.2f,
                        maxAlpha = 0.4f
                    )
                    .clip(CircleShape)
                    .background(WakeUpColors.iosOrange.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = WakeUpColors.iosOrange,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickStatsHyperRow(
    totalWakeUps: Int,
    successRate: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Wake-ups stat
        StatHyperGlassCard(
            modifier = Modifier.weight(1f),
            accentColor = WakeUpColors.iosYellow
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(WakeUpColors.iosYellow.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = WakeUpColors.iosYellow,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = totalWakeUps.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Wake-ups",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // Success rate stat
        StatHyperGlassCard(
            modifier = Modifier.weight(1f),
            accentColor = WakeUpColors.iosGreen
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(WakeUpColors.iosGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = WakeUpColors.iosGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "${successRate.toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Success Rate",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun SleepSoundsHyperCard(onClick: () -> Unit) {
    HyperGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .physicsGlassInteraction { onClick() },
        cornerRadius = 24.dp,
        blurAmount = 25.dp,
        gradientColors = listOf(
            WakeUpColors.iosPurple.copy(alpha = 0.12f),
            WakeUpColors.iosBlue.copy(alpha = 0.06f),
            Color.White.copy(alpha = 0.03f)
        ),
        glowColor = WakeUpColors.iosPurple.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    WakeUpColors.iosPurple.copy(alpha = 0.4f),
                                    WakeUpColors.iosBlue.copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column {
                    Text(
                        text = "Sleep Sounds",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Relax & fall asleep faster",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Arrow indicator
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(WakeUpColors.iosPurple.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Open",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun GlassFAB(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fab_scale"
    )

    val glowRadius by animateDpAsState(
        targetValue = if (isPressed) 40.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "fab_glow"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.6f else 0.3f,
        animationSpec = tween(200),
        label = "fab_glow_alpha"
    )

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(scale)
            .drawBehind {
                // Outer glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            WakeUpColors.iosBlue.copy(alpha = glowAlpha),
                            WakeUpColors.iosPurple.copy(alpha = glowAlpha * 0.5f),
                            Color.Transparent
                        )
                    ),
                    radius = (36.dp + glowRadius).toPx()
                )
            }
            .clip(RoundedCornerShape(24.dp))
            .blur(20.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WakeUpColors.iosBlue.copy(alpha = 0.35f),
                        WakeUpColors.iosPurple.copy(alpha = 0.25f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.4f),
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = {
                isPressed = true
                onClick()
                isPressed = false
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Alarm",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
