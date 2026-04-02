package com.wakeup.app.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wakeup.app.data.alarm.OEMBroadcastReceivers
import com.wakeup.app.domain.usecase.RescheduleAllAlarmsUseCase
import com.wakeup.app.domain.usecase.GetEnabledAlarmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var rescheduleAllAlarmsUseCase: RescheduleAllAlarmsUseCase
    
    @Inject
    lateinit var getEnabledAlarmsUseCase: GetEnabledAlarmsUseCase
    
    @Inject
    lateinit var oemAlarmScheduler: OEMAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Register OEM broadcast receivers for aggressive OEMs
            OEMBroadcastReceivers.registerAll(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                // First try OEM-aware scheduling
                val alarms = getEnabledAlarmsUseCase().first()
                val results = oemAlarmScheduler.rescheduleAllWithOEM(alarms)
                
                // Log results
                val successCount = results.count { it.success }
                val failureCount = results.size - successCount
                
                // If OEM scheduling had failures, fall back to standard scheduling
                if (failureCount > 0) {
                    val result = rescheduleAllAlarmsUseCase()
                    result.getOrNull()?.let { failedCount ->
                        if (failedCount > 0) {
                            // Some alarms were scheduled with inexact fallback
                            // This is acceptable - they'll still fire but may have slight delay
                        }
                    }
                }
            }
        }
    }
}
