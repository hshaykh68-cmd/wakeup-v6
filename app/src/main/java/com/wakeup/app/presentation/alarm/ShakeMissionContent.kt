package com.wakeup.app.presentation.alarm

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.domain.service.HapticsController
import com.wakeup.app.core.util.ShakeDetector
import com.wakeup.app.data.mission.MissionData
import kotlinx.coroutines.delay

/**
 * Shake mission content - displays shake counter with timer
 */
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
internal fun ShakeMissionContent(
    missionData: MissionData,
    hapticsController: HapticsController,
    onMissionComplete: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val shakeDetector = remember { ShakeDetector(context) }

    val requiredShakes = missionData.answer.toIntOrNull() ?: 20
    val timeLimit = missionData.metadata["timeLimit"]?.toIntOrNull() ?: 15

    var shakeCount by remember { mutableStateOf(0) }
    var timeRemaining by remember { mutableStateOf(timeLimit) }
    var isFailed by remember { mutableStateOf(false) }
    var isComplete by remember { mutableStateOf(false) }

    // Collect shake count from detector
    val detectorShakeCount by shakeDetector.shakeCountFlow.collectAsState()

    LaunchedEffect(detectorShakeCount) {
        if (!isComplete && !isFailed) {
            shakeCount = detectorShakeCount
            if (shakeCount >= requiredShakes) {
                isComplete = true
                shakeDetector.stopListening()
                onMissionComplete(true)
            }
        }
    }

    // Countdown timer
    LaunchedEffect(Unit) {
        shakeDetector.startListening {}
        while (timeRemaining > 0 && !isComplete && !isFailed) {
            delay(1000)
            timeRemaining--
        }
        if (timeRemaining == 0 && !isComplete) {
            isFailed = true
            shakeDetector.stopListening()
        }
    }

    // Cleanup with DisposableEffect
    DisposableEffect(Unit) {
        onDispose {
            shakeDetector.stopListening()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Timer display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (timeRemaining <= 5) WakeUpColors.iosRed.copy(alpha = 0.2f)
                    else Color.White.copy(alpha = 0.1f)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Time Remaining",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$timeRemaining s",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (timeRemaining <= 5) WakeUpColors.iosRed else Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Shake counter
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            WakeUpColors.iosBlue.copy(alpha = 0.3f),
                            WakeUpColors.iosBlue.copy(alpha = 0.1f)
                        )
                    )
                )
                .border(
                    width = 3.dp,
                    color = if (isComplete) WakeUpColors.iosGreen else WakeUpColors.iosBlue,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$shakeCount",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "/ $requiredShakes",
                    fontSize = 24.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Shake progress indicator
        LinearProgressIndicator(
            progress = shakeCount.toFloat() / requiredShakes,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = if (isComplete) WakeUpColors.iosGreen else WakeUpColors.iosBlue,
            trackColor = Color.White.copy(alpha = 0.2f),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Status text
        when {
            isComplete -> {
                Text(
                    text = "Mission Complete!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = WakeUpColors.iosGreen
                )
            }
            isFailed -> {
                Text(
                    text = "Time's Up!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = WakeUpColors.iosRed
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Shake count: $shakeCount / $requiredShakes",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        shakeDetector.resetCount()
                        shakeCount = 0
                        timeRemaining = timeLimit
                        isFailed = false
                        shakeDetector.startListening {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WakeUpColors.iosBlue)
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Try Again")
                }
            }
            else -> {
                Text(
                    text = "Shake your phone!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${requiredShakes - shakeCount} more shakes needed",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
