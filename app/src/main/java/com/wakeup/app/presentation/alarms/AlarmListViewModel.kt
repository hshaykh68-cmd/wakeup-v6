package com.wakeup.app.presentation.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeup.app.domain.model.Alarm
import com.wakeup.app.domain.usecase.DeleteAlarmUseCase
import com.wakeup.app.domain.usecase.DuplicateAlarmUseCase
import com.wakeup.app.domain.usecase.GetAllAlarmsUseCase
import com.wakeup.app.domain.usecase.ToggleAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmListViewModel @Inject constructor(
    getAllAlarmsUseCase: GetAllAlarmsUseCase,
    private val toggleAlarmUseCase: ToggleAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val duplicateAlarmUseCase: DuplicateAlarmUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    val alarms = getAllAlarmsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Mark loading as false once we have data
        viewModelScope.launch {
            alarms.collect { _isLoading.value = false }
        }
    }

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            toggleAlarmUseCase(alarm)
        }
    }

    fun deleteAlarm(alarmId: String) {
        viewModelScope.launch {
            deleteAlarmUseCase(alarmId)
        }
    }

    fun duplicateAlarm(alarmId: String) {
        viewModelScope.launch {
            duplicateAlarmUseCase(alarmId)
        }
    }
}
