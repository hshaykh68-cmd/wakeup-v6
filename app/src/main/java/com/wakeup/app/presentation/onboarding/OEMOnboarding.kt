package com.wakeup.app.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakeup.app.core.oem.AggressionLevel
import com.wakeup.app.core.oem.OEMDetector
import com.wakeup.app.presentation.oem.OEMSetupScreen
import com.wakeup.app.presentation.oem.OEMSetupViewModel
import javax.inject.Inject

/**
 * Wrapper that determines if OEM setup should be shown during onboarding.
 * 
 * Flow:
 * 1. After regular onboarding completes, check if device is from aggressive OEM
 * 2. If yes, show OEM setup screen
 * 3. If no (stock Android/Pixel), skip directly to main app
 */
class OEMOnboardingCoordinator @Inject constructor(
    private val oemDetector: OEMDetector
) {
    /**
     * Check if OEM setup should be shown.
     */
    fun shouldShowOEMSetup(): Boolean {
        val oem = oemDetector.detectOEM()
        return oem.aggressionLevel != AggressionLevel.NONE
    }
    
    /**
     * Get the OEM type for setup.
     */
    fun getOEMType() = oemDetector.detectOEM()
}

/**
 * Composable that handles the OEM setup flow after onboarding.
 * 
 * @param onComplete Called when OEM setup is complete or not needed
 * @param onNavigateToOEMSetup Called to navigate to standalone OEM setup screen
 */
@Composable
fun PostOnboardingOEMFlow(
    viewModel: OEMSetupViewModel = hiltViewModel(),
    onComplete: () -> Unit,
    onNavigateToOEMSetup: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState) {
        // If already certified or not aggressive OEM, skip
        if (uiState.oemCertification?.isCertified == true) {
            onComplete()
            return@LaunchedEffect
        }
        
        // If device is not aggressive, skip OEM setup
        if (uiState.deviceProfile?.isAggressive == false) {
            onComplete()
            return@LaunchedEffect
        }
        
        // Otherwise, navigate to OEM setup
        if (uiState.deviceProfile != null && !uiState.oemCertification?.hasSeenSetup!!) {
            onNavigateToOEMSetup()
        }
    }
}

/**
 * Standalone OEM setup flow that can be triggered from onboarding completion.
 */
@Composable
fun OnboardingOEMSetupScreen(
    onSetupComplete: () -> Unit,
    onSkip: () -> Unit
) {
    OEMSetupScreen(
        onSetupComplete = onSetupComplete,
        onBackClick = onSkip
    )
}
