package com.wakeup.app.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.wakeup.app.core.service.SmartWakeService
import com.wakeup.app.data.local.dao.AlarmDao
import com.wakeup.app.domain.model.Alarm
import com.wakeup.app.domain.usecase.AlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmDao: AlarmDao
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: Alarm): Boolean {
        val nextRingTime = alarm.getNextRingTime()
        val triggerAtMillis = nextRingTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // If Smart Wake is enabled, start the SmartWakeService instead of scheduling exact alarm
        if (alarm.smartWakeEnabled) {
            SmartWakeService.startService(
                context = context,
                alarmId = alarm.id,
                targetTimeMs = triggerAtMillis,
                windowMinutes = alarm.smartWakeWindowMinutes
            )
            return true
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmReceiver.EXTRA_ALARM_SOUND_URI, alarm.soundUri)
            putExtra(AlarmReceiver.EXTRA_ALARM_VIBRATION, alarm.useVibration)
            putExtra(AlarmReceiver.EXTRA_ALARM_GRADUAL_VOLUME, alarm.gradualVolume)
            putExtra(AlarmReceiver.EXTRA_MISSION_TYPE, alarm.missionType.name)
            putExtra(AlarmReceiver.EXTRA_MISSION_DIFFICULTY, alarm.missionDifficulty.name)
            putExtra(AlarmReceiver.EXTRA_STRICT_MODE, alarm.strictMode)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_ENABLED, alarm.snoozeEnabled)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_INTERVAL, alarm.snoozeInterval)
            putExtra(AlarmReceiver.EXTRA_MAX_SNOOZES, alarm.maxSnoozes)
            putExtra(AlarmReceiver.EXTRA_PHOTO_REFERENCE_PATH, alarm.photoReferencePath)
            putExtra(AlarmReceiver.EXTRA_PHOTO_REFERENCE_HASH, alarm.photoReferenceHash)
            putExtra(AlarmReceiver.EXTRA_BARCODE_VALUE, alarm.barcodeValue)
            putExtra(AlarmReceiver.EXTRA_BARCODE_FORMAT, alarm.barcodeFormat?.toString())
            putExtra(AlarmReceiver.EXTRA_SMART_WAKE_ENABLED, alarm.smartWakeEnabled)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.pendingIntentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    true
                } else {
                    // Fallback to inexact alarm when exact alarm permission is not granted
                    // This ensures the alarm still fires, though not at the exact time
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    false // Return false to indicate exact alarm was not scheduled
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                true
            }
        } catch (e: Exception) {
            // If scheduling fails completely, return false
            false
        }
    }

    override fun cancel(alarmId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        
        // Try to get the alarm's pendingIntentId from database
        // Use a coroutine to fetch it since DAO is suspend function
        var pendingIntentId: Int? = null
        kotlinx.coroutines.runBlocking {
            pendingIntentId = alarmDao.getPendingIntentId(alarmId)
        }
        
        // Use pendingIntentId if available, otherwise fall back to hash for backwards compatibility
        val requestCode = pendingIntentId ?: alarmId.hashCode()
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    override fun snooze(alarm: Alarm, snoozeMinutes: Int): Boolean {
        val triggerAtMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmReceiver.EXTRA_ALARM_SOUND_URI, alarm.soundUri)
            putExtra(AlarmReceiver.EXTRA_ALARM_VIBRATION, alarm.useVibration)
            putExtra(AlarmReceiver.EXTRA_ALARM_GRADUAL_VOLUME, alarm.gradualVolume)
            putExtra(AlarmReceiver.EXTRA_MISSION_TYPE, alarm.missionType.name)
            putExtra(AlarmReceiver.EXTRA_MISSION_DIFFICULTY, alarm.missionDifficulty.name)
            putExtra(AlarmReceiver.EXTRA_STRICT_MODE, alarm.strictMode)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_ENABLED, alarm.snoozeEnabled)
            putExtra(AlarmReceiver.EXTRA_IS_SNOOZE, true)
            putExtra(AlarmReceiver.EXTRA_PHOTO_REFERENCE_PATH, alarm.photoReferencePath)
            putExtra(AlarmReceiver.EXTRA_PHOTO_REFERENCE_HASH, alarm.photoReferenceHash)
            putExtra(AlarmReceiver.EXTRA_BARCODE_VALUE, alarm.barcodeValue)
            putExtra(AlarmReceiver.EXTRA_BARCODE_FORMAT, alarm.barcodeFormat?.toString())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.pendingIntentId + 1000000, // Use separate range for snooze to avoid collision with main alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    true
                } else {
                    // Fallback to inexact alarm when exact alarm permission is not granted
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    false // Return false to indicate exact alarm was not scheduled
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                true
            }
        } catch (e: Exception) {
            // If scheduling fails completely, return false
            false
        }
    }

    override fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Not required before Android 12
        }
    }
}
