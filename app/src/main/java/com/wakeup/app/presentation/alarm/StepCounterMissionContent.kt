package com.wakeup.app.presentation.alarm

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.domain.service.HapticsController
import com.wakeup.app.core.util.StepDetector
import com.wakeup.app.domain.model.MissionDifficulty
import kotlinx.coroutines.launch

/**
 * Composable for Step Counter mission.
 * User must walk a target number of steps to dismiss the alarm.
 * Uses hardware step counter when available, falls back to accelerometer.
 */
@Composable
fun StepCounterMissionContent(
    missionDifficulty: MissionDifficulty,
    hapticsController: HapticsController,
    onMissionComplete: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val stepDetector = remember { StepDetector(context) }

    val targetSteps = remember { stepDetector.getTargetStepsForDifficulty(missionDifficulty.ordinal) }
    var currentSteps by remember { mutableStateOf(0) }
    var isComplete by remember { mutableStateOf(false) }
    var isSensorAvailable by remember { mutableStateOf(true) }

    // Animation for step milestone
    val scope = rememberCoroutineScope()
    val milestoneScale = remember { Animatable(1f) }

    // Check sensor availability
    LaunchedEffect(Unit) {
        isSensorAvailable = stepDetector.isStepDetectionAvailable()
        if (!isSensorAvailable) {
            // Fallback - allow manual completion
            return@LaunchedEffect
        }

        stepDetector.resetCount()
        stepDetector.startListening(
            targetSteps = targetSteps,
            onStep = { steps ->
                currentSteps = steps

                // Haptic feedback every 10 steps
                if (stepDetector.shouldTriggerHaptic()) {
                    hapticsController.performLightImpact()

                    // Pulse animation on milestone
                    scope.launch {
                        milestoneScale.animateTo(
                            targetValue = 1.2f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        milestoneScale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                }
            },
            onComplete = {
                isComplete = true
                hapticsController.performSuccess()
                onMissionComplete(true)
            }
        )
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            stepDetector.stopListening()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isSensorAvailable) {
            // Fallback UI when sensor not available
            SensorNotAvailableFallback(onMissionComplete)
            return@Column
        }

        // Header
        Text(
            text = "Walk to Dismiss",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Take ${targetSteps} steps to turn off the alarm",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Progress ring with step count
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background circle
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = Color.White.copy(alpha = 0.1f),
                strokeWidth = 12.dp,
                trackColor = Color.Transparent
            )

            // Progress arc
            val progress = (currentSteps.toFloat() / targetSteps).coerceIn(0f, 1f)
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = if (progress >= 1f) WakeUpColors.iosGreen else WakeUpColors.iosBlue,
                strokeWidth = 12.dp,
                trackColor = Color.Transparent
            )

            // Center content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(milestoneScale.value)
            ) {
                // Walking icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    WakeUpColors.iosBlue.copy(alpha = 0.3f),
                                    WakeUpColors.iosBlue.copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = "Walking",
                        modifier = Modifier.size(32.dp),
                        tint = if (progress >= 1f) WakeUpColors.iosGreen else WakeUpColors.iosBlue
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Step count display
                Text(
                    text = "$currentSteps",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "/ $targetSteps steps",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Progress text
        val remainingSteps = (targetSteps - currentSteps).coerceAtLeast(0)
        Text(
            text = if (remainingSteps > 0) {
                "$remainingSteps steps remaining"
            } else {
                "Mission complete!"
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = if (remainingSteps == 0) WakeUpColors.iosGreen else Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Motivational messages based on progress
        val message = when {
            currentSteps == 0 -> "Start walking! 🚶"
            currentSteps < targetSteps / 4 -> "Good start! Keep going! 👟"
            currentSteps < targetSteps / 2 -> "You're making progress! 💪"
            currentSteps < targetSteps * 3 / 4 -> "Almost there! 🏃"
            currentSteps < targetSteps -> "Just a few more steps! 🎯"
            else -> "Great job! Alarm dismissed! 🎉"
        }

        Text(
            text = message,
            fontSize = 16.sp,
            color = WakeUpColors.iosBlue,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Difficulty indicator
        Text(
            text = "Difficulty: ${missionDifficulty.displayName()}",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        // Manual fallback button (shown after 30 seconds or if user is struggling)
        if (currentSteps > 0 && currentSteps < targetSteps) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onMissionComplete(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    "I'm having trouble - Skip Mission",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SensorNotAvailableFallback(onMissionComplete: (Boolean) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsWalk,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = WakeUpColors.iosOrange
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Step sensor not available",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your device doesn't have a step counter sensor. You can skip this mission.",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onMissionComplete(true) },
            colors = ButtonDefaults.buttonColors(
                containerColor = WakeUpColors.iosBlue
            )
        ) {
            Text("Dismiss Alarm")
        }
    }
}
