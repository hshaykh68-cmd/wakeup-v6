package com.wakeup.app.data.repository

import com.wakeup.app.data.local.dao.AlarmDao
import com.wakeup.app.data.local.entity.AlarmEntity
import com.wakeup.app.domain.model.Alarm
import com.wakeup.app.domain.repository.WidgetStateRepository
import com.wakeup.app.domain.usecase.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetStateRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler
) : WidgetStateRepository {

    override fun getNextAlarmFlow(): Flow<Alarm?> {
        return alarmDao.getEnabledAlarms().map { entities ->
            entities.map { it.toAlarm() }
                .minByOrNull { it.getNextRingTime() }
        }
    }

    override suspend fun toggleAlarm(alarmId: String) {
        val alarm = alarmDao.getAlarmById(alarmId)?.toAlarm()
        alarm?.let {
            val newState = !it.isEnabled
            alarmDao.updateAlarm(
                it.copy(isEnabled = newState).toEntity()
            )
            if (newState) {
                alarmScheduler.schedule(it.copy(isEnabled = true))
            } else {
                alarmScheduler.cancel(it.id)
            }
        }
    }

    private fun AlarmEntity.toAlarm(): Alarm {
        return Alarm(
            id = this.id,
            hour = this.hour,
            minute = this.minute,
            label = this.label,
            isEnabled = this.isEnabled,
            repeatDays = this.repeatDays,
            soundUri = this.soundUri,
            useVibration = this.useVibration,
            gradualVolume = this.gradualVolume,
            missionType = this.missionType,
            missionDifficulty = this.missionDifficulty,
            strictMode = this.strictMode,
            snoozeEnabled = this.snoozeEnabled,
            snoozeInterval = this.snoozeInterval,
            maxSnoozes = this.maxSnoozes,
            photoReferencePath = this.photoReferencePath,
            photoReferenceHash = this.photoReferenceHash,
            barcodeValue = this.barcodeValue,
            barcodeFormat = this.barcodeFormat,
            createdAt = this.createdAt
        )
    }

    private fun Alarm.toEntity(): AlarmEntity {
        return AlarmEntity(
            id = this.id,
            hour = this.hour,
            minute = this.minute,
            label = this.label,
            isEnabled = this.isEnabled,
            repeatDays = this.repeatDays,
            soundUri = this.soundUri,
            useVibration = this.useVibration,
            gradualVolume = this.gradualVolume,
            missionType = this.missionType,
            missionDifficulty = this.missionDifficulty,
            strictMode = this.strictMode,
            snoozeEnabled = this.snoozeEnabled,
            snoozeInterval = this.snoozeInterval,
            maxSnoozes = this.maxSnoozes,
            photoReferencePath = this.photoReferencePath,
            photoReferenceHash = this.photoReferenceHash,
            barcodeValue = this.barcodeValue,
            barcodeFormat = this.barcodeFormat,
            createdAt = this.createdAt
        )
    }
}
