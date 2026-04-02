package com.wakeup.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Sleep session entity for tracking sleep periods.
 * Records sleep start/end times, quality metrics, and Smart Wake data.
 */
@Entity(tableName = "sleep_sessions")
data class SleepSession(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "alarm_id")
    val alarmId: String?,  // Associated alarm (if any)
    
    @ColumnInfo(name = "start_time")
    val startTime: Long,  // Epoch millis
    
    @ColumnInfo(name = "end_time")
    val endTime: Long?,   // Epoch millis (null if still sleeping)
    
    @ColumnInfo(name = "sleep_duration_minutes")
    val sleepDurationMinutes: Int = 0,
    
    @ColumnInfo(name = "deep_sleep_minutes")
    val deepSleepMinutes: Int = 0,
    
    @ColumnInfo(name = "light_sleep_minutes")
    val lightSleepMinutes: Int = 0,
    
    @ColumnInfo(name = "awake_minutes")
    val awakeMinutes: Int = 0,
    
    @ColumnInfo(name = "sleep_quality_score")
    val sleepQualityScore: Float = 0f,  // 0-100
    
    @ColumnInfo(name = "smart_wake_used")
    val smartWakeUsed: Boolean = false,
    
    @ColumnInfo(name = "smart_wake_triggered")
    val smartWakeTriggered: Boolean = false,  // Was Smart Wake actually used?
    
    @ColumnInfo(name = "woke_in_smart_window")
    val wokeInSmartWindow: Boolean = false,  // Did user wake within Smart Wake window?
    
    @ColumnInfo(name = "snooze_count")
    val snoozeCount: Int = 0,
    
    @ColumnInfo(name = "mission_completed")
    val missionCompleted: Boolean = false,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Get start time as LocalDateTime.
     */
    fun getStartDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(startTime),
            ZoneId.systemDefault()
        )
    }
    
    /**
     * Get end time as LocalDateTime (null if not ended).
     */
    fun getEndDateTime(): LocalDateTime? {
        return endTime?.let {
            LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(it),
                ZoneId.systemDefault()
            )
        }
    }
    
    /**
     * Calculate sleep efficiency (sleep time / total time in bed).
     */
    fun getSleepEfficiency(): Float {
        val totalTime = sleepDurationMinutes.toFloat()
        if (totalTime <= 0) return 0f
        
        val sleepTime = (deepSleepMinutes + lightSleepMinutes).toFloat()
        return (sleepTime / totalTime * 100).coerceIn(0f, 100f)
    }
    
    /**
     * Check if this is a completed sleep session.
     */
    fun isCompleted(): Boolean = endTime != null
    
    companion object {
        /**
         * Create a new sleep session for an alarm.
         */
        fun createForAlarm(
            alarmId: String,
            startTime: Long = System.currentTimeMillis()
        ): SleepSession {
            return SleepSession(
                id = java.util.UUID.randomUUID().toString(),
                alarmId = alarmId,
                startTime = startTime,
                endTime = null,
                smartWakeUsed = false,
                smartWakeTriggered = false
            )
        }
        
        /**
         * Create a manual sleep tracking session.
         */
        fun createManual(startTime: Long = System.currentTimeMillis()): SleepSession {
            return SleepSession(
                id = java.util.UUID.randomUUID().toString(),
                alarmId = null,
                startTime = startTime,
                endTime = null
            )
        }
    }
}

/**
 * Data class for sleep statistics.
 */
data class SleepStats(
    val totalSessions: Int,
    val averageDurationMinutes: Float,
    val averageQualityScore: Float,
    val averageEfficiency: Float,
    val totalSmartWakeUses: Int,
    val smartWakeSuccessRate: Float,  // % of times woke in window
    val weeklyTrend: List<DailySleepData>,
    val bestNight: SleepSession?,
    val worstNight: SleepSession?
)

/**
 * Daily sleep data for trends.
 */
data class DailySleepData(
    val date: LocalDateTime,
    val durationMinutes: Int,
    val qualityScore: Float,
    val efficiency: Float
)
