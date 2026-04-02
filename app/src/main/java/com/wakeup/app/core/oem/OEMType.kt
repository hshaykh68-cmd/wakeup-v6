package com.wakeup.app.core.oem

/**
 * Enum representing all supported OEM types with their characteristics.
 */
enum class OEMType(
    val brandName: String,
    val displayName: String,
    val aggressionLevel: AggressionLevel
) {
    SAMSUNG(
        brandName = "samsung",
        displayName = "Samsung One UI",
        aggressionLevel = AggressionLevel.MODERATE
    ),
    XIAOMI(
        brandName = "xiaomi",
        displayName = "Xiaomi MIUI/HyperOS",
        aggressionLevel = AggressionLevel.EXTREME
    ),
    OPPO(
        brandName = "oppo",
        displayName = "OPPO ColorOS",
        aggressionLevel = AggressionLevel.HIGH
    ),
    ONEPLUS(
        brandName = "oneplus",
        displayName = "OnePlus OxygenOS/ColorOS",
        aggressionLevel = AggressionLevel.MODERATE
    ),
    HUAWEI(
        brandName = "huawei",
        displayName = "Huawei EMUI/HarmonyOS",
        aggressionLevel = AggressionLevel.EXTREME
    ),
    VIVO(
        brandName = "vivo",
        displayName = "VIVO Funtouch OS/OriginOS",
        aggressionLevel = AggressionLevel.HIGH
    ),
    MOTOROLA(
        brandName = "motorola",
        displayName = "Motorola",
        aggressionLevel = AggressionLevel.LOW
    ),
    REALME(
        brandName = "realme",
        displayName = "Realme UI",
        aggressionLevel = AggressionLevel.HIGH
    ),
    GOOGLE(
        brandName = "google",
        displayName = "Google Pixel",
        aggressionLevel = AggressionLevel.NONE
    ),
    SONY(
        brandName = "sony",
        displayName = "Sony Xperia",
        aggressionLevel = AggressionLevel.LOW
    ),
    NOKIA(
        brandName = "nokia",
        displayName = "Nokia",
        aggressionLevel = AggressionLevel.LOW
    ),
    UNKNOWN(
        brandName = "unknown",
        displayName = "Unknown Device",
        aggressionLevel = AggressionLevel.MODERATE
    );

    companion object {
        fun fromBrand(brand: String): OEMType {
            return when (brand.lowercase()) {
                "samsung" -> SAMSUNG
                "xiaomi", "redmi", "poco" -> XIAOMI
                "oppo" -> OPPO
                "oneplus" -> ONEPLUS
                "huawei", "honor" -> HUAWEI
                "vivo", "iqoo" -> VIVO
                "motorola", "motorola solutions" -> MOTOROLA
                "realme" -> REALME
                "google" -> GOOGLE
                "sony" -> SONY
                "nokia", "hmd global" -> NOKIA
                else -> UNKNOWN
            }
        }
    }
}

/**
 * Aggression level of OEM battery optimization.
 */
enum class AggressionLevel {
    NONE,       // Stock Android - minimal issues (Pixel, some Motorola)
    LOW,        // Minor optimizations (Sony, Nokia, Motorola)
    MODERATE,   // Some aggressive features (Samsung, OnePlus)
    HIGH,       // Very aggressive (OPPO, VIVO, Realme)
    EXTREME     // Extremely aggressive (Xiaomi, Huawei)
}

/**
 * Different scheduling strategies based on OEM aggression level.
 */
enum class SchedulingStrategy {
    STANDARD,           // Stock Android - single AlarmManager
    REDUNDANT_WAKELOCK, // Samsung-style - AlarmManager + partial wakelock
    FOREGROUND_SERVICE, // Aggressive OEMs - persistent notification required
    DUAL_ALARM,         // Xiaomi-style - exact + inexact backup alarms
    SCREEN_ON_WAKE      // Nuclear option - SCREEN_ON broadcast receiver
}

/**
 * Types of workarounds that can be applied for specific OEMs.
 */
enum class WorkaroundType {
    LOCK_IN_RECENTS,           // Lock app in recent apps (Xiaomi, OPPO)
    AUTO_START_PERMISSION,     // Auto-start permission (Xiaomi, OPPO, VIVO)
    BATTERY_OPTIMIZATION,      // Standard battery optimization ignore
    APP_LAUNCH_SETTINGS,       // Huawei-specific app launch settings
    UNMONITORED_APPS,          // Samsung unmonitored apps list
    IGNORE_BACKGROUND_LIMITS,  // Background activity permission
    HIGH_POWER_USAGE,          // VIVO high background power usage
    QUICK_FREEZE_DISABLE,      // OPPO/OnePlus quick freeze
    STAMINA_WHITELIST,         // Sony Stamina mode
    EVENWELL_WHITELIST         // Nokia Evenwell battery optimization
}

/**
 * Individual setup steps for OEM configuration.
 */
data class SetupStep(
    val id: String,
    val title: String,
    val description: String,
    val deepLinkAction: String?,  // Android intent action to open settings
    val deepLinkUri: String?,      // URI for settings page
    val workaroundType: WorkaroundType,
    val isRequired: Boolean = true,
    val estimatedTimeSeconds: Int = 30
)

/**
 * System settings that need to be configured for OEM compatibility.
 */
enum class SystemSetting {
    BATTERY_OPTIMIZATION,
    AUTO_START,
    APP_LAUNCH,
    BACKGROUND_ACTIVITY,
    LOCK_IN_RECENTS,
    STAMINA_MODE,
    ADAPTIVE_BATTERY,
    HIGH_POWER_USAGE,
    QUICK_FREEZE,
    UNMONITORED_APPS
}
