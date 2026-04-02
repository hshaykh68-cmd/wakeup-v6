package com.wakeup.app.presentation.sleep

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.domain.model.SleepSound
import com.wakeup.app.domain.model.SleepTimerOption
import com.wakeup.app.presentation.navigation.Screen
import com.wakeup.app.presentation.sleep.SleepSoundsUiState

import android.app.Activity
import android.content.Context
import androidx.compose.material3.AlertDialog
import com.wakeup.app.core.util.BatteryOptimizationHelper

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SleepSoundsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit = {},
    viewModel: SleepSoundsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val activity = context as? Activity

    // Enable immersive mode (hide system UI)
    DisposableEffect(Unit) {
        val window = activity?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, it.decorView) }

        insetsController?.let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Refresh battery optimization status when screen becomes active
    DisposableEffect(Unit) {
        viewModel.refreshBatteryOptimizationStatus()
        onDispose { }
    }

    // Premium Dialog
    if (uiState.showPremiumDialog) {
        PremiumDialog(
            onDismiss = { viewModel.hidePremiumDialog() },
            onGoPremium = {
                viewModel.hidePremiumDialog()
                onNavigateToPremium()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Animated background gradient
        AnimatedBackground(isPlaying = uiState.isPlaying)

        // Content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            SleepSoundsTopBar(
                onNavigateBack = onNavigateBack,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Battery Optimization Warning (if needed)
            if (!uiState.isIgnoringBatteryOptimizations) {
                BatteryOptimizationCard(
                    onClick = {
                        activity?.let { showBatteryOptimizationDialog(context, it) }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Sound Icon
            AnimatedSoundIcon(
                sound = uiState.selectedSound,
                isPlaying = uiState.isPlaying,
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sound Name
            AnimatedContent(
                targetState = uiState.selectedSound?.name ?: "Select a Sound",
                transitionSpec = { fadeIn() with fadeOut() },
                label = "sound_name"
            ) { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Timer Display
            val remainingTime = uiState.remainingTimeMillis
            if (remainingTime != null && remainingTime > 0) {
                Text(
                    text = viewModel.formatRemainingTime(),
                    style = MaterialTheme.typography.titleLarge,
                    color = WakeUpColors.iosBlue,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Play/Pause Button
            PlayPauseButton(
                isPlaying = uiState.isPlaying,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (!uiState.isPlaying && !uiState.isPremium) {
                        // Trying to play but not premium - show dialog
                        viewModel.showPremiumDialog()
                    } else {
                        viewModel.togglePlayPause()
                    }
                },
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Timer Selector
            TimerSelector(
                selectedOption = uiState.timerOption,
                onOptionSelected = { option ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.setTimer(option)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sound Grid
            Text(
                text = "Choose Sound",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            SoundGrid(
                sounds = uiState.availableSounds,
                selectedSound = uiState.selectedSound,
                onSoundSelected = { sound ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.selectSound(sound)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SleepSoundsTopBar(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = null,
                tint = WakeUpColors.iosPurple,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Sleep Sounds",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun AnimatedSoundIcon(
    sound: SleepSound?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.1f else 1f,
        animationSpec = tween(1500),
        label = "scale"
    )

    val blurRadius by animateDpAsState(
        targetValue = if (isPlaying) 20.dp else 0.dp,
        animationSpec = tween(1500),
        label = "blur"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Glow effect when playing
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .blur(blurRadius)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                WakeUpColors.iosBlue.copy(alpha = 0.6f),
                                WakeUpColors.iosPurple.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Icon
        Box(
            modifier = Modifier
                .scale(scale)
                .size(100.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            WakeUpColors.iosBlue.copy(alpha = 0.8f),
                            WakeUpColors.iosPurple.copy(alpha = 0.8f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (sound != null) {
                Icon(
                    imageVector = sound.icon,
                    contentDescription = sound.name,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(100),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Glassmorphic border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        )

        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                scaleIn(animationSpec = tween(200)) with scaleOut(animationSpec = tween(200))
            },
            label = "play_pause_icon"
        ) { playing ->
            if (playing) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalAnimationApi::class)
@Composable
private fun TimerSelector(
    selectedOption: SleepTimerOption,
    onOptionSelected: (SleepTimerOption) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SleepTimerOption.entries.forEach { option ->
            TimerChip(
                option = option,
                isSelected = selectedOption == option,
                onClick = { onOptionSelected(option) }
            )
        }
    }
}

@Composable
private fun TimerChip(
    option: SleepTimerOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        WakeUpColors.iosBlue.copy(alpha = 0.3f)
    } else {
        Color.White.copy(alpha = 0.08f)
    }

    val textColor = if (isSelected) {
        WakeUpColors.iosBlue
    } else {
        Color.White.copy(alpha = 0.7f)
    }

    val borderColor = if (isSelected) {
        WakeUpColors.iosBlue.copy(alpha = 0.5f)
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = option.displayText,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun SoundGrid(
    sounds: List<SleepSound>,
    selectedSound: SleepSound?,
    onSoundSelected: (SleepSound) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sounds) { sound ->
            SoundCard(
                sound = sound,
                isSelected = selectedSound?.id == sound.id,
                onClick = { onSoundSelected(sound) }
            )
        }
    }
}

@Composable
private fun SoundCard(
    sound: SleepSound,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(
                WakeUpColors.iosBlue.copy(alpha = 0.3f),
                WakeUpColors.iosPurple.copy(alpha = 0.2f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.1f),
                Color.White.copy(alpha = 0.05f)
            )
        )
    }

    val borderColor = if (isSelected) {
        WakeUpColors.iosBlue.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.1f)
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                WakeUpColors.iosBlue.copy(alpha = 0.3f)
                            } else {
                                Color.White.copy(alpha = 0.1f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = sound.icon,
                        contentDescription = null,
                        tint = if (isSelected) WakeUpColors.iosBlue else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Name
                Text(
                    text = sound.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun AnimatedBackground(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")

    // Animated color positions for gradient
    val colorPosition1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color1"
    )

    val colorPosition2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color2"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPlaying) 0.6f else 0.3f,
        animationSpec = tween(2000),
        label = "background_alpha"
    )

    // Create animated gradient colors
    val color1 = lerpColor(
        start = Color(0xFF1a1a2e),
        stop = Color(0xFF16213e),
        fraction = colorPosition1
    )

    val color2 = lerpColor(
        start = Color(0xFF0f3460),
        stop = Color(0xFF1a1a2e),
        fraction = colorPosition2
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color1.copy(alpha = alpha),
                        color2.copy(alpha = alpha * 0.7f),
                        Color.Black
                    ),
                    center = androidx.compose.ui.geometry.Offset(
                        0.5f + (colorPosition1 - 0.5f) * 0.3f,
                        0.5f + (colorPosition2 - 0.5f) * 0.3f
                    )
                )
            )
    )

    // Floating particles effect when playing
    if (isPlaying) {
        FloatingParticles()
    }
}

@Composable
private fun FloatingParticles() {
    val particleCount = 20
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    Box(modifier = Modifier.fillMaxSize()) {
        repeat(particleCount) { index ->
            val offsetX by infiniteTransition.animateFloat(
                initialValue = (index % 5) * 0.2f,
                targetValue = ((index % 5) * 0.2f + 0.3f) % 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (5000 + index * 500),
                        easing = EaseInOutSine
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particle_x_$index"
            )

            val offsetY by infiniteTransition.animateFloat(
                initialValue = (index / 5) * 0.2f,
                targetValue = ((index / 5) * 0.2f + 0.2f) % 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (6000 + index * 400),
                        easing = EaseInOutSine
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particle_y_$index"
            )

            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (3000 + index * 200),
                        easing = EaseInOutSine
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particle_alpha_$index"
            )

            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (4000 + index * 300),
                        easing = EaseInOutSine
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particle_scale_$index"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size((4 + index % 4).dp)
                        .scale(scale)
                        .alpha(alpha)
                        .background(
                            WakeUpColors.iosBlue.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .align(
                            Alignment.TopStart
                        )
                )
            }
        }
    }
}

private fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = androidx.compose.ui.util.lerp(start.red, stop.red, fraction),
        green = androidx.compose.ui.util.lerp(start.green, stop.green, fraction),
        blue = androidx.compose.ui.util.lerp(start.blue, stop.blue, fraction),
        alpha = androidx.compose.ui.util.lerp(start.alpha, stop.alpha, fraction)
    )
}

@Composable
private fun BatteryOptimizationCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            WakeUpColors.iosOrange.copy(alpha = 0.2f),
                            WakeUpColors.iosRed.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                    contentDescription = null,
                    tint = WakeUpColors.iosOrange,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Battery Optimization Active",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to disable for uninterrupted sleep sounds",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.ArrowForward,
                    contentDescription = "Open settings",
                    tint = WakeUpColors.iosOrange,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun showBatteryOptimizationDialog(context: Context, activity: Activity) {
    android.app.AlertDialog.Builder(activity)
        .setTitle("Battery Optimization")
        .setMessage(
            "To ensure sleep sounds continue playing reliably throughout the night, " +
            "please disable battery optimization for WakeUp.\n\n" +
            "This prevents Android from automatically stopping the audio to save battery."
        )
        .setPositiveButton("Open Settings") { _, _ ->
            val intent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
            try {
                activity.startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                }
                activity.startActivity(fallbackIntent)
            }
        }
        .setNegativeButton("Not Now") { dialog, _ ->
            dialog.dismiss()
        }
        .setCancelable(true)
        .show()
}

@Composable
private fun PremiumDialog(
    onDismiss: () -> Unit,
    onGoPremium: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1a1a2e)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Premium Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    WakeUpColors.iosOrange,
                                    WakeUpColors.iosYellow
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Premium Feature",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = "Sleep Sounds are available exclusively for premium users. Upgrade to enjoy relaxing sounds for better sleep.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Go Premium Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    WakeUpColors.iosOrange,
                                    WakeUpColors.iosYellow
                                )
                            )
                        )
                        .clickable(onClick = onGoPremium)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Go Premium",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Maybe Later Button
                Text(
                    text = "Maybe Later",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.clickable(onClick = onDismiss)
                )
            }
        }
    }
}
