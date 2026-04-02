package com.wakeup.app.domain.repository

import com.wakeup.app.domain.model.SleepSound

/**
 * Repository for sleep sound operations.
 * Abstracts the service layer from the ViewModel for testability.
 */
interface SleepSoundRepository {
    suspend fun play(soundId: String, fadeIn: Boolean = true)
    suspend fun pause()
    suspend fun stop()
    suspend fun setTimer(minutes: Int)
}
