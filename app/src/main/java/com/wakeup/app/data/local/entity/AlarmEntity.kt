package com.wakeup.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wakeup.app.domain.model.Alarm
import com.wakeup.app.domain.model.MissionDifficulty
import com.wakeup.app.domain.model.MissionType
import java.time.DayOfWeek

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "pending_intent_id")
    val pendingIntentId: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean,
    val repeatDays: List<DayOfWeek>,
    val soundUri: String?,
    val useVibration: Boolean,
    val gradualVolume: Boolean,
    val missionType: MissionType,
    val missionDifficulty: MissionDifficulty,
    val strictMode: Boolean,
    val snoozeEnabled: Boolean,
    val snoozeInterval: Int,
    val maxSnoozes: Int,
    val photoReferencePath: String?,
    val photoReferenceHash: String?,
    val barcodeValue: String?,
    val barcodeFormat: Int?,
    @ColumnInfo(name = "smart_wake_enabled")
    val smartWakeEnabled: Boolean = false,
    @ColumnInfo(name = "smart_wake_window_minutes")
    val smartWakeWindowMinutes: Int = 30,
    val createdAt: Long
) {
    fun toDomainModel(): Alarm {
        return Alarm(
            id = id,
            hour = hour,
            minute = minute,
            label = label,
            isEnabled = isEnabled,
            repeatDays = repeatDays,
            soundUri = soundUri,
            useVibration = useVibration,
            gradualVolume = gradualVolume,
            missionType = missionType,
            missionDifficulty = missionDifficulty,
            strictMode = strictMode,
            snoozeEnabled = snoozeEnabled,
            snoozeInterval = snoozeInterval,
            maxSnoozes = maxSnoozes,
            photoReferencePath = photoReferencePath,
            photoReferenceHash = photoReferenceHash,
            barcodeValue = barcodeValue,
            barcodeFormat = barcodeFormat,
            smartWakeEnabled = smartWakeEnabled,
            smartWakeWindowMinutes = smartWakeWindowMinutes,
            pendingIntentId = pendingIntentId,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomainModel(alarm: Alarm): AlarmEntity {
            return AlarmEntity(
                id = alarm.id,
                pendingIntentId = alarm.pendingIntentId,
                hour = alarm.hour,
                minute = alarm.minute,
                label = alarm.label,
                isEnabled = alarm.isEnabled,
                repeatDays = alarm.repeatDays,
                soundUri = alarm.soundUri,
                useVibration = alarm.useVibration,
                gradualVolume = alarm.gradualVolume,
                missionType = alarm.missionType,
                missionDifficulty = alarm.missionDifficulty,
                strictMode = alarm.strictMode,
                snoozeEnabled = alarm.snoozeEnabled,
                snoozeInterval = alarm.snoozeInterval,
                maxSnoozes = alarm.maxSnoozes,
                photoReferencePath = alarm.photoReferencePath,
                photoReferenceHash = alarm.photoReferenceHash,
                barcodeValue = alarm.barcodeValue,
                barcodeFormat = alarm.barcodeFormat,
                smartWakeEnabled = alarm.smartWakeEnabled,
                smartWakeWindowMinutes = alarm.smartWakeWindowMinutes,
                createdAt = alarm.createdAt
            )
        }
    }
}
