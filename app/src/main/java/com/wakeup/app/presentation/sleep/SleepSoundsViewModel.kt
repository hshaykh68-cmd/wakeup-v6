package com.wakeup.app.presentation.sleep

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeup.app.core.util.SleepSoundManager
import com.wakeup.app.data.service.SleepSoundService
import com.wakeup.app.domain.model.SleepSound
import com.wakeup.app.domain.model.SleepSounds
import com.wakeup.app.domain.model.SleepTimerOption
import com.wakeup.app.domain.repository.SettingsRepository
import com.wakeup.app.domain.repository.SleepSoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.wakeup.app.core.util.BatteryOptimizationHelper

/**
 * UI State for Sleep Sounds screen
 */
data class SleepSoundsUiState(
    val isPlaying: Boolean = false,
    val selectedSound: SleepSound? = null,
    val timerOption: SleepTimerOption = SleepTimerOption.OFF,
    val remainingTimeMillis: Long? = null,
    val volume: Float = 1.0f,
    val availableSounds: List<SleepSound> = SleepSounds.ALL,
    val isIgnoringBatteryOptimizations: Boolean = true,
    val isPremium: Boolean = false,
    val showPremiumDialog: Boolean = false
)

/**
 * ViewModel for Sleep Sounds screen
 */
@HiltViewModel
class SleepSoundsViewModel @Inject constructor(
    private val sleepSoundRepository: SleepSoundRepository,
    private val sleepSoundManager: SleepSoundManager,
    private val settingsRepository: SettingsRepository,
    private val batteryOptimizationHelper: BatteryOptimizationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SleepSoundsUiState())
    val uiState: StateFlow<SleepSoundsUiState> = _uiState.asStateFlow()

    init {
        // Check battery optimization status
        _uiState.value = _uiState.value.copy(
            isIgnoringBatteryOptimizations = batteryOptimizationHelper.isIgnoringBatteryOptimizations()
        )
        viewModelScope.launch {
            // Collect playing state
            sleepSoundManager.isPlaying.collectLatest { isPlaying ->
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
            }
        }

        viewModelScope.launch {
            // Collect current sound
            sleepSoundManager.currentSound.collectLatest { sound ->
                _uiState.value = _uiState.value.copy(selectedSound = sound)
            }
        }

        viewModelScope.launch {
            // Collect remaining time
            sleepSoundManager.remainingTime.collectLatest { remainingTime ->
                _uiState.value = _uiState.value.copy(remainingTimeMillis = remainingTime)
            }
        }

        viewModelScope.launch {
            // Collect volume
            sleepSoundManager.volume.collectLatest { volume ->
                _uiState.value = _uiState.value.copy(volume = volume)
            }
        }

        // Load last played sound, timer preference, and premium status
        viewModelScope.launch {
            val lastSound = settingsRepository.getLastSleepSound()
            val timerMinutes = settingsRepository.getSleepTimerMinutes()
            val isPremium = settingsRepository.isPremiumUser()
            
            _uiState.value = _uiState.value.copy(
                selectedSound = lastSound?.let { SleepSounds.getById(it) } ?: SleepSounds.OCEAN_WAVES,
                timerOption = SleepTimerOption.fromMinutes(timerMinutes),
                isPremium = isPremium
            )
        }
    }

    /**
     * Check if user can play sleep sounds (premium feature)
     */
    fun canPlaySleepSounds(): Boolean {
        return _uiState.value.isPremium
    }

    /**
     * Show premium dialog
     */
    fun showPremiumDialog() {
        _uiState.value = _uiState.value.copy(showPremiumDialog = true)
    }

    /**
     * Hide premium dialog
     */
    fun hidePremiumDialog() {
        _uiState.value = _uiState.value.copy(showPremiumDialog = false)
    }

    /**
     * Play the selected sleep sound
     */
    fun playSound(sound: SleepSound? = null) {
        val soundToPlay = sound ?: _uiState.value.selectedSound ?: SleepSounds.OCEAN_WAVES
        
        viewModelScope.launch {
            // Save as last played sound
            settingsRepository.setLastSleepSound(soundToPlay.id)
            
            // Use repository for background playback
            sleepSoundRepository.play(soundToPlay.id, fadeIn = true)
        }
    }

    /**
     * Pause the currently playing sound
     */
    fun pauseSound() {
        viewModelScope.launch {
            sleepSoundRepository.pause()
        }
    }

    /**
     * Stop the currently playing sound with fade out
     */
    fun stopSound() {
        viewModelScope.launch {
            sleepSoundRepository.stop()
        }
    }

    /**
     * Toggle play/pause for the selected sound
     */
    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            pauseSound()
        } else {
            playSound()
        }
    }

    /**
     * Select a different sleep sound
     */
    fun selectSound(sound: SleepSound) {
        _uiState.value = _uiState.value.copy(selectedSound = sound)
        
        // If already playing, switch to new sound
        if (_uiState.value.isPlaying) {
            playSound(sound)
        }
        
        viewModelScope.launch {
            settingsRepository.setLastSleepSound(sound.id)
        }
    }

    /**
     * Set the sleep timer
     */
    fun setTimer(option: SleepTimerOption) {
        _uiState.value = _uiState.value.copy(timerOption = option)
        
        viewModelScope.launch {
            sleepSoundRepository.setTimer(option.minutes)
            settingsRepository.setSleepTimerMinutes(option.minutes)
        }
    }

    /**
     * Cancel the sleep timer
     */
    fun cancelTimer() {
        setTimer(SleepTimerOption.OFF)
    }

    /**
     * Format remaining time as MM:SS
     */
    fun formatRemainingTime(): String {
        val millis = _uiState.value.remainingTimeMillis ?: return ""
        if (millis <= 0) return "00:00"
        
        val minutes = (millis / 60000).toInt()
        val seconds = ((millis % 60000) / 1000).toInt()
        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }

    override fun onCleared() {
        super.onCleared()
        // Don't stop sound here - let the service continue in background
    }

    /**
     * Refresh battery optimization status
     */
    fun refreshBatteryOptimizationStatus() {
        _uiState.value = _uiState.value.copy(
            isIgnoringBatteryOptimizations = batteryOptimizationHelper.isIgnoringBatteryOptimizations()
        )
    }

    /**
     * Check if battery optimization dialog should be shown
     */
    fun shouldShowBatteryOptimizationDialog(): Boolean {
        return !batteryOptimizationHelper.isIgnoringBatteryOptimizations()
    }
}
