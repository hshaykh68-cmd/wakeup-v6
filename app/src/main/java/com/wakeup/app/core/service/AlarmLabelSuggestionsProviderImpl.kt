package com.wakeup.app.core.service

import com.wakeup.app.domain.service.AlarmLabelSuggestionsProvider
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

enum class LabelCategory {
    WORK,
    HEALTH,
    TRAVEL,
    EDUCATION,
    EVENT,
    PERSONAL
}

data class SmartLabelSuggestion(
    val label: String,
    val category: LabelCategory,
    val icon: String,
    val confidence: Float
)

/**
 * Real implementation of AlarmLabelSuggestionsProvider.
 */
@Singleton
class AlarmLabelSuggestionsProviderImpl @Inject constructor() : AlarmLabelSuggestionsProvider {

    private val random = Random.Default

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

    private fun getTimeContext(hour: Int): LabelCategory {
        return when (hour) {
            in 4..6 -> LabelCategory.HEALTH
            in 7..8 -> LabelCategory.EDUCATION
            in 9..11 -> LabelCategory.WORK
            in 12..13 -> LabelCategory.PERSONAL
            in 14..17 -> LabelCategory.WORK
            in 18..20 -> LabelCategory.HEALTH
            else -> LabelCategory.PERSONAL
        }
    }

    override fun getSuggestionsForCurrentTime(): List<String> {
        val hour = LocalTime.now().hour
        return getSuggestionsForHour(hour)
    }

    override fun getSuggestionsForHour(hour: Int): List<String> {
        val timeContext = getTimeContext(hour)
        val prioritized = coreSuggestions.sortedByDescending { suggestion ->
            val categoryBonus = if (suggestion.category == timeContext) 0.3f else 0f
            suggestion.confidence + categoryBonus + random.nextFloat() * 0.1f
        }
        return prioritized.take(5).map { it.label }.shuffled(random)
    }

    override fun getAllSuggestions(): List<String> {
        return coreSuggestions.map { it.label }.shuffled(random)
    }
}
