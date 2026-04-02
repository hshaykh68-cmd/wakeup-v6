package com.wakeup.app.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.wakeup.app.MainActivity
import com.wakeup.app.R
import com.wakeup.app.core.util.SleepSoundManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service for sleep sound playback.
 * Keeps audio playing in background with a persistent notification.
 */
@AndroidEntryPoint
class SleepSoundService : Service() {

    @Inject
    lateinit var sleepSoundManager: SleepSoundManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val binder = LocalBinder()

    companion object {
        const val CHANNEL_ID = "sleep_sound_channel"
        const val NOTIFICATION_ID = 1002

        const val ACTION_PLAY = "com.wakeup.app.action.PLAY_SLEEP_SOUND"
        const val ACTION_PAUSE = "com.wakeup.app.action.PAUSE_SLEEP_SOUND"
        const val ACTION_STOP = "com.wakeup.app.action.STOP_SLEEP_SOUND"
        const val ACTION_SET_TIMER = "com.wakeup.app.action.SET_SLEEP_TIMER"

        const val EXTRA_SOUND_ID = "sound_id"
        const val EXTRA_TIMER_MINUTES = "timer_minutes"
        const val EXTRA_FADE_IN = "fade_in"

        fun createPlayIntent(context: Context, soundId: String, fadeIn: Boolean = true): Intent {
            return Intent(context, SleepSoundService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_SOUND_ID, soundId)
                putExtra(EXTRA_FADE_IN, fadeIn)
            }
        }

        fun createStopIntent(context: Context): Intent {
            return Intent(context, SleepSoundService::class.java).apply {
                action = ACTION_STOP
            }
        }

        fun createSetTimerIntent(context: Context, minutes: Int): Intent {
            return Intent(context, SleepSoundService::class.java).apply {
                action = ACTION_SET_TIMER
                putExtra(EXTRA_TIMER_MINUTES, minutes)
            }
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): SleepSoundService = this@SleepSoundService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Collect playing state to update notification
        serviceScope.launch {
            sleepSoundManager.isPlaying.collectLatest { isPlaying ->
                if (isPlaying) {
                    updateNotification()
                } else {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }

        // Collect remaining time to update notification
        serviceScope.launch {
            sleepSoundManager.remainingTime.collectLatest { remainingTime ->
                if (sleepSoundManager.isPlaying.value) {
                    updateNotification(remainingTime)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val soundId = intent.getStringExtra(EXTRA_SOUND_ID)
                val fadeIn = intent.getBooleanExtra(EXTRA_FADE_IN, true)
                soundId?.let {
                    startForegroundService()
                    sleepSoundManager.playSound(it, fadeIn)
                }
            }
            ACTION_PAUSE -> {
                sleepSoundManager.pauseSound()
                updateNotification()
            }
            ACTION_STOP -> {
                sleepSoundManager.stopSound(fadeOut = true)
            }
            ACTION_SET_TIMER -> {
                val minutes = intent.getIntExtra(EXTRA_TIMER_MINUTES, 0)
                sleepSoundManager.setTimer(minutes)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        sleepSoundManager.cleanup()
    }

    private fun startForegroundService() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(remainingTime: Long? = null) {
        val notification = buildNotification(remainingTime)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(remainingTime: Long? = null): Notification {
        val soundName = sleepSoundManager.currentSound.value?.name ?: "Sleep Sounds"
        val isPlaying = sleepSoundManager.isPlaying.value

        // Content text with timer info
        val contentText = when {
            remainingTime != null && remainingTime > 0 -> {
                val minutes = (remainingTime / 60000).toInt()
                val seconds = ((remainingTime % 60000) / 1000).toInt()
                "Timer: ${minutes}m ${seconds}s remaining"
            }
            else -> if (isPlaying) "Playing" else "Paused"
        }

        // Main activity intent
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to_sleep_sounds", true)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Play/Pause action
        val playPauseIntent = Intent(this, SleepSoundService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
            putExtra(EXTRA_SOUND_ID, sleepSoundManager.currentSound.value?.id)
            putExtra(EXTRA_FADE_IN, false)
        }
        val playPausePendingIntent = PendingIntent.getService(
            this, 1, playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playPauseIcon = if (isPlaying) R.drawable.ic_alarm_widget else R.drawable.ic_alarm_widget
        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        // Stop action
        val stopIntent = Intent(this, SleepSoundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(soundName)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(contentPendingIntent)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
            .addAction(R.drawable.ic_alarm_widget, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sleep Sounds"
            val descriptionText = "Sleep sound playback controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
