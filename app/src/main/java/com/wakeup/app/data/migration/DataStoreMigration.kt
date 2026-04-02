package com.wakeup.app.data.migration

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles migration of DataStore data from legacy shared store to separate stores.
 * This separates settings and stats data for better performance and maintainability.
 */
@Singleton
class DataStoreMigration @Inject constructor(
    private val context: Context,
    private val settingsDataStore: DataStore<Preferences>,
    private val statsDataStore: DataStore<Preferences>
) {

    companion object {
        private val MIGRATION_VERSION_KEY = intPreferencesKey("datastore_migration_version")
        private const val CURRENT_MIGRATION_VERSION = 1

        // Legacy keys that need to be migrated
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val DEFAULT_MISSION_TYPE_KEY = stringPreferencesKey("default_mission_type")
        private val DEFAULT_DIFFICULTY_KEY = stringPreferencesKey("default_difficulty")
        private val PREMIUM_USER_KEY = booleanPreferencesKey("premium_user")
        private val PREMIUM_TYPE_KEY = stringPreferencesKey("premium_type")
        private val USE_24_HOUR_FORMAT_KEY = booleanPreferencesKey("use_24_hour_format")
        private val DEFAULT_VIBRATION_KEY = booleanPreferencesKey("default_vibration")
        private val GRADUAL_VOLUME_KEY = booleanPreferencesKey("gradual_volume")
        private val SNOOZE_DURATION_KEY = intPreferencesKey("snooze_duration")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val DEFAULT_ALARM_SOUND_KEY = stringPreferencesKey("default_alarm_sound")
        private val USE_IOS_STYLE_TIME_PICKER_KEY = booleanPreferencesKey("use_ios_style_time_picker")
        private val LAST_SLEEP_SOUND_KEY = stringPreferencesKey("last_sleep_sound")
        private val SLEEP_TIMER_MINUTES_KEY = intPreferencesKey("sleep_timer_minutes")

        // Stats keys
        private val CURRENT_STREAK_KEY = intPreferencesKey("current_streak")
        private val BEST_STREAK_KEY = intPreferencesKey("best_streak")
        private val TOTAL_MISSIONS_COMPLETED_KEY = intPreferencesKey("total_missions_completed")
        private val LAST_WAKE_TIME_KEY = longPreferencesKey("last_wake_time")
    }

    /**
     * Performs migration if needed. Should be called on app startup.
     */
    suspend fun migrateIfNeeded() {
        val currentVersion = settingsDataStore.data.first()[MIGRATION_VERSION_KEY] ?: 0

        if (currentVersion < CURRENT_MIGRATION_VERSION) {
            performMigration(currentVersion)
            settingsDataStore.edit { prefs ->
                prefs[MIGRATION_VERSION_KEY] = CURRENT_MIGRATION_VERSION
            }
        }
    }

    private suspend fun performMigration(fromVersion: Int) {
        when (fromVersion) {
            0 -> migrateFromVersion0()
        }
    }

    private suspend fun migrateFromVersion0() {
        // In version 0, all data was in a single shared preferences file
        // We need to ensure data is properly separated into settings and stats stores

        // Migration logic: copy any legacy shared preferences data to the appropriate DataStore
        // This is a placeholder - actual implementation would read from legacy SharedPreferences
        // and migrate to the new DataStore structure

        // Settings are already in the settingsDataStore
        // Stats need to be migrated to statsDataStore

        // Check if we have legacy stats data in settings store that needs to move
        val settingsData = settingsDataStore.data.first()

        // Migrate stats data to statsDataStore if present
        statsDataStore.edit { statsPrefs ->
            settingsData[CURRENT_STREAK_KEY]?.let { statsPrefs[CURRENT_STREAK_KEY] = it }
            settingsData[BEST_STREAK_KEY]?.let { statsPrefs[BEST_STREAK_KEY] = it }
            settingsData[TOTAL_MISSIONS_COMPLETED_KEY]?.let { statsPrefs[TOTAL_MISSIONS_COMPLETED_KEY] = it }
            settingsData[LAST_WAKE_TIME_KEY]?.let { statsPrefs[LAST_WAKE_TIME_KEY] = it }
        }
    }
}
