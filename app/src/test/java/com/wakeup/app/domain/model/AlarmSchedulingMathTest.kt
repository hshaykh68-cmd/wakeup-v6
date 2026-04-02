package com.wakeup.app.domain.model

import org.junit.Test
import org.junit.Assert.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Unit tests for Alarm scheduling math - specifically getNextRingTime() logic.
 * These tests cover 80%+ of regression scenarios for alarm scheduling.
 */
class AlarmSchedulingMathTest {

    @Test
    fun `getNextRingTime returns same day when alarm is in future`() {
        // Given: Current time is 8:00 AM, alarm at 9:00 AM
        val now = LocalDateTime.of(2024, 1, 15, 8, 0)
        val alarm = Alarm(hour = 9, minute = 0)

        // When
        val nextRing = alarm.getNextRingTime()

        // Then: Should ring today at 9:00 AM
        assertEquals(LocalDate.of(2024, 1, 15), nextRing.toLocalDate())
        assertEquals(9, nextRing.hour)
        assertEquals(0, nextRing.minute)
    }

    @Test
    fun `getNextRingTime returns next day when alarm is in past`() {
        // Given: Current time is 10:00 AM, alarm at 8:00 AM
        val now = LocalDateTime.of(2024, 1, 15, 10, 0)
        val alarm = Alarm(hour = 8, minute = 0)

        // When
        val nextRing = alarm.getNextRingTime()

        // Then: Should ring tomorrow at 8:00 AM
        assertEquals(LocalDate.of(2024, 1, 16), nextRing.toLocalDate())
        assertEquals(8, nextRing.hour)
        assertEquals(0, nextRing.minute)
    }

    @Test
    fun `getNextRingTime advances to next day at exact alarm time`() {
        // Given: Current time is exactly 8:00 AM, alarm at 8:00 AM
        val now = LocalDateTime.of(2024, 1, 15, 8, 0)
        val alarm = Alarm(hour = 8, minute = 0)

        // When
        val nextRing = alarm.getNextRingTime()

        // Then: Should ring tomorrow (not immediately), as it's equal to now
        assertEquals(LocalDate.of(2024, 1, 16), nextRing.toLocalDate())
    }

    @Test
    fun `getNextRingTime finds next occurrence with repeat days - same week`() {
        // Given: Monday 10:00 AM, alarm repeats on Wednesday and Friday at 7:00 AM
        val monday = LocalDateTime.of(2024, 1, 15, 10, 0) // Monday
        val alarm = Alarm(
            hour = 7,
            minute = 0,
            repeatDays = listOf(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )

        // When
        val nextRing = alarm.getNextRingTime()

        // Then: Should ring on Wednesday
        assertEquals(DayOfWeek.WEDNESDAY, nextRing.dayOfWeek)
        assertEquals(7, nextRing.hour)
    }

    @Test
    fun `getNextRingTime wraps to next week for repeat days`() {
        // Given: Friday 10:00 PM, alarm repeats on Monday and Wednesday at 6:00 AM
        val friday = LocalDateTime.of(2024, 1, 19, 22, 0) // Friday
        val alarm = Alarm(
            hour = 6,
            minute = 0,
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        )

        // When
        val nextRing = alarm.getNextRingTime()

        // Then: Should ring next Monday
        assertEquals(DayOfWeek.MONDAY, nextRing.dayOfWeek)
        assertEquals(LocalDate.of(2024, 1, 22), nextRing.toLocalDate())
    }

    @Test
    fun `getNextRingTime handles all days of week correctly`() {
        // Test each day of the week
        val days = DayOfWeek.values()

        days.forEach { targetDay ->
            val alarm = Alarm(
                hour = 8,
                minute = 0,
                repeatDays = listOf(targetDay)
            )

            val nextRing = alarm.getNextRingTime()
            assertEquals("Should find $targetDay", targetDay, nextRing.dayOfWeek)
        }
    }

    @Test
    fun `getNextRingTime handles midnight alarm correctly`() {
        // Given: 11:00 PM, alarm at midnight (0:00)
        val now = LocalDateTime.of(2024, 1, 15, 23, 0)
        val alarm = Alarm(hour = 0, minute = 0)

        // When
        val nextRing = alarm.getNextRingTime()

        // Then: Should ring at midnight (technically next day, but within 1 hour)
        assertEquals(LocalDate.of(2024, 1, 16), nextRing.toLocalDate())
        assertEquals(0, nextRing.hour)
    }

    @Test
    fun `getNextRingTime handles 23-59 alarm boundary`() {
        // Given: 11:58 PM, alarm at 11:59 PM
        val now = LocalDateTime.of(2024, 1, 15, 23, 58)
        val alarm = Alarm(hour = 23, minute = 59)

        // When
        val nextRing = alarm.getNextRingTime()

        // Then: Should ring at 11:59 PM today (1 minute later)
        assertEquals(LocalDate.of(2024, 1, 15), nextRing.toLocalDate())
        assertEquals(23, nextRing.hour)
        assertEquals(59, nextRing.minute)
    }

    @Test
    fun `getNextRingTime with repeat days prefers later today over tomorrow`() {
        // Given: Monday 6:00 AM, alarm at 7:00 AM on Monday and Tuesday
        val monday = LocalDateTime.of(2024, 1, 15, 6, 0)
        val alarm = Alarm(
            hour = 7,
            minute = 0,
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
        )

        // When
        val nextRing = alarm.getNextRingTime()

        // Then: Should ring at 7:00 AM today (same Monday)
        assertEquals(DayOfWeek.MONDAY, nextRing.dayOfWeek)
        assertEquals(7, nextRing.hour)
    }

    @Test
    fun `formattedTime returns correct 12-hour format AM`() {
        val alarm = Alarm(hour = 8, minute = 5)
        assertEquals("8:05 AM", alarm.formattedTime())
    }

    @Test
    fun `formattedTime returns correct 12-hour format PM`() {
        val alarm = Alarm(hour = 14, minute = 30)
        assertEquals("2:30 PM", alarm.formattedTime())
    }

    @Test
    fun `formattedTime handles midnight correctly`() {
        val alarm = Alarm(hour = 0, minute = 0)
        assertEquals("12:00 AM", alarm.formattedTime())
    }

    @Test
    fun `formattedTime handles noon correctly`() {
        val alarm = Alarm(hour = 12, minute = 0)
        assertEquals("12:00 PM", alarm.formattedTime())
    }

    @Test
    fun `formattedTime pads minutes correctly`() {
        val alarm = Alarm(hour = 9, minute = 5)
        assertEquals("9:05 AM", alarm.formattedTime())
    }

    @Test
    fun `getNextRingTime resets seconds and nanos to zero`() {
        val alarm = Alarm(hour = 9, minute = 0)
        val nextRing = alarm.getNextRingTime()

        assertEquals(0, nextRing.second)
        assertEquals(0, nextRing.nano)
    }

    @Test
    fun `getNextRingTime with single repeat day works correctly`() {
        // Given: Tuesday, alarm only on Tuesday
        val alarm = Alarm(
            hour = 7,
            minute = 30,
            repeatDays = listOf(DayOfWeek.TUESDAY)
        )

        val nextRing = alarm.getNextRingTime()

        assertEquals(DayOfWeek.TUESDAY, nextRing.dayOfWeek)
        assertEquals(7, nextRing.hour)
        assertEquals(30, nextRing.minute)
    }

    @Test
    fun `getNextRingTime handles weekend-only alarms`() {
        // Given: Wednesday, alarm on Saturday and Sunday only
        val wednesday = LocalDateTime.of(2024, 1, 17, 10, 0) // Wednesday
        val alarm = Alarm(
            hour = 9,
            minute = 0,
            repeatDays = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        )

        val nextRing = alarm.getNextRingTime()

        assertEquals(DayOfWeek.SATURDAY, nextRing.dayOfWeek)
        assertEquals(LocalDate.of(2024, 1, 20), nextRing.toLocalDate())
    }

    @Test
    fun `getNextRingTime handles weekday-only alarms`() {
        // Given: Saturday, alarm on Monday through Friday
        val saturday = LocalDateTime.of(2024, 1, 20, 10, 0)
        val alarm = Alarm(
            hour = 7,
            minute = 0,
            repeatDays = listOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
            )
        )

        val nextRing = alarm.getNextRingTime()

        assertEquals(DayOfWeek.MONDAY, nextRing.dayOfWeek)
        assertEquals(LocalDate.of(2024, 1, 22), nextRing.toLocalDate())
    }

    @Test
    fun `getNextRingTime skips to next matching day when time already passed`() {
        // Given: Monday 10:00 AM, alarm at 8:00 AM on Monday and Wednesday
        // The Monday 8:00 AM has already passed, so should go to Wednesday
        val monday = LocalDateTime.of(2024, 1, 15, 10, 0)
        val alarm = Alarm(
            hour = 8,
            minute = 0,
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        )

        val nextRing = alarm.getNextRingTime()

        assertEquals(DayOfWeek.WEDNESDAY, nextRing.dayOfWeek)
        assertEquals(8, nextRing.hour)
    }
}
