package com.wakeup.app.domain.service

/**
 * Interface for haptic feedback controller.
 * Allows for testable implementations and dependency injection.
 */
interface HapticsController {
    fun performSuccess()
    fun performError()
    fun performLightImpact()
    fun performMediumImpact()
    fun performHeavyImpact()
    fun performTick()
    fun performCustomPattern(timings: LongArray, amplitudes: IntArray)
}
