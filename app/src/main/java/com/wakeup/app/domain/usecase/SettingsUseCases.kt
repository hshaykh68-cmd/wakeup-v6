package com.wakeup.app.domain.usecase

import com.wakeup.app.domain.model.MissionDifficulty
import com.wakeup.app.domain.model.MissionType
import com.wakeup.app.domain.model.PremiumType
import com.wakeup.app.domain.repository.SettingsRepository
import com.wakeup.app.domain.repository.ThemeMode
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun isOnboardingCompleted(): Boolean {
        return settingsRepository.isOnboardingCompleted()
    }

    suspend fun getDefaultMissionType(): MissionType {
        return settingsRepository.getDefaultMissionType()
    }

    suspend fun getDefaultDifficulty(): MissionDifficulty {
        return settingsRepository.getDefaultDifficulty()
    }

    suspend fun isPremiumUser(): Boolean {
        return settingsRepository.isPremiumUser()
    }

    suspend fun getPremiumType(): PremiumType {
        return settingsRepository.getPremiumType()
    }

    suspend fun getUse24HourFormat(): Boolean {
        return settingsRepository.getUse24HourFormat()
    }

    // New settings
    suspend fun getDefaultVibration(): Boolean {
        return settingsRepository.getDefaultVibration()
    }

    suspend fun getGradualVolume(): Boolean {
        return settingsRepository.getGradualVolume()
    }

    suspend fun getSnoozeDuration(): Int {
        return settingsRepository.getSnoozeDuration()
    }

    suspend fun getThemeMode(): ThemeMode {
        return settingsRepository.getThemeMode()
    }

    suspend fun getDefaultAlarmSound(): String? {
        return settingsRepository.getDefaultAlarmSound()
    }

    suspend fun getUseIOSStyleTimePicker(): Boolean {
        return settingsRepository.getUseIOSStyleTimePicker()
    }
}

class SaveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun setOnboardingCompleted(completed: Boolean) {
        settingsRepository.setOnboardingCompleted(completed)
    }

    suspend fun setDefaultMissionType(type: MissionType) {
        settingsRepository.setDefaultMissionType(type)
    }

    suspend fun setDefaultDifficulty(difficulty: MissionDifficulty) {
        settingsRepository.setDefaultDifficulty(difficulty)
    }

    suspend fun setPremiumUser(isPremium: Boolean) {
        settingsRepository.setPremiumUser(isPremium)
    }

    suspend fun setUse24HourFormat(use24Hour: Boolean) {
        settingsRepository.setUse24HourFormat(use24Hour)
    }

    // New settings
    suspend fun setDefaultVibration(enabled: Boolean) {
        settingsRepository.setDefaultVibration(enabled)
    }

    suspend fun setGradualVolume(enabled: Boolean) {
        settingsRepository.setGradualVolume(enabled)
    }

    suspend fun setSnoozeDuration(minutes: Int) {
        settingsRepository.setSnoozeDuration(minutes)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        settingsRepository.setThemeMode(mode)
    }

    suspend fun setDefaultAlarmSound(soundUri: String?) {
        settingsRepository.setDefaultAlarmSound(soundUri)
    }

    suspend fun setUseIOSStyleTimePicker(enabled: Boolean) {
        settingsRepository.setUseIOSStyleTimePicker(enabled)
    }
}
