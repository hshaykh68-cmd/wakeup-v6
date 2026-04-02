package com.wakeup.app.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeup.app.domain.model.StatsTimeRange
import com.wakeup.app.domain.model.UserStats
import com.wakeup.app.domain.model.WakeHistory
import com.wakeup.app.domain.usecase.GetRecentHistoryUseCase
import com.wakeup.app.domain.usecase.GetUserStatsFlowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class StatsUiState(
    val stats: UserStats = UserStats(),
    val filteredHistory: List<WakeHistory> = emptyList(),
    val selectedTimeRange: StatsTimeRange = StatsTimeRange.WEEKLY,
    val isLoading: Boolean = false
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getUserStatsFlowUseCase: GetUserStatsFlowUseCase,
    private val getRecentHistoryUseCase: GetRecentHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private val _allHistory = MutableStateFlow<List<WakeHistory>>(emptyList())

    init {
        collectStats()
        loadAllHistory()
    }

    private fun collectStats() {
        viewModelScope.launch {
            getUserStatsFlowUseCase().collect { userStats ->
                _uiState.value = _uiState.value.copy(stats = userStats)
            }
        }
    }

    private fun loadAllHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Load all history (ALL_TIME) by passing null, then filter based on selected range
            getRecentHistoryUseCase(days = null).collect { history ->
                _allHistory.value = history
                applyTimeRangeFilter()
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Set the time range filter and update displayed data
     */
    fun setTimeRange(timeRange: StatsTimeRange) {
        _uiState.value = _uiState.value.copy(selectedTimeRange = timeRange)
        applyTimeRangeFilter()
    }

    /**
     * Filter history based on selected time range
     */
    private fun applyTimeRangeFilter() {
        val allHistory = _allHistory.value
        val timeRange = _uiState.value.selectedTimeRange

        val filteredHistory = if (timeRange == StatsTimeRange.ALL_TIME) {
            allHistory
        } else {
            val cutoffDate = LocalDateTime.now().minusDays(timeRange.days.toLong())
            allHistory.filter { it.alarmTime.isAfter(cutoffDate) || it.alarmTime.isEqual(cutoffDate) }
        }

        _uiState.value = _uiState.value.copy(filteredHistory = filteredHistory)
    }

    /**
     * Get stats for the currently selected time range
     */
    fun getFilteredStats(): UserStats {
        val filteredHistory = _uiState.value.filteredHistory
        val allStats = _uiState.value.stats

        if (filteredHistory.isEmpty()) {
            return UserStats() // Return empty stats if no data in range
        }

        // Calculate filtered stats based on filtered history
        val totalWakeUps = filteredHistory.count { it.success }
        val failedWakeUps = filteredHistory.count { !it.success }
        val successRate = if (filteredHistory.isNotEmpty()) {
            (totalWakeUps.toFloat() / filteredHistory.size) * 100
        } else 0f

        val missionsCompleted = filteredHistory.count { it.missionCompleted }

        return allStats.copy(
            successRate = successRate,
            totalMissionsCompleted = missionsCompleted,
            streakInfo = allStats.streakInfo.copy(
                totalWakeUps = totalWakeUps,
                failedWakeUps = failedWakeUps
            )
        )
    }
}
