package com.wakeup.app.domain.model

/**
 * Represents time range options for stats filtering
 */
enum class StatsTimeRange(val displayName: String, val days: Int) {
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30),
    ALL_TIME("All-time", Int.MAX_VALUE);

    companion object {
        fun fromDays(days: Int): StatsTimeRange {
            return when {
                days <= 7 -> WEEKLY
                days <= 30 -> MONTHLY
                else -> ALL_TIME
            }
        }
    }
}
