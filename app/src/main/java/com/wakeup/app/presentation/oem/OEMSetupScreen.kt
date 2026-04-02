package com.wakeup.app.presentation.oem

import android.content.ActivityNotFoundException
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakeup.app.core.oem.AggressionLevel
import com.wakeup.app.core.oem.OEMConfiguration
import com.wakeup.app.core.oem.OEMType
import com.wakeup.app.core.oem.SetupStep
import com.wakeup.app.core.theme.WakeUpColors

import androidx.compose.animation.ExperimentalAnimationApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun OEMSetupScreen(
    viewModel: OEMSetupViewModel = hiltViewModel(),
    onSetupComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Setup") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            when {
                uiState.isLoading -> {
                    OEMSetupLoading()
                }
                uiState.showCertificationAward -> {
                    OEMCertificationAwardScreen(
                        oemType = uiState.deviceProfile?.oemType ?: OEMType.UNKNOWN,
                        deviceModel = uiState.deviceProfile?.deviceModel ?: "Your Device",
                        onContinue = onSetupComplete,
                        onDismiss = viewModel::dismissCertificationAward
                    )
                }
                uiState.error != null -> {
                    OEMSetupError(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadOEMSetup() }
                    )
                }
                else -> {
                    OEMSetupContent(
                        deviceProfile = uiState.deviceProfile,
                        oemConfiguration = uiState.oemConfiguration,
                        currentStepIndex = uiState.currentStepIndex,
                        completedSteps = uiState.completedStepIds,
                        onStepComplete = viewModel::completeStep,
                        onStepSkip = viewModel::skipStep,
                        onNextStep = viewModel::nextStep,
                        onPreviousStep = viewModel::previousStep,
                        onGetDeepLink = viewModel::getDeepLinkIntent,
                        onSetupComplete = onSetupComplete
                    )
                }
            }
        }
    }
}

@Composable
private fun OEMSetupLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = WakeUpColors.iosBlue)
            Text(
                text = "Detecting your device...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OEMSetupError(error: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = WakeUpColors.iosRed
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun OEMSetupContent(
    deviceProfile: com.wakeup.app.core.oem.DeviceProfile?,
    oemConfiguration: OEMConfiguration?,
    currentStepIndex: Int,
    completedSteps: List<String>,
    onStepComplete: (SetupStep) -> Unit,
    onStepSkip: (SetupStep) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onGetDeepLink: (SetupStep) -> android.content.Intent?,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    if (oemConfiguration == null || deviceProfile == null) return

    val steps = oemConfiguration.setupSteps
    val currentStep = steps.getOrNull(currentStepIndex)
    val progress = if (steps.isNotEmpty()) (currentStepIndex + 1).toFloat() / steps.size else 1f

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with OEM info
        OEMHeader(deviceProfile, oemConfiguration)

        // Warning banner for aggressive OEMs
        if (oemConfiguration.aggressionLevel in listOf(AggressionLevel.HIGH, AggressionLevel.EXTREME)) {
            WarningBanner(message = oemConfiguration.warningMessage)
        }

        // Progress indicator
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = WakeUpColors.iosBlue,
            trackColor = Color.White.copy(alpha = 0.1f)
        )

        // Step counter
        Text(
            text = "Step ${currentStepIndex + 1} of ${steps.size}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Step content
        AnimatedContent(
            targetState = currentStepIndex,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } + fadeIn() with
                    slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() with
                    slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "step_animation"
        ) { index ->
            val step = steps.getOrNull(index)
            if (step != null) {
                SetupStepCard(
                    step = step,
                    isCompleted = completedSteps.contains(step.id),
                    onComplete = { onStepComplete(step) },
                    onSkip = { onStepSkip(step) },
                    onOpenSettings = {
                        val intent = onGetDeepLink(step)
                        intent?.let {
                            try {
                                context.startActivity(it)
                            } catch (e: ActivityNotFoundException) {
                                // Handle error
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = if (currentStepIndex > 0) onPreviousStep else onSetupComplete,
                enabled = currentStepIndex > 0
            ) {
                Text(if (currentStepIndex > 0) "Previous" else "Skip Setup")
            }

            Button(
                onClick = if (currentStepIndex < steps.size - 1) onNextStep else onSetupComplete,
                colors = ButtonDefaults.buttonColors(containerColor = WakeUpColors.iosBlue)
            ) {
                if (currentStepIndex < steps.size - 1) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Next")
                } else {
                    Text("Complete Setup")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun OEMHeader(
    deviceProfile: com.wakeup.app.core.oem.DeviceProfile,
    config: OEMConfiguration
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = when (deviceProfile.oemType.aggressionLevel) {
                        AggressionLevel.EXTREME -> listOf(
                            WakeUpColors.iosRed.copy(alpha = 0.2f),
                            WakeUpColors.iosOrange.copy(alpha = 0.1f)
                        )
                        AggressionLevel.HIGH -> listOf(
                            WakeUpColors.iosOrange.copy(alpha = 0.2f),
                            WakeUpColors.iosYellow.copy(alpha = 0.1f)
                        )
                        else -> listOf(
                            WakeUpColors.iosBlue.copy(alpha = 0.2f),
                            WakeUpColors.iosPurple.copy(alpha = 0.1f)
                        )
                    }
                )
            )
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // OEM Icon placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = deviceProfile.oemType.name.take(2),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = config.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = deviceProfile.deviceModel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (config.dontkillmyappUrl.isNotEmpty()) {
                TextButton(
                    onClick = {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(config.dontkillmyappUrl)
                        )
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            // Handle error
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View dontkillmyapp.com guide")
                }
            }
        }
    }
}

@Composable
private fun WarningBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WakeUpColors.iosOrange.copy(alpha = 0.15f))
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = WakeUpColors.iosOrange,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = WakeUpColors.iosOrange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SetupStepCard(
    step: SetupStep,
    isCompleted: Boolean,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        label = "completion_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = WakeUpColors.iosGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (step.deepLinkAction != null) {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = WakeUpColors.iosBlue)
                ) {
                    Text("Open Settings")
                }
            }

            if (!isCompleted) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Skip")
                    }

                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = WakeUpColors.iosGreen)
                    ) {
                        Text("Done")
                    }
                }
            }

            if (!step.isRequired) {
                Text(
                    text = "Optional step",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// Placeholder - would need to import actual component
@Composable
private fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        content()
    }
}
