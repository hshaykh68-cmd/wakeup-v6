package com.wakeup.app.data.repository

import android.content.Context
import android.content.Intent
import com.wakeup.app.data.service.SleepSoundService
import com.wakeup.app.domain.repository.SleepSoundRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SleepSoundRepository that delegates to SleepSoundService.
 */
@Singleton
class SleepSoundRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SleepSoundRepository {

    override suspend fun play(soundId: String, fadeIn: Boolean) {
        val intent = SleepSoundService.createPlayIntent(context, soundId, fadeIn)
        context.startService(intent)
    }

    override suspend fun pause() {
        val intent = Intent(context, SleepSoundService::class.java).apply {
            action = SleepSoundService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    override suspend fun stop() {
        val intent = SleepSoundService.createStopIntent(context)
        context.startService(intent)
    }

    override suspend fun setTimer(minutes: Int) {
        val intent = SleepSoundService.createSetTimerIntent(context, minutes)
        context.startService(intent)
    }
}
