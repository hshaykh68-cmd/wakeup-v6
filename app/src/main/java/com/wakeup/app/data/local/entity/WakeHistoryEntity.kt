package com.wakeup.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wakeup.app.domain.model.WakeHistory
import java.time.LocalDateTime

@Entity(tableName = "wake_history")
data class WakeHistoryEntity(
    @PrimaryKey
    val id: String,
    val alarmId: String,
    val alarmTime: LocalDateTime,
    val wakeTime: LocalDateTime?,
    val snoozeCount: Int,
    val missionCompleted: Boolean,
    val success: Boolean,
    val createdAt: Long
) {
    fun toDomainModel(): WakeHistory {
        return WakeHistory(
            id = id,
            alarmId = alarmId,
            alarmTime = alarmTime,
            wakeTime = wakeTime,
            snoozeCount = snoozeCount,
            missionCompleted = missionCompleted,
            success = success,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomainModel(history: WakeHistory): WakeHistoryEntity {
            return WakeHistoryEntity(
                id = history.id,
                alarmId = history.alarmId,
                alarmTime = history.alarmTime,
                wakeTime = history.wakeTime,
                snoozeCount = history.snoozeCount,
                missionCompleted = history.missionCompleted,
                success = history.success,
                createdAt = history.createdAt
            )
        }
    }
}
