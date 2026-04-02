package com.wakeup.app.data.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.wakeup.app.R
import com.wakeup.app.core.util.AlarmSoundManager
import com.wakeup.app.data.alarm.AlarmWakeLock
import com.wakeup.app.domain.model.MissionDifficulty
import com.wakeup.app.domain.model.MissionType
import com.wakeup.app.domain.repository.WakeHistoryRepository
import com.wakeup.app.presentation.alarm.AlarmRingingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {

    @Inject
    lateinit var alarmSoundManager: AlarmSoundManager

    @Inject
    lateinit var wakeHistoryRepository: WakeHistoryRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val CHANNEL_ID = "alarm_channel"
    private val NOTIFICATION_ID = 1001
    private val WAKE_LOCK_TAG = "WakeUp::AlarmService"
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getStringExtra(AlarmReceiver.EXTRA_ALARM_ID) ?: return START_NOT_STICKY
        
        // Acquire wake lock to keep device awake during alarm
        acquireWakeLock()
        
        // Release the broadcast receiver's temporary wake lock
        AlarmWakeLock.release()
        
        val alarmLabel = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_LABEL) ?: "Alarm"
        val soundUri = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_SOUND_URI)
        val useVibration = intent.getBooleanExtra(AlarmReceiver.EXTRA_ALARM_VIBRATION, true)
        val gradualVolume = intent.getBooleanExtra(AlarmReceiver.EXTRA_ALARM_GRADUAL_VOLUME, true)

        val missionType = intent.getStringExtra(AlarmReceiver.EXTRA_MISSION_TYPE)?.let {
            MissionType.valueOf(it)
        } ?: MissionType.MATH

        val missionDifficulty = intent.getStringExtra(AlarmReceiver.EXTRA_MISSION_DIFFICULTY)?.let {
            MissionDifficulty.valueOf(it)
        } ?: MissionDifficulty.EASY

        val strictMode = intent.getBooleanExtra(AlarmReceiver.EXTRA_STRICT_MODE, false)
        val snoozeEnabled = intent.getBooleanExtra(AlarmReceiver.EXTRA_SNOOZE_ENABLED, true)
        val snoozeInterval = intent.getIntExtra(AlarmReceiver.EXTRA_SNOOZE_INTERVAL, 5)
        val maxSnoozes = intent.getIntExtra(AlarmReceiver.EXTRA_MAX_SNOOZES, 3)
        val isSnooze = intent.getBooleanExtra(AlarmReceiver.EXTRA_IS_SNOOZE, false)

        val photoReferencePath = intent.getStringExtra(AlarmReceiver.EXTRA_PHOTO_REFERENCE_PATH)
        val photoReferenceHash = intent.getStringExtra(AlarmReceiver.EXTRA_PHOTO_REFERENCE_HASH)
        val barcodeValue = intent.getStringExtra(AlarmReceiver.EXTRA_BARCODE_VALUE)
        val barcodeFormat = intent.getStringExtra(AlarmReceiver.EXTRA_BARCODE_FORMAT)

        // Record alarm trigger in history
        serviceScope.launch {
            wakeHistoryRepository.recordAlarmTriggered(alarmId, System.currentTimeMillis())
        }

        // Build the notification
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).let { intent ->
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            intent.putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, alarmLabel)
            intent.putExtra(AlarmReceiver.EXTRA_ALARM_SOUND_URI, soundUri)
            intent.putExtra(AlarmReceiver.EXTRA_ALARM_VIBRATION, useVibration)
            intent.putExtra(AlarmReceiver.EXTRA_ALARM_GRADUAL_VOLUME, gradualVolume)
            intent.putExtra(AlarmReceiver.EXTRA_MISSION_TYPE, missionType.name)
            intent.putExtra(AlarmReceiver.EXTRA_MISSION_DIFFICULTY, missionDifficulty.name)
            intent.putExtra(AlarmReceiver.EXTRA_STRICT_MODE, strictMode)
            intent.putExtra(AlarmReceiver.EXTRA_SNOOZE_ENABLED, snoozeEnabled)
            intent.putExtra(AlarmReceiver.EXTRA_SNOOZE_INTERVAL, snoozeInterval)
            intent.putExtra(AlarmReceiver.EXTRA_MAX_SNOOZES, maxSnoozes)
            intent.putExtra(AlarmReceiver.EXTRA_IS_SNOOZE, isSnooze)
            intent.putExtra(AlarmReceiver.EXTRA_PHOTO_REFERENCE_PATH, photoReferencePath)
            intent.putExtra(AlarmReceiver.EXTRA_PHOTO_REFERENCE_HASH, photoReferenceHash)
            intent.putExtra(AlarmReceiver.EXTRA_BARCODE_VALUE, barcodeValue)
            intent.putExtra(AlarmReceiver.EXTRA_BARCODE_FORMAT, barcodeFormat)
            intent
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_widget)
            .setContentTitle(alarmLabel)
            .setContentText("Wake up! Complete the mission to dismiss.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        // Play sound
        soundUri?.let { uri ->
            android.net.Uri.parse(uri)?.let { parsedUri ->
                alarmSoundManager.playAlarm(parsedUri, gradualVolume)
            }
        } ?: run {
            alarmSoundManager.playAlarm(null, gradualVolume)
        }

        // Start vibration
        if (useVibration) {
            alarmSoundManager.startVibration()
        }

        // Launch the full-screen activity
        startActivity(fullScreenIntent)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
        serviceScope.cancel()
        alarmSoundManager.stopAlarm()
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            
            // Release any existing wake lock first
            releaseWakeLock()
            
            // Create and acquire new partial wake lock
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                WAKE_LOCK_TAG
            ).apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L) // 10 minutes max - should be enough for any alarm
            }
        } catch (e: Exception) {
            // Fail silently - alarm will still try to work without wake lock
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            // Ignore release errors
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "WakeUp alarm notifications"
                setBypassDnd(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
