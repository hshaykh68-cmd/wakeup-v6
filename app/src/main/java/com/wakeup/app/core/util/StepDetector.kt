package com.wakeup.app.core.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Utility class for detecting steps using hardware step counter or accelerometer fallback.
 * Implements efficient step counting with battery optimization and haptic feedback triggers.
 */
class StepDetector(private val context: Context) {

    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var accelerometer: Sensor? = null
    private var sensorListener: SensorEventListener? = null

    // For hardware step counter
    private var initialStepCount = -1
    private var currentStepCount = 0

    // For accelerometer fallback
    private var lastAcceleration = 0f
    private var lastStepTime = 0L
    private val stepThreshold = 12f // m/s² threshold for step detection
    private val stepDelayMs = 250L // minimum time between steps (max 4 steps/sec)
    private var accelerometerStepCount = 0

    private val _stepCountFlow = MutableStateFlow(0)
    val stepCountFlow: StateFlow<Int> = _stepCountFlow.asStateFlow()

    private var isListening = false
    private var useHardwareStepCounter = false

    /**
     * Check if hardware step counter is available (more accurate and battery efficient).
     */
    fun isHardwareStepCounterAvailable(): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        return sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
    }

    /**
     * Check if any step detection is available (hardware or accelerometer).
     */
    fun isStepDetectionAvailable(): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        return sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
                || sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
    }

    /**
     * Start listening for step events.
     * Prefers hardware step counter, falls back to accelerometer.
     * Must be called from the main thread.
     */
    fun startListening(targetSteps: Int, onStep: (currentSteps: Int) -> Unit, onComplete: () -> Unit) {
        if (isListening) return

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        useHardwareStepCounter = stepCounterSensor != null

        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_STEP_COUNTER -> handleHardwareStep(event, targetSteps, onStep, onComplete)
                    Sensor.TYPE_ACCELEROMETER -> handleAccelerometerStep(event, targetSteps, onStep, onComplete)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not needed for step detection
            }
        }

        if (useHardwareStepCounter) {
            // Use hardware step counter (battery efficient)
            sensorManager?.registerListener(
                sensorListener,
                stepCounterSensor,
                SensorManager.SENSOR_DELAY_UI
            )
        } else if (accelerometer != null) {
            // Fallback to accelerometer
            sensorManager?.registerListener(
                sensorListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )
        } else {
            // No sensors available
            return
        }

        isListening = true
    }

    private fun handleHardwareStep(
        event: SensorEvent,
        targetSteps: Int,
        onStep: (currentSteps: Int) -> Unit,
        onComplete: () -> Unit
    ) {
        val totalSteps = event.values[0].toInt()

        if (initialStepCount == -1) {
            initialStepCount = totalSteps
        }

        currentStepCount = totalSteps - initialStepCount
        _stepCountFlow.value = currentStepCount
        onStep(currentStepCount)

        if (currentStepCount >= targetSteps) {
            stopListening()
            onComplete()
        }
    }

    private fun handleAccelerometerStep(
        event: SensorEvent,
        targetSteps: Int,
        onStep: (currentSteps: Int) -> Unit,
        onComplete: () -> Unit
    ) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate magnitude
        val magnitude = sqrt(x * x + y * y + z * z)
        val delta = abs(magnitude - lastAcceleration)
        lastAcceleration = magnitude

        val currentTime = System.currentTimeMillis()

        // Detect step based on acceleration spike and debounce
        if (delta > stepThreshold && (currentTime - lastStepTime) > stepDelayMs) {
            lastStepTime = currentTime
            accelerometerStepCount++
            _stepCountFlow.value = accelerometerStepCount
            onStep(accelerometerStepCount)

            if (accelerometerStepCount >= targetSteps) {
                stopListening()
                onComplete()
            }
        }
    }

    /**
     * Stop listening for step events.
     * Should be called to prevent battery drain when mission is complete or dismissed.
     */
    fun stopListening() {
        if (!isListening) return

        sensorListener?.let { listener ->
            sensorManager?.unregisterListener(listener)
        }

        sensorListener = null
        sensorManager = null
        stepCounterSensor = null
        accelerometer = null
        isListening = false
    }

    /**
     * Reset the step count.
     */
    fun resetCount() {
        initialStepCount = -1
        currentStepCount = 0
        accelerometerStepCount = 0
        _stepCountFlow.value = 0
    }

    /**
     * Get the current step count.
     */
    fun getCurrentStepCount(): Int = _stepCountFlow.value

    /**
     * Get target steps based on difficulty.
     */
    fun getTargetStepsForDifficulty(difficultyOrdinal: Int): Int {
        return when (difficultyOrdinal) {
            0 -> 20  // EASY
            1 -> 50  // MEDIUM
            2 -> 100 // HARD
            else -> 50
        }
    }

    /**
     * Check if we should trigger haptic feedback (every 10 steps).
     */
    fun shouldTriggerHaptic(): Boolean {
        val steps = _stepCountFlow.value
        return steps > 0 && steps % 10 == 0
    }
}
