package com.wakeup.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wakeup.app.data.local.entity.SleepSession
import kotlinx.coroutines.flow.Flow

/**
 * DAO for sleep session data access.
 */
@Dao
interface SleepDao {
    
    /**
     * Insert a new sleep session.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SleepSession)
    
    /**
     * Update an existing sleep session.
     */
    @Update
    suspend fun updateSession(session: SleepSession)
    
    /**
     * Delete a sleep session.
     */
    @Delete
    suspend fun deleteSession(session: SleepSession)
    
    /**
     * Get all sleep sessions ordered by start time (descending).
     */
    @Query("SELECT * FROM sleep_sessions ORDER BY start_time DESC")
    fun getAllSessions(): Flow<List<SleepSession>>
    
    /**
     * Get sleep sessions for a specific alarm.
     */
    @Query("SELECT * FROM sleep_sessions WHERE alarm_id = :alarmId ORDER BY start_time DESC")
    fun getSessionsForAlarm(alarmId: String): Flow<List<SleepSession>>
    
    /**
     * Get the most recent sleep session.
     */
    @Query("SELECT * FROM sleep_sessions ORDER BY start_time DESC LIMIT 1")
    suspend fun getMostRecentSession(): SleepSession?
    
    /**
     * Get sleep sessions within a date range.
     */
    @Query("SELECT * FROM sleep_sessions WHERE start_time >= :startTime AND start_time <= :endTime ORDER BY start_time DESC")
    fun getSessionsInRange(startTime: Long, endTime: Long): Flow<List<SleepSession>>
    
    /**
     * Get completed sessions (with end time) in date range.
     */
    @Query("SELECT * FROM sleep_sessions WHERE end_time IS NOT NULL AND start_time >= :startTime AND start_time <= :endTime ORDER BY start_time DESC")
    suspend fun getCompletedSessionsInRange(startTime: Long, endTime: Long): List<SleepSession>
    
    /**
     * Get incomplete session (active sleep tracking).
     */
    @Query("SELECT * FROM sleep_sessions WHERE end_time IS NULL LIMIT 1")
    suspend fun getActiveSession(): SleepSession?
    
    /**
     * Get sessions that used Smart Wake.
     */
    @Query("SELECT * FROM sleep_sessions WHERE smart_wake_used = 1 ORDER BY start_time DESC")
    fun getSmartWakeSessions(): Flow<List<SleepSession>>
    
    /**
     * Get Smart Wake success stats.
     */
    @Query("SELECT COUNT(*) FROM sleep_sessions WHERE smart_wake_used = 1")
    suspend fun getTotalSmartWakeUses(): Int
    
    /**
     * Get count of Smart Wake successes (woke in window).
     */
    @Query("SELECT COUNT(*) FROM sleep_sessions WHERE smart_wake_used = 1 AND woke_in_smart_window = 1")
    suspend fun getSmartWakeSuccessCount(): Int
    
    /**
     * Delete all sessions older than a timestamp.
     */
    @Query("DELETE FROM sleep_sessions WHERE start_time < :timestamp")
    suspend fun deleteSessionsOlderThan(timestamp: Long)
    
    /**
     * Get average sleep duration in minutes.
     */
    @Query("SELECT AVG(sleep_duration_minutes) FROM sleep_sessions WHERE end_time IS NOT NULL")
    suspend fun getAverageSleepDuration(): Float?
    
    /**
     * Get average sleep quality score.
     */
    @Query("SELECT AVG(sleep_quality_score) FROM sleep_sessions WHERE end_time IS NOT NULL")
    suspend fun getAverageQualityScore(): Float?
    
    /**
     * Get total session count.
     */
    @Query("SELECT COUNT(*) FROM sleep_sessions")
    suspend fun getSessionCount(): Int
}
