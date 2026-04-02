package com.wakeup.app.core.util

import android.content.Context
import android.media.ToneGenerator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Utility class for playing sound effects during mission gameplay.
 * Uses ToneGenerator for pattern tones and simple beeps.
 * 
 * NOTE: This is specifically for MISSION gameplay sounds (pattern memory, etc.)
 * For ALARM sounds, use AlarmSoundManager instead.
 */
class MissionSoundManager(private val context: Context) {

    companion object {
        // Tone frequencies for Memory Mission (Hz)
        const val TONE_1 = 440  // A4
        const val TONE_2 = 554  // C#5
        const val TONE_3 = 659  // E5
        const val TONE_4 = 784  // G5

        // Pattern playback speeds by difficulty (milliseconds per step)
        const val SPEED_EASY_MS = 800L    // Slower, more time to memorize
        const val SPEED_MEDIUM_MS = 600L  // Normal pace
        const val SPEED_HARD_MS = 400L    // Fast, challenging
    }

    private var toneGenerator: ToneGenerator? = null

    init {
        toneGenerator = ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 80)
    }

    /**
     * Play a tone corresponding to a pattern number (1-4).
     * Each number has a unique pitch for auditory pattern recognition.
     */
    fun playPatternTone(number: Int, durationMs: Int = 200) {
        val toneType = when (number) {
            1 -> TONE_1
            2 -> TONE_2
            3 -> TONE_3
            4 -> TONE_4
            else -> TONE_1
        }
        toneGenerator?.startTone(toneType, durationMs)
    }

    /**
     * Play an error sound for wrong pattern input.
     */
    fun playErrorSound() {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300)
    }

    /**
     * Play a success sound for correct pattern completion.
     */
    fun playSuccessSound() {
        // Play ascending arpeggio
        toneGenerator?.startTone(TONE_1, 100)
        GlobalScope.launch {
            delay(100)
            toneGenerator?.startTone(TONE_2, 100)
            delay(100)
            toneGenerator?.startTone(TONE_3, 150)
        }
    }

    /**
     * Get the pattern playback speed based on difficulty.
     * @param difficultyOrdinal MissionDifficulty ordinal (0=EASY, 1=MEDIUM, 2=HARD)
     * @return Delay in milliseconds between pattern steps
     */
    fun getPatternSpeedMs(difficultyOrdinal: Int): Long {
        return when (difficultyOrdinal) {
            0 -> SPEED_EASY_MS
            1 -> SPEED_MEDIUM_MS
            2 -> SPEED_HARD_MS
            else -> SPEED_MEDIUM_MS
        }
    }

    /**
     * Release resources when no longer needed.
     */
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
