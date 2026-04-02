package com.wakeup.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.wakeup.app.domain.model.StreakInfo
import com.wakeup.app.domain.model.UserStats
import com.wakeup.app.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

import javax.inject.Named

@Singleton
class StatsRepositoryImpl @Inject constructor(
    @Named("stats") private val dataStore: DataStore<Preferences>
) : StatsRepository {

    private object PreferencesKeys {
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val BEST_STREAK = intPreferencesKey("best_streak")
        val TOTAL_WAKE_UPS = intPreferencesKey("total_wake_ups")
        val FAILED_WAKE_UPS = intPreferencesKey("failed_wake_ups")
        val LAST_WAKE_DATE = longPreferencesKey("last_wake_date")
        val MISSIONS_COMPLETED = intPreferencesKey("missions_completed")
        val CONSECUTIVE_FAILURES = intPreferencesKey("consecutive_failures")
    }

    override suspend fun getUserStats(): UserStats {
        val prefs = dataStore.data.first()
        val streakInfo = StreakInfo(
            currentStreak = prefs[PreferencesKeys.CURRENT_STREAK] ?: 0,
            bestStreak = prefs[PreferencesKeys.BEST_STREAK] ?: 0,
            totalWakeUps = prefs[PreferencesKeys.TOTAL_WAKE_UPS] ?: 0,
            failedWakeUps = prefs[PreferencesKeys.FAILED_WAKE_UPS] ?: 0,
            lastWakeDate = prefs[PreferencesKeys.LAST_WAKE_DATE]
        )
        
        val totalAttempts = streakInfo.totalWakeUps + streakInfo.failedWakeUps
        val successRate = if (totalAttempts > 0) {
            (streakInfo.totalWakeUps.toFloat() / totalAttempts) * 100
        } else 0f
        
        return UserStats(
            streakInfo = streakInfo,
            averageSnoozes = 0f, // Calculated from history
            successRate = successRate,
            totalMissionsCompleted = prefs[PreferencesKeys.MISSIONS_COMPLETED] ?: 0
        )
    }

    override suspend fun updateStreak(success: Boolean) {
        dataStore.edit { prefs ->
            val currentStreak = prefs[PreferencesKeys.CURRENT_STREAK] ?: 0
            val bestStreak = prefs[PreferencesKeys.BEST_STREAK] ?: 0
            val totalWakeUps = prefs[PreferencesKeys.TOTAL_WAKE_UPS] ?: 0
            val failedWakeUps = prefs[PreferencesKeys.FAILED_WAKE_UPS] ?: 0
            val lastWakeDate = prefs[PreferencesKeys.LAST_WAKE_DATE]

            val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val yesterday = today - 24 * 60 * 60 * 1000

            if (success) {
                // Check if this is continuing the streak
                val newStreak = if (lastWakeDate != null && lastWakeDate >= yesterday) {
                    currentStreak + 1
                } else {
                    1 // Start new streak
                }
                
                prefs[PreferencesKeys.CURRENT_STREAK] = newStreak
                if (newStreak > bestStreak) {
                    prefs[PreferencesKeys.BEST_STREAK] = newStreak
                }
                prefs[PreferencesKeys.TOTAL_WAKE_UPS] = totalWakeUps + 1
                prefs[PreferencesKeys.LAST_WAKE_DATE] = today
                // Reset consecutive failures on success
                prefs[PreferencesKeys.CONSECUTIVE_FAILURES] = 0
            } else {
                // Grace period: only reset streak after 2+ consecutive failures
                val consecutiveFailures = (prefs[PreferencesKeys.CONSECUTIVE_FAILURES] ?: 0) + 1
                prefs[PreferencesKeys.CONSECUTIVE_FAILURES] = consecutiveFailures
                prefs[PreferencesKeys.FAILED_WAKE_UPS] = failedWakeUps + 1
                
                // Reset streak only after grace period threshold (2 failures)
                if (consecutiveFailures >= 2) {
                    prefs[PreferencesKeys.CURRENT_STREAK] = 0
                }
                // Note: streak is preserved for the first failure, giving user another chance
            }
        }
    }

    override fun getUserStatsFlow(): Flow<UserStats> {
        return dataStore.data.map { prefs ->
            val streakInfo = StreakInfo(
                currentStreak = prefs[PreferencesKeys.CURRENT_STREAK] ?: 0,
                bestStreak = prefs[PreferencesKeys.BEST_STREAK] ?: 0,
                totalWakeUps = prefs[PreferencesKeys.TOTAL_WAKE_UPS] ?: 0,
                failedWakeUps = prefs[PreferencesKeys.FAILED_WAKE_UPS] ?: 0,
                lastWakeDate = prefs[PreferencesKeys.LAST_WAKE_DATE]
            )

            val totalAttempts = streakInfo.totalWakeUps + streakInfo.failedWakeUps
            val successRate = if (totalAttempts > 0) {
                (streakInfo.totalWakeUps.toFloat() / totalAttempts) * 100
            } else 0f

            UserStats(
                streakInfo = streakInfo,
                averageSnoozes = 0f,
                successRate = successRate,
                totalMissionsCompleted = prefs[PreferencesKeys.MISSIONS_COMPLETED] ?: 0
            )
        }
    }

    override suspend fun incrementMissionCount() {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.MISSIONS_COMPLETED] ?: 0
            prefs[PreferencesKeys.MISSIONS_COMPLETED] = current + 1
        }
    }

    override fun getStreakFlow(): Flow<Int> {
        return dataStore.data.map { it[PreferencesKeys.CURRENT_STREAK] ?: 0 }
    }
}
