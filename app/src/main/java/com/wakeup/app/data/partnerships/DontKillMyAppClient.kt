package com.wakeup.app.data.partnerships

import com.wakeup.app.core.oem.OEMConfiguration
import com.wakeup.app.core.oem.OEMConfigurationRegistry
import com.wakeup.app.core.oem.OEMType
import com.wakeup.app.domain.model.ReliabilityReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client for integrating with dontkillmyapp.com resources and community.
 * 
 * This client provides:
 * 1. Deep links to OEM-specific guides
 * 2. Access to community-curated workarounds
 * 3. Contribution of anonymized reliability data (opt-in)
 */
@Singleton
class DontKillMyAppClient @Inject constructor(
    private val oemConfigRegistry: OEMConfigurationRegistry
) {
    companion object {
        const val BASE_URL = "https://dontkillmyapp.com"
        const val API_ENDPOINT = "https://api.dontkillmyapp.com/v1"
    }

    /**
     * Get the dontkillmyapp.com URL for a specific OEM.
     */
    fun getOEMGuideUrl(oemType: OEMType): String {
        return when (oemType) {
            OEMType.SAMSUNG -> "$BASE_URL/samsung"
            OEMType.XIAOMI -> "$BASE_URL/xiaomi"
            OEMType.OPPO -> "$BASE_URL/oppo"
            OEMType.ONEPLUS -> "$BASE_URL/oneplus"
            OEMType.HUAWEI -> "$BASE_URL/huawei"
            OEMType.VIVO -> "$BASE_URL/vivo"
            OEMType.MOTOROLA -> "$BASE_URL/motorola"
            OEMType.REALME -> "$BASE_URL/realme"
            OEMType.GOOGLE -> "$BASE_URL/stock-android"
            OEMType.SONY -> "$BASE_URL/sony"
            OEMType.NOKIA -> "$BASE_URL/nokia"
            OEMType.UNKNOWN -> BASE_URL
        }
    }

    /**
     * Get the OEM guide URL from configuration.
     */
    fun getOEMGuideUrlFromConfig(oemType: OEMType): String {
        return oemConfigRegistry.getConfiguration(oemType).dontkillmyappUrl
    }

    /**
     * Get the severity rating (1-5 thumbs down) for an OEM.
     * This is a static mapping based on dontkillmyapp.com ratings.
     */
    fun getOEMSeverityRating(oemType: OEMType): Int {
        return when (oemType) {
            OEMType.HUAWEI,
            OEMType.XIAOMI -> 5 // Worst offenders
            
            OEMType.OPPO,
            OEMType.VIVO,
            OEMType.REALME -> 4 // Very aggressive
            
            OEMType.SAMSUNG -> 3 // Moderate
            
            OEMType.ONEPLUS -> 2 // Moderate-low
            
            OEMType.MOTOROLA,
            OEMType.NOKIA,
            OEMType.SONY -> 1 // Low aggression
            
            OEMType.GOOGLE -> 0 // Stock Android, no issues
            
            OEMType.UNKNOWN -> 3 // Unknown, assume moderate
        }
    }

    /**
     * Get severity description for display.
     */
    fun getSeverityDescription(rating: Int): String {
        return when (rating) {
            5 -> "Extreme - Multiple workarounds required"
            4 -> "High - Significant setup needed"
            3 -> "Moderate - Some configuration required"
            2 -> "Low - Minimal changes needed"
            1 -> "Very Low - Almost stock behavior"
            0 -> "None - Stock Android"
            else -> "Unknown"
        }
    }

    /**
     * Get visual severity indicator (emoji).
     */
    fun getSeverityEmoji(rating: Int): String {
        return when (rating) {
            5 -> "👎👎👎👎👎"
            4 -> "👎👎👎👎"
            3 -> "👎👎👎"
            2 -> "👎👎"
            1 -> "👎"
            0 -> "✅"
            else -> "❓"
        }
    }

    /**
     * Get the main dontkillmyapp.com URL.
     */
    fun getMainUrl(): String = BASE_URL

    /**
     * Get the "about" URL explaining why OEMs kill apps.
     */
    fun getAboutUrl(): String = "$BASE_URL/about"

    /**
     * Get the problem/solution guide URL.
     */
    fun getProblemGuideUrl(): String = "$BASE_URL/problem"

    /**
     * Open the OEM guide in browser (returns the intent URL).
     */
    fun getOEMGuideIntentUrl(oemType: OEMType): String {
        return getOEMGuideUrl(oemType)
    }

    /**
     * Get a comparison URL showing all OEMs.
     */
    fun getComparisonUrl(): String = "$BASE_URL/all"

    /**
     * Check if dontkillmyapp.com is reachable (for diagnostics).
     */
    suspend fun isReachable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(BASE_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            val responseCode = connection.responseCode
            connection.disconnect()
            responseCode == 200
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Generate a shareable link to dontkillmyapp.com with referral.
     * (If dontkillmyapp.com adds referral tracking in the future)
     */
    fun getShareableLink(oemType: OEMType?): String {
        val base = if (oemType != null && oemType != OEMType.UNKNOWN) {
            getOEMGuideUrl(oemType)
        } else {
            BASE_URL
        }
        // Could add UTM parameters for tracking
        return "$base?utm_source=wakeup_app&utm_medium=app_share"
    }
}

/**
 * Data class representing dontkillmyapp.com guide content.
 * (Could be fetched from API if available in the future)
 */
data class DontKillMyAppGuide(
    val oemType: OEMType,
    val oemName: String,
    val severityRating: Int,
    val severityDescription: String,
    val userSolution: String,
    val developerSolution: String,
    val lastUpdated: String,
    val communityTips: List<String>
)

/**
 * Extension function to create a DontKillMyAppGuide from OEM configuration.
 */
fun OEMConfiguration.toDontKillMyAppGuide(client: DontKillMyAppClient): DontKillMyAppGuide {
    val severity = client.getOEMSeverityRating(oemType)
    return DontKillMyAppGuide(
        oemType = oemType,
        oemName = displayName,
        severityRating = severity,
        severityDescription = client.getSeverityDescription(severity),
        userSolution = warningMessage,
        developerSolution = "Special workarounds: ${specialWorkarounds.joinToString()}",
        lastUpdated = "2024", // Could be dynamic
        communityTips = setupSteps.map { "${it.title}: ${it.description}" }
    )
}
