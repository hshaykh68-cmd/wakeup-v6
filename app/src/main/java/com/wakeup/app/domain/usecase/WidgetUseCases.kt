package com.wakeup.app.domain.usecase

import com.wakeup.app.domain.model.Alarm
import com.wakeup.app.domain.repository.WidgetStateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNextAlarmFlowUseCase @Inject constructor(
    private val widgetStateRepository: WidgetStateRepository
) {
    operator fun invoke(): Flow<Alarm?> {
        return widgetStateRepository.getNextAlarmFlow()
    }
}

class ToggleAlarmFromWidgetUseCase @Inject constructor(
    private val widgetStateRepository: WidgetStateRepository
) {
    suspend operator fun invoke(alarmId: String) {
        widgetStateRepository.toggleAlarm(alarmId)
    }
}
