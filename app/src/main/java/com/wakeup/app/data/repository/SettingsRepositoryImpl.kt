package com.wakeup.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wakeup.app.domain.model.PremiumType
import com.wakeup.app.domain.model.MissionDifficulty
import com.wakeup.app.domain.model.MissionType
import com.wakeup.app.domain.repository.SettingsRepository
import com.wakeup.app.domain.repository.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

import javax.inject.Named

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @Named("settings") private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DEFAULT_MISSION_TYPE = stringPreferencesKey("default_mission_type")
        val DEFAULT_DIFFICULTY = stringPreferencesKey("default_difficulty")
        val IS_PREMIUM_USER = booleanPreferencesKey("is_premium_user")
        val PREMIUM_TYPE = stringPreferencesKey("premium_type")
        val USE_24_HOUR_FORMAT = booleanPreferencesKey("use_24_hour_format")
        val DEFAULT_VIBRATION = booleanPreferencesKey("default_vibration")
        val GRADUAL_VOLUME = booleanPreferencesKey("gradual_volume")
        val SNOOZE_DURATION = intPreferencesKey("snooze_duration")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEFAULT_ALARM_SOUND = stringPreferencesKey("default_alarm_sound")
        val LAST_SLEEP_SOUND = stringPreferencesKey("last_sleep_sound")
        val SLEEP_TIMER_MINUTES = intPreferencesKey("sleep_timer_minutes")
        val USE_IOS_STYLE_TIME_PICKER = booleanPreferencesKey("use_ios_style_time_picker")
    }

    override suspend fun isOnboardingCompleted(): Boolean {
        return dataStore.data.map { it[PreferencesKeys.ONBOARDING_COMPLETED] ?: false }.first()
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }

    override suspend fun getDefaultMissionType(): MissionType {
        val typeString = dataStore.data.map { it[PreferencesKeys.DEFAULT_MISSION_TYPE] }.first()
        return typeString?.let { MissionType.valueOf(it) } ?: MissionType.MATH
    }

    override suspend fun setDefaultMissionType(type: MissionType) {
        dataStore.edit { it[PreferencesKeys.DEFAULT_MISSION_TYPE] = type.name }
    }

    override suspend fun getDefaultDifficulty(): MissionDifficulty {
        val difficultyString = dataStore.data.map { it[PreferencesKeys.DEFAULT_DIFFICULTY] }.first()
        return difficultyString?.let { MissionDifficulty.valueOf(it) } ?: MissionDifficulty.EASY
    }

    override suspend fun setDefaultDifficulty(difficulty: MissionDifficulty) {
        dataStore.edit { it[PreferencesKeys.DEFAULT_DIFFICULTY] = difficulty.name }
    }

    override suspend fun isPremiumUser(): Boolean {
        return dataStore.data.map { it[PreferencesKeys.IS_PREMIUM_USER] ?: false }.first()
    }

    override suspend fun setPremiumUser(isPremium: Boolean) {
        dataStore.edit { it[PreferencesKeys.IS_PREMIUM_USER] = isPremium }
    }

    override suspend fun getUse24HourFormat(): Boolean {
        return dataStore.data.map { it[PreferencesKeys.USE_24_HOUR_FORMAT] ?: false }.first()
    }

    override suspend fun setUse24HourFormat(use24Hour: Boolean) {
        dataStore.edit { it[PreferencesKeys.USE_24_HOUR_FORMAT] = use24Hour }
    }

    override suspend fun getDefaultVibration(): Boolean {
        return dataStore.data.map { it[PreferencesKeys.DEFAULT_VIBRATION] ?: true }.first()
    }

    override suspend fun setDefaultVibration(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.DEFAULT_VIBRATION] = enabled }
    }

    override suspend fun getGradualVolume(): Boolean {
        return dataStore.data.map { it[PreferencesKeys.GRADUAL_VOLUME] ?: true }.first()
    }

    override suspend fun setGradualVolume(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.GRADUAL_VOLUME] = enabled }
    }

    override suspend fun getSnoozeDuration(): Int {
        return dataStore.data.map { it[PreferencesKeys.SNOOZE_DURATION] ?: 5 }.first()
    }

    override suspend fun setSnoozeDuration(minutes: Int) {
        dataStore.edit { it[PreferencesKeys.SNOOZE_DURATION] = minutes }
    }

    override suspend fun getThemeMode(): ThemeMode {
        val modeString = dataStore.data.map { it[PreferencesKeys.THEME_MODE] }.first()
        return modeString?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    override suspend fun getDefaultAlarmSound(): String? {
        return dataStore.data.map { it[PreferencesKeys.DEFAULT_ALARM_SOUND] }.first()
    }

    override suspend fun setDefaultAlarmSound(soundUri: String?) {
        dataStore.edit { 
            if (soundUri != null) {
                it[PreferencesKeys.DEFAULT_ALARM_SOUND] = soundUri 
            } else {
                it.remove(PreferencesKeys.DEFAULT_ALARM_SOUND)
            }
        }
    }

    override suspend fun getUseIOSStyleTimePicker(): Boolean {
        return dataStore.data.map { it[PreferencesKeys.USE_IOS_STYLE_TIME_PICKER] ?: false }.first()
    }

    override suspend fun setUseIOSStyleTimePicker(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.USE_IOS_STYLE_TIME_PICKER] = enabled }
    }

    override suspend fun getPremiumType(): PremiumType {
        val typeString = dataStore.data.map { it[PreferencesKeys.PREMIUM_TYPE] }.first()
        return typeString?.let { PremiumType.valueOf(it) } ?: PremiumType.NONE
    }

    override suspend fun setPremiumType(type: PremiumType) {
        dataStore.edit { it[PreferencesKeys.PREMIUM_TYPE] = type.name }
    }

    override fun getPremiumTypeFlow(): Flow<PremiumType> {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.PREMIUM_TYPE]?.let { PremiumType.valueOf(it) } ?: PremiumType.NONE
        }
    }

    fun getPremiumUserFlow(): Flow<Boolean> {
        return dataStore.data.map { it[PreferencesKeys.IS_PREMIUM_USER] ?: false }
    }

    // Sleep Sound preferences
    override suspend fun getLastSleepSound(): String? {
        return dataStore.data.map { it[PreferencesKeys.LAST_SLEEP_SOUND] }.first()
    }

    override suspend fun setLastSleepSound(soundId: String?) {
        dataStore.edit {
            if (soundId != null) {
                it[PreferencesKeys.LAST_SLEEP_SOUND] = soundId
            } else {
                it.remove(PreferencesKeys.LAST_SLEEP_SOUND)
            }
        }
    }

    override suspend fun getSleepTimerMinutes(): Int {
        return dataStore.data.map { it[PreferencesKeys.SLEEP_TIMER_MINUTES] ?: 0 }.first()
    }

    override suspend fun setSleepTimerMinutes(minutes: Int) {
        dataStore.edit { it[PreferencesKeys.SLEEP_TIMER_MINUTES] = minutes }
    }
}
