package com.wakeup.app.presentation.alarm

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.core.util.ShakeDetector
import com.wakeup.app.core.util.MissionSoundManager
import com.wakeup.app.core.util.StepDetector
import com.wakeup.app.core.billing.BillingManager
import com.wakeup.app.domain.model.MissionDifficulty
import com.wakeup.app.domain.model.MissionType
import com.wakeup.app.domain.service.HapticsController
import com.wakeup.app.data.mission.MissionData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MissionScreen(
    missionType: MissionType,
    missionDifficulty: MissionDifficulty,
    missionData: MissionData,
    alarmId: String,
    photoReferencePath: String? = null,
    photoReferenceHash: String? = null,
    barcodeValue: String? = null,
    barcodeFormat: String? = null,
    snoozeInterval: Int = 5,
    hapticsController: HapticsController,
    onMissionComplete: (Boolean) -> Unit,
    onSnooze: ((Int) -> Unit)?
) {
    var userInput by remember { mutableStateOf("") }
    var attempts by remember { mutableStateOf(0) }
    var showError by remember { mutableStateOf(false) }
    var isComplete by remember { mutableStateOf(false) }
    
    // For memory mission pattern
    val patternInput = remember { mutableStateListOf<Int>() }
    var showPattern by remember { mutableStateOf(true) }
    
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WakeUpColors.iosDarkBackground,
                        WakeUpColors.iosBlue.copy(alpha = 0.2f),
                        WakeUpColors.iosDarkBackground
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    text = missionType.displayName(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Complete to dismiss alarm",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            // Mission Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (missionType) {
                    MissionType.MATH -> MathMissionContent(
                        missionData = missionData,
                        userInput = userInput,
                        onInputChange = { 
                            userInput = it
                            showError = false
                        },
                        showError = showError
                    )
                    MissionType.MEMORY -> MemoryMissionContent(
                        missionData = missionData,
                        patternInput = patternInput,
                        showPattern = showPattern,
                        strictMode = missionData.strictMode,
                        hapticsController = hapticsController,
                        onShowPattern = { showPattern = it },
                        onPatternComplete = { isCorrect ->
                            if (isCorrect) {
                                hapticsController.performSuccess()
                                isComplete = true
                                onMissionComplete(true)
                            } else {
                                hapticsController.performError()
                                patternInput.clear()
                            }
                        }
                    )
                    MissionType.TYPING -> TypingMissionContent(
                        missionData = missionData,
                        userInput = userInput,
                        onInputChange = {
                            userInput = it
                            showError = false
                        },
                        showError = showError
                    )
                    MissionType.SHAKE -> ShakeMissionContent(
                        missionData = missionData,
                        hapticsController = hapticsController,
                        onMissionComplete = { success ->
                            if (success) {
                                hapticsController.performSuccess()
                            } else {
                                hapticsController.performError()
                            }
                            isComplete = true
                            onMissionComplete(success)
                        }
                    )
                    MissionType.BARCODE -> BarcodeMissionContent(
                        missionData = missionData,
                        onMissionComplete = { success ->
                            if (success) {
                                hapticsController.performSuccess()
                            } else {
                                hapticsController.performError()
                            }
                            isComplete = true
                            onMissionComplete(success)
                        }
                    )
                    MissionType.PHOTO -> PhotoMissionContent(
                        missionData = missionData,
                        onMissionComplete = { success ->
                            if (success) {
                                hapticsController.performSuccess()
                            } else {
                                hapticsController.performError()
                            }
                            isComplete = true
                            onMissionComplete(success)
                        }
                    )
                    MissionType.STEP -> StepCounterMissionContent(
                        missionDifficulty = missionData.difficulty,
                        hapticsController = hapticsController,
                        onMissionComplete = { success ->
                            if (success) {
                                hapticsController.performSuccess()
                            } else {
                                hapticsController.performError()
                            }
                            isComplete = true
                            onMissionComplete(success)
                        }
                    )
                    MissionType.COMBO_BARCODE_PHOTO -> ComboMissionContent(
                        missionData = missionData,
                        onMissionComplete = { success ->
                            if (success) {
                                hapticsController.performSuccess()
                            } else {
                                hapticsController.performError()
                            }
                            isComplete = true
                            onMissionComplete(success)
                        }
                    )
                }
            }
            
            // Bottom Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (missionType != MissionType.MEMORY && missionType != MissionType.SHAKE && 
                    missionType != MissionType.BARCODE && missionType != MissionType.PHOTO &&
                    missionType != MissionType.STEP && missionType != MissionType.COMBO_BARCODE_PHOTO) {
                    Button(
                        onClick = {
                            attempts++
                            if (validateMission(missionType, userInput, missionData)) {
                                hapticsController.performSuccess()
                                isComplete = true
                                onMissionComplete(true)
                            } else {
                                hapticsController.performError()
                                showError = true
                                userInput = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WakeUpColors.iosBlue
                        )
                    ) {
                        Text(
                            text = "Submit",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                if (attempts > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Attempt $attempts",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                
                if (onSnooze != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = {
                        hapticsController.performMediumImpact()
                        onSnooze(snoozeInterval)
                    }) {
                        Text(
                            text = "Snooze ($snoozeInterval min)",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

private fun validateMission(type: MissionType, userInput: String, missionData: MissionData): Boolean {
    return when (type) {
        MissionType.MATH -> userInput.trim() == missionData.answer
        MissionType.TYPING -> userInput.trim().lowercase() == missionData.answer.trim().lowercase()
        else -> false
    }
}
