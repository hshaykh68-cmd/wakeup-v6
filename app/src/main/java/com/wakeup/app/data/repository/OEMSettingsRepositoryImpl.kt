package com.wakeup.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.wakeup.app.core.oem.OEMType
import com.wakeup.app.core.oem.WorkaroundType
import com.wakeup.app.domain.model.OEMCertification
import com.wakeup.app.domain.model.ReliabilityReport
import com.wakeup.app.domain.model.SetupStepStatus
import com.wakeup.app.domain.model.VerificationMethod
import com.wakeup.app.domain.repository.OEMSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

import javax.inject.Named

@Singleton
class OEMSettingsRepositoryImpl @Inject constructor(
    @Named("settings") private val dataStore: DataStore<Preferences>
) : OEMSettingsRepository {

    private object PreferencesKeys {
        val OEM_TYPE = stringPreferencesKey("oem_type")
        val DEVICE_MODEL = stringPreferencesKey("oem_device_model")
        val IS_CERTIFIED = booleanPreferencesKey("oem_is_certified")
        val CERTIFICATION_DATE = longPreferencesKey("oem_certification_date")
        val COMPLETED_STEPS = stringSetPreferencesKey("oem_completed_steps")
        val ACTIVE_WORKAROUNDS = stringSetPreferencesKey("oem_active_workarounds")
        val VERIFICATION_SCORE = intPreferencesKey("oem_verification_score")
        val LAST_VERIFICATION_DATE = longPreferencesKey("oem_last_verification_date")
        val HAS_SEEN_SETUP = booleanPreferencesKey("oem_has_seen_setup")
        val DISMISSED_WARNING = booleanPreferencesKey("oem_dismissed_warning")
        val RELIABILITY_REPORTS = stringPreferencesKey("oem_reliability_reports_json")
        val SETUP_COMPLETED = booleanPreferencesKey("oem_setup_completed")
    }

    override suspend fun getOEMCertification(): OEMCertification? {
        val prefs = dataStore.data.first()
        val oemType = prefs[PreferencesKeys.OEM_TYPE]?.let { OEMType.valueOf(it) } ?: return null
        val deviceModel = prefs[PreferencesKeys.DEVICE_MODEL] ?: "Unknown"
        
        return OEMCertification(
            oemType = oemType,
            deviceModel = deviceModel,
            isCertified = prefs[PreferencesKeys.IS_CERTIFIED] ?: false,
            certificationDate = prefs[PreferencesKeys.CERTIFICATION_DATE]?.let { Instant.ofEpochMilli(it) },
            completedSteps = prefs[PreferencesKeys.COMPLETED_STEPS]?.toList() ?: emptyList(),
            activeWorkarounds = prefs[PreferencesKeys.ACTIVE_WORKAROUNDS]
                ?.mapNotNull { try { WorkaroundType.valueOf(it) } catch (e: Exception) { null } }
                ?: emptyList(),
            verificationScore = prefs[PreferencesKeys.VERIFICATION_SCORE] ?: 0,
            lastVerificationDate = prefs[PreferencesKeys.LAST_VERIFICATION_DATE]?.let { Instant.ofEpochMilli(it) },
            shareableBadgeUrl = null, // Generated on demand
            hasSeenSetup = prefs[PreferencesKeys.HAS_SEEN_SETUP] ?: false,
            dismissedWarning = prefs[PreferencesKeys.DISMISSED_WARNING] ?: false
        )
    }

    override suspend fun saveOEMCertification(certification: OEMCertification) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.OEM_TYPE] = certification.oemType.name
            prefs[PreferencesKeys.DEVICE_MODEL] = certification.deviceModel
            prefs[PreferencesKeys.IS_CERTIFIED] = certification.isCertified
            prefs[PreferencesKeys.CERTIFICATION_DATE] = certification.certificationDate?.toEpochMilli() ?: 0L
            prefs[PreferencesKeys.COMPLETED_STEPS] = certification.completedSteps.toSet()
            prefs[PreferencesKeys.ACTIVE_WORKAROUNDS] = certification.activeWorkarounds.map { it.name }.toSet()
            prefs[PreferencesKeys.VERIFICATION_SCORE] = certification.verificationScore
            prefs[PreferencesKeys.LAST_VERIFICATION_DATE] = certification.lastVerificationDate?.toEpochMilli() ?: 0L
            prefs[PreferencesKeys.HAS_SEEN_SETUP] = certification.hasSeenSetup
            prefs[PreferencesKeys.DISMISSED_WARNING] = certification.dismissedWarning
        }
    }

    override suspend fun markStepCompleted(stepId: String, verificationMethod: String, confidence: Int) {
        dataStore.edit { prefs ->
            val currentSteps = prefs[PreferencesKeys.COMPLETED_STEPS] ?: emptySet()
            prefs[PreferencesKeys.COMPLETED_STEPS] = currentSteps + stepId
        }
    }

    override suspend fun markSetupSeen() {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.HAS_SEEN_SETUP] = true
        }
    }

    override suspend fun dismissWarning() {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.DISMISSED_WARNING] = true
        }
    }

    override suspend fun awardCertification() {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_CERTIFIED] = true
            prefs[PreferencesKeys.CERTIFICATION_DATE] = System.currentTimeMillis()
        }
    }

    override suspend fun isCertified(): Boolean {
        return dataStore.data.first()[PreferencesKeys.IS_CERTIFIED] ?: false
    }

    override suspend fun getCompletedSteps(): List<String> {
        return dataStore.data.first()[PreferencesKeys.COMPLETED_STEPS]?.toList() ?: emptyList()
    }

    override suspend fun getActiveWorkarounds(): List<WorkaroundType> {
        return dataStore.data.first()[PreferencesKeys.ACTIVE_WORKAROUNDS]
            ?.mapNotNull { try { WorkaroundType.valueOf(it) } catch (e: Exception) { null } }
            ?: emptyList()
    }

    override suspend fun addWorkaround(workaroundType: WorkaroundType) {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.ACTIVE_WORKAROUNDS] ?: emptySet()
            prefs[PreferencesKeys.ACTIVE_WORKAROUNDS] = current + workaroundType.name
        }
    }

    override suspend fun removeWorkaround(workaroundType: WorkaroundType) {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.ACTIVE_WORKAROUNDS] ?: emptySet()
            prefs[PreferencesKeys.ACTIVE_WORKAROUNDS] = current - workaroundType.name
        }
    }

    override suspend fun recordReliabilityReport(report: ReliabilityReport) {
        val reports = getRecentReliabilityReports(100).toMutableList()
        reports.add(report)
        
        // Keep only last 100 reports
        val trimmedReports = reports.takeLast(100)
        
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.RELIABILITY_REPORTS] = Json.encodeToString(trimmedReports)
        }
    }

    override suspend fun getRecentReliabilityReports(count: Int): List<ReliabilityReport> {
        val json = dataStore.data.first()[PreferencesKeys.RELIABILITY_REPORTS] ?: return emptyList()
        return try {
            Json.decodeFromString<List<ReliabilityReport>>(json).takeLast(count)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getStoredOEMType(): OEMType? {
        return dataStore.data.first()[PreferencesKeys.OEM_TYPE]?.let {
            try { OEMType.valueOf(it) } catch (e: Exception) { null }
        }
    }

    override suspend fun storeOEMType(oemType: OEMType) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.OEM_TYPE] = oemType.name
        }
    }

    override suspend fun isOEMSetupRequired(): Boolean {
        val prefs = dataStore.data.first()
        val hasSeenSetup = prefs[PreferencesKeys.HAS_SEEN_SETUP] ?: false
        val setupCompleted = prefs[PreferencesKeys.SETUP_COMPLETED] ?: false
        val oemType = prefs[PreferencesKeys.OEM_TYPE]?.let { OEMType.valueOf(it) }
        
        // Setup is required if:
        // 1. User hasn't seen the setup screen AND
        // 2. Setup isn't marked as completed AND
        // 3. Either no OEM stored OR the OEM is aggressive
        return !hasSeenSetup && !setupCompleted && 
               (oemType == null || oemType.aggressionLevel != com.wakeup.app.core.oem.AggressionLevel.NONE)
    }

    override suspend fun markOEMSetupCompleted() {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SETUP_COMPLETED] = true
        }
    }

    override suspend fun getLastVerificationDate(): Long? {
        return dataStore.data.first()[PreferencesKeys.LAST_VERIFICATION_DATE]
    }

    override suspend fun updateLastVerification() {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LAST_VERIFICATION_DATE] = System.currentTimeMillis()
        }
    }

    override fun getOEMCertificationFlow(): Flow<OEMCertification?> {
        return dataStore.data.map { prefs ->
            val oemType = prefs[PreferencesKeys.OEM_TYPE]?.let { 
                try { OEMType.valueOf(it) } catch (e: Exception) { null } 
            } ?: return@map null
            
            val deviceModel = prefs[PreferencesKeys.DEVICE_MODEL] ?: "Unknown"
            
            OEMCertification(
                oemType = oemType,
                deviceModel = deviceModel,
                isCertified = prefs[PreferencesKeys.IS_CERTIFIED] ?: false,
                certificationDate = prefs[PreferencesKeys.CERTIFICATION_DATE]?.let { Instant.ofEpochMilli(it) },
                completedSteps = prefs[PreferencesKeys.COMPLETED_STEPS]?.toList() ?: emptyList(),
                activeWorkarounds = prefs[PreferencesKeys.ACTIVE_WORKAROUNDS]
                    ?.mapNotNull { try { WorkaroundType.valueOf(it) } catch (e: Exception) { null } }
                    ?: emptyList(),
                verificationScore = prefs[PreferencesKeys.VERIFICATION_SCORE] ?: 0,
                lastVerificationDate = prefs[PreferencesKeys.LAST_VERIFICATION_DATE]?.let { Instant.ofEpochMilli(it) },
                shareableBadgeUrl = null,
                hasSeenSetup = prefs[PreferencesKeys.HAS_SEEN_SETUP] ?: false,
                dismissedWarning = prefs[PreferencesKeys.DISMISSED_WARNING] ?: false
            )
        }
    }

    override suspend fun clearAllOEMSettings() {
        dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.OEM_TYPE)
            prefs.remove(PreferencesKeys.DEVICE_MODEL)
            prefs.remove(PreferencesKeys.IS_CERTIFIED)
            prefs.remove(PreferencesKeys.CERTIFICATION_DATE)
            prefs.remove(PreferencesKeys.COMPLETED_STEPS)
            prefs.remove(PreferencesKeys.ACTIVE_WORKAROUNDS)
            prefs.remove(PreferencesKeys.VERIFICATION_SCORE)
            prefs.remove(PreferencesKeys.LAST_VERIFICATION_DATE)
            prefs.remove(PreferencesKeys.HAS_SEEN_SETUP)
            prefs.remove(PreferencesKeys.DISMISSED_WARNING)
            prefs.remove(PreferencesKeys.RELIABILITY_REPORTS)
            prefs.remove(PreferencesKeys.SETUP_COMPLETED)
        }
    }
}
