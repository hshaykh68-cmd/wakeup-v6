package com.wakeup.app.core.util

import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Data class representing a morning quote with context
 */
data class MorningQuote(
    val text: String,
    val timeContext: String,
    val category: QuoteCategory
)

enum class QuoteCategory {
    EARLY_BIRD,
    PRODUCTIVITY,
    WELLNESS,
    MOTIVATION
}

/**
 * Helper class for generating dynamic morning quotes based on wake-up time.
 * Creates positive emotional peaks at the app's most critical moment.
 */
@Singleton
class MorningQuotesHelper @Inject constructor() {

    private val random = Random.Default

    // Early morning quotes (before 6 AM) - for the elite early risers
    private val earlyBirdQuotes = listOf(
        "The early bird gets the worm... and the peace.",
        "While others sleep, you conquer.",
        "Your future self is already proud of you.",
        "Legends wake up before the sun.",
        "You just joined the 5 AM Club.",
        "Success starts when the world is quiet.",
        "The city belongs to early risers.",
        "You've already won the day."
    )

    // Dawn quotes (6 AM - 7 AM) - the golden hour
    private val dawnQuotes = listOf(
        "A fresh start, a clean slate, a new beginning.",
        "Today is full of possibilities.",
        "Rise and shine, it's your time.",
        "Another day to be amazing.",
        "The best time to start is now.",
        "Your potential is limitless today.",
        "Make today count.",
        "Good morning, champion."
    )

    // Morning quotes (7 AM - 9 AM) - productive morning
    private val morningQuotes = listOf(
        "Fuel your ambition today.",
        "Small steps lead to big changes.",
        "You've got this, and you've got time.",
        "Make progress, not excuses.",
        "Your goals are waiting.",
        "Consistency beats intensity.",
        "One day closer to your dreams.",
        "Discipline creates freedom."
    )

    // Late morning quotes (9 AM+) - still commendable
    private val lateMorningQuotes = listOf(
        "Better late than never, better now than later.",
        "Every wake-up is a victory.",
        "Your commitment shows in your actions.",
        "Progress, not perfection.",
        "You're building habits that last.",
        "Showing up is half the battle.",
        "Consistency is your superpower.",
        "Keep the momentum going."
    )

    // Fun statistics based on wake time
    private fun getStatisticForTime(hour: Int, minute: Int): String {
        return when {
            hour < 5 -> "Only 2% of people are awake right now. You're exceptional."
            hour < 6 -> "Only 5% of people are awake. You're among the elite."
            hour < 7 -> "Only 10% of people are awake. You're ahead of the curve."
            hour < 8 -> "Only 25% of people are awake. You're in the productive minority."
            hour < 9 -> "The world is waking up. You're already ahead."
            else -> "Every wake-up is a win. Keep building that streak."
        }
    }

    /**
     * Get a dynamic morning quote based on the current time.
     * Returns a unique combination of motivational quote + time-based statistic.
     */
    fun getQuoteForCurrentTime(): MorningQuote {
        val now = LocalTime.now()
        return getQuoteForTime(now.hour, now.minute)
    }

    /**
     * Get a dynamic morning quote for a specific time.
     */
    fun getQuoteForTime(hour: Int, minute: Int): MorningQuote {
        val timeString = String.format("%d:%02d %s",
            if (hour == 0) 12 else if (hour > 12) hour - 12 else hour,
            minute,
            if (hour >= 12) "PM" else "AM"
        )

        val (quote, category) = when {
            hour < 6 -> {
                earlyBirdQuotes.random(random) to QuoteCategory.EARLY_BIRD
            }
            hour < 7 -> {
                dawnQuotes.random(random) to QuoteCategory.WELLNESS
            }
            hour < 9 -> {
                morningQuotes.random(random) to QuoteCategory.PRODUCTIVITY
            }
            else -> {
                lateMorningQuotes.random(random) to QuoteCategory.MOTIVATION
            }
        }

        val statistic = getStatisticForTime(hour, minute)
        val timeContext = "$timeString — $statistic"

        return MorningQuote(
            text = quote,
            timeContext = timeContext,
            category = category
        )
    }

    /**
     * Get a streak-based motivational message.
     */
    fun getStreakQuote(streak: Int): String {
        return when {
            streak >= 100 -> "100+ days! You're unstoppable! 🏆"
            streak >= 30 -> "$streak days! You've built an unbreakable habit! 🔥"
            streak >= 14 -> "$streak days! Your consistency is inspiring! 💪"
            streak >= 7 -> "One week strong! Keep the momentum! ⭐"
            streak >= 3 -> "$streak day streak! You're building something great! ✨"
            streak == 1 -> "Day 1! Every journey starts with a single step. 🌟"
            else -> "Let's start fresh today! 🌅"
        }
    }
}
