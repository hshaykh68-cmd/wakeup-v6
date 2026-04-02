package com.wakeup.app.domain.model

import androidx.annotation.RawRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Waves
import androidx.compose.ui.graphics.vector.ImageVector
import com.wakeup.app.R

/**
 * Categories of sleep sounds
 */
enum class SoundCategory {
    NATURE,
    WEATHER,
    WHITE_NOISE
}

/**
 * Represents a sleep sound available for playback
 */
data class SleepSound(
    val id: String,
    val name: String,
    val icon: ImageVector,
    @RawRes val rawResId: Int,
    val category: SoundCategory,
    val description: String = ""
)

/**
 * Available sleep sounds in the app
 */
object SleepSounds {
    val OCEAN_WAVES = SleepSound(
        id = "ocean_waves",
        name = "Ocean Waves",
        icon = Icons.Default.BeachAccess,
        rawResId = R.raw.ocean_waves,
        category = SoundCategory.NATURE,
        description = "Gentle ocean waves crashing on the shore"
    )

    val FOREST_BIRDS = SleepSound(
        id = "forest_birds",
        name = "Forest Birds",
        icon = Icons.Default.Forest,
        rawResId = R.raw.forest_birds,
        category = SoundCategory.NATURE,
        description = "Peaceful forest with singing birds"
    )

    val RELAXING_NATURE = SleepSound(
        id = "relaxing_nature",
        name = "Relaxing Nature",
        icon = Icons.Default.Nature,
        rawResId = R.raw.relaxing_nature,
        category = SoundCategory.NATURE,
        description = "Calming natural ambience"
    )

    val RAIN_THUNDER = SleepSound(
        id = "rain_thunder",
        name = "Rain & Thunder",
        icon = Icons.Default.Thunderstorm,
        rawResId = R.raw.rain_and_thunderstorm,
        category = SoundCategory.WEATHER,
        description = "Soothing rain with distant thunder"
    )

    val ARTIC_WIND = SleepSound(
        id = "artic_wind",
        name = "Arctic Wind",
        icon = Icons.Default.AcUnit,
        rawResId = R.raw.artic_wind,
        category = SoundCategory.WEATHER,
        description = "Gentle arctic wind blowing"
    )

    val WHITE_NOISE_RAIN = SleepSound(
        id = "white_noise_rain",
        name = "White Noise",
        icon = Icons.Default.Cloud,
        rawResId = R.raw.white_noise_rain,
        category = SoundCategory.WHITE_NOISE,
        description = "Consistent white noise for focus"
    )

    /**
     * List of all available sleep sounds
     */
    val ALL = listOf(
        OCEAN_WAVES,
        FOREST_BIRDS,
        RELAXING_NATURE,
        RAIN_THUNDER,
        ARTIC_WIND,
        WHITE_NOISE_RAIN
    )

    /**
     * Get a sleep sound by its ID
     */
    fun getById(id: String): SleepSound? {
        return ALL.find { it.id == id }
    }
}

/**
 * Represents a sleep timer duration option
 */
enum class SleepTimerOption(val minutes: Int, val displayText: String) {
    OFF(0, "Off"),
    MIN_10(10, "10m"),
    MIN_20(20, "20m"),
    MIN_30(30, "30m"),
    MIN_60(60, "60m");

    companion object {
        fun fromMinutes(minutes: Int): SleepTimerOption {
            return entries.find { it.minutes == minutes } ?: OFF
        }
    }
}
