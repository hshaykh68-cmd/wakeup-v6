package com.wakeup.app.core.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * Utility class for detecting shake gestures using the accelerometer.
 * Implements debouncing and threshold-based detection to prevent false positives.
 */
class ShakeDetector(private val context: Context) {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var sensorListener: SensorEventListener? = null

    private var lastShakeTime = 0L
    private var shakeCount = 0
    private val shakeThreshold = 2.0f // m/s² threshold for shake detection
    private val debounceMs = 250L // minimum time between shakes

    private val _shakeCountFlow = MutableStateFlow(0)
    val shakeCountFlow: StateFlow<Int> = _shakeCountFlow.asStateFlow()

    private var isListening = false

    /**
     * Start listening for shake events.
     * Must be called from the main thread.
     */
    fun startListening(onShake: () -> Unit) {
        if (isListening) return

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            // No accelerometer available
            return
        }

        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Calculate magnitude minus gravity (9.81 m/s²)
                val magnitude = sqrt(x * x + y * y + z * z)
                val delta = magnitude - SensorManager.GRAVITY_EARTH

                val currentTime = System.currentTimeMillis()

                // Check if shake is above threshold and debounced
                if (delta > shakeThreshold && (currentTime - lastShakeTime) > debounceMs) {
                    lastShakeTime = currentTime
                    shakeCount++
                    _shakeCountFlow.value = shakeCount
                    onShake()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not needed for shake detection
            }
        }

        // Use UI delay for shake detection - sufficient for gesture detection with better battery
        sensorManager?.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )

        isListening = true
    }

    /**
     * Stop listening for shake events.
     * Should be called to prevent battery drain when not needed.
     */
    fun stopListening() {
        if (!isListening) return

        sensorListener?.let { listener ->
            sensorManager?.unregisterListener(listener)
        }

        sensorListener = null
        sensorManager = null
        accelerometer = null
        isListening = false
    }

    /**
     * Reset the shake count.
     */
    fun resetCount() {
        shakeCount = 0
        _shakeCountFlow.value = 0
        lastShakeTime = 0L
    }

    /**
     * Check if the accelerometer is available on this device.
     */
    fun isAccelerometerAvailable(): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        return sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
    }

    /**
     * Get the current shake count.
     */
    fun getCurrentShakeCount(): Int = shakeCount
}
