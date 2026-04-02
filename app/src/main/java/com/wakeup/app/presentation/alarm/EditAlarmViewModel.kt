package com.wakeup.app.presentation.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeup.app.domain.model.Alarm
import com.wakeup.app.domain.usecase.DeleteAlarmUseCase
import com.wakeup.app.domain.usecase.GetAlarmByIdUseCase
import com.wakeup.app.domain.usecase.UpdateAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditAlarmViewModel @Inject constructor(
    private val getAlarmByIdUseCase: GetAlarmByIdUseCase,
    private val updateAlarmUseCase: UpdateAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase
) : ViewModel() {

    private val _alarm = MutableStateFlow<Alarm?>(null)
    val alarm: StateFlow<Alarm?> = _alarm.asStateFlow()

    fun loadAlarm(alarmId: String) {
        viewModelScope.launch {
            _alarm.value = getAlarmByIdUseCase(alarmId)
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            updateAlarmUseCase(alarm)
        }
    }

    fun deleteAlarm(alarmId: String) {
        viewModelScope.launch {
            deleteAlarmUseCase(alarmId)
        }
    }
}
