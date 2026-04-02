package com.wakeup.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeup.app.domain.model.PremiumType
import com.wakeup.app.domain.repository.ThemeMode
import com.wakeup.app.domain.usecase.GetSettingsUseCase
import com.wakeup.app.domain.usecase.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase
) : ViewModel() {

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _premiumType = MutableStateFlow(PremiumType.NONE)
    val premiumType: StateFlow<PremiumType> = _premiumType.asStateFlow()

    private val _use24Hour = MutableStateFlow(false)
    val use24Hour: StateFlow<Boolean> = _use24Hour.asStateFlow()

    private val _defaultVibration = MutableStateFlow(true)
    val defaultVibration: StateFlow<Boolean> = _defaultVibration.asStateFlow()

    private val _gradualVolume = MutableStateFlow(true)
    val gradualVolume: StateFlow<Boolean> = _gradualVolume.asStateFlow()

    private val _snoozeDuration = MutableStateFlow(5)
    val snoozeDuration: StateFlow<Int> = _snoozeDuration.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _useIOSStyleTimePicker = MutableStateFlow(false)
    val useIOSStyleTimePicker: StateFlow<Boolean> = _useIOSStyleTimePicker.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _isPremium.value = getSettingsUseCase.isPremiumUser()
            _premiumType.value = getSettingsUseCase.getPremiumType()
            _use24Hour.value = getSettingsUseCase.getUse24HourFormat()
            _defaultVibration.value = getSettingsUseCase.getDefaultVibration()
            _gradualVolume.value = getSettingsUseCase.getGradualVolume()
            _snoozeDuration.value = getSettingsUseCase.getSnoozeDuration()
            _themeMode.value = getSettingsUseCase.getThemeMode()
            _useIOSStyleTimePicker.value = getSettingsUseCase.getUseIOSStyleTimePicker()
        }
    }

    fun setPremiumUser(isPremium: Boolean) {
        viewModelScope.launch {
            saveSettingsUseCase.setPremiumUser(isPremium)
            _isPremium.value = isPremium
        }
    }

    fun setUse24HourFormat(use24Hour: Boolean) {
        viewModelScope.launch {
            saveSettingsUseCase.setUse24HourFormat(use24Hour)
            _use24Hour.value = use24Hour
        }
    }

    fun setDefaultVibration(enabled: Boolean) {
        viewModelScope.launch {
            saveSettingsUseCase.setDefaultVibration(enabled)
            _defaultVibration.value = enabled
        }
    }

    fun setGradualVolume(enabled: Boolean) {
        viewModelScope.launch {
            saveSettingsUseCase.setGradualVolume(enabled)
            _gradualVolume.value = enabled
        }
    }

    fun setSnoozeDuration(minutes: Int) {
        viewModelScope.launch {
            saveSettingsUseCase.setSnoozeDuration(minutes)
            _snoozeDuration.value = minutes
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            saveSettingsUseCase.setThemeMode(mode)
            _themeMode.value = mode
        }
    }

    fun setUseIOSStyleTimePicker(enabled: Boolean) {
        viewModelScope.launch {
            saveSettingsUseCase.setUseIOSStyleTimePicker(enabled)
            _useIOSStyleTimePicker.value = enabled
        }
    }
}
