package com.wakeup.app.presentation.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeup.app.core.util.BatteryOptimizationHelper
import com.wakeup.app.domain.model.MissionDifficulty
import com.wakeup.app.domain.model.MissionType
import com.wakeup.app.domain.usecase.CreateAlarmUseCase
import com.wakeup.app.domain.usecase.GetAllAlarmsUseCase
import com.wakeup.app.domain.usecase.GetSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class CreateAlarmViewModel @Inject constructor(
    private val createAlarmUseCase: CreateAlarmUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val getAllAlarmsUseCase: GetAllAlarmsUseCase,
    private val batteryOptimizationHelper: BatteryOptimizationHelper
) : ViewModel() {

    private val _defaultSettings = MutableStateFlow<DefaultAlarmSettings?>(null)
    val defaultSettings: StateFlow<DefaultAlarmSettings?> = _defaultSettings

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium

    private val _alarmCount = MutableStateFlow(0)
    val alarmCount: StateFlow<Int> = _alarmCount

    private val _showUpsell = MutableStateFlow(false)
    val showUpsell: StateFlow<Boolean> = _showUpsell

    private val _canCreateAlarm = MutableStateFlow(true)
    val canCreateAlarm: StateFlow<Boolean> = _canCreateAlarm

    private val _showBatteryOptimizationWarning = MutableStateFlow(false)
    val showBatteryOptimizationWarning: StateFlow<Boolean> = _showBatteryOptimizationWarning

    private val _useIOSStyleTimePicker = MutableStateFlow(false)
    val useIOSStyleTimePicker: StateFlow<Boolean> = _useIOSStyleTimePicker

    companion object {
        const val FREE_ALARM_LIMIT = 3
    }

    init {
        loadDefaultSettings()
        checkPremiumStatus()
        loadAlarmCount()
        checkBatteryOptimization()
    }

    private fun checkBatteryOptimization() {
        viewModelScope.launch {
            _showBatteryOptimizationWarning.value = !batteryOptimizationHelper.isIgnoringBatteryOptimizations()
        }
    }

    fun dismissBatteryOptimizationWarning() {
        _showBatteryOptimizationWarning.value = false
    }

    private fun loadDefaultSettings() {
        viewModelScope.launch {
            _defaultSettings.value = DefaultAlarmSettings(
                missionType = getSettingsUseCase.getDefaultMissionType(),
                missionDifficulty = getSettingsUseCase.getDefaultDifficulty(),
                useVibration = getSettingsUseCase.getDefaultVibration(),
                gradualVolume = getSettingsUseCase.getGradualVolume(),
                snoozeDuration = getSettingsUseCase.getSnoozeDuration(),
                soundUri = getSettingsUseCase.getDefaultAlarmSound()
            )
            _useIOSStyleTimePicker.value = getSettingsUseCase.getUseIOSStyleTimePicker()
        }
    }

    private fun checkPremiumStatus() {
        viewModelScope.launch {
            _isPremium.value = getSettingsUseCase.isPremiumUser()
        }
    }

    private fun loadAlarmCount() {
        viewModelScope.launch {
            getAllAlarmsUseCase().collect { alarms ->
                _alarmCount.value = alarms.size
                _canCreateAlarm.value = _isPremium.value || alarms.size < FREE_ALARM_LIMIT
            }
        }
    }

    fun checkCanCreateAlarm(): Boolean {
        return if (_canCreateAlarm.value) {
            true
        } else {
            _showUpsell.value = true
            false
        }
    }

    fun dismissUpsell() {
        _showUpsell.value = false
    }

    fun isMissionTypeAvailable(missionType: MissionType): Boolean {
        return when (missionType) {
            MissionType.PHOTO, MissionType.BARCODE, MissionType.COMBO_BARCODE_PHOTO -> _isPremium.value
            MissionType.STEP -> true // STEP is free, available to all users
            else -> true
        }
    }

    fun createAlarm(
        hour: Int,
        minute: Int,
        label: String,
        repeatDays: List<DayOfWeek>,
        missionType: MissionType,
        missionDifficulty: MissionDifficulty,
        strictMode: Boolean,
        useVibration: Boolean,
        gradualVolume: Boolean,
        snoozeEnabled: Boolean,
        soundUri: String? = null,
        smartWakeEnabled: Boolean = false,
        smartWakeWindowMinutes: Int = 30,
        barcodeValue: String? = null,
        barcodeFormat: Int? = null,
        photoReferencePath: String? = null,
        photoReferenceHash: String? = null
    ) {
        viewModelScope.launch {
            // Check alarm limit again before creating
            val currentAlarms = getAllAlarmsUseCase().first()
            if (!_isPremium.value && currentAlarms.size >= FREE_ALARM_LIMIT) {
                _showUpsell.value = true
                return@launch
            }

            // Check if mission type is premium
            if (!_isPremium.value && (missionType == MissionType.PHOTO || missionType == MissionType.BARCODE || missionType == MissionType.COMBO_BARCODE_PHOTO)) {
                return@launch
            }

            createAlarmUseCase(
                hour = hour,
                minute = minute,
                label = label,
                repeatDays = repeatDays,
                soundUri = soundUri,
                useVibration = useVibration,
                gradualVolume = gradualVolume,
                missionType = missionType,
                missionDifficulty = missionDifficulty,
                strictMode = strictMode,
                snoozeEnabled = snoozeEnabled,
                snoozeInterval = _defaultSettings.value?.snoozeDuration ?: 5,
                maxSnoozes = 3,
                barcodeValue = barcodeValue,
                barcodeFormat = barcodeFormat,
                photoReferencePath = photoReferencePath,
                photoReferenceHash = photoReferenceHash,
                smartWakeEnabled = smartWakeEnabled,
                smartWakeWindowMinutes = smartWakeWindowMinutes
            )
        }
    }
}

data class DefaultAlarmSettings(
    val missionType: MissionType,
    val missionDifficulty: MissionDifficulty,
    val useVibration: Boolean,
    val gradualVolume: Boolean,
    val snoozeDuration: Int,
    val soundUri: String?
)
