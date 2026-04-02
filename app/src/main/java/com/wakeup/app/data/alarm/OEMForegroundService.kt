package com.wakeup.app.data.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.wakeup.app.R
import com.wakeup.app.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

/**
 * Lightweight foreground service that runs during the sleep window
 * to prevent OEMs from killing the app before the alarm fires.
 * 
 * This is especially important for aggressive OEMs like Xiaomi, Huawei, and OPPO.
 */
@AndroidEntryPoint
class OEMForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "oem_foreground_service"
        const val CHANNEL_NAME = "Alarm Protection"
        const val NOTIFICATION_ID = 1003
        
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ALARM_TIME = "alarm_time"
        const val EXTRA_STOP_SERVICE = "stop_service"
        
        /**
         * Start the foreground service to protect an upcoming alarm.
         */
        fun startForAlarm(context: Context, alarmId: String, alarmTimeMillis: Long) {
            val intent = Intent(context, OEMForegroundService::class.java).apply {
                putExtra(EXTRA_ALARM_ID, alarmId)
                putExtra(EXTRA_ALARM_TIME, alarmTimeMillis)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        /**
         * Stop the foreground service.
         */
        fun stop(context: Context) {
            val intent = Intent(context, OEMForegroundService::class.java).apply {
                putExtra(EXTRA_STOP_SERVICE, true)
            }
            context.startService(intent)
        }
    }
    
    private var alarmId: String? = null
    private var alarmTimeMillis: Long = 0L
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Check if we should stop
        if (intent?.getBooleanExtra(EXTRA_STOP_SERVICE, false) == true) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        alarmId = intent?.getStringExtra(EXTRA_ALARM_ID)
        alarmTimeMillis = intent?.getLongExtra(EXTRA_ALARM_TIME, 0L) ?: 0L
        
        // Start as foreground service
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        // Schedule automatic stop after alarm time
        scheduleAutoStop()
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Low importance to be non-intrusive
            ).apply {
                description = "Ensures your alarm will ring reliably even when your phone tries to optimize battery usage"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun buildNotification(): android.app.Notification {
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val alarmTimeText = if (alarmTimeMillis > 0) {
            val minutesUntil = ((alarmTimeMillis - System.currentTimeMillis()) / 60000).toInt()
            when {
                minutesUntil <= 0 -> "Alarm is ringing soon"
                minutesUntil < 60 -> "Alarm in $minutesUntil min"
                else -> "Alarm in ${minutesUntil / 60}h ${minutesUntil % 60}m"
            }
        } else {
            "WakeUp is protecting your alarm"
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm protection active")
            .setContentText(alarmTimeText)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Assume this exists
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setShowWhen(false)
            .build()
    }
    
    private fun scheduleAutoStop() {
        if (alarmTimeMillis > 0) {
            val delayMillis = (alarmTimeMillis - System.currentTimeMillis()) + TimeUnit.MINUTES.toMillis(5)
            if (delayMillis > 0) {
                // Use a simple handler for auto-stop
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    stopSelf()
                }, delayMillis)
            } else {
                // Alarm time has passed, stop immediately
                stopSelf()
            }
        }
    }
    
    /**
     * Update the notification with current time remaining.
     */
    fun updateNotification() {
        if (alarmTimeMillis > 0) {
            val notification = buildNotification()
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }
}
