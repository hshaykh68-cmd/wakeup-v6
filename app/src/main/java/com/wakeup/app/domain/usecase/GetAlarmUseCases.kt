package com.wakeup.app.domain.usecase

import com.wakeup.app.domain.model.Alarm
import com.wakeup.app.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetAllAlarmsUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    operator fun invoke(): Flow<List<Alarm>> {
        return alarmRepository.getAllAlarms()
    }
}

class GetEnabledAlarmsUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    operator fun invoke(): Flow<List<Alarm>> {
        return alarmRepository.getEnabledAlarms()
    }
}

class GetAlarmByIdUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(alarmId: String): Alarm? {
        return alarmRepository.getAlarmById(alarmId)
    }
}
