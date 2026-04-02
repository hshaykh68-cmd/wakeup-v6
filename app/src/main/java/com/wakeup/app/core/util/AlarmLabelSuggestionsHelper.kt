package com.wakeup.app.core.util

import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Data class representing a smart label suggestion
 */
data class SmartLabelSuggestion(
    val label: String,
    val category: LabelCategory,
    val icon: String,
    val confidence: Float
)

enum class LabelCategory {
    WORK,
    HEALTH,
    TRAVEL,
    EDUCATION,
    EVENT,
    PERSONAL
}

/**
 * Helper class for smart alarm label suggestions.
 * Provides contextual suggestions based on time of day and user patterns.
 */
@Singleton
class AlarmLabelSuggestionsHelper @Inject constructor() {

    private val random = Random.Default

    // Core suggestion pool with metadata
    private val coreSuggestions = listOf(
        SmartLabelSuggestion("Work", LabelCategory.WORK, "work", 0.9f),
        SmartLabelSuggestion("Gym", LabelCategory.HEALTH, "fitness_center", 0.8f),
        SmartLabelSuggestion("Flight", LabelCategory.TRAVEL, "flight", 0.7f),
        SmartLabelSuggestion("School", LabelCategory.EDUCATION, "school", 0.8f),
        SmartLabelSuggestion("Meeting", LabelCategory.WORK, "event", 0.85f),
        SmartLabelSuggestion("Study", LabelCategory.EDUCATION, "menu_book", 0.75f),
        SmartLabelSuggestion("Run", LabelCategory.HEALTH, "directions_run", 0.7f),
        SmartLabelSuggestion("Yoga", LabelCategory.HEALTH, "self_improvement", 0.65f),
        SmartLabelSuggestion("Breakfast", LabelCategory.PERSONAL, "restaurant", 0.6f),
        SmartLabelSuggestion("Meditation", LabelCategory.HEALTH, "spa", 0.6f),
        SmartLabelSuggestion("Interview", LabelCategory.EVENT, "business", 0.75f),
        SmartLabelSuggestion("Doctor", LabelCategory.HEALTH, "local_hospital", 0.7f),
        SmartLabelSuggestion("Date", LabelCategory.PERSONAL, "favorite", 0.65f),
        SmartLabelSuggestion("Presentation", LabelCategory.WORK, "present_to_all", 0.8f),
        SmartLabelSuggestion("Class", LabelCategory.EDUCATION, "class", 0.75f)
    )

    // Time-based context for smarter suggestions
    private fun getTimeContext(hour: Int): LabelCategory {
        return when (hour) {
            in 4..6 -> LabelCategory.HEALTH  // Early risers - gym, run, yoga
            in 7..8 -> LabelCategory.EDUCATION  // School time
            in 9..11 -> LabelCategory.WORK  // Work day starts
            in 12..13 -> LabelCategory.PERSONAL  // Lunch/appointments
            in 14..17 -> LabelCategory.WORK  // Afternoon work
            in 18..20 -> LabelCategory.HEALTH  // Evening workout
            else -> LabelCategory.PERSONAL
        }
    }

    /**
     * Get smart label suggestions for the current time.
     * Returns 4-5 contextual suggestions with primary ones first.
     */
    fun getSuggestionsForCurrentTime(): List<String> {
        val hour = LocalTime.now().hour
        return getSuggestionsForHour(hour)
    }

    /**
     * Get smart label suggestions for a specific hour.
     */
    fun getSuggestionsForHour(hour: Int): List<String> {
        val timeContext = getTimeContext(hour)

        // Prioritize based on time context
        val prioritized = coreSuggestions.sortedByDescending { suggestion ->
            val categoryBonus = if (suggestion.category == timeContext) 0.3f else 0f
            suggestion.confidence + categoryBonus + random.nextFloat() * 0.1f
        }

        // Return top 5, shuffled slightly for variety
        return prioritized.take(5).map { it.label }.shuffled(random)
    }

    /**
     * Get all available suggestions (for full suggestion list).
     */
    fun getAllSuggestions(): List<String> {
        return coreSuggestions.map { it.label }.shuffled(random)
    }

    /**
     * Get suggestions by category.
     */
    fun getSuggestionsByCategory(category: LabelCategory): List<String> {
        return coreSuggestions
            .filter { it.category == category }
            .map { it.label }
    }

    /**
     * Search suggestions by query.
     */
    fun searchSuggestions(query: String): List<String> {
        if (query.isBlank()) return getAllSuggestions().take(6)

        return coreSuggestions
            .filter { it.label.contains(query, ignoreCase = true) }
            .sortedByDescending { it.confidence }
            .map { it.label }
            .take(5)
    }

    /**
     * Get suggestions formatted with their categories for UI display.
     */
    fun getSuggestionsWithCategories(): List<Pair<String, LabelCategory>> {
        val hour = LocalTime.now().hour
        val timeContext = getTimeContext(hour)

        return coreSuggestions
            .sortedByDescending { suggestion ->
                val categoryBonus = if (suggestion.category == timeContext) 0.3f else 0f
                suggestion.confidence + categoryBonus
            }
            .take(5)
            .map { it.label to it.category }
    }
}
