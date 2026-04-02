package com.wakeup.app.domain.service

/**
 * Interface for alarm label suggestions provider.
 * Allows for testable implementations and dependency injection.
 */
interface AlarmLabelSuggestionsProvider {
    fun getSuggestionsForCurrentTime(): List<String>
    fun getSuggestionsForHour(hour: Int): List<String>
    fun getAllSuggestions(): List<String>
}
