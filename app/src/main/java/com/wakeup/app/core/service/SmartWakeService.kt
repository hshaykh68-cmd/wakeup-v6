package com.wakeup.app.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.wakeup.app.R
import com.wakeup.app.core.util.SleepPhase
import com.wakeup.app.core.util.SleepPhaseDetector
import com.wakeup.app.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Foreground service for Smart Wake sleep monitoring.
 * Monitors sleep phases and triggers alarm during optimal light sleep window.
 *
 * Service lifecycle:
 * 1. Start monitoring during smart wake window (e.g., 6:30-7:00 AM)
 * 2. Detect light sleep phases
 * 3. Trigger alarm when light sleep detected within window
 * 4. If no light sleep by end of window, trigger alarm anyway
 */
class SmartWakeService : Service() {

    companion object {
        const val CHANNEL_ID = "smart_wake_channel"
        const val NOTIFICATION_ID = 1004

        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_TARGET_TIME_MS = "target_time_ms"
        const val EXTRA_WINDOW_MINUTES = "window_minutes"
        const val EXTRA_ENABLED = "enabled"

        private const val TAG = "SmartWakeService"

        /**
         * Start the Smart Wake service.
         */
        fun startService(
            context: Context,
            alarmId: String,
            targetTimeMs: Long,
            windowMinutes: Int
        ) {
            val intent = Intent(context, SmartWakeService::class.java).apply {
                putExtra(EXTRA_ALARM_ID, alarmId)
                putExtra(EXTRA_TARGET_TIME_MS, targetTimeMs)
                putExtra(EXTRA_WINDOW_MINUTES, windowMinutes)
                putExtra(EXTRA_ENABLED, true)
            }
            context.startForegroundService(intent)
        }

        /**
         * Stop the Smart Wake service.
         */
        fun stopService(context: Context) {
            val intent = Intent(context, SmartWakeService::class.java).apply {
                putExtra(EXTRA_ENABLED, false)
            }
            context.startService(intent)
        }
    }

    private lateinit var sleepPhaseDetector: SleepPhaseDetector
    private lateinit var notificationManager: NotificationManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var alarmId: String? = null
    private var targetTimeMs: Long = 0
    private var windowMinutes: Int = 30
    private var isMonitoring = false

    private var windowStartTime: Long = 0
    private var windowEndTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        sleepPhaseDetector = SleepPhaseDetector(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val enabled = intent?.getBooleanExtra(EXTRA_ENABLED, false) ?: false

        if (!enabled) {
            stopMonitoring()
            stopSelf()
            return START_NOT_STICKY
        }

        alarmId = intent?.getStringExtra(EXTRA_ALARM_ID)
        targetTimeMs = intent?.getLongExtra(EXTRA_TARGET_TIME_MS, 0) ?: 0
        windowMinutes = intent?.getIntExtra(EXTRA_WINDOW_MINUTES, 30) ?: 30

        if (alarmId == null || targetTimeMs == 0L) {
            Log.e(TAG, "Missing required parameters")
            stopSelf()
            return START_NOT_STICKY
        }

        // Calculate window times
        val windowMs = windowMinutes * 60 * 1000
        windowEndTime = targetTimeMs
        windowStartTime = targetTimeMs - windowMs

        val currentTime = System.currentTimeMillis()

        // Check if we're within the window
        if (currentTime < windowStartTime) {
            // Too early, schedule start
            Log.d(TAG, "Smart wake window hasn't started yet, scheduling...")
            serviceScope.launch {
                delay(windowStartTime - currentTime)
                if (isActive()) startMonitoring()
            }
        } else if (currentTime < windowEndTime) {
            // Within window, start immediately
            startMonitoring()
        } else {
            // Window passed
            Log.w(TAG, "Smart wake window has already passed")
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
        serviceScope.cancel()
    }

    private fun startMonitoring() {
        if (isMonitoring) return

        isMonitoring = true
        sleepPhaseDetector.startMonitoring()

        // Start foreground with notification
        val notification = createNotification("Monitoring sleep for Smart Wake...")
        startForeground(NOTIFICATION_ID, notification)

        Log.d(TAG, "Smart Wake monitoring started for alarm $alarmId")

        // Collect sleep phase changes
        serviceScope.launch {
            sleepPhaseDetector.currentPhaseFlow.collectLatest { phase ->
                onSleepPhaseChanged(phase)
            }
        }

        // Update notification with movement intensity
        serviceScope.launch {
            sleepPhaseDetector.movementIntensityFlow.collectLatest { intensity ->
                updateNotification(intensity)
            }
        }

        // Set up window end timeout
        val remainingTime = windowEndTime - System.currentTimeMillis()
        if (remainingTime > 0) {
            serviceScope.launch {
                delay(remainingTime)
                onWindowEnd()
            }
        }
    }

    private fun stopMonitoring() {
        if (!isMonitoring) return

        isMonitoring = false
        sleepPhaseDetector.stopMonitoring()

        Log.d(TAG, "Smart Wake monitoring stopped")
    }

    private fun onSleepPhaseChanged(phase: SleepPhase) {
        Log.d(TAG, "Sleep phase changed: $phase")

        when (phase) {
            SleepPhase.LIGHT_SLEEP -> {
                // Optimal time to wake!
                triggerSmartWake()
            }
            SleepPhase.AWAKE -> {
                // User is already awake, trigger alarm
                triggerSmartWake()
            }
            else -> {
                // Keep monitoring
            }
        }
    }

    private fun onWindowEnd() {
        Log.d(TAG, "Smart wake window ended, triggering alarm")

        // Window ended, trigger alarm regardless of sleep phase
        triggerSmartWake()
    }

    private fun triggerSmartWake() {
        if (!isMonitoring) return

        Log.d(TAG, "Triggering Smart Wake for alarm $alarmId")

        // Send broadcast to trigger alarm
        val intent = Intent(ACTION_SMART_WAKE_TRIGGER).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            setPackage(packageName)
        }
        sendBroadcast(intent)

        // Stop monitoring and service
        stopMonitoring()
        stopSelf()
    }

    private fun isActive(): Boolean {
        return isMonitoring && serviceScope.isActive
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Smart Wake",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors sleep phases for optimal wake time"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Smart Wake Active")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(intensity: Float) {
        val contentText = when {
            intensity < 0.2f -> "Deep sleep detected"
            intensity < 0.5f -> "Light movement"
            intensity < 0.8f -> "Moderate movement"
            else -> "Significant movement detected"
        }

        val notification = createNotification(contentText)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Get time remaining until window ends.
     */
    fun getTimeUntilWindowEnd(): Long {
        return windowEndTime - System.currentTimeMillis()
    }

    /**
     * Check if currently within smart wake window.
     */
    fun isInWindow(): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime >= windowStartTime && currentTime < windowEndTime
    }
}

/**
 * Broadcast action for Smart Wake trigger.
 */
const val ACTION_SMART_WAKE_TRIGGER = "com.wakeup.app.SMART_WAKE_TRIGGER"
