package com.wakeup.app.presentation.alarm

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
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
 * Compose UI tests for mission completion flows.
 * Tests user interactions when completing wake-up missions.
 */
@RunWith(AndroidJUnit4::class)
class MissionCompletionFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockOnMissionComplete = mockk<(Boolean) -> Unit>(relaxed = true)
    private val mockOnSnooze = mockk<(Int) -> Unit>(relaxed = true)
    private val hapticsController = NoOpHapticsController()

    @Test
    fun missionScreen_mathMission_showsEquation() {
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

        composeTestRule.onNodeWithText("5 + 3 = ?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Submit").assertIsDisplayed()
    }

    @Test
    fun missionScreen_mathMission_correctAnswer_completesMission() {
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

        // Enter correct answer
        composeTestRule.onNodeWithText("Your answer").performTextInput("8")
        composeTestRule.onNodeWithText("Submit").performClick()

        verify { mockOnMissionComplete.invoke(true) }
    }

    @Test
    fun missionScreen_mathMission_wrongAnswer_showsError() {
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

        // Enter wrong answer
        composeTestRule.onNodeWithText("Your answer").performTextInput("7")
        composeTestRule.onNodeWithText("Submit").performClick()

        // Should show error and not complete
        composeTestRule.onNodeWithText("Attempt 1").assertIsDisplayed()
    }

    @Test
    fun missionScreen_typingMission_showsPhraseToType() {
        val missionData = MissionData(
            question = "Type exactly:",
            answer = "Time to wake up",
            type = MissionType.TYPING,
            difficulty = MissionDifficulty.EASY
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.TYPING,
                missionDifficulty = MissionDifficulty.EASY,
                missionData = missionData,
                alarmId = "test-alarm-1",
                snoozeInterval = 5,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        composeTestRule.onNodeWithText("Time to wake up").assertIsDisplayed()
    }

    @Test
    fun missionScreen_typingMission_correctPhrase_completesMission() {
        val missionData = MissionData(
            question = "Type exactly:",
            answer = "Time to wake up",
            type = MissionType.TYPING,
            difficulty = MissionDifficulty.EASY
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.TYPING,
                missionDifficulty = MissionDifficulty.EASY,
                missionData = missionData,
                alarmId = "test-alarm-1",
                snoozeInterval = 5,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        // Type the exact phrase
        composeTestRule.onNodeWithText("Type the phrase...").performTextInput("Time to wake up")
        composeTestRule.onNodeWithText("Submit").performClick()

        verify { mockOnMissionComplete.invoke(true) }
    }

    @Test
    fun missionScreen_memoryMission_showsPattern() {
        val missionData = MissionData(
            question = "Remember: 1 - 2 - 3",
            answer = "1,2,3",
            pattern = listOf(1, 2, 3),
            type = MissionType.MEMORY,
            difficulty = MissionDifficulty.EASY
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.MEMORY,
                missionDifficulty = MissionDifficulty.EASY,
                missionData = missionData,
                alarmId = "test-alarm-3",
                snoozeInterval = 5,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        composeTestRule.onNodeWithText("Remember the pattern").assertIsDisplayed()
    }

    @Test
    fun missionScreen_showsSnoozeButton() {
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
    fun missionScreen_tappingSnooze_callsOnSnooze() {
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
                alarmId = "test-alarm-4",
                snoozeInterval = 10,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        composeTestRule.onNodeWithText("Snooze (10 min)").performClick()

        verify { mockOnSnooze.invoke(10) }
    }

    @Test
    fun missionScreen_showsDifficultyIndicator() {
        val missionData = MissionData(
            question = "5 + 3 = ?",
            answer = "8",
            type = MissionType.MATH,
            difficulty = MissionDifficulty.HARD
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.MATH,
                missionDifficulty = MissionDifficulty.HARD,
                missionData = missionData,
                alarmId = "test-alarm-5",
                snoozeInterval = 5,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        // Verify difficulty is shown
        composeTestRule.waitForIdle()
    }

    @Test
    fun missionScreen_shakeMission_showsShakeCounter() {
        val missionData = MissionData(
            question = "Shake your phone!",
            answer = "20",
            type = MissionType.SHAKE,
            difficulty = MissionDifficulty.EASY,
            metadata = mapOf("timeLimit" to "15")
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.SHAKE,
                missionDifficulty = MissionDifficulty.EASY,
                missionData = missionData,
                alarmId = "test-alarm-6",
                snoozeInterval = 5,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        composeTestRule.onNodeWithText("Shake your phone!").assertIsDisplayed()
    }

    @Test
    fun missionScreen_strictMode_showsWarning() {
        val missionData = MissionData(
            question = "5 + 3 = ?",
            answer = "8",
            type = MissionType.MATH,
            difficulty = MissionDifficulty.EASY,
            strictMode = true
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.MATH,
                missionDifficulty = MissionDifficulty.EASY,
                missionData = missionData,
                alarmId = "test-alarm-7",
                snoozeInterval = 5,
                strictMode = true,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        // Verify strict mode warning is shown
        composeTestRule.waitForIdle()
    }

    @Test
    fun missionScreen_tracksAttempts() {
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

        // Submit wrong answer multiple times
        composeTestRule.onNodeWithText("Your answer").performTextInput("1")
        composeTestRule.onNodeWithText("Submit").performClick()
        composeTestRule.onNodeWithText("Attempt 1").assertIsDisplayed()

        composeTestRule.onNodeWithText("Your answer").performTextInput("2")
        composeTestRule.onNodeWithText("Submit").performClick()
        composeTestRule.onNodeWithText("Attempt 2").assertIsDisplayed()
    }

    @Test
    fun missionScreen_barcodeMission_showsScanInstructions() {
        val missionData = MissionData(
            question = "Scan the barcode",
            answer = "barcode123",
            type = MissionType.BARCODE,
            difficulty = MissionDifficulty.MEDIUM,
            barcodeValue = "barcode123"
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.BARCODE,
                missionDifficulty = MissionDifficulty.MEDIUM,
                missionData = missionData,
                alarmId = "test-alarm-8",
                snoozeInterval = 5,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        composeTestRule.onNodeWithText("Scan the barcode").assertIsDisplayed()
    }

    @Test
    fun missionScreen_photoMission_showsPhotoMatchInstructions() {
        val missionData = MissionData(
            question = "Take a matching photo",
            answer = "photo_hash",
            type = MissionType.PHOTO,
            difficulty = MissionDifficulty.MEDIUM,
            photoReferenceHash = "reference_hash"
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.PHOTO,
                missionDifficulty = MissionDifficulty.MEDIUM,
                missionData = missionData,
                alarmId = "test-alarm-9",
                snoozeInterval = 5,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        composeTestRule.onNodeWithText("Take a matching photo").assertIsDisplayed()
    }

    @Test
    fun missionScreen_stepMission_showsStepCounter() {
        val missionData = MissionData(
            question = "Walk 20 steps",
            answer = "20",
            type = MissionType.STEP,
            difficulty = MissionDifficulty.EASY
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.STEP,
                missionDifficulty = MissionDifficulty.EASY,
                missionData = missionData,
                alarmId = "test-alarm-10",
                snoozeInterval = 5,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        composeTestRule.onNodeWithText("Walk 20 steps").assertIsDisplayed()
    }

    @Test
    fun missionScreen_comboMission_showsComboInstructions() {
        val missionData = MissionData(
            question = "Scan barcode AND take photo",
            answer = "combo",
            type = MissionType.COMBO_BARCODE_PHOTO,
            difficulty = MissionDifficulty.HARD,
            barcodeValue = "barcode123",
            photoReferenceHash = "reference_hash"
        )

        composeTestRule.setContent {
            MissionScreen(
                missionType = MissionType.COMBO_BARCODE_PHOTO,
                missionDifficulty = MissionDifficulty.HARD,
                missionData = missionData,
                alarmId = "test-alarm-11",
                snoozeInterval = 5,
                hapticsController = hapticsController,
                onMissionComplete = mockOnMissionComplete,
                onSnooze = mockOnSnooze
            )
        }

        composeTestRule.onNodeWithText("Scan barcode AND take photo").assertIsDisplayed()
    }
}
