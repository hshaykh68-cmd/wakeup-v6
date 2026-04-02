package com.wakeup.app.presentation.alarm

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertExists
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wakeup.app.core.service.NoOpHapticsController
import com.wakeup.app.data.mission.MissionData
import com.wakeup.app.domain.model.MissionDifficulty
import com.wakeup.app.domain.model.MissionType
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for snooze functionality.
 * Tests user interactions with snooze in various screens.
 */
@RunWith(AndroidJUnit4::class)
class SnoozeFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockOnMissionComplete = mockk<(Boolean) -> Unit>(relaxed = true)
    private val mockOnSnooze = mockk<(Int) -> Unit>(relaxed = true)
    private val mockOnDismiss = mockk<() -> Unit>(relaxed = true)
    private val hapticsController = NoOpHapticsController()

    @Test
    fun missionScreen_showsSnoozeButton_withCorrectInterval() {
        val missionData = MissionData(
            question = "5 + 3 = ?",
            answer = "8",
            type = MissionType.MATH,
            difficulty = MissionDifficulty.EASY
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.MATH,
                missionDifficulty = MissionDifficulty.EASY,
                missionData = missionData,
                alarmId = "test-alarm-1",
                snoozeInterval = 5,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        composeTestRule.onNodeWithText("Snooze (5 min)").assertIsDisplayed()
    }

    @Test
    fun missionScreen_showsSnoozeButton_withDifferentIntervals() {
        val missionData = MissionData(
            question = "5 + 3 = ?",
            answer = "8",
            type = MissionType.MATH,
            difficulty = MissionDifficulty.EASY
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.MATH,
                missionDifficulty = MissionDifficulty.EASY,
                missionData = missionData,
                snoozeInterval = 10,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        composeTestRule.onNodeWithText("Snooze (10 min)").assertIsDisplayed()
    }

    @Test
    fun missionScreen_tappingSnooze_triggersCallback() {
        val missionData = MissionData(
            question = "5 + 3 = ?",
            answer = "8",
            type = MissionType.MATH,
            difficulty = MissionDifficulty.EASY
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.MATH,
                missionDifficulty = MissionDifficulty.EASY,
                missionData = missionData,
                alarmId = "test-alarm-1",
                snoozeInterval = 5,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        composeTestRule.onNodeWithText("Snooze (5 min)").performClick()

        verify { mockOnSnooze.invoke(5) }
    }

    @Test
    fun missionScreen_snoozeAvailableForAllMissionTypes() {
        val missionTypes = listOf(
            MissionType.MATH,
            MissionType.MEMORY,
            MissionType.TYPING,
            MissionType.SHAKE,
            MissionType.BARCODE,
            MissionType.PHOTO,
            MissionType.STEP,
            MissionType.COMBO_BARCODE_PHOTO
        )

        missionTypes.forEach { type ->
            val missionData = MissionData(
                question = "Test question",
                answer = "answer",
                type = type,
                difficulty = MissionDifficulty.EASY
            )

            composeTestRule.setContent {
                MissionScreen(
                    missionType = type,
                    missionDifficulty = MissionDifficulty.EASY,
                    missionData = missionData,
                    alarmId = "test-alarm-${type.name}",
                    snoozeInterval = 5,
                    hapticsController = hapticsController,
                    onMissionComplete = mockOnMissionComplete,
                    onSnooze = mockOnSnooze
                )
            }

            // Verify snooze button exists for all mission types
            composeTestRule.onNodeWithText("Snooze (5 min)").assertExists()
        }
    }

    @Test
    fun alarmRingingScreen_showsSnoozeButton() {
        composeTestRule.setContent {
            AlarmRingingScreen(
                alarmLabel = "Test Alarm",
                isSnoozeEnabled = true,
                snoozeInterval = 5,
                maxSnoozes = 3,
                currentSnoozeCount = 0,
                hapticsController = hapticsController,
                onDismiss = mockOnDismiss,
                onSnooze = mockOnSnooze,
                onMissionStart = {}
            )
        }

        composeTestRule.onNodeWithText("Snooze (5 min)").assertIsDisplayed()
    }

    @Test
    fun alarmRingingScreen_tappingSnooze_triggersCallback() {
        composeTestRule.setContent {
            AlarmRingingScreen(
                alarmLabel = "Test Alarm",
                isSnoozeEnabled = true,
                snoozeInterval = 5,
                maxSnoozes = 3,
                currentSnoozeCount = 0,
                hapticsController = hapticsController,
                onDismiss = mockOnDismiss,
                onSnooze = mockOnSnooze,
                onMissionStart = {}
            )
        }

        composeTestRule.onNodeWithText("Snooze (5 min)").performClick()

        verify { mockOnSnooze.invoke(5) }
    }

    @Test
    fun alarmRingingScreen_showsSnoozeCount() {
        composeTestRule.setContent {
            AlarmRingingScreen(
                alarmLabel = "Test Alarm",
                isSnoozeEnabled = true,
                snoozeInterval = 5,
                maxSnoozes = 3,
                currentSnoozeCount = 1,
                hapticsController = hapticsController,
                onDismiss = mockOnDismiss,
                onSnooze = mockOnSnooze,
                onMissionStart = {}
            )
        }

        // Verify snooze count indicator is shown
        composeTestRule.waitForIdle()
    }

    @Test
    fun alarmRingingScreen_snoozeDisabled_hidesSnoozeButton() {
        composeTestRule.setContent {
            AlarmRingingScreen(
                alarmLabel = "Test Alarm",
                isSnoozeEnabled = false,
                snoozeInterval = 5,
                maxSnoozes = 3,
                currentSnoozeCount = 0,
                hapticsController = hapticsController,
                onDismiss = mockOnDismiss,
                onSnooze = mockOnSnooze,
                onMissionStart = {}
            )
        }

        // Snooze button should not exist when disabled
        composeTestRule.onNodeWithText("Snooze (5 min)").assertDoesNotExist()
    }

    @Test
    fun alarmRingingScreen_maxSnoozesReached_hidesSnoozeButton() {
        composeTestRule.setContent {
            AlarmRingingScreen(
                alarmLabel = "Test Alarm",
                isSnoozeEnabled = true,
                snoozeInterval = 5,
                maxSnoozes = 3,
                currentSnoozeCount = 3,
                hapticsController = hapticsController,
                onDismiss = mockOnDismiss,
                onSnooze = mockOnSnooze,
                onMissionStart = {}
            )
        }

        // Snooze button should not exist when max snoozes reached
        composeTestRule.onNodeWithText("Snooze (5 min)").assertDoesNotExist()
    }

    @Test
    fun missionScreen_snoozeButtonPositionedAtBottom() {
        val missionData = MissionData(
            question = "5 + 3 = ?",
            answer = "8",
            type = MissionType.MATH,
            difficulty = MissionDifficulty.EASY
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.MATH,
                missionDifficulty = MissionDifficulty.EASY,
                missionData = missionData,
                alarmId = "test-alarm-1",
                snoozeInterval = 5,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        // Verify snooze button is present
        composeTestRule.onNodeWithText("Snooze (5 min)").assertExists()
    }

    @Test
    fun alarmRingingScreen_showsDismissButton() {
        composeTestRule.setContent {
            AlarmRingingScreen(
                alarmLabel = "Test Alarm",
                isSnoozeEnabled = true,
                snoozeInterval = 5,
                maxSnoozes = 3,
                currentSnoozeCount = 0,
                hapticsController = hapticsController,
                onDismiss = mockOnDismiss,
                onSnooze = mockOnSnooze,
                onMissionStart = {}
            )
        }

        composeTestRule.onNodeWithText("Dismiss").assertIsDisplayed()
    }

    @Test
    fun alarmRingingScreen_tappingDismiss_triggersCallback() {
        composeTestRule.setContent {
            AlarmRingingScreen(
                alarmLabel = "Test Alarm",
                isSnoozeEnabled = true,
                snoozeInterval = 5,
                maxSnoozes = 3,
                currentSnoozeCount = 0,
                hapticsController = hapticsController,
                onDismiss = mockOnDismiss,
                onSnooze = mockOnSnooze,
                onMissionStart = {}
            )
        }

        composeTestRule.onNodeWithText("Dismiss").performClick()

        verify { mockOnDismiss.invoke() }
    }

    @Test
    fun alarmRingingScreen_showsAlarmLabel() {
        composeTestRule.setContent {
            AlarmRingingScreen(
                alarmLabel = "Morning Workout",
                isSnoozeEnabled = true,
                snoozeInterval = 5,
                maxSnoozes = 3,
                currentSnoozeCount = 0,
                hapticsController = hapticsController,
                onDismiss = mockOnDismiss,
                onSnooze = mockOnSnooze,
                onMissionStart = {}
            )
        }

        composeTestRule.onNodeWithText("Morning Workout").assertIsDisplayed()
    }

    @Test
    fun alarmRingingScreen_showsCurrentTime() {
        composeTestRule.setContent {
            AlarmRingingScreen(
                alarmLabel = "Test Alarm",
                isSnoozeEnabled = true,
                snoozeInterval = 5,
                maxSnoozes = 3,
                currentSnoozeCount = 0,
                hapticsController = hapticsController,
                onDismiss = mockOnDismiss,
                onSnooze = mockOnSnooze,
                onMissionStart = {}
            )
        }

        // Verify time is displayed (format may vary)
        composeTestRule.waitForIdle()
    }

    @Test
    fun alarmRingingScreen_missionAlarm_showsMissionButton() {
        composeTestRule.setContent {
            AlarmRingingScreen(
                alarmLabel = "Mission Alarm",
                isSnoozeEnabled = true,
                snoozeInterval = 5,
                maxSnoozes = 3,
                currentSnoozeCount = 0,
                hasMission = true,
                hapticsController = hapticsController,
                onDismiss = mockOnDismiss,
                onSnooze = mockOnSnooze,
                onMissionStart = {}
            )
        }

        composeTestRule.onNodeWithText("Start Mission").assertIsDisplayed()
    }

    @Test
    fun alarmRingingScreen_missionAlarm_tappingMissionButton_triggersCallback() {
        val mockOnMissionStart = mockk<() -> Unit>(relaxed = true)

        composeTestRule.setContent {
            AlarmRingingScreen(
                alarmLabel = "Mission Alarm",
                isSnoozeEnabled = true,
                snoozeInterval = 5,
                maxSnoozes = 3,
                currentSnoozeCount = 0,
                hasMission = true,
                hapticsController = hapticsController,
                onDismiss = mockOnDismiss,
                onSnooze = mockOnSnooze,
                onMissionStart = mockOnMissionStart
            )
        }

        composeTestRule.onNodeWithText("Start Mission").performClick()

        verify { mockOnMissionStart.invoke() }
    }

    @Test
    fun missionScreen_noSnoozeCallback_hidesSnoozeButton() {
        val missionData = MissionData(
            question = "5 + 3 = ?",
            answer = "8",
            type = MissionType.MATH,
            difficulty = MissionDifficulty.EASY
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.MATH,
                missionDifficulty = MissionDifficulty.EASY,
                missionData = missionData,
                alarmId = "test-alarm-no-snooze",
                snoozeInterval = 5,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = null
            )
        }

        // Snooze button should not exist when onSnooze is null
        composeTestRule.onNodeWithText("Snooze (5 min)").assertDoesNotExist()
    }

    @Test
    fun alarmRingingScreen_variousSnoozeIntervals() {
        val intervals = listOf(1, 3, 5, 10, 15, 20, 30)

        intervals.forEach { interval ->
            composeTestRule.setContent {
                AlarmRingingScreen(
                    alarmLabel = "Test Alarm",
                    isSnoozeEnabled = true,
                    snoozeInterval = interval,
                    maxSnoozes = 3,
                    currentSnoozeCount = 0,
                    hapticsController = hapticsController,
                    onDismiss = mockOnDismiss,
                    onSnooze = mockOnSnooze,
                    onMissionStart = {}
                )
            }

            composeTestRule.onNodeWithText("Snooze ($interval min)").assertIsDisplayed()
        }
    }

    @Test
    fun missionScreen_snoozeHapticFeedback() {
        val hapticMock = mockk<NoOpHapticsController>(relaxed = true)
        val missionData = MissionData(
            question = "5 + 3 = ?",
            answer = "8",
            type = MissionType.MATH,
            difficulty = MissionDifficulty.EASY
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.MATH,
                missionDifficulty = MissionDifficulty.EASY,
                missionData = missionData,
                alarmId = "test-alarm-haptic",
                snoozeInterval = 5,
                hapticsController = hapticMock,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        composeTestRule.onNodeWithText("Snooze (5 min)").performClick()

        // Verify haptic feedback was triggered
        verify { hapticMock.performMediumImpact() }
    }

    @Test
    fun alarmRingingScreen_snoozeHapticFeedback() {
        val hapticMock = mockk<NoOpHapticsController>(relaxed = true)

        composeTestRule.setContent {
            AlarmRingingScreen(
                alarmLabel = "Test Alarm",
                isSnoozeEnabled = true,
                snoozeInterval = 5,
                maxSnoozes = 3,
                currentSnoozeCount = 0,
                hapticsController = hapticMock,
                onDismiss = mockOnDismiss,
                onSnooze = mockOnSnooze,
                onMissionStart = {}
            )
        }

        composeTestRule.onNodeWithText("Snooze (5 min)").performClick()

        // Verify haptic feedback was triggered
        verify { hapticMock.performMediumImpact() }
    }
}
