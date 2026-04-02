package com.wakeup.app.core.util

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.app.AlertDialog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for managing SCHEDULE_EXACT_ALARM permission on Android 12+ (API 31+).
 * 
 * On Android 12+, apps need explicit user permission to schedule exact alarms.
 * This helper checks the permission status and provides a way to redirect
 * users to system settings to grant the permission.
 */
@Singleton
class ExactAlarmPermissionHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Check if the app can schedule exact alarms.
     * On Android 12+ (API 31+), this requires the SCHEDULE_EXACT_ALARM permission.
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true // Not required before Android 12
        }
    }

    /**
     * Check if exact alarm permission is required for this device.
     */
    fun isPermissionRequired(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    /**
     * Request the user to grant SCHEDULE_EXACT_ALARM permission.
     * Shows a dialog explaining why this is needed before opening system settings.
     */
    fun requestExactAlarmPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return // Not needed on older devices
        }

        if (canScheduleExactAlarms()) {
            return // Already granted
        }

        showExactAlarmPermissionDialog(activity)
    }

    /**
     * Show an educational dialog before requesting exact alarm permission.
     */
    private fun showExactAlarmPermissionDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Exact Alarm Permission Required")
            .setMessage(
                "WakeUp needs permission to schedule exact alarms so your alarms ring at the precise time you set.\n\n" +
                "Please enable 'Alarms & reminders' permission for WakeUp in system settings."
            )
            .setPositiveButton("Open Settings") { _, _ ->
                openExactAlarmSettings(activity)
            }
            .setNegativeButton("Not Now") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Open system settings for exact alarm permission.
     */
    fun openExactAlarmSettings(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            try {
                activity.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to general app settings if specific intent fails
                val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                activity.startActivity(fallbackIntent)
            }
        }
    }

    /**
     * Check if permission is needed and show dialog if appropriate.
     * Call this when user creates or enables an alarm and scheduling fails.
     */
    fun checkAndRequestPermission(activity: Activity, force: Boolean = false) {
        if (!canScheduleExactAlarms() && (force || isPermissionRequired())) {
            requestExactAlarmPermission(activity)
        }
    }
}
