package com.wakeup.app.core.service

import com.wakeup.app.domain.service.HapticsController
import javax.inject.Inject
import javax.inject.Singleton

/**
 * No-op implementation of HapticsController for testing.
 * Does not perform any actual haptic feedback.
 */
@Singleton
class NoOpHapticsController @Inject constructor() : HapticsController {
    override fun performSuccess() { /* No-op */ }
    override fun performError() { /* No-op */ }
    override fun performLightImpact() { /* No-op */ }
    override fun performMediumImpact() { /* No-op */ }
    override fun performHeavyImpact() { /* No-op */ }
    override fun performTick() { /* No-op */ }
    override fun performCustomPattern(timings: LongArray, amplitudes: IntArray) { /* No-op */ }
}
