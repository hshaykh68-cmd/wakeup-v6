package com.wakeup.app.domain.usecase

import com.wakeup.app.domain.model.UserStats
import com.wakeup.app.domain.model.WakeHistory
import com.wakeup.app.domain.repository.StatsRepository
import com.wakeup.app.domain.repository.WakeHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserStatsUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(): UserStats {
        return statsRepository.getUserStats()
    }
}

class GetStreakFlowUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    operator fun invoke(): Flow<Int> {
        return statsRepository.getStreakFlow()
    }
}

class RecordWakeAttemptUseCase @Inject constructor(
    private val wakeHistoryRepository: WakeHistoryRepository,
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(
        historyId: String,
        snoozeCount: Int,
        missionCompleted: Boolean,
        success: Boolean
    ) {
        wakeHistoryRepository.recordWakeAttempt(historyId, snoozeCount, missionCompleted, success)
        if (success) {
            statsRepository.updateStreak(true)
            if (missionCompleted) {
                statsRepository.incrementMissionCount()
            }
        } else {
            statsRepository.updateStreak(false)
        }
    }
}

class GetUserStatsFlowUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    operator fun invoke(): Flow<UserStats> {
        return statsRepository.getUserStatsFlow()
    }
}

class GetRecentHistoryUseCase @Inject constructor(
    private val wakeHistoryRepository: WakeHistoryRepository
) {
    operator fun invoke(days: Int? = null): Flow<List<WakeHistory>> {
        return wakeHistoryRepository.getRecentHistory(days)
    }
}
