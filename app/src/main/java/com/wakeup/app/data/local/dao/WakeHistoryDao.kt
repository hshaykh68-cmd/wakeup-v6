package com.wakeup.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wakeup.app.data.local.entity.WakeHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WakeHistoryDao {
    @Query("SELECT * FROM wake_history ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<WakeHistoryEntity>>

    @Query("SELECT * FROM wake_history WHERE createdAt >= :sinceTimestamp ORDER BY createdAt DESC")
    fun getHistorySince(sinceTimestamp: Long): Flow<List<WakeHistoryEntity>>

    @Query("SELECT * FROM wake_history WHERE alarmId = :alarmId ORDER BY createdAt DESC")
    suspend fun getHistoryForAlarm(alarmId: String): List<WakeHistoryEntity>

    @Query("SELECT * FROM wake_history WHERE id = :historyId")
    suspend fun getHistoryById(historyId: String): WakeHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: WakeHistoryEntity)

    @Update
    suspend fun updateHistory(history: WakeHistoryEntity)

    @Query("UPDATE wake_history SET snoozeCount = :snoozeCount, missionCompleted = :missionCompleted, success = :success WHERE id = :historyId")
    suspend fun updateWakeAttempt(
        historyId: String,
        snoozeCount: Int,
        missionCompleted: Boolean,
        success: Boolean
    )
}
