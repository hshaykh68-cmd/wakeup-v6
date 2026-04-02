package com.wakeup.app.core.oem

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface for detecting OEM information from the device.
 */
interface OEMDetector {
    /**
     * Detect the OEM type from device properties.
     */
    fun detectOEM(): OEMType

    /**
     * Get the OS version string.
     */
    fun detectOSVersion(): String

    /**
     * Get the device model name.
     */
    fun detectDeviceModel(): String

    /**
     * Check if this OEM has aggressive battery optimization.
     */
    fun isOEMAggressive(): Boolean

    /**
     * Get the recommended scheduling strategy for this device.
     */
    fun getRecommendedStrategy(): SchedulingStrategy

    /**
     * Detect MIUI version for Xiaomi devices (if applicable).
     */
    fun detectMIUIVersion(): String?

    /**
     * Detect One UI version for Samsung devices (if applicable).
     */
    fun detectOneUIVersion(): String?

    /**
     * Detect EMUI version for Huawei devices (if applicable).
     */
    fun detectEMUIVersion(): String?

    /**
     * Detect ColorOS version for OPPO/OnePlus/Realme devices (if applicable).
     */
    fun detectColorOSVersion(): String?

    /**
     * Get a detailed device profile for analytics/debugging.
     */
    fun getDeviceProfile(): DeviceProfile
}

/**
 * Implementation of OEMDetector that uses Build properties and system checks.
 */
@Singleton
class OEMDetectorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : OEMDetector {

    override fun detectOEM(): OEMType {
        val brand = Build.BRAND?.lowercase() ?: ""
        val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
        val device = Build.DEVICE?.lowercase() ?: ""

        // Check for specific indicators first
        return when {
            // Samsung
            brand.contains("samsung") || manufacturer.contains("samsung") -> OEMType.SAMSUNG

            // Xiaomi/Redmi/POCO
            brand.contains("xiaomi") || manufacturer.contains("xiaomi") ||
            brand.contains("redmi") || device.startsWith("redmi") ||
            brand.contains("poco") || device.startsWith("poco") -> OEMType.XIAOMI

            // OPPO
            brand.contains("oppo") || manufacturer.contains("oppo") -> OEMType.OPPO

            // OnePlus
            brand.contains("oneplus") || manufacturer.contains("oneplus") -> OEMType.ONEPLUS

            // Huawei/Honor
            brand.contains("huawei") || manufacturer.contains("huawei") ||
            brand.contains("honor") || manufacturer.contains("honor") -> OEMType.HUAWEI

            // VIVO/iQOO
            brand.contains("vivo") || manufacturer.contains("vivo") ||
            brand.contains("iqoo") || device.contains("iqoo") -> OEMType.VIVO

            // Motorola
            brand.contains("motorola") || manufacturer.contains("motorola") ||
            brand.contains("mot") || device.startsWith("mot-") -> OEMType.MOTOROLA

            // Realme
            brand.contains("realme") || manufacturer.contains("realme") -> OEMType.REALME

            // Google/Pixel
            brand.contains("google") || manufacturer.contains("google") -> OEMType.GOOGLE

            // Sony
            brand.contains("sony") || manufacturer.contains("sony") -> OEMType.SONY

            // Nokia/HMD
            brand.contains("nokia") || manufacturer.contains("hmd") -> OEMType.NOKIA

            else -> OEMType.UNKNOWN
        }
    }

    override fun detectOSVersion(): String {
        return "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    override fun detectDeviceModel(): String {
        return Build.MODEL ?: "Unknown Model"
    }

    override fun isOEMAggressive(): Boolean {
        val oem = detectOEM()
        return when (oem.aggressionLevel) {
            AggressionLevel.HIGH,
            AggressionLevel.EXTREME -> true
            else -> false
        }
    }

    override fun getRecommendedStrategy(): SchedulingStrategy {
        val oem = detectOEM()
        return when (oem.aggressionLevel) {
            AggressionLevel.EXTREME -> SchedulingStrategy.SCREEN_ON_WAKE
            AggressionLevel.HIGH -> SchedulingStrategy.DUAL_ALARM
            AggressionLevel.MODERATE -> SchedulingStrategy.FOREGROUND_SERVICE
            AggressionLevel.LOW -> SchedulingStrategy.REDUNDANT_WAKELOCK
            AggressionLevel.NONE -> SchedulingStrategy.STANDARD
        }
    }

    override fun detectMIUIVersion(): String? {
        if (detectOEM() != OEMType.XIAOMI) return null
        return readSystemProperty("ro.miui.ui.version.name")
            ?: readSystemProperty("ro.miui.ui.version.code")
    }

    override fun detectOneUIVersion(): String? {
        if (detectOEM() != OEMType.SAMSUNG) return null
        // One UI version is loosely tied to Android version
        // One UI 6 = Android 14, One UI 5 = Android 13, etc.
        val oneUIVersion = when (Build.VERSION.SDK_INT) {
            34 -> "6.0" // Android 14
            33 -> "5.1" // Android 13
            32 -> "4.1" // Android 12L
            31 -> "4.0" // Android 12
            30 -> "3.1" // Android 11
            29 -> "2.5" // Android 10
            else -> "Unknown"
        }
        return oneUIVersion
    }

    override fun detectEMUIVersion(): String? {
        if (detectOEM() != OEMType.HUAWEI) return null
        return readSystemProperty("ro.build.version.emui")
    }

    override fun detectColorOSVersion(): String? {
        if (detectOEM() !in listOf(OEMType.OPPO, OEMType.ONEPLUS, OEMType.REALME)) return null
        return readSystemProperty("ro.build.version.opporom")
            ?: readSystemProperty("ro.build.version.ota")
    }

    /**
     * Read a system property using SystemProperties reflection (avoids Runtime.exec).
     */
    private fun readSystemProperty(name: String): String? = try {
        val systemProperties = Class.forName("android.os.SystemProperties")
        val get = systemProperties.getMethod("get", String::class.java, String::class.java)
        (get.invoke(null, name, "") as? String)?.takeIf { it.isNotBlank() }
    } catch (e: Exception) { null }

    /**
     * Check if a specific system feature is available.
     */
    fun hasSystemFeature(feature: String): Boolean {
        return context.packageManager.hasSystemFeature(feature)
    }

    /**
     * Check if the device has a specific OEM customization.
     */
    fun hasOEMCustomization(): Boolean {
        val oem = detectOEM()
        return oem != OEMType.GOOGLE && oem != OEMType.UNKNOWN
    }

    /**
     * Get a detailed device profile for analytics/debugging.
     */
    override fun getDeviceProfile(): DeviceProfile {
        val oem = detectOEM()
        return DeviceProfile(
            oemType = oem,
            deviceModel = detectDeviceModel(),
            osVersion = detectOSVersion(),
            oemUiVersion = when (oem) {
                OEMType.XIAOMI -> detectMIUIVersion()
                OEMType.SAMSUNG -> detectOneUIVersion()
                OEMType.HUAWEI -> detectEMUIVersion()
                OEMType.OPPO, OEMType.ONEPLUS, OEMType.REALME -> detectColorOSVersion()
                else -> null
            },
            aggressionLevel = oem.aggressionLevel,
            recommendedStrategy = getRecommendedStrategy(),
            isAggressive = isOEMAggressive()
        )
    }
}

/**
 * Complete device profile information.
 */
data class DeviceProfile(
    val oemType: OEMType,
    val deviceModel: String,
    val osVersion: String,
    val oemUiVersion: String?,
    val aggressionLevel: AggressionLevel,
    val recommendedStrategy: SchedulingStrategy,
    val isAggressive: Boolean
)
