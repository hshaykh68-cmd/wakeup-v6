package com.wakeup.app.domain.repository

import com.wakeup.app.core.oem.OEMType
import com.wakeup.app.core.oem.WorkaroundType
import com.wakeup.app.domain.model.OEMCertification
import com.wakeup.app.domain.model.ReliabilityReport
import com.wakeup.app.domain.model.SetupStepStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for OEM-specific settings and certifications.
 */
interface OEMSettingsRepository {
    
    /**
     * Get the current OEM certification for the device.
     */
    suspend fun getOEMCertification(): OEMCertification?
    
    /**
     * Save or update the OEM certification.
     */
    suspend fun saveOEMCertification(certification: OEMCertification)
    
    /**
     * Mark a specific setup step as completed.
     */
    suspend fun markStepCompleted(stepId: String, verificationMethod: String, confidence: Int)
    
    /**
     * Mark the user as having seen the OEM setup screen.
     */
    suspend fun markSetupSeen()
    
    /**
     * Dismiss the OEM warning banner.
     */
    suspend fun dismissWarning()
    
    /**
     * Award the "WakeUp Certified" badge.
     */
    suspend fun awardCertification()
    
    /**
     * Check if the device is certified.
     */
    suspend fun isCertified(): Boolean
    
    /**
     * Get the list of completed step IDs.
     */
    suspend fun getCompletedSteps(): List<String>
    
    /**
     * Get the list of active workarounds.
     */
    suspend fun getActiveWorkarounds(): List<WorkaroundType>
    
    /**
     * Add an active workaround.
     */
    suspend fun addWorkaround(workaroundType: WorkaroundType)
    
    /**
     * Remove an active workaround.
     */
    suspend fun removeWorkaround(workaroundType: WorkaroundType)
    
    /**
     * Record a reliability report.
     */
    suspend fun recordReliabilityReport(report: ReliabilityReport)
    
    /**
     * Get reliability reports for analytics (last N reports).
     */
    suspend fun getRecentReliabilityReports(count: Int): List<ReliabilityReport>
    
    /**
     * Get the OEM type stored in settings.
     */
    suspend fun getStoredOEMType(): OEMType?
    
    /**
     * Store the detected OEM type.
     */
    suspend fun storeOEMType(oemType: OEMType)
    
    /**
     * Check if OEM setup is required (first time or new device).
     */
    suspend fun isOEMSetupRequired(): Boolean
    
    /**
     * Mark OEM setup as completed.
     */
    suspend fun markOEMSetupCompleted()
    
    /**
     * Get the last time the certification was verified.
     */
    suspend fun getLastVerificationDate(): Long?
    
    /**
     * Update the last verification timestamp.
     */
    suspend fun updateLastVerification()
    
    /**
     * Get certification as a Flow for reactive UI.
     */
    fun getOEMCertificationFlow(): Flow<OEMCertification?>
    
    /**
     * Clear all OEM settings (for testing or reset).
     */
    suspend fun clearAllOEMSettings()
}
