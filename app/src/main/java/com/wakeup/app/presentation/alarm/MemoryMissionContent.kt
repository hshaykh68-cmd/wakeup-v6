package com.wakeup.app.presentation.alarm

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.core.util.MissionSoundManager
import com.wakeup.app.domain.service.HapticsController
import com.wakeup.app.data.mission.MissionData
import com.wakeup.app.domain.model.MissionDifficulty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Memory mission content - pattern memorization game with Simon Says style
 */
@Composable
internal fun MemoryMissionContent(
    missionData: MissionData,
    patternInput: MutableList<Int>,
    showPattern: Boolean,
    strictMode: Boolean = false,
    hapticsController: HapticsController,
    onShowPattern: (Boolean) -> Unit,
    onPatternComplete: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { MissionSoundManager(context) }
    val scope = rememberCoroutineScope()
    val targetPattern = missionData.pattern
    var hasFailed by remember { mutableStateOf(false) }

    // Animation states for button presses
    val buttonScales = remember { Array(4) { Animatable(1f) } }
    val buttonAlphas = remember { Array(4) { Animatable(1f) } }

    // Animation state for pattern display (highlighting current item)
    var highlightedIndex by remember { mutableStateOf(-1) }

    // Get pattern playback speed based on difficulty
    val patternSpeedMs = soundManager.getPatternSpeedMs(missionData.difficulty.ordinal)

    // Play pattern with animation and sound when showing
    LaunchedEffect(showPattern) {
        if (showPattern) {
            highlightedIndex = -1
            // Sequentially highlight and play tone for each pattern item
            targetPattern.forEachIndexed { index, number ->
                delay(patternSpeedMs)
                highlightedIndex = index
                soundManager.playPatternTone(number)
                hapticsController.performTick()
                delay(patternSpeedMs / 2)  // Show highlight for half the interval
                highlightedIndex = -1
            }
        }
    }

    // Cleanup sound manager
    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    if (showPattern) {
        // Show the pattern to memorize with animation
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Remember this pattern:",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Animated pattern display
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                targetPattern.forEachIndexed { index, number ->
                    val isHighlighted = index == highlightedIndex
                    val scale by animateFloatAsState(
                        targetValue = if (isHighlighted) 1.15f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .scale(scale)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when (number) {
                                    1 -> WakeUpColors.iosRed
                                    2 -> WakeUpColors.iosBlue
                                    3 -> WakeUpColors.iosGreen
                                    4 -> WakeUpColors.iosYellow
                                    else -> WakeUpColors.iosPurple
                                }.copy(alpha = if (isHighlighted) 1f else 0.6f)
                            )
                            .border(
                                width = if (isHighlighted) 3.dp else 0.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number.toString(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Difficulty indicator
            val speedText = when (missionData.difficulty) {
                MissionDifficulty.EASY -> "Slow pace - Take your time"
                MissionDifficulty.MEDIUM -> "Normal pace"
                MissionDifficulty.HARD -> "Fast pace - Stay focused!"
            }
            Text(
                text = speedText,
                fontSize = 14.sp,
                color = WakeUpColors.iosOrange,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    hapticsController.performMediumImpact()
                    onShowPattern(false)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WakeUpColors.iosBlue)
            ) {
                Text("I'm Ready")
            }
        }
    } else {
        // Input phase
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Repeat the pattern:",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Show entered pattern
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(targetPattern.size) { index ->
                    val entered = patternInput.getOrNull(index)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (entered != null) {
                                    when (entered) {
                                        1 -> WakeUpColors.iosRed
                                        2 -> WakeUpColors.iosBlue
                                        3 -> WakeUpColors.iosGreen
                                        4 -> WakeUpColors.iosYellow
                                        else -> WakeUpColors.iosPurple
                                    }
                                } else Color.White.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (entered != null) {
                            Text(
                                text = entered.toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Number pad with enhanced animations
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimatedNumberButton(
                        number = 1,
                        color = WakeUpColors.iosRed,
                        patternInput = patternInput,
                        targetPattern = targetPattern,
                        scale = buttonScales[0],
                        alpha = buttonAlphas[0],
                        haptics = hapticsController,
                        soundManager = soundManager
                    ) {
                        patternInput.add(1)
                        validateAndPlayPattern(
                            patternInput,
                            targetPattern,
                            strictMode,
                            hasFailed,
                            { hasFailed = it },
                            onPatternComplete,
                            hapticsController,
                            soundManager
                        )
                    }
                    AnimatedNumberButton(
                        number = 2,
                        color = WakeUpColors.iosBlue,
                        patternInput = patternInput,
                        targetPattern = targetPattern,
                        scale = buttonScales[1],
                        alpha = buttonAlphas[1],
                        haptics = hapticsController,
                        soundManager = soundManager
                    ) {
                        patternInput.add(2)
                        validateAndPlayPattern(
                            patternInput,
                            targetPattern,
                            strictMode,
                            hasFailed,
                            { hasFailed = it },
                            onPatternComplete,
                            hapticsController,
                            soundManager
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimatedNumberButton(
                        number = 3,
                        color = WakeUpColors.iosGreen,
                        patternInput = patternInput,
                        targetPattern = targetPattern,
                        scale = buttonScales[2],
                        alpha = buttonAlphas[2],
                        haptics = hapticsController,
                        soundManager = soundManager
                    ) {
                        patternInput.add(3)
                        validateAndPlayPattern(
                            patternInput,
                            targetPattern,
                            strictMode,
                            hasFailed,
                            { hasFailed = it },
                            onPatternComplete,
                            hapticsController,
                            soundManager
                        )
                    }
                    AnimatedNumberButton(
                        number = 4,
                        color = WakeUpColors.iosYellow,
                        patternInput = patternInput,
                        targetPattern = targetPattern,
                        scale = buttonScales[3],
                        alpha = buttonAlphas[3],
                        haptics = hapticsController,
                        soundManager = soundManager
                    ) {
                        patternInput.add(4)
                        validateAndPlayPattern(
                            patternInput,
                            targetPattern,
                            strictMode,
                            hasFailed,
                            { hasFailed = it },
                            onPatternComplete,
                            hapticsController,
                            soundManager
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Check button - only show when pattern is complete or in strict mode on failure
            if (patternInput.size == targetPattern.size || (strictMode && hasFailed)) {
                val isCorrect = if (strictMode && hasFailed) false else patternInput.toList() == targetPattern
                Button(
                    onClick = { onPatternComplete(isCorrect) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCorrect) WakeUpColors.iosGreen else WakeUpColors.iosRed
                    )
                ) {
                    Icon(Icons.Default.Check, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isCorrect) "Correct!" else if (strictMode && hasFailed) "Failed - Try Again" else "Wrong Pattern")
                }
            }

            // Strict mode indicator
            if (strictMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ Strict Mode: One mistake = fail",
                    fontSize = 14.sp,
                    color = WakeUpColors.iosOrange,
                    fontWeight = FontWeight.Medium
                )
            }

            // Reset button
            TextButton(
                onClick = {
                    patternInput.clear()
                    onShowPattern(true)
                }
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Show Pattern Again")
            }
        }
    }
}

/**
 * Animated number button with scale and alpha animations for visual feedback
 */
@Composable
private fun AnimatedNumberButton(
    number: Int,
    color: Color,
    patternInput: MutableList<Int>,
    targetPattern: List<Int>,
    scale: Animatable<Float, *>,
    alpha: Animatable<Float, *>,
    haptics: HapticsController,
    soundManager: MissionSoundManager? = null,
    onClick: () -> Unit
) {
    val isDisabled = patternInput.size >= targetPattern.size
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            // Trigger haptic, animation, and sound
            haptics.performLightImpact()
            soundManager?.playPatternTone(number)

            scope.launch {
                // Scale down then up (press effect)
                scale.animateTo(
                    targetValue = 0.9f,
                    animationSpec = tween(50, easing = FastOutLinearInEasing)
                )
                scale.animateTo(
                    targetValue = 1.05f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                )
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(100)
                )
            }

            scope.launch {
                // Flash alpha
                alpha.animateTo(
                    targetValue = 0.7f,
                    animationSpec = tween(50)
                )
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(150)
                )
            }

            onClick()
        },
        modifier = Modifier
            .size(80.dp)
            .scale(scale.value)
            .graphicsLayer { this.alpha = alpha.value },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.3f)
        ),
        enabled = !isDisabled
    ) {
        Text(
            text = number.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Validates the pattern input and handles strict mode logic
 */
private fun validateAndPlayPattern(
    patternInput: MutableList<Int>,
    targetPattern: List<Int>,
    strictMode: Boolean,
    hasFailed: Boolean,
    setFailed: (Boolean) -> Unit,
    onPatternComplete: (Boolean) -> Unit,
    haptics: HapticsController,
    soundManager: MissionSoundManager? = null
) {
    if (strictMode) {
        // Check if current input matches target so far
        val currentIndex = patternInput.size - 1
        if (currentIndex >= 0 && currentIndex < targetPattern.size) {
            if (patternInput[currentIndex] != targetPattern[currentIndex]) {
                // Wrong input in strict mode
                setFailed(true)
                soundManager?.playErrorSound()
                haptics.performError()
                return
            }
        }

        // Check if pattern is complete and correct
        if (patternInput.size == targetPattern.size && !hasFailed) {
            soundManager?.playSuccessSound()
            haptics.performSuccess()
            onPatternComplete(true)
        }
    } else {
        // Non-strict mode: only validate when complete
        if (patternInput.size == targetPattern.size) {
            val isCorrect = patternInput.toList() == targetPattern
            if (isCorrect) {
                soundManager?.playSuccessSound()
                haptics.performSuccess()
                onPatternComplete(true)
            }
        }
    }
}
