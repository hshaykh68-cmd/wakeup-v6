package com.wakeup.app.domain.usecase

import com.wakeup.app.domain.model.Alarm
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ScheduleAlarmUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(alarm: Alarm): Boolean {
        return alarmScheduler.schedule(alarm)
    }
}

class CancelAlarmUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(alarmId: String) {
        alarmScheduler.cancel(alarmId)
    }
}

class RescheduleAllAlarmsUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val getEnabledAlarmsUseCase: GetEnabledAlarmsUseCase
) {
    suspend operator fun invoke(): Result<Int> {
        return try {
            val alarms = getEnabledAlarmsUseCase().first()
            var failedCount = 0
            alarms.forEach { 
                val scheduled = alarmScheduler.schedule(it)
                if (!scheduled) failedCount++
            }
            if (failedCount > 0) {
                // Return success but indicate some alarms used fallback scheduling
                Result.success(failedCount)
            } else {
                Result.success(0)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class SnoozeAlarmUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(alarm: Alarm, snoozeMinutes: Int = 5): Boolean {
        return alarmScheduler.snooze(alarm, snoozeMinutes)
    }
}

interface AlarmScheduler {
    fun schedule(alarm: Alarm): Boolean
    fun cancel(alarmId: String)
    fun snooze(alarm: Alarm, snoozeMinutes: Int): Boolean
    fun canScheduleExactAlarms(): Boolean
}
