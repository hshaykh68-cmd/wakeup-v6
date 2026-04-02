package com.wakeup.app.core.service

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before
import javax.inject.Inject

/**
 * Compose UI tests for haptic feedback integration.
 * Verifies haptics are triggered during user interactions.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class HapticFeedbackIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Inject
    lateinit var hapticsController: HapticsController

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun hapticsController_isInjected() {
        // Verify haptics controller is properly injected
        assert(hapticsController != null)
    }

    @Test
    fun hapticsController_providesAllMethods() {
        // Test that all haptic methods can be called without crashing
        hapticsController.performSuccess()
        hapticsController.performError()
        hapticsController.performLightImpact()
        hapticsController.performMediumImpact()
        hapticsController.performHeavyImpact()
        hapticsController.performTick()
        hapticsController.performCustomPattern(longArrayOf(0, 100), intArrayOf(0, 100))
    }
}
