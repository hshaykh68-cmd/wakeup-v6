package com.wakeup.app.core.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.wakeup.app.R
import com.wakeup.app.domain.model.SleepSound
import com.wakeup.app.domain.model.SleepSounds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages sleep sound playback with fade in/out, volume control, and sleep timer.
 * Similar to AlarmSoundManager but optimized for ambient sleep sounds.
 */
@Singleton
class SleepSoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var fadeJob: Job? = null
    private var timerJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSound = MutableStateFlow<SleepSound?>(null)
    val currentSound: StateFlow<SleepSound?> = _currentSound.asStateFlow()

    private val _remainingTime = MutableStateFlow<Long?>(null)
    val remainingTime: StateFlow<Long?> = _remainingTime.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Fade durations in milliseconds
    companion object {
        const val FADE_IN_DURATION_MS = 1500L
        const val FADE_OUT_DURATION_MS = 2500L
        const val FADE_STEP_MS = 50L
    }

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    /**
     * Play a sleep sound with optional fade in
     */
    fun playSound(soundId: String, fadeIn: Boolean = true) {
        val sound = SleepSounds.getById(soundId) ?: return

        // Stop any currently playing sound
        stopSound(fadeOut = false)

        _currentSound.value = sound

        if (requestAudioFocus()) {
            initializeMediaPlayer(sound)
            if (fadeIn) {
                startFadeIn()
            } else {
                mediaPlayer?.start()
                _isPlaying.value = true
                _volume.value = 1.0f
            }
        }
    }

    /**
     * Stop playback with optional fade out
     */
    fun stopSound(fadeOut: Boolean = true) {
        timerJob?.cancel()
        timerJob = null
        _remainingTime.value = null

        if (!fadeOut) {
            stopPlaybackImmediately()
        } else {
            startFadeOut()
        }
    }

    /**
     * Pause playback (for notification controls)
     */
    fun pauseSound() {
        fadeJob?.cancel()
        mediaPlayer?.pause()
        _isPlaying.value = false
    }

    /**
     * Resume playback
     */
    fun resumeSound() {
        if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
            mediaPlayer?.start()
            _isPlaying.value = true
        }
    }

    /**
     * Set playback volume (0.0 to 1.0)
     */
    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        _volume.value = clampedVolume
        mediaPlayer?.setVolume(clampedVolume, clampedVolume)
    }

    /**
     * Check if a sound is currently playing
     */
    fun isPlaying(): Boolean = _isPlaying.value

    /**
     * Set a sleep timer in minutes (0 = no timer)
     */
    fun setTimer(minutes: Int) {
        timerJob?.cancel()

        if (minutes <= 0) {
            _remainingTime.value = null
            return
        }

        val durationMillis = minutes * 60 * 1000L
        _remainingTime.value = durationMillis

        timerJob = coroutineScope.launch {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + durationMillis

            while (isActive && System.currentTimeMillis() < endTime) {
                val remaining = endTime - System.currentTimeMillis()
                _remainingTime.value = remaining.coerceAtLeast(0)
                delay(1000) // Update every second
            }

            if (isActive) {
                _remainingTime.value = 0
                stopSound(fadeOut = true)
            }
        }
    }

    /**
     * Cancel the sleep timer without stopping playback
     */
    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _remainingTime.value = null
    }

    /**
     * Get the current timer remaining in minutes
     */
    fun getTimerMinutes(): Int {
        val remaining = _remainingTime.value ?: return 0
        return (remaining / (60 * 1000)).toInt()
    }

    private fun initializeMediaPlayer(sound: SleepSound) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context.resources.openRawResourceFd(sound.rawResId))
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                setVolume(0f, 0f) // Start at 0 volume for fade in
                prepare()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }

    private fun startFadeIn() {
        fadeJob?.cancel()

        val steps = (FADE_IN_DURATION_MS / FADE_STEP_MS).toInt()
        val volumeStep = 1.0f / steps

        mediaPlayer?.start()
        _isPlaying.value = true

        fadeJob = coroutineScope.launch {
            for (i in 0..steps) {
                if (!isActive) break
                val volume = (i * volumeStep).coerceIn(0f, 1f)
                mediaPlayer?.setVolume(volume, volume)
                _volume.value = volume
                delay(FADE_STEP_MS)
            }
            mediaPlayer?.setVolume(1f, 1f)
            _volume.value = 1f
        }
    }

    private fun startFadeOut() {
        fadeJob?.cancel()

        val steps = (FADE_OUT_DURATION_MS / FADE_STEP_MS).toInt()
        val volumeStep = 1.0f / steps

        fadeJob = coroutineScope.launch {
            for (i in steps downTo 0) {
                if (!isActive) break
                val volume = (i * volumeStep).coerceIn(0f, 1f)
                mediaPlayer?.setVolume(volume, volume)
                _volume.value = volume
                delay(FADE_STEP_MS)
            }
            stopPlaybackImmediately()
        }
    }

    private fun stopPlaybackImmediately() {
        fadeJob?.cancel()
        fadeJob = null

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        _isPlaying.value = false
        _currentSound.value = null
        _volume.value = 1.0f

        abandonAudioFocus()
    }

    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS,
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                            stopSound(fadeOut = true)
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                            setVolume(0.3f)
                        }
                        AudioManager.AUDIOFOCUS_GAIN -> {
                            setVolume(1.0f)
                            if (!_isPlaying.value && mediaPlayer != null) {
                                resumeSound()
                            }
                        }
                    }
                }
                .build()

            val result = audioManager?.requestAudioFocus(audioFocusRequest!!)
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager?.requestAudioFocus(
                { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS,
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                            stopSound(fadeOut = true)
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                            setVolume(0.3f)
                        }
                        AudioManager.AUDIOFOCUS_GAIN -> {
                            setVolume(1.0f)
                        }
                    }
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            @Suppress("DEPRECATION")
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager?.abandonAudioFocusRequest(it)
            }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
    }

    /**
     * Clean up resources. Call when the app is being destroyed.
     */
    fun cleanup() {
        timerJob?.cancel()
        fadeJob?.cancel()
        coroutineScope.cancel()
        stopPlaybackImmediately()
    }
}
