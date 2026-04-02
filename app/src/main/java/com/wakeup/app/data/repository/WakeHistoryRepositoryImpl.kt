package com.wakeup.app.data.repository

import com.wakeup.app.data.local.dao.WakeHistoryDao
import com.wakeup.app.data.local.entity.WakeHistoryEntity
import com.wakeup.app.domain.model.WakeHistory
import com.wakeup.app.domain.repository.WakeHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WakeHistoryRepositoryImpl @Inject constructor(
    private val wakeHistoryDao: WakeHistoryDao
) : WakeHistoryRepository {

    override suspend fun recordAlarmTriggered(alarmId: String, alarmTime: Long): WakeHistory {
        val history = WakeHistoryEntity(
            id = UUID.randomUUID().toString(),
            alarmId = alarmId,
            alarmTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(alarmTime),
                java.time.ZoneId.systemDefault()
            ),
            wakeTime = null,
            snoozeCount = 0,
            missionCompleted = false,
            success = false,
            createdAt = System.currentTimeMillis()
        )
        wakeHistoryDao.insertHistory(history)
        return history.toDomainModel()
    }

    override suspend fun recordWakeAttempt(
        historyId: String,
        snoozeCount: Int,
        missionCompleted: Boolean,
        success: Boolean
    ) {
        wakeHistoryDao.updateWakeAttempt(historyId, snoozeCount, missionCompleted, success)
    }

    override suspend fun getHistoryForAlarm(alarmId: String): List<WakeHistory> {
        return wakeHistoryDao.getHistoryForAlarm(alarmId).map { it.toDomainModel() }
    }

    override fun getAllHistory(): Flow<List<WakeHistory>> {
        return wakeHistoryDao.getAllHistory().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getRecentHistory(days: Int?): Flow<List<WakeHistory>> {
        return if (days == null) {
            // ALL_TIME - return all history without date filter
            getAllHistory()
        } else {
            val sinceTimestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000)
            wakeHistoryDao.getHistorySince(sinceTimestamp).map { entities ->
                entities.map { it.toDomainModel() }
            }
        }
    }
}
