package com.wakeup.app.presentation.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeup.app.data.local.dao.SleepDao
import com.wakeup.app.data.local.entity.DailySleepData
import com.wakeup.app.data.local.entity.SleepSession
import com.wakeup.app.data.local.entity.SleepStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * ViewModel for Sleep screen and sleep analytics.
 */
@HiltViewModel
class SleepViewModel @Inject constructor(
    private val sleepDao: SleepDao
) : ViewModel() {

    private val _sleepSessions = MutableStateFlow<List<SleepSession>>(emptyList())
    val sleepSessions: StateFlow<List<SleepSession>> = _sleepSessions.asStateFlow()

    private val _recentSession = MutableStateFlow<SleepSession?>(null)
    val recentSession: StateFlow<SleepSession?> = _recentSession.asStateFlow()

    private val _sleepStats = MutableStateFlow<SleepStats?>(null)
    val sleepStats: StateFlow<SleepStats?> = _sleepStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeSession = MutableStateFlow<SleepSession?>(null)
    val activeSession: StateFlow<SleepSession?> = _activeSession.asStateFlow()

    init {
        loadSleepData()
        checkActiveSession()
    }

    /**
     * Load all sleep sessions and calculate statistics.
     */
    private fun loadSleepData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Collect all sessions
            sleepDao.getAllSessions().collectLatest { sessions ->
                _sleepSessions.value = sessions
                _recentSession.value = sessions.firstOrNull()
                
                // Calculate stats
                calculateStats(sessions)
                
                _isLoading.value = false
            }
        }
    }

    /**
     * Check for any active (incomplete) sleep session.
     */
    private fun checkActiveSession() {
        viewModelScope.launch {
            val active = sleepDao.getActiveSession()
            _activeSession.value = active
        }
    }

    /**
     * Calculate comprehensive sleep statistics.
     */
    private suspend fun calculateStats(sessions: List<SleepSession>) {
        if (sessions.isEmpty()) {
            _sleepStats.value = null
            return
        }

        val completedSessions = sessions.filter { it.isCompleted() }
        
        if (completedSessions.isEmpty()) {
            _sleepStats.value = null
            return
        }

        // Calculate averages
        val avgDuration = completedSessions.map { it.sleepDurationMinutes }.average().toFloat()
        val avgQuality = completedSessions.map { it.sleepQualityScore }.average().toFloat()
        val avgEfficiency = completedSessions.map { it.getSleepEfficiency() }.average().toFloat()

        // Smart Wake stats
        val smartWakeSessions = completedSessions.filter { it.smartWakeUsed }
        val totalSmartWakeUses = smartWakeSessions.size
        val smartWakeSuccesses = smartWakeSessions.count { it.wokeInSmartWindow }
        val successRate = if (totalSmartWakeUses > 0) {
            (smartWakeSuccesses.toFloat() / totalSmartWakeUses * 100)
        } else 0f

        // Weekly trend (last 7 days)
        val now = LocalDateTime.now()
        val weekAgo = now.minusDays(7)
        val weekStart = weekAgo.toEpochMillis()
        val weekEnd = now.toEpochMillis()

        val weeklySessions = completedSessions.filter { 
            it.startTime in weekStart..weekEnd 
        }.groupBy { 
            it.getStartDateTime().toLocalDate() 
        }

        val weeklyTrend = (0..6).map { daysAgo ->
            val date = now.minusDays(daysAgo.toLong()).toLocalDate()
            val daySessions = weeklySessions[date] ?: emptyList()
            
            if (daySessions.isNotEmpty()) {
                val session = daySessions.first()
                DailySleepData(
                    date = date.atStartOfDay(),
                    durationMinutes = session.sleepDurationMinutes,
                    qualityScore = session.sleepQualityScore,
                    efficiency = session.getSleepEfficiency()
                )
            } else {
                DailySleepData(
                    date = date.atStartOfDay(),
                    durationMinutes = 0,
                    qualityScore = 0f,
                    efficiency = 0f
                )
            }
        }.reversed()

        // Find best and worst nights
        val bestNight = completedSessions.maxByOrNull { it.sleepQualityScore }
        val worstNight = completedSessions.minByOrNull { it.sleepQualityScore }

        _sleepStats.value = SleepStats(
            totalSessions = completedSessions.size,
            averageDurationMinutes = avgDuration,
            averageQualityScore = avgQuality,
            averageEfficiency = avgEfficiency,
            totalSmartWakeUses = totalSmartWakeUses,
            smartWakeSuccessRate = successRate,
            weeklyTrend = weeklyTrend,
            bestNight = bestNight,
            worstNight = worstNight
        )
    }

    /**
     * Start a new manual sleep session.
     */
    fun startSleepSession(alarmId: String? = null) {
        viewModelScope.launch {
            val session = if (alarmId != null) {
                SleepSession.createForAlarm(alarmId)
            } else {
                SleepSession.createManual()
            }
            
            sleepDao.insertSession(session)
            _activeSession.value = session
        }
    }

    /**
     * End the active sleep session.
     */
    fun endSleepSession(
        smartWakeUsed: Boolean = false,
        wokeInWindow: Boolean = false,
        snoozeCount: Int = 0,
        missionCompleted: Boolean = false
    ) {
        viewModelScope.launch {
            val active = _activeSession.value ?: return@launch
            
            val endTime = System.currentTimeMillis()
            val durationMinutes = ((endTime - active.startTime) / 60000).toInt()
            
            // Estimate sleep phases (simplified)
            // In a real implementation, this would come from actual sleep phase detection
            val deepSleepMinutes = (durationMinutes * 0.25).toInt()  // ~25% deep sleep
            val lightSleepMinutes = (durationMinutes * 0.55).toInt()  // ~55% light sleep
            val awakeMinutes = durationMinutes - deepSleepMinutes - lightSleepMinutes
            
            // Calculate quality score based on duration and efficiency
            val optimalDuration = 480  // 8 hours in minutes
            val durationScore = (durationMinutes.toFloat() / optimalDuration * 50).coerceIn(0f, 50f)
            val efficiency = (deepSleepMinutes + lightSleepMinutes).toFloat() / durationMinutes * 100
            val efficiencyScore = (efficiency / 100 * 50).coerceIn(0f, 50f)
            val qualityScore = durationScore + efficiencyScore
            
            val updatedSession = active.copy(
                endTime = endTime,
                sleepDurationMinutes = durationMinutes,
                deepSleepMinutes = deepSleepMinutes,
                lightSleepMinutes = lightSleepMinutes,
                awakeMinutes = awakeMinutes,
                sleepQualityScore = qualityScore,
                smartWakeUsed = smartWakeUsed,
                smartWakeTriggered = smartWakeUsed,
                wokeInSmartWindow = wokeInWindow,
                snoozeCount = snoozeCount,
                missionCompleted = missionCompleted
            )
            
            sleepDao.updateSession(updatedSession)
            _activeSession.value = null
            
            // Reload data to update stats
            checkActiveSession()
        }
    }

    /**
     * Delete a sleep session.
     */
    fun deleteSession(session: SleepSession) {
        viewModelScope.launch {
            sleepDao.deleteSession(session)
        }
    }

    /**
     * Get sessions for a specific time range.
     */
    fun getSessionsInRange(startTime: Long, endTime: Long) = 
        sleepDao.getSessionsInRange(startTime, endTime)

    /**
     * Format duration in hours and minutes.
     */
    fun formatDuration(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) {
            "${hours}h ${mins}m"
        } else {
            "${mins}m"
        }
    }

    /**
     * Convert LocalDateTime to epoch milliseconds.
     */
    private fun LocalDateTime.toEpochMillis(): Long {
        return atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}

/**
 * UI state for sleep screen.
 */
data class SleepUiState(
    val sessions: List<SleepSession> = emptyList(),
    val recentSession: SleepSession? = null,
    val activeSession: SleepSession? = null,
    val stats: SleepStats? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
