package com.wakeup.app.core.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmSoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var currentRingtone: Ringtone? = null
    private var audioManager: AudioManager? = null
    private var originalVolume: Int = 0
    private var vibrator: Vibrator? = null
    private var volumeRampJob: Job? = null
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playAlarm(soundUri: Uri?, gradualVolume: Boolean = true) {
        stopAlarm()

        val ringtoneUri = soundUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        currentRingtone = RingtoneManager.getRingtone(context, ringtoneUri)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            currentRingtone?.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        }

        if (gradualVolume) {
            startGradualVolumeIncrease()
        } else {
            audioManager?.setStreamVolume(
                AudioManager.STREAM_ALARM,
                audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM) ?: 7,
                0
            )
        }

        currentRingtone?.play()
    }

    fun startVibration(pattern: LongArray = longArrayOf(0, 500, 500, 500)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, 0)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    fun stopAlarm() {
        // Cancel existing coroutine job
        volumeRampJob?.cancel()
        volumeRampJob = null
        currentRingtone?.stop()
        currentRingtone = null
        stopVibration()
        // Always restore original volume, even if it was 0
        audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
    }

    fun stopVibration() {
        vibrator?.cancel()
    }

    private fun startGradualVolumeIncrease() {
        originalVolume = audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 0
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM) ?: 7

        // Cancel any existing ramp job
        volumeRampJob?.cancel()

        volumeRampJob = coroutineScope.launch {
            for (i in 1..maxVolume) {
                if (!isActive) break
                audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, i, 0)
                delay(1000)
            }
        }
    }

    /**
     * Clean up resources. Call this when the app is being destroyed.
     */
    fun cleanup() {
        coroutineScope.cancel()
        volumeRampJob = null
    }

    fun getSystemRingtones(): List<Pair<String, Uri>> {
        val manager = RingtoneManager(context)
        manager.setType(RingtoneManager.TYPE_ALARM)
        val cursor = manager.cursor
        val ringtones = mutableListOf<Pair<String, Uri>>()

        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = manager.getRingtoneUri(cursor.position)
            ringtones.add(title to uri)
        }

        return ringtones
    }
}
