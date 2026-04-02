package com.wakeup.app.data.alarm

import android.content.Context
import android.os.PowerManager
import android.os.SystemClock

/**
 * Helper class for managing wake locks during alarm execution.
 * Ensures the device stays awake until the alarm service is fully started
 * and while the alarm is actively ringing.
 */
object AlarmWakeLock {
    private const val WAKE_LOCK_TAG = "WakeUp::AlarmWakeLock"
    private const val ACQUISITION_TIMEOUT_MS = 60_000L // 1 minute max

    private var wakeLock: PowerManager.WakeLock? = null

    /**
     * Acquire a partial wake lock to keep CPU running.
     * Call this in AlarmReceiver.onReceive() before starting the service.
     */
    fun acquire(context: Context) {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            
            // Release any existing wake lock first
            release()
            
            // Create and acquire new wake lock
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                WAKE_LOCK_TAG
            ).apply {
                setReferenceCounted(false)
                acquire(ACQUISITION_TIMEOUT_MS)
            }
        } catch (e: Exception) {
            // Fail silently - alarm will still try to fire without wake lock
        }
    }

    /**
     * Release the wake lock.
     * Call this once the alarm service has taken over.
     */
    fun release() {
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

    /**
     * Check if wake lock is currently held.
     */
    fun isHeld(): Boolean {
        return wakeLock?.isHeld ?: false
    }
}
