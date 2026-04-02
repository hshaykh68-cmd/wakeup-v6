package com.wakeup.app.presentation.oem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeup.app.core.oem.DeviceProfile
import com.wakeup.app.core.oem.OEMConfiguration
import com.wakeup.app.core.oem.OEMConfigurationRegistry
import com.wakeup.app.core.oem.OEMDetector
import com.wakeup.app.core.oem.OEMType
import com.wakeup.app.core.oem.SetupStep
import com.wakeup.app.core.oem.WorkaroundType
import com.wakeup.app.core.oem.createDeepLinkIntent
import com.wakeup.app.domain.model.OEMCertification
import com.wakeup.app.domain.model.VerificationResult
import com.wakeup.app.domain.repository.OEMSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class OEMSetupUiState(
    val isLoading: Boolean = true,
    val deviceProfile: DeviceProfile? = null,
    val oemConfiguration: OEMConfiguration? = null,
    val oemCertification: OEMCertification? = null,
    val currentStepIndex: Int = 0,
    val completedStepIds: List<String> = emptyList(),
    val canProceed: Boolean = false,
    val showCertificationAward: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OEMSetupViewModel @Inject constructor(
    private val oemDetector: OEMDetector,
    private val oemConfigRegistry: OEMConfigurationRegistry,
    private val oemSettingsRepository: OEMSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OEMSetupUiState())
    val uiState: StateFlow<OEMSetupUiState> = _uiState.asStateFlow()

    init {
        loadOEMSetup()
    }

    internal fun loadOEMSetup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Detect device profile
                val profile = oemDetector.getDeviceProfile()
                
                // Get configuration for this OEM
                val config = oemConfigRegistry.getConfiguration(profile.oemType)
                
                // Get existing certification if any
                val certification = oemSettingsRepository.getOEMCertification()
                
                // Store the OEM type
                oemSettingsRepository.storeOEMType(profile.oemType)
                
                // Mark that user has seen setup
                oemSettingsRepository.markSetupSeen()
                
                _uiState.value = OEMSetupUiState(
                    isLoading = false,
                    deviceProfile = profile,
                    oemConfiguration = config,
                    oemCertification = certification,
                    currentStepIndex = 0,
                    completedStepIds = certification?.completedSteps ?: emptyList(),
                    canProceed = config.setupSteps.isEmpty() || certification?.completedSteps?.isNotEmpty() == true,
                    showCertificationAward = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load OEM setup: ${e.message}"
                )
            }
        }
    }

    /**
     * Mark a setup step as completed.
     */
    fun completeStep(step: SetupStep, verificationMethod: String = "USER_CONFIRMED", confidence: Int = 100) {
        viewModelScope.launch {
            // Add to completed steps
            val currentCompleted = _uiState.value.completedStepIds.toMutableList()
            if (!currentCompleted.contains(step.id)) {
                currentCompleted.add(step.id)
            }
            
            // Save to repository
            oemSettingsRepository.markStepCompleted(step.id, verificationMethod, confidence)
            
            // Add workaround if applicable
            if (step.workaroundType != null) {
                oemSettingsRepository.addWorkaround(step.workaroundType)
            }
            
            // Update UI state
            val newIndex = (_uiState.value.currentStepIndex + 1).coerceAtMost(
                (_uiState.value.oemConfiguration?.setupSteps?.size ?: 1) - 1
            )
            
            _uiState.value = _uiState.value.copy(
                completedStepIds = currentCompleted,
                currentStepIndex = newIndex,
                canProceed = true
            )
            
            // Check if all steps completed
            checkCertificationAward()
        }
    }

    /**
     * Skip a setup step (mark as completed with lower confidence).
     */
    fun skipStep(step: SetupStep) {
        completeStep(step, "USER_SKIPPED", confidence = 50)
    }

    /**
     * Go to next step.
     */
    fun nextStep() {
        val config = _uiState.value.oemConfiguration ?: return
        val newIndex = (_uiState.value.currentStepIndex + 1).coerceAtMost(config.setupSteps.size - 1)
        _uiState.value = _uiState.value.copy(currentStepIndex = newIndex)
    }

    /**
     * Go to previous step.
     */
    fun previousStep() {
        val newIndex = (_uiState.value.currentStepIndex - 1).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(currentStepIndex = newIndex)
    }

    /**
     * Award certification if all required steps are completed.
     */
    private fun checkCertificationAward() {
        viewModelScope.launch {
            val config = _uiState.value.oemConfiguration ?: return@launch
            val completedSteps = _uiState.value.completedStepIds
            
            // Check if all required steps are completed
            val requiredSteps = config.setupSteps.filter { it.isRequired }.map { it.id }
            val allRequiredCompleted = requiredSteps.all { completedSteps.contains(it) }
            
            if (allRequiredCompleted && !_uiState.value.oemCertification?.isCertified!!) {
                // Award certification
                oemSettingsRepository.awardCertification()
                oemSettingsRepository.markOEMSetupCompleted()
                
                _uiState.value = _uiState.value.copy(
                    showCertificationAward = true,
                    oemCertification = _uiState.value.oemCertification?.copy(
                        isCertified = true,
                        certificationDate = Instant.now(),
                        verificationScore = calculateVerificationScore(config, completedSteps)
                    )
                )
            }
        }
    }

    /**
     * Calculate verification score based on completed steps.
     */
    private fun calculateVerificationScore(config: OEMConfiguration, completedSteps: List<String>): Int {
        val totalSteps = config.setupSteps.size
        val completedCount = completedSteps.intersect(config.setupSteps.map { it.id }.toSet()).size
        return (completedCount * 100) / totalSteps
    }

    /**
     * Dismiss the certification award screen.
     */
    fun dismissCertificationAward() {
        _uiState.value = _uiState.value.copy(showCertificationAward = false)
    }

    /**
     * Create deep link intent for a setup step.
     */
    fun getDeepLinkIntent(step: SetupStep): android.content.Intent? {
        return step.createDeepLinkIntent()
    }

    /**
     * Check if certification should be shown (for settings screen).
     */
    fun shouldShowCertification(): Boolean {
        return _uiState.value.oemCertification?.isCertified == true
    }

    /**
     * Get aggressive OEM warning message.
     */
    fun getWarningMessage(): String? {
        return _uiState.value.oemConfiguration?.warningMessage
    }

    /**
     * Check if current OEM is aggressive.
     */
    fun isAggressiveOEM(): Boolean {
        return _uiState.value.deviceProfile?.isAggressive == true
    }

    /**
     * Dismiss the OEM warning banner.
     */
    fun dismissWarning() {
        viewModelScope.launch {
            oemSettingsRepository.dismissWarning()
            _uiState.value = _uiState.value.copy(
                oemCertification = _uiState.value.oemCertification?.copy(dismissedWarning = true)
            )
        }
    }
}
