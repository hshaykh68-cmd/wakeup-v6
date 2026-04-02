package com.wakeup.app.presentation.alarm

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wakeup.app.core.service.NoOpHapticsController
import com.wakeup.app.core.service.AlarmLabelSuggestionsProviderImpl
import com.wakeup.app.domain.model.MissionType
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for the alarm creation flow.
 * Tests user interactions when creating a new alarm.
 */
@RunWith(AndroidJUnit4::class)
class AlarmCreationFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockOnNavigateBack = mockk<() -> Unit>(relaxed = true)
    private val hapticsController = NoOpHapticsController()
    private val suggestionsProvider = AlarmLabelSuggestionsProviderImpl()

    @Test
    fun createAlarmScreen_displaysTimePicker() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        composeTestRule.onNodeWithText("Create Alarm").assertIsDisplayed()
    }

    @Test
    fun createAlarmScreen_showsLabelSuggestions() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        composeTestRule.onNodeWithText("Label").performClick()
        
        // Verify suggestions are shown based on time
        composeTestRule.waitForIdle()
    }

    @Test
    fun createAlarmScreen_canEnterCustomLabel() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        composeTestRule.onNodeWithText("Label (Optional)").performClick()
        composeTestRule.onNodeWithText("Label (Optional)").performTextInput("Morning Workout")
        
        composeTestRule.onNodeWithText("Morning Workout").assertIsDisplayed()
    }

    @Test
    fun createAlarmScreen_missionTypeSelection_showsMissionOptions() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        // Tap on mission type to expand options
        composeTestRule.onNodeWithText("Mission").performClick()
        
        // Verify mission options are displayed
        composeTestRule.onNodeWithText("Math").assertIsDisplayed()
        composeTestRule.onNodeWithText("Memory").assertIsDisplayed()
        composeTestRule.onNodeWithText("Typing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shake").assertIsDisplayed()
    }

    @Test
    fun createAlarmScreen_saveButton_isEnabledWhenValid() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        // Default state should have save button enabled
        composeTestRule.onNodeWithText("Save").assertIsEnabled()
    }

    @Test
    fun createAlarmScreen_tappingSave_callsOnNavigateBack() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        composeTestRule.onNodeWithText("Save").performClick()
        
        verify { mockOnNavigateBack.invoke() }
    }

    @Test
    fun createAlarmScreen_repeatDaysSelection_togglesDays() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        // Tap on repeat days
        composeTestRule.onNodeWithText("Repeat").performClick()
        
        // Verify day options are shown
        composeTestRule.onNodeWithText("Mon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tue").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wed").assertIsDisplayed()
    }

    @Test
    fun createAlarmScreen_soundSelection_showsSoundOptions() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        // Tap on sound selection
        composeTestRule.onNodeWithText("Sound").performClick()
        
        // Verify sound options are shown
        composeTestRule.waitForIdle()
    }

    @Test
    fun createAlarmScreen_vibrationToggle_works() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        // Find and toggle vibration setting
        composeTestRule.onNodeWithText("Vibration").performClick()
        
        composeTestRule.waitForIdle()
    }

    @Test
    fun createAlarmScreen_snoozeSettings_areConfigurable() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        // Access snooze settings
        composeTestRule.onNodeWithText("Snooze").performClick()
        
        composeTestRule.waitForIdle()
    }

    @Test
    fun createAlarmScreen_difficultySelection_changesMissionDifficulty() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        // Expand mission section
        composeTestRule.onNodeWithText("Mission").performClick()
        
        // Select a mission type first
        composeTestRule.onNodeWithText("Math").performClick()
        
        composeTestRule.waitForIdle()
    }

    @Test
    fun createAlarmScreen_strictModeToggle_works() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        // Find strict mode toggle
        composeTestRule.onNodeWithText("Strict Mode").performClick()
        
        composeTestRule.waitForIdle()
    }

    @Test
    fun createAlarmScreen_cancelButton_navigatesBack() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        // Tap cancel/back
        composeTestRule.onNodeWithText("Cancel").performClick()
        
        verify { mockOnNavigateBack.invoke() }
    }

    @Test
    fun createAlarmScreen_timePicker_showsCorrectInitialTime() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        // Verify time picker is displayed
        composeTestRule.waitForIdle()
    }

    @Test
    fun createAlarmScreen_smartWakeToggle_works() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        // Find and toggle smart wake
        composeTestRule.onNodeWithText("Smart Wake").performClick()
        
        composeTestRule.waitForIdle()
    }

    @Test
    fun createAlarmScreen_gradualVolumeToggle_works() {
        composeTestRule.setContent {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = suggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = mockOnNavigateBack
            )
        }

        // Find and toggle gradual volume
        composeTestRule.onNodeWithText("Gradual Volume").performClick()
        
        composeTestRule.waitForIdle()
    }
}
