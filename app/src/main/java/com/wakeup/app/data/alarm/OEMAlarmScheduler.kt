package com.wakeup.app.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.wakeup.app.core.oem.OEMDetector
import com.wakeup.app.core.oem.SchedulingStrategy
import com.wakeup.app.domain.model.Alarm
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of OEM-specific alarm scheduling.
 */
data class SchedulingResult(
    val success: Boolean,
    val strategyUsed: SchedulingStrategy,
    val primaryAlarmId: Int,
    val backupAlarmId: Int? = null,
    val error: String? = null
)

/**
 * OEM-aware alarm scheduler that applies manufacturer-specific workarounds
 * to ensure alarm reliability.
 */
@Singleton
class OEMAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val oemDetector: OEMDetector,
    private val standardScheduler: AlarmSchedulerImpl
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedule an alarm using the best strategy for the current OEM.
     */
    fun scheduleWithOEMWorkarounds(alarm: Alarm): SchedulingResult {
        val strategy = oemDetector.getRecommendedStrategy()
        
        return when (strategy) {
            SchedulingStrategy.STANDARD -> scheduleStandard(alarm)
            SchedulingStrategy.REDUNDANT_WAKELOCK -> scheduleWithWakelock(alarm)
            SchedulingStrategy.FOREGROUND_SERVICE -> scheduleWithForegroundService(alarm)
            SchedulingStrategy.DUAL_ALARM -> scheduleDualAlarm(alarm)
            SchedulingStrategy.SCREEN_ON_WAKE -> scheduleWithScreenOnReceiver(alarm)
        }
    }

    /**
     * Get the recommended scheduling strategy for the current device.
     */
    fun getRecommendedStrategy(): SchedulingStrategy {
        return oemDetector.getRecommendedStrategy()
    }

    /**
     * Standard scheduling - single exact alarm.
     */
    private fun scheduleStandard(alarm: Alarm): SchedulingResult {
        return try {
            standardScheduler.schedule(alarm)
            SchedulingResult(
                success = true,
                strategyUsed = SchedulingStrategy.STANDARD,
                primaryAlarmId = alarm.pendingIntentId
            )
        } catch (e: Exception) {
            SchedulingResult(
                success = false,
                strategyUsed = SchedulingStrategy.STANDARD,
                primaryAlarmId = alarm.pendingIntentId,
                error = e.message
            )
        }
    }

    /**
     * Schedule with redundant wakelock - for Samsung and similar.
     */
    private fun scheduleWithWakelock(alarm: Alarm): SchedulingResult {
        // First schedule the standard alarm
        val standardResult = scheduleStandard(alarm)
        
        if (!standardResult.success) {
            return standardResult
        }

        // Schedule a backup alarm 2 minutes later as safety net
        val backupAlarmId = alarm.pendingIntentId + 100000
        val backupIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGERED
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_IS_BACKUP_ALARM, true)
        }
        
        val backupPendingIntent = PendingIntent.getBroadcast(
            context,
            backupAlarmId,
            backupIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val triggerTime = calculateTriggerTime(alarm).plusMinutes(2)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime.toInstant().toEpochMilli(),
                        backupPendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.toInstant().toEpochMilli(),
                    backupPendingIntent
                )
            }

            return SchedulingResult(
                success = true,
                strategyUsed = SchedulingStrategy.REDUNDANT_WAKELOCK,
                primaryAlarmId = alarm.pendingIntentId,
                backupAlarmId = backupAlarmId
            )
        } catch (e: Exception) {
            // Backup alarm failed, but primary succeeded
            return SchedulingResult(
                success = true, // Still considered success
                strategyUsed = SchedulingStrategy.REDUNDANT_WAKELOCK,
                primaryAlarmId = alarm.pendingIntentId,
                error = "Backup alarm failed: ${e.message}"
            )
        }
    }

    /**
     * Schedule with foreground service requirement - for aggressive OEMs.
     */
    private fun scheduleWithForegroundService(alarm: Alarm): SchedulingResult {
        // First schedule the dual alarm approach
        val dualResult = scheduleDualAlarm(alarm)
        
        // Additionally, schedule the foreground service to start
        // before the alarm time to keep the process alive
        val serviceIntent = Intent(context, OEMForegroundService::class.java).apply {
            putExtra(OEMForegroundService.EXTRA_ALARM_ID, alarm.id)
            putExtra(OEMForegroundService.EXTRA_ALARM_TIME, calculateTriggerTime(alarm).toInstant().toEpochMilli())
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        return dualResult.copy(
            strategyUsed = SchedulingStrategy.FOREGROUND_SERVICE
        )
    }

    /**
     * Schedule dual alarms - exact + inexact backup (Xiaomi strategy).
     */
    private fun scheduleDualAlarm(alarm: Alarm): SchedulingResult {
        // Schedule exact primary alarm
        val primaryResult = try {
            standardScheduler.schedule(alarm)
            true
        } catch (e: Exception) {
            false
        }

        if (!primaryResult) {
            return SchedulingResult(
                success = false,
                strategyUsed = SchedulingStrategy.DUAL_ALARM,
                primaryAlarmId = alarm.pendingIntentId,
                error = "Primary alarm scheduling failed"
            )
        }

        // Schedule inexact backup alarm
        val backupAlarmId = alarm.pendingIntentId + 200000
        val backupIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGERED
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_IS_BACKUP_ALARM, true)
        }
        
        val backupPendingIntent = PendingIntent.getBroadcast(
            context,
            backupAlarmId,
            backupIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val triggerTime = calculateTriggerTime(alarm).plusMinutes(1)
            
            // Use setAndAllowWhileIdle for backup - less strict than exact
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime.toInstant().toEpochMilli(),
                backupPendingIntent
            )

            return SchedulingResult(
                success = true,
                strategyUsed = SchedulingStrategy.DUAL_ALARM,
                primaryAlarmId = alarm.pendingIntentId,
                backupAlarmId = backupAlarmId
            )
        } catch (e: Exception) {
            return SchedulingResult(
                success = true, // Primary succeeded
                strategyUsed = SchedulingStrategy.DUAL_ALARM,
                primaryAlarmId = alarm.pendingIntentId,
                error = "Backup alarm failed: ${e.message}"
            )
        }
    }

    /**
     * Nuclear option: Register SCREEN_ON receiver for the most aggressive OEMs.
     */
    private fun scheduleWithScreenOnReceiver(alarm: Alarm): SchedulingResult {
        // This is the most aggressive strategy - combines dual alarm with
        // a broadcast receiver that listens for screen on events
        val dualResult = scheduleDualAlarm(alarm)

        // Register SCREEN_ON receiver for this specific alarm
        OEMScreenOnReceiver.registerAlarm(context, alarm)
        return dualResult.copy(
            strategyUsed = SchedulingStrategy.SCREEN_ON_WAKE
        )
    }

    /**
     * Cancel an alarm and all its OEM-specific backups.
     */
    fun cancelOEMAlarm(alarm: Alarm) {
        // Cancel primary
        standardScheduler.cancel(alarm.id)
        
        // Cancel backup alarms
        val backupIds = listOf(
            alarm.pendingIntentId + 100000, // REDUNDANT_WAKELOCK backup
            alarm.pendingIntentId + 200000  // DUAL_ALARM backup
        )
        
        backupIds.forEach { backupId ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                backupId,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }

        // Unregister SCREEN_ON receiver if registered
        OEMScreenOnReceiver.unregisterAlarm(context, alarm)

        // Stop foreground service if running
        context.stopService(Intent(context, OEMForegroundService::class.java))
    }

    /**
     * Reschedule all alarms using OEM-specific strategies.
     */
    fun rescheduleAllWithOEM(alarms: List<Alarm>): List<SchedulingResult> {
        return alarms.map { scheduleWithOEMWorkarounds(it) }
    }

    /**
     * Calculate the trigger time for an alarm.
     */
    private fun calculateTriggerTime(alarm: Alarm): ZonedDateTime {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        var triggerTime = now
            .withHour(alarm.hour)
            .withMinute(alarm.minute)
            .withSecond(0)
            .withNano(0)

        // If time has passed today, schedule for tomorrow
        if (triggerTime.isBefore(now)) {
            triggerTime = triggerTime.plusDays(1)
        }

        // Handle repeat days
        if (alarm.repeatDays.isNotEmpty()) {
            val targetDay = alarm.repeatDays.firstOrNull { day ->
                val dayTrigger = triggerTime.with(day)
                dayTrigger.isAfter(now)
            } ?: alarm.repeatDays.first()
            
            triggerTime = triggerTime.with(targetDay)
            if (triggerTime.isBefore(now)) {
                triggerTime = triggerTime.plusWeeks(1)
            }
        }

        return triggerTime
    }

    /**
     * Schedule a health check alarm to verify alarm scheduling is working.
     */
    fun scheduleHealthCheck(): SchedulingResult {
        val healthCheckId = 999999
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_HEALTH_CHECK
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            healthCheckId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return try {
            // Schedule for 15 minutes from now
            val triggerTime = System.currentTimeMillis() + (15 * 60 * 1000)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }

            SchedulingResult(
                success = true,
                strategyUsed = SchedulingStrategy.STANDARD,
                primaryAlarmId = healthCheckId
            )
        } catch (e: Exception) {
            SchedulingResult(
                success = false,
                strategyUsed = SchedulingStrategy.STANDARD,
                primaryAlarmId = healthCheckId,
                error = e.message
            )
        }
    }
}

// Extension constant for AlarmReceiver
const val ACTION_HEALTH_CHECK = "com.wakeup.app.ACTION_HEALTH_CHECK"
const val ACTION_ALARM_TRIGGERED = "com.wakeup.app.ACTION_ALARM_TRIGGERED"
const val EXTRA_IS_BACKUP_ALARM = "is_backup_alarm"
