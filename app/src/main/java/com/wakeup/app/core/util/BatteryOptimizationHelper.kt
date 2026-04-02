package com.wakeup.app.core.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.app.AlertDialog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for managing battery optimization settings.
 * Sleep sounds require uninterrupted background playback which may be affected
 * by Doze mode and App Standby on Android 6.0+ (API 23+).
 */
@Singleton
class BatteryOptimizationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Check if the app is ignoring battery optimizations.
     * This is required for reliable sleep sound playback in background.
     */
    fun isIgnoringBatteryOptimizations(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } else {
            true // Pre-Marshmallow devices don't have this restriction
        }
    }

    /**
     * Request the user to disable battery optimizations for this app.
     * Shows a dialog explaining why this is needed before opening system settings.
     */
    fun requestIgnoreBatteryOptimizations(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return // Not needed on older devices
        }

        if (isIgnoringBatteryOptimizations()) {
            return // Already ignored
        }

        showBatteryOptimizationDialog(activity)
    }

    /**
     * Show an educational dialog before requesting battery optimization exemption.
     */
    private fun showBatteryOptimizationDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Battery Optimization")
            .setMessage(
                "To ensure sleep sounds continue playing reliably throughout the night, " +
                "please disable battery optimization for WakeUp.\n\n" +
                "This prevents Android from automatically stopping the audio to save battery."
            )
            .setPositiveButton("Open Settings") { _, _ ->
                openBatteryOptimizationSettings(activity)
            }
            .setNegativeButton("Not Now") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Open system settings for battery optimization.
     */
    private fun openBatteryOptimizationSettings(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
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
     * Directly request ignore battery optimization (opens system dialog).
     * This requires REQUEST_IGNORE_BATTERY_OPTIMIZATIONS permission.
     */
    fun requestIgnoreBatteryOptimizationsDirect(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            try {
                activity.startActivity(intent)
            } catch (e: Exception) {
                // Fall back to settings page
                openBatteryOptimizationSettings(activity)
            }
        }
    }

    /**
     * Check if battery optimization is needed and show dialog if appropriate.
     * Call this when user starts sleep sounds for the first time.
     */
    fun checkAndRequestBatteryOptimization(activity: Activity, force: Boolean = false) {
        if (!isIgnoringBatteryOptimizations() && (force || shouldShowBatteryOptimizationPrompt())) {
            requestIgnoreBatteryOptimizations(activity)
        }
    }

    /**
     * Determine if we should show the battery optimization prompt.
     * Could be based on shared preferences or other logic.
     */
    private fun shouldShowBatteryOptimizationPrompt(): Boolean {
        // For now, always show if not ignored - user can dismiss with "Not Now"
        return true
    }
}
