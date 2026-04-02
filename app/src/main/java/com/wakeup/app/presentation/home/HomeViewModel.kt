package com.wakeup.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeup.app.domain.model.Alarm
import com.wakeup.app.domain.usecase.GetAllAlarmsUseCase
import com.wakeup.app.domain.usecase.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllAlarmsUseCase: GetAllAlarmsUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val stats = getUserStatsUseCase()
            getAllAlarmsUseCase().collect { alarms ->
                val enabledAlarms = alarms.filter { it.isEnabled }
                val nextAlarm = enabledAlarms.minByOrNull { it.getNextRingTime() }
                
                _uiState.value = HomeUiState(
                    nextAlarm = nextAlarm,
                    currentStreak = stats.streakInfo.currentStreak,
                    bestStreak = stats.streakInfo.bestStreak,
                    totalWakeUps = stats.streakInfo.totalWakeUps,
                    successRate = stats.successRate,
                    greeting = getGreeting()
                )
            }
        }
    }

    private fun getGreeting(): String {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }
}

data class HomeUiState(
    val nextAlarm: Alarm? = null,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalWakeUps: Int = 0,
    val successRate: Float = 0f,
    val greeting: String = "Hello"
)
