package com.wakeup.app.core.util

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class representing a streak milestone
 */
data class StreakMilestone(
    val days: Int,
    val title: String,
    val message: String,
    val isMajor: Boolean
)

/**
 * Helper class for managing streak milestones and celebrations.
 * Detects when users hit 3, 7, 14, 30, 100 day streaks and triggers celebrations.
 */
@Singleton
class StreakMilestoneHelper @Inject constructor() {

    companion object {
        val MILESTONE_DAYS = listOf(3, 7, 14, 30, 100)
    }

    private val milestones = mapOf(
        3 to StreakMilestone(
            days = 3,
            title = "3-Day Streak!",
            message = "You've woken up on time for 3 days straight. Great start!",
            isMajor = false
        ),
        7 to StreakMilestone(
            days = 7,
            title = "One Week Strong!",
            message = "You've woken up on time for 7 days straight. That's a week of consistency!",
            isMajor = true
        ),
        14 to StreakMilestone(
            days = 14,
            title = "Two Week Champion!",
            message = "You've woken up on time for 14 days straight. Your habit is forming!",
            isMajor = true
        ),
        30 to StreakMilestone(
            days = 30,
            title = "30-Day Legend!",
            message = "You've woken up on time for 30 days straight. A full month of discipline!",
            isMajor = true
        ),
        100 to StreakMilestone(
            days = 100,
            title = "100-Day Warrior!",
            message = "You've woken up on time for 100 days straight. You're unstoppable!",
            isMajor = true
        )
    )

    /**
     * Check if the current streak hits a milestone.
     * @return The milestone if hit, null otherwise
     */
    fun checkMilestone(currentStreak: Int): StreakMilestone? {
        return milestones[currentStreak]
    }

    /**
     * Check if the current streak is a milestone day.
     */
    fun isMilestoneDay(streak: Int): Boolean {
        return MILESTONE_DAYS.contains(streak)
    }

    /**
     * Get the next milestone for a given streak.
     */
    fun getNextMilestone(currentStreak: Int): StreakMilestone? {
        return MILESTONE_DAYS
            .filter { it > currentStreak }
            .minOrNull()
            ?.let { milestones[it] }
    }

    /**
     * Get progress toward next milestone as a percentage.
     */
    fun getProgressToNextMilestone(currentStreak: Int): Float {
        val nextMilestoneDays = MILESTONE_DAYS.find { it > currentStreak } ?: return 1f
        val prevMilestoneDays = MILESTONE_DAYS.findLast { it <= currentStreak } ?: 0
        val range = nextMilestoneDays - prevMilestoneDays
        val progress = currentStreak - prevMilestoneDays
        return progress.toFloat() / range
    }

    /**
     * Get notification content for a milestone.
     */
    fun getNotificationContent(milestone: StreakMilestone): Pair<String, String> {
        return milestone.title to milestone.message
    }

    /**
     * Get celebration animation resource based on milestone level.
     * Returns animation name that should exist in raw resources.
     */
    fun getCelebrationAnimation(milestone: StreakMilestone): String {
        return when {
            milestone.days >= 100 -> "celebration_gold"
            milestone.days >= 30 -> "celebration_purple"
            milestone.days >= 14 -> "celebration_blue"
            milestone.days >= 7 -> "celebration_green"
            else -> "celebration_simple"
        }
    }

    /**
     * Get all milestone messages for sharing.
     */
    fun getShareMessage(milestone: StreakMilestone): String {
        return "🏆 I just hit a ${milestone.days}-day wake-up streak with WakeUp! " +
               "Consistency is key. #WakeUpApp #MorningRoutine #${milestone.days}Days"
    }
}
