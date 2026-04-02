package com.wakeup.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Unit tests for StatsRepositoryImpl.updateStreak() logic.
 * Tests streak calculation, continuation, reset, and edge cases.
 */
@ExperimentalCoroutinesApi
class StatsRepositoryStreakTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var statsRepository: StatsRepositoryImpl
    private lateinit var preferences: MutableMap<Preferences.Key<*>, Any?>

    companion object {
        private val CURRENT_STREAK = intPreferencesKey("current_streak")
        private val BEST_STREAK = intPreferencesKey("best_streak")
        private val TOTAL_WAKE_UPS = intPreferencesKey("total_wake_ups")
        private val FAILED_WAKE_UPS = intPreferencesKey("failed_wake_ups")
        private val LAST_WAKE_DATE = longPreferencesKey("last_wake_date")
        private val MISSIONS_COMPLETED = intPreferencesKey("missions_completed")
    }

    @Before
    fun setup() {
        preferences = mutableMapOf()
        dataStore = createMockDataStore()
        statsRepository = StatsRepositoryImpl(dataStore)
    }

    // ==================== SUCCESSFUL WAKE STREAK TESTS ====================

    @Test
    fun `updateStreak with success increments current streak when yesterday was last wake`() = runTest {
        // Given: Yesterday was the last wake day with streak of 5
        val yesterday = getTodayEpochMilli() - 24 * 60 * 60 * 1000
        preferences[CURRENT_STREAK] = 5
        preferences[BEST_STREAK] = 10
        preferences[TOTAL_WAKE_UPS] = 20
        preferences[LAST_WAKE_DATE] = yesterday

        // When: Successful wake today
        statsRepository.updateStreak(success = true)

        // Then: Streak continues
        assertEquals(6, preferences[CURRENT_STREAK])
        assertEquals(21, preferences[TOTAL_WAKE_UPS])
        assertEquals(getTodayEpochMilli(), preferences[LAST_WAKE_DATE])
    }

    @Test
    fun `updateStreak with success starts new streak when gap exists`() = runTest {
        // Given: Last wake was 3 days ago
        val threeDaysAgo = getTodayEpochMilli() - 3 * 24 * 60 * 60 * 1000
        preferences[CURRENT_STREAK] = 5
        preferences[BEST_STREAK] = 10
        preferences[TOTAL_WAKE_UPS] = 20
        preferences[LAST_WAKE_DATE] = threeDaysAgo

        // When: Successful wake today
        statsRepository.updateStreak(success = true)

        // Then: New streak starts at 1
        assertEquals(1, preferences[CURRENT_STREAK])
        assertEquals(21, preferences[TOTAL_WAKE_UPS])
    }

    @Test
    fun `updateStreak with success updates best streak when current exceeds best`() = runTest {
        // Given: Current streak (9) is about to exceed best streak (10)
        val yesterday = getTodayEpochMilli() - 24 * 60 * 60 * 1000
        preferences[CURRENT_STREAK] = 9
        preferences[BEST_STREAK] = 10
        preferences[LAST_WAKE_DATE] = yesterday

        // When: Successful wake today (streak becomes 10, matches best)
        statsRepository.updateStreak(success = true)

        // Then: Best streak updated since 10 >= 10
        assertEquals(10, preferences[CURRENT_STREAK])
        assertEquals(10, preferences[BEST_STREAK])
    }

    @Test
    fun `updateStreak with success sets best streak when new record`() = runTest {
        // Given: Current streak (10) will exceed best streak (10) - actually same, test for 11
        val yesterday = getTodayEpochMilli() - 24 * 60 * 60 * 1000
        preferences[CURRENT_STREAK] = 10
        preferences[BEST_STREAK] = 10
        preferences[LAST_WAKE_DATE] = yesterday

        // When: Successful wake today
        statsRepository.updateStreak(success = true)

        // Then: Best streak should be 11
        assertEquals(11, preferences[CURRENT_STREAK])
        assertEquals(11, preferences[BEST_STREAK])
    }

    @Test
    fun `updateStreak with success from no previous wake starts streak at 1`() = runTest {
        // Given: No previous wake data
        preferences[CURRENT_STREAK] = 0
        preferences[BEST_STREAK] = 0
        preferences[TOTAL_WAKE_UPS] = 0
        preferences[LAST_WAKE_DATE] = null

        // When: First successful wake
        statsRepository.updateStreak(success = true)

        // Then: Streak starts at 1
        assertEquals(1, preferences[CURRENT_STREAK])
        assertEquals(1, preferences[BEST_STREAK])
        assertEquals(1, preferences[TOTAL_WAKE_UPS])
    }

    // ==================== FAILED WAKE STREAK TESTS ====================

    @Test
    fun `updateStreak with failure resets current streak to 0`() = runTest {
        // Given: Current streak is 7
        preferences[CURRENT_STREAK] = 7
        preferences[FAILED_WAKE_UPS] = 2

        // When: Failed wake
        statsRepository.updateStreak(success = false)

        // Then: Streak reset, failure counted
        assertEquals(0, preferences[CURRENT_STREAK])
        assertEquals(3, preferences[FAILED_WAKE_UPS])
    }

    @Test
    fun `updateStreak with failure preserves best streak`() = runTest {
        // Given: Current streak is 5, best is 10
        preferences[CURRENT_STREAK] = 5
        preferences[BEST_STREAK] = 10

        // When: Failed wake
        statsRepository.updateStreak(success = false)

        // Then: Best streak preserved
        assertEquals(0, preferences[CURRENT_STREAK])
        assertEquals(10, preferences[BEST_STREAK])
    }

    @Test
    fun `updateStreak with failure does not update last wake date`() = runTest {
        // Given: Last wake was yesterday
        val yesterday = getTodayEpochMilli() - 24 * 60 * 60 * 1000
        preferences[LAST_WAKE_DATE] = yesterday

        // When: Failed wake
        statsRepository.updateStreak(success = false)

        // Then: Last wake date unchanged
        assertEquals(yesterday, preferences[LAST_WAKE_DATE])
    }

    // ==================== STREAK CONTINUATION LOGIC TESTS ====================

    @Test
    fun `streak continues when last wake was yesterday`() = runTest {
        val yesterday = getTodayEpochMilli() - 24 * 60 * 60 * 1000
        preferences[CURRENT_STREAK] = 5
        preferences[LAST_WAKE_DATE] = yesterday

        statsRepository.updateStreak(success = true)

        assertEquals(6, preferences[CURRENT_STREAK])
    }

    @Test
    fun `streak continues when last wake was today earlier`() = runTest {
        // Edge case: Multiple successful wakes in same day (shouldn't happen but test)
        val today = getTodayEpochMilli()
        preferences[CURRENT_STREAK] = 5
        preferences[LAST_WAKE_DATE] = today

        statsRepository.updateStreak(success = true)

        // Should still increment as lastWakeDate >= yesterday
        assertEquals(6, preferences[CURRENT_STREAK])
    }

    @Test
    fun `streak breaks when last wake was day before yesterday`() = runTest {
        val dayBeforeYesterday = getTodayEpochMilli() - 2 * 24 * 60 * 60 * 1000
        preferences[CURRENT_STREAK] = 5
        preferences[LAST_WAKE_DATE] = dayBeforeYesterday

        statsRepository.updateStreak(success = true)

        // Streak breaks because gap > 1 day
        assertEquals(1, preferences[CURRENT_STREAK])
    }

    // ==================== DATE CALCULATION TESTS ====================

    @Test
    fun `today calculation is at start of day`() = runTest {
        // Verify today is calculated as start of day (midnight)
        val expectedToday = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val actualToday = getTodayEpochMilli()

        // Should be within same day (allowing for test execution time)
        assertEquals(expectedToday / 86400000, actualToday / 86400000)
    }

    @Test
    fun `yesterday calculation is exactly 24 hours before today`() = runTest {
        val today = getTodayEpochMilli()
        val yesterday = today - 24 * 60 * 60 * 1000

        // Verify the calculation used in updateStreak
        assertEquals(today - 86400000, yesterday)
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    fun `updateStreak handles null lastWakeDate gracefully`() = runTest {
        preferences[CURRENT_STREAK] = 0
        preferences[LAST_WAKE_DATE] = null

        statsRepository.updateStreak(success = true)

        assertEquals(1, preferences[CURRENT_STREAK])
        assertNotNull(preferences[LAST_WAKE_DATE])
    }

    @Test
    fun `updateStreak handles very long streak correctly`() = runTest {
        val yesterday = getTodayEpochMilli() - 24 * 60 * 60 * 1000
        preferences[CURRENT_STREAK] = 999
        preferences[BEST_STREAK] = 999
        preferences[LAST_WAKE_DATE] = yesterday

        statsRepository.updateStreak(success = true)

        assertEquals(1000, preferences[CURRENT_STREAK])
        assertEquals(1000, preferences[BEST_STREAK])
    }

    @Test
    fun `incrementMissionCount increments counter correctly`() = runTest {
        preferences[MISSIONS_COMPLETED] = 15

        statsRepository.incrementMissionCount()

        assertEquals(16, preferences[MISSIONS_COMPLETED])
    }

    @Test
    fun `incrementMissionCount starts from zero when no previous value`() = runTest {
        preferences[MISSIONS_COMPLETED] = null

        statsRepository.incrementMissionCount()

        assertEquals(1, preferences[MISSIONS_COMPLETED])
    }

    // ==================== GET USER STATS TESTS ====================

    @Test
    fun `getUserStats calculates success rate correctly`() = runTest {
        preferences[TOTAL_WAKE_UPS] = 80
        preferences[FAILED_WAKE_UPS] = 20

        val stats = statsRepository.getUserStats()

        assertEquals(80.0f, stats.successRate, 0.01f)
    }

    @Test
    fun `getUserStats returns zero success rate when no attempts`() = runTest {
        preferences[TOTAL_WAKE_UPS] = 0
        preferences[FAILED_WAKE_UPS] = 0

        val stats = statsRepository.getUserStats()

        assertEquals(0f, stats.successRate)
    }

    @Test
    fun `getUserStats calculates success rate with only failures`() = runTest {
        preferences[TOTAL_WAKE_UPS] = 0
        preferences[FAILED_WAKE_UPS] = 10

        val stats = statsRepository.getUserStats()

        assertEquals(0f, stats.successRate)
    }

    @Test
    fun `getUserStats returns correct streak info`() = runTest {
        preferences[CURRENT_STREAK] = 7
        preferences[BEST_STREAK] = 14
        preferences[TOTAL_WAKE_UPS] = 50
        preferences[FAILED_WAKE_UPS] = 5
        preferences[LAST_WAKE_DATE] = getTodayEpochMilli()

        val stats = statsRepository.getUserStats()

        assertEquals(7, stats.streakInfo.currentStreak)
        assertEquals(14, stats.streakInfo.bestStreak)
        assertEquals(50, stats.streakInfo.totalWakeUps)
        assertEquals(5, stats.streakInfo.failedWakeUps)
    }

    // ==================== FLOW TESTS ====================

    @Test
    fun `getStreakFlow emits current streak value`() = runTest {
        preferences[CURRENT_STREAK] = 5

        val streak = statsRepository.getStreakFlow().first()

        assertEquals(5, streak)
    }

    @Test
    fun `getStreakFlow emits zero when no streak data`() = runTest {
        preferences[CURRENT_STREAK] = null

        val streak = statsRepository.getStreakFlow().first()

        assertEquals(0, streak)
    }

    // ==================== HELPER METHODS ====================

    private fun getTodayEpochMilli(): Long {
        return LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun createMockDataStore(): DataStore<Preferences> {
        return mockk<DataStore<Preferences>>(relaxed = true).apply {
            coEvery { data } returns flowOf(createMockPreferences())
            coEvery { edit(captureCoroutine<suspend Preferences.MutablePreferences.() -> Unit>()) } coAnswers {
                val block = firstArg<suspend Preferences.MutablePreferences.() -> Unit>()
                val mutablePrefs = MockMutablePreferences(preferences)
                block(mutablePrefs)
                preferences.putAll(mutablePrefs.getChanges())
                mockk<Preferences>(relaxed = true)
            }
        }
    }

    private fun createMockPreferences(): Preferences {
        return mockk<Preferences>(relaxed = true).apply {
            every { this@apply.get<Int>(any()) } answers {
                val key = firstArg<Preferences.Key<Int>>()
                preferences[key] as? Int
            }
            every { this@apply.get<Long>(any()) } answers {
                val key = firstArg<Preferences.Key<Long>>()
                preferences[key] as? Long
            }
        }
    }

    private class MockMutablePreferences(
        private val basePreferences: Map<Preferences.Key<*>, Any?>
    ) : Preferences.MutablePreferences {
        private val changes = mutableMapOf<Preferences.Key<*>, Any?>()

        override fun <T> set(key: Preferences.Key<T>, value: T) {
            changes[key] = value
        }

        override fun <T> remove(key: Preferences.Key<T>): T? {
            @Suppress("UNCHECKED_CAST")
            val oldValue = changes[key] as? T ?: basePreferences[key] as? T
            changes[key] = null
            return oldValue
        }

        override fun clear() {
            changes.clear()
            basePreferences.keys.forEach { changes[it] = null }
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T> get(key: Preferences.Key<T>): T? {
            return changes[key] as? T ?: basePreferences[key] as? T
        }

        fun getChanges(): Map<Preferences.Key<*>, Any?> = changes.toMap()

        override val asMap: Map<Preferences.Key<*>, Any>
            get() = (basePreferences + changes).filterValues { it != null } as Map<Preferences.Key<*>, Any>

        override fun <T> contains(key: Preferences.Key<T>): Boolean {
            return changes.containsKey(key) || basePreferences.containsKey(key)
        }
    }
}
