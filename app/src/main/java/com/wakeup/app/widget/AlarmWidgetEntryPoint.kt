package com.wakeup.app.widget

import com.wakeup.app.domain.usecase.GetNextAlarmFlowUseCase
import com.wakeup.app.domain.usecase.ToggleAlarmFromWidgetUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AlarmWidgetEntryPoint {
    fun getNextAlarmFlowUseCase(): GetNextAlarmFlowUseCase
    fun toggleAlarmFromWidgetUseCase(): ToggleAlarmFromWidgetUseCase
}
