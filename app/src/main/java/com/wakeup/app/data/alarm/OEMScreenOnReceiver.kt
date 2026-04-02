package com.wakeup.app.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import com.wakeup.app.domain.model.Alarm
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * Nuclear option receiver for extremely aggressive OEMs (primarily Xiaomi).
 * 
 * This receiver listens for SCREEN_ON broadcasts which are triggered when the user
 * turns on their screen. On Xiaomi devices with aggressive battery optimization,
 * this can be the only reliable way to wake the app to check if an alarm should fire.
 * 
 * This is used as a last resort backup when:
 * 1. The alarm is scheduled within the next few minutes
 * 2. The device is from an extremely aggressive OEM
 * 3. Other scheduling methods may have been killed
 */
@AndroidEntryPoint
class OEMScreenOnReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmScheduler: AlarmSchedulerImpl
    
    companion object {
        private val registeredAlarms = mutableSetOf<String>()
        private var isReceiverRegistered = false
        
        /**
         * Register this receiver for a specific alarm.
         * Should be called when scheduling alarms for aggressive OEMs.
         */
        fun registerAlarm(context: Context, alarm: Alarm) {
            registeredAlarms.add(alarm.id)
            
            if (!isReceiverRegistered) {
                val filter = IntentFilter().apply {
                    addAction(Intent.ACTION_SCREEN_ON)
                    addAction(Intent.ACTION_USER_PRESENT)
                }
                
                val receiver = OEMScreenOnReceiver()
                context.applicationContext.registerReceiver(receiver, filter)
                isReceiverRegistered = true
            }
        }
        
        /**
         * Unregister this receiver for a specific alarm.
         */
        fun unregisterAlarm(context: Context, alarm: Alarm) {
            registeredAlarms.remove(alarm.id)
            
            // If no more alarms are registered, we could unregister the receiver
            // but we keep it active for simplicity
        }
        
        /**
         * Clear all registered alarms.
         */
        fun clearAll() {
            registeredAlarms.clear()
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON,
            Intent.ACTION_USER_PRESENT -> {
                // Check if any registered alarm should fire soon
                checkAlarmsOnWake(context)
            }
        }
    }
    
    private fun checkAlarmsOnWake(context: Context) {
        val now = LocalDateTime.now(ZoneId.systemDefault())
        
        // This would need to be connected to the alarm repository
        // For now, this is a placeholder implementation
        // In production, you'd query the database for upcoming alarms
        
        // If there's an alarm within the next 2 minutes that we haven't
        // triggered yet, we should trigger it now
        
        // Note: This is a safety net - the primary alarm scheduling
        // should handle normal cases. This is only for when the
        // OEM has killed our scheduled alarms.
    }
}

/**
 * Helper to register all OEM broadcast receivers at app startup.
 */
object OEMBroadcastReceivers {
    
    fun registerAll(context: Context) {
        OEMTimeTickReceiver.register(context)
        OEMChargingReceiver.register(context)
        // OEMScreenOnReceiver is registered per-alarm as needed
    }
}
