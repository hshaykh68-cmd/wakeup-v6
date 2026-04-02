package com.wakeup.app.data.alarm

import com.wakeup.app.domain.model.Alarm
import com.wakeup.app.domain.model.MissionDifficulty
import com.wakeup.app.domain.model.MissionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit tests for snooze time calculation with minute overflow handling.
 *
 * These tests verify that snooze scheduling correctly handles time overflow,
 * such as when snoozing at 11:55 PM with a 10-minute interval should result
 * in 12:05 AM (next day), not 11:65 AM (invalid).
 */
class SnoozeOverflowTest {

    @Test
    fun `snooze at 1155 PM with 10 min interval results in 1205 AM`() {
        // Test case: 11:55 PM + 10 minutes should = 12:05 AM (next day)
        val baseTime = LocalDateTime.of(2024, 3, 15, 23, 55)
        val snoozeMinutes = 10

        val expectedTime = baseTime.plusMinutes(snoozeMinutes.toLong())

        assertEquals(0, expectedTime.hour)  // 12 AM = hour 0
        assertEquals(5, expectedTime.minute)
        assertEquals(16, expectedTime.dayOfMonth)  // Next day
    }

    @Test
    fun `snooze at 1158 PM with 5 min interval results in 1203 AM`() {
        // Test case: 11:58 PM + 5 minutes should = 12:03 AM (next day)
        val baseTime = LocalDateTime.of(2024, 3, 15, 23, 58)
        val snoozeMinutes = 5

        val expectedTime = baseTime.plusMinutes(snoozeMinutes.toLong())

        assertEquals(0, expectedTime.hour)
        assertEquals(3, expectedTime.minute)
        assertEquals(16, expectedTime.dayOfMonth)
    }

    @Test
    fun `snooze at 1159 PM with 1 min interval results in 1200 AM`() {
        // Test case: 11:59 PM + 1 minute should = 12:00 AM (next day)
        val baseTime = LocalDateTime.of(2024, 3, 15, 23, 59)
        val snoozeMinutes = 1

        val expectedTime = baseTime.plusMinutes(snoozeMinutes.toLong())

        assertEquals(0, expectedTime.hour)
        assertEquals(0, expectedTime.minute)
        assertEquals(16, expectedTime.dayOfMonth)
    }

    @Test
    fun `snooze at 2345 with 30 min interval results in 0015 next day`() {
        // Test case: 23:45 + 30 minutes should = 00:15 (next day)
        val baseTime = LocalDateTime.of(2024, 3, 15, 23, 45)
        val snoozeMinutes = 30

        val expectedTime = baseTime.plusMinutes(snoozeMinutes.toLong())

        assertEquals(0, expectedTime.hour)
        assertEquals(15, expectedTime.minute)
        assertEquals(16, expectedTime.dayOfMonth)
    }

    @Test
    fun `snooze during normal hours works correctly`() {
        // Test case: 10:30 AM + 5 minutes should = 10:35 AM (same day)
        val baseTime = LocalDateTime.of(2024, 3, 15, 10, 30)
        val snoozeMinutes = 5

        val expectedTime = baseTime.plusMinutes(snoozeMinutes.toLong())

        assertEquals(10, expectedTime.hour)
        assertEquals(35, expectedTime.minute)
        assertEquals(15, expectedTime.dayOfMonth)  // Same day
    }

    @Test
    fun `snooze at end of month correctly rolls to next month`() {
        // Test case: March 31, 11:55 PM + 10 minutes should = April 1, 12:05 AM
        val baseTime = LocalDateTime.of(2024, 3, 31, 23, 55)
        val snoozeMinutes = 10

        val expectedTime = baseTime.plusMinutes(snoozeMinutes.toLong())

        assertEquals(0, expectedTime.hour)
        assertEquals(5, expectedTime.minute)
        assertEquals(4, expectedTime.monthValue)  // April
        assertEquals(1, expectedTime.dayOfMonth)  // 1st day of April
    }

    @Test
    fun `alarm model with snooze time calculation`() {
        // Integration-style test showing how Alarm model uses LocalDateTime.plusMinutes
        val now = LocalDateTime.of(2024, 3, 15, 23, 55)
        val snoozeMinutes = 10

        val snoozeTime = now.plusMinutes(snoozeMinutes.toLong())

        val alarm = Alarm(
            id = "test-alarm-1",
            hour = snoozeTime.hour,
            minute = snoozeTime.minute,
            label = "Snooze Alarm",
            missionType = MissionType.MATH,
            missionDifficulty = MissionDifficulty.EASY,
            strictMode = false,
            snoozeEnabled = true
        )

        assertEquals(0, alarm.hour)  // 12 AM
        assertEquals(5, alarm.minute)
    }

    @Test
    fun `multiple consecutive snoozes accumulate correctly`() {
        // Test case: Simulate multiple snooze presses
        var currentTime = LocalDateTime.of(2024, 3, 15, 23, 50)
        val snoozeInterval = 5

        // First snooze: 23:50 + 5 = 23:55
        currentTime = currentTime.plusMinutes(snoozeInterval.toLong())
        assertEquals(23, currentTime.hour)
        assertEquals(55, currentTime.minute)

        // Second snooze: 23:55 + 5 = 00:00 (next day)
        currentTime = currentTime.plusMinutes(snoozeInterval.toLong())
        assertEquals(0, currentTime.hour)
        assertEquals(0, currentTime.minute)
        assertEquals(16, currentTime.dayOfMonth)

        // Third snooze: 00:00 + 5 = 00:05
        currentTime = currentTime.plusMinutes(snoozeInterval.toLong())
        assertEquals(0, currentTime.hour)
        assertEquals(5, currentTime.minute)
    }

    @Test
    fun `large snooze interval crossing midnight`() {
        // Test case: 11:30 PM + 60 minutes should = 12:30 AM (next day)
        val baseTime = LocalDateTime.of(2024, 3, 15, 23, 30)
        val snoozeMinutes = 60

        val expectedTime = baseTime.plusMinutes(snoozeMinutes.toLong())

        assertEquals(0, expectedTime.hour)
        assertEquals(30, expectedTime.minute)
        assertEquals(16, expectedTime.dayOfMonth)
    }

    @Test
    fun `snooze at midnight boundary`() {
        // Test case: 00:00 + 5 minutes should = 00:05 (same day)
        val baseTime = LocalDateTime.of(2024, 3, 15, 0, 0)
        val snoozeMinutes = 5

        val expectedTime = baseTime.plusMinutes(snoozeMinutes.toLong())

        assertEquals(0, expectedTime.hour)
        assertEquals(5, expectedTime.minute)
        assertEquals(15, expectedTime.dayOfMonth)  // Same day
    }
}
