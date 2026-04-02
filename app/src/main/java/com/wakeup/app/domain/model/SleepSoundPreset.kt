package com.wakeup.app.domain.model

/**
 * Represents a preset combination of sleep sounds for future V2 layering feature.
 * This allows users to mix multiple sounds with individual volume levels.
 */
data class SleepSoundPreset(
    val id: String,
    val name: String,
    val description: String,
    val layers: List<SleepSoundLayer>,
    val isDefault: Boolean = false,
    val isUserCreated: Boolean = false
)

/**
 * Represents a single layer in a sleep sound preset with volume control.
 */
data class SleepSoundLayer(
    val sound: SleepSound,
    val volume: Float = 1.0f, // 0.0 to 1.0
    val isEnabled: Boolean = true
)

/**
 * Predefined sleep sound presets for V2 layering feature.
 * These combine multiple sounds for rich ambient experiences.
 */
object SleepSoundPresets {

    /**
     * Rainy night preset - combines rain/thunder with subtle white noise
     */
    val RAINY_NIGHT = SleepSoundPreset(
        id = "rainy_night",
        name = "Rainy Night",
        description = "Heavy rain with distant thunder and soft white noise",
        layers = listOf(
            SleepSoundLayer(SleepSounds.RAIN_THUNDER, volume = 0.8f),
            SleepSoundLayer(SleepSounds.WHITE_NOISE_RAIN, volume = 0.3f)
        ),
        isDefault = true
    )

    /**
     * Ocean calm preset - ocean waves with gentle nature ambience
     */
    val OCEAN_CALM = SleepSoundPreset(
        id = "ocean_calm",
        name = "Ocean Calm",
        description = "Gentle ocean waves with soft nature ambience",
        layers = listOf(
            SleepSoundLayer(SleepSounds.OCEAN_WAVES, volume = 0.7f),
            SleepSoundLayer(SleepSounds.RELAXING_NATURE, volume = 0.4f)
        ),
        isDefault = true
    )

    /**
     * Forest retreat preset - forest birds with wind ambience
     */
    val FOREST_RETREAT = SleepSoundPreset(
        id = "forest_retreat",
        name = "Forest Retreat",
        description = "Morning forest with birds and gentle wind",
        layers = listOf(
            SleepSoundLayer(SleepSounds.FOREST_BIRDS, volume = 0.6f),
            SleepSoundLayer(SleepSounds.ARTIC_WIND, volume = 0.3f)
        ),
        isDefault = true
    )

    /**
     * Deep sleep preset - white noise dominant with subtle rain
     */
    val DEEP_SLEEP = SleepSoundPreset(
        id = "deep_sleep",
        name = "Deep Sleep",
        description = "Consistent white noise with very subtle rain",
        layers = listOf(
            SleepSoundLayer(SleepSounds.WHITE_NOISE_RAIN, volume = 0.9f),
            SleepSoundLayer(SleepSounds.RAIN_THUNDER, volume = 0.2f)
        ),
        isDefault = true
    )

    /**
     * All available default presets
     */
    val DEFAULT_PRESETS = listOf(
        RAINY_NIGHT,
        OCEAN_CALM,
        FOREST_RETREAT,
        DEEP_SLEEP
    )

    /**
     * Get a preset by its ID
     */
    fun getById(id: String): SleepSoundPreset? {
        return DEFAULT_PRESETS.find { it.id == id }
    }
}

/**
 * Data class for saving user-created presets to DataStore or Room.
 * This is a simplified version for storage that can be reconstructed into SleepSoundPreset.
 */
data class SavedSleepPreset(
    val id: String,
    val name: String,
    val description: String,
    val layerData: List<SavedLayerData>
)

/**
 * Serializable layer data for storage
 */
data class SavedLayerData(
    val soundId: String,
    val volume: Float,
    val isEnabled: Boolean
)
