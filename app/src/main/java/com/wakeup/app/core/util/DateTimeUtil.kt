package com.wakeup.app.core.util

import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateTimeUtil {
    
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    private val timeFormatter24h = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
    
    fun formatTime(hour: Int, minute: Int, use24Hour: Boolean = false): String {
        val time = java.time.LocalTime.of(hour, minute)
        return if (use24Hour) {
            time.format(timeFormatter24h)
        } else {
            time.format(timeFormatter)
        }
    }
    
    fun formatDayOfWeek(dayOfWeek: DayOfWeek): String {
        return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
    
    fun getCurrentTimeFormatted(): String {
        val now = java.time.LocalTime.now()
        return now.format(timeFormatter)
    }
    
    fun getDayAbbreviations(): List<String> {
        return DayOfWeek.values().map { day ->
            day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1)
        }
    }
    
    fun getFullDayNames(): List<String> {
        return DayOfWeek.values().map { day ->
            day.getDisplayName(TextStyle.FULL, Locale.getDefault())
        }
    }
    
    fun formatRepeatDays(repeatDays: List<DayOfWeek>): String {
        return when {
            repeatDays.isEmpty() -> "One time"
            repeatDays.size == 7 -> "Every day"
            repeatDays.size == 5 && !repeatDays.contains(DayOfWeek.SATURDAY) && !repeatDays.contains(DayOfWeek.SUNDAY) -> "Weekdays"
            repeatDays.size == 2 && repeatDays.contains(DayOfWeek.SATURDAY) && repeatDays.contains(DayOfWeek.SUNDAY) -> "Weekends"
            else -> repeatDays.joinToString(", ") { formatDayOfWeek(it) }
        }
    }
}
