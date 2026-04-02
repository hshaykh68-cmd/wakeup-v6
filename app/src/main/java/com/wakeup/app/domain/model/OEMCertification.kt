package com.wakeup.app.domain.model

import com.wakeup.app.core.oem.OEMType
import com.wakeup.app.core.oem.SetupStep
import com.wakeup.app.core.oem.WorkaroundType
import java.time.Instant

/**
 * Represents the certification status for a specific OEM device.
 */
data class OEMCertification(
    val oemType: OEMType,
    val deviceModel: String,
    val isCertified: Boolean,
    val certificationDate: Instant?,
    val completedSteps: List<String>, // IDs of completed setup steps
    val activeWorkarounds: List<WorkaroundType>,
    val verificationScore: Int, // 0-100
    val lastVerificationDate: Instant?,
    val shareableBadgeUrl: String?, // Deep link for social sharing
    val hasSeenSetup: Boolean, // Whether user has viewed setup screen
    val dismissedWarning: Boolean // Whether user dismissed warning banner
)

/**
 * Status of a specific setup step.
 */
data class SetupStepStatus(
    val stepId: String,
    val isCompleted: Boolean,
    val completedDate: Instant?,
    val verificationMethod: VerificationMethod,
    val confidence: Int // 0-100, how sure we are it's completed
)

enum class VerificationMethod {
    SYSTEM_CHECK,      // We detected the setting is enabled
    USER_CONFIRMED,    // User tapped "Done"
    HEURISTIC,         // Based on behavior (e.g., alarm fired on time)
    ASSUMED            // We think it's done but can't verify
}

/**
 * A reliability report for analytics (privacy-preserving).
 */
data class ReliabilityReport(
    val oemType: OEMType,
    val osVersion: String,      // Android version only
    val deviceModelHash: String, // Hashed model name
    val alarmFired: Boolean,
    val scheduledTime: Long,    // Hour only for privacy
    val delayMinutes: Int?,     // If fired late
    val workaroundsActive: List<WorkaroundType>,
    val schedulingStrategy: String,
    val reportDate: Instant
)

/**
 * Result of verifying OEM setup.
 */
data class VerificationResult(
    val isFullyVerified: Boolean,
    val completedSteps: List<SetupStepStatus>,
    val pendingSteps: List<SetupStep>,
    val overallScore: Int,
    val recommendation: String
)
