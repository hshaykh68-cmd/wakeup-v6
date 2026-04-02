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
 * Sleep phase detector using accelerometer movement analysis.
 * Detects light sleep vs deep sleep based on movement patterns.
 * 
 * Algorithm:
 * - Collects accelerometer data in windows
 * - Calculates movement intensity (acceleration variance)
 * - Light sleep = higher movement variance
 * - Deep sleep = low movement variance
 * - Awake = very high movement spikes
 */
class SleepPhaseDetector(private val context: Context) {

    companion object {
        // Movement thresholds (m/s²)
        const val DEEP_SLEEP_THRESHOLD = 0.3f      // Very still
        const val LIGHT_SLEEP_THRESHOLD = 1.5f     // Moderate movement
        const val AWAKE_THRESHOLD = 3.0f           // Significant movement

        // Analysis window (ms) - check every 5 minutes
        const val ANALYSIS_WINDOW_MS = 300000L

        // Minimum samples needed for analysis
        const val MIN_SAMPLES = 50

        // Light sleep requires sustained moderate movement
        const val LIGHT_SLEEP_MIN_DURATION_MS = 60000L // 1 minute of light movement
    }

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var sensorListener: SensorEventListener? = null

    // Movement data buffer
    private val movementReadings = mutableListOf<Float>()
    private var lastAcceleration = 0f
    private var windowStartTime = 0L

    // State flows for sleep phase detection
    private val _currentPhaseFlow = MutableStateFlow(SleepPhase.UNKNOWN)
    val currentPhaseFlow: StateFlow<SleepPhase> = _currentPhaseFlow.asStateFlow()

    private val _movementIntensityFlow = MutableStateFlow(0f)
    val movementIntensityFlow: StateFlow<Float> = _movementIntensityFlow.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private var lightSleepStartTime = 0L
    private var isListening = false

    /**
     * Start monitoring sleep phases.
     * Uses accelerometer with batching to save battery.
     */
    fun startMonitoring() {
        if (isListening) return

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            return
        }

        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                processAccelerometerData(event)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not needed for sleep detection
            }
        }

        // Use normal delay for battery efficiency (not GAME or FASTEST)
        sensorManager?.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        windowStartTime = System.currentTimeMillis()
        isListening = true
        _isMonitoring.value = true
    }

    /**
     * Stop monitoring.
     */
    fun stopMonitoring() {
        if (!isListening) return

        sensorListener?.let { listener ->
            sensorManager?.unregisterListener(listener)
        }

        sensorListener = null
        sensorManager = null
        accelerometer = null
        isListening = false
        _isMonitoring.value = false
        movementReadings.clear()
    }

    /**
     * Check if accelerometer is available.
     */
    fun isAvailable(): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        return sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
    }

    /**
     * Process accelerometer data and detect sleep phase.
     */
    private fun processAccelerometerData(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate magnitude
        val magnitude = sqrt(x * x + y * y + z * z)
        val delta = abs(magnitude - lastAcceleration)
        lastAcceleration = magnitude

        movementReadings.add(delta)

        // Analyze window every 5 minutes
        val currentTime = System.currentTimeMillis()
        if (currentTime - windowStartTime >= ANALYSIS_WINDOW_MS && movementReadings.size >= MIN_SAMPLES) {
            analyzeSleepPhase()
            movementReadings.clear()
            windowStartTime = currentTime
        }
    }

    /**
     * Analyze collected movement data to determine sleep phase.
     */
    private fun analyzeSleepPhase() {
        if (movementReadings.isEmpty()) return

        // Calculate statistics
        val avgMovement = movementReadings.average().toFloat()
        val maxMovement = movementReadings.maxOrNull() ?: 0f
        val variance = movementReadings.map { (it - avgMovement) * (it - avgMovement) }.average().toFloat()

        // Movement intensity score (0-1)
        val intensity = (avgMovement / LIGHT_SLEEP_THRESHOLD).coerceIn(0f, 1f)
        _movementIntensityFlow.value = intensity

        // Determine sleep phase
        val newPhase = when {
            maxMovement > AWAKE_THRESHOLD -> SleepPhase.AWAKE
            avgMovement > LIGHT_SLEEP_THRESHOLD -> {
                // Check if light sleep has been sustained
                if (lightSleepStartTime == 0L) {
                    lightSleepStartTime = System.currentTimeMillis()
                    SleepPhase.DEEP_SLEEP // Still transitioning
                } else {
                    val lightDuration = System.currentTimeMillis() - lightSleepStartTime
                    if (lightDuration >= LIGHT_SLEEP_MIN_DURATION_MS) {
                        SleepPhase.LIGHT_SLEEP
                    } else {
                        SleepPhase.DEEP_SLEEP
                    }
                }
            }
            else -> {
                // Deep sleep or no movement
                lightSleepStartTime = 0L
                SleepPhase.DEEP_SLEEP
            }
        }

        _currentPhaseFlow.value = newPhase
    }

    /**
     * Get current sleep phase.
     */
    fun getCurrentPhase(): SleepPhase = _currentPhaseFlow.value

    /**
     * Check if currently in light sleep (optimal for waking).
     */
    fun isInLightSleep(): Boolean {
        return _currentPhaseFlow.value == SleepPhase.LIGHT_SLEEP
    }

    /**
     * Reset detector state.
     */
    fun reset() {
        movementReadings.clear()
        lightSleepStartTime = 0L
        _currentPhaseFlow.value = SleepPhase.UNKNOWN
        _movementIntensityFlow.value = 0f
        windowStartTime = System.currentTimeMillis()
    }
}

/**
 * Sleep phases detected by the algorithm.
 */
enum class SleepPhase {
    UNKNOWN,      // Not enough data
    DEEP_SLEEP,   // Very still, minimal movement
    LIGHT_SLEEP,  // Moderate movement
    AWAKE         // Significant movement
}

/**
 * Data class for sleep analysis results.
 */
data class SleepAnalysisResult(
    val phase: SleepPhase,
    val movementIntensity: Float,
    val timestamp: Long = System.currentTimeMillis()
)
