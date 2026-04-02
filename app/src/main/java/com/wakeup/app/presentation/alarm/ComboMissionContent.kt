package com.wakeup.app.presentation.alarm

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.data.mission.MissionData

/**
 * Combo Mission Content - Sequential 2-factor verification mission.
 * Phase 1: Scan the saved barcode
 * Phase 2: Take a matching photo of the object
 *
 * This provides higher reliability than either mission alone - the user must both
 * have the object (barcode) AND be near it (photo match).
 */
@Composable
fun ComboMissionContent(
    missionData: MissionData,
    onMissionComplete: (Boolean) -> Unit
) {
    var currentPhase by remember { mutableStateOf(ComboPhase.BARCODE) }
    var barcodeCompleted by remember { mutableStateOf(false) }
    var photoCompleted by remember { mutableStateOf(false) }
    var attempts by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mission header
        Text(
            text = "Object Verification",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Two-step verification to dismiss alarm",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress indicator
        ComboProgressIndicator(
            currentPhase = currentPhase,
            barcodeCompleted = barcodeCompleted,
            photoCompleted = photoCompleted,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phase content
        AnimatedContent(
            targetState = currentPhase,
            transitionSpec = {
                slideInVertically { it } + fadeIn() togetherWith
                        slideOutVertically { -it } + fadeOut()
            },
            modifier = Modifier.weight(1f)
        ) { phase ->
            when (phase) {
                ComboPhase.BARCODE -> {
                    // Barcode scanning phase
                    BarcodePhaseContent(
                        missionData = missionData,
                        onBarcodeSuccess = {
                            barcodeCompleted = true
                            currentPhase = ComboPhase.PHOTO
                        },
                        onBarcodeFailed = {
                            attempts++
                        }
                    )
                }

                ComboPhase.PHOTO -> {
                    // Photo matching phase
                    PhotoPhaseContent(
                        missionData = missionData,
                        onPhotoSuccess = {
                            photoCompleted = true
                            currentPhase = ComboPhase.COMPLETE
                        },
                        onRetry = {
                            // Stay in photo phase, allow retry
                        }
                    )
                }

                ComboPhase.COMPLETE -> {
                    // Success state
                    SuccessPhaseContent(onComplete = { onMissionComplete(true) })
                }
            }
        }

        // Attempt counter for difficulty tracking
        if (attempts > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Attempts: $attempts",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ComboProgressIndicator(
    currentPhase: ComboPhase,
    barcodeCompleted: Boolean,
    photoCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Progress bar
        val progress = when {
            photoCompleted -> 1.0f
            barcodeCompleted -> 0.5f
            else -> 0.25f
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when {
                photoCompleted -> WakeUpColors.iosGreen
                barcodeCompleted -> WakeUpColors.iosBlue
                else -> WakeUpColors.iosOrange
            },
            trackColor = Color.White.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Step indicators
        Row(modifier = Modifier.fillMaxWidth()) {
            // Step 1: Barcode
            StepIndicator(
                icon = Icons.Default.QrCodeScanner,
                label = "Scan Barcode",
                isActive = currentPhase == ComboPhase.BARCODE,
                isCompleted = barcodeCompleted,
                modifier = Modifier.weight(1f)
            )

            // Arrow
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = if (barcodeCompleted) WakeUpColors.iosGreen else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Step 2: Photo
            StepIndicator(
                icon = Icons.Default.PhotoCamera,
                label = "Match Photo",
                isActive = currentPhase == ComboPhase.PHOTO,
                isCompleted = photoCompleted,
                modifier = Modifier.weight(1f)
            )

            // Arrow
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = if (photoCompleted) WakeUpColors.iosGreen else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Step 3: Complete
            StepIndicator(
                icon = Icons.Default.Lock,
                label = "Complete",
                isActive = currentPhase == ComboPhase.COMPLETE,
                isCompleted = photoCompleted,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StepIndicator(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val color = when {
        isCompleted -> WakeUpColors.iosGreen
        isActive -> WakeUpColors.iosBlue
        else -> Color.White.copy(alpha = 0.4f)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BarcodePhaseContent(
    missionData: MissionData,
    onBarcodeSuccess: () -> Unit,
    onBarcodeFailed: () -> Unit
) {
    // Use existing BarcodeMissionContent with a callback wrapper
    Column {
        // Phase header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = WakeUpColors.iosBlue.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Step 1: Scan Barcode",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = WakeUpColors.iosBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "First, scan the barcode on your saved object",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Reuse existing barcode mission content
        BarcodeMissionContent(
            missionData = missionData,
            onMissionComplete = { success ->
                if (success) {
                    onBarcodeSuccess()
                } else {
                    onBarcodeFailed()
                }
            }
        )
    }
}

@Composable
private fun PhotoPhaseContent(
    missionData: MissionData,
    onPhotoSuccess: () -> Unit,
    onRetry: () -> Unit
) {
    Column {
        // Phase header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = WakeUpColors.iosPurple.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Step 2: Photo Verification",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = WakeUpColors.iosPurple
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Now take a matching photo of the object",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This verifies you're actually at the object",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Reuse existing photo mission content
        PhotoMissionContent(
            missionData = missionData,
            onMissionComplete = { success ->
                if (success) {
                    onPhotoSuccess()
                } else {
                    onRetry()
                }
            }
        )
    }
}

@Composable
private fun SuccessPhaseContent(
    onComplete: () -> Unit
) {
    // Auto-complete after showing success
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(WakeUpColors.iosGreen.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Success animation
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(60.dp))
                    .background(WakeUpColors.iosGreen.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = WakeUpColors.iosGreen,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Verification Complete!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = WakeUpColors.iosGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Both barcode and photo verified successfully",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2-factor verification badge
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = WakeUpColors.iosGreen.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = WakeUpColors.iosGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "2-Factor Verified",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = WakeUpColors.iosGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            CircularProgressIndicator(
                color = WakeUpColors.iosGreen,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dismissing alarm...",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

private enum class ComboPhase {
    BARCODE,
    PHOTO,
    COMPLETE
}
