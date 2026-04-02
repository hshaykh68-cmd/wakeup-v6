package com.wakeup.app.core.oem

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Configuration data class containing all OEM-specific settings and workarounds.
 */
data class OEMConfiguration(
    val oemType: OEMType,
    val displayName: String,
    val aggressionLevel: AggressionLevel,
    val requiredPermissions: List<String>,
    val requiredSystemSettings: List<SystemSetting>,
    val specialWorkarounds: List<WorkaroundType>,
    val dontkillmyappUrl: String,
    val setupSteps: List<SetupStep>,
    val warningMessage: String,
    val estimatedSetupTimeMinutes: Int
)

/**
 * Registry that provides OEM configurations for all supported manufacturers.
 */
@Singleton
class OEMConfigurationRegistry @Inject constructor() {

    private val configurations = mapOf(
        OEMType.SAMSUNG to createSamsungConfig(),
        OEMType.XIAOMI to createXiaomiConfig(),
        OEMType.OPPO to createOPPOConfig(),
        OEMType.ONEPLUS to createOnePlusConfig(),
        OEMType.HUAWEI to createHuaweiConfig(),
        OEMType.VIVO to createVIVOConfig(),
        OEMType.MOTOROLA to createMotorolaConfig(),
        OEMType.REALME to createRealmeConfig(),
        OEMType.GOOGLE to createGoogleConfig(),
        OEMType.SONY to createSonyConfig(),
        OEMType.NOKIA to createNokiaConfig(),
        OEMType.UNKNOWN to createGenericConfig()
    )

    /**
     * Get configuration for a specific OEM type.
     */
    fun getConfiguration(oemType: OEMType): OEMConfiguration {
        return configurations[oemType] ?: createGenericConfig()
    }

    /**
     * Get all aggressive OEM configurations (HIGH and EXTREME levels).
     */
    fun getAggressiveOEMs(): List<OEMConfiguration> {
        return configurations.values.filter {
            it.aggressionLevel in listOf(AggressionLevel.HIGH, AggressionLevel.EXTREME)
        }
    }

    /**
     * Check if a specific OEM requires special setup.
     */
    fun requiresSpecialSetup(oemType: OEMType): Boolean {
        val config = getConfiguration(oemType)
        return config.aggressionLevel != AggressionLevel.NONE &&
               config.setupSteps.isNotEmpty()
    }

    private fun createSamsungConfig(): OEMConfiguration {
        return OEMConfiguration(
            oemType = OEMType.SAMSUNG,
            displayName = "Samsung One UI",
            aggressionLevel = AggressionLevel.MODERATE,
            requiredPermissions = listOf(),
            requiredSystemSettings = listOf(
                SystemSetting.BATTERY_OPTIMIZATION,
                SystemSetting.ADAPTIVE_BATTERY,
                SystemSetting.UNMONITORED_APPS
            ),
            specialWorkarounds = listOf(
                WorkaroundType.UNMONITORED_APPS,
                WorkaroundType.BATTERY_OPTIMIZATION
            ),
            dontkillmyappUrl = "https://dontkillmyapp.com/samsung",
            setupSteps = listOf(
                SetupStep(
                    id = "samsung_battery_opt",
                    title = "Disable Battery Optimization",
                    description = "Prevent Samsung from putting WakeUp to sleep when the screen is off.",
                    deepLinkAction = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.BATTERY_OPTIMIZATION,
                    isRequired = true,
                    estimatedTimeSeconds = 30
                ),
                SetupStep(
                    id = "samsung_unmonitored",
                    title = "Add to Unmonitored Apps",
                    description = "In Settings > Battery > Background usage limits, add WakeUp to 'Never sleeping apps'.",
                    deepLinkAction = Settings.ACTION_BATTERY_SAVER_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.UNMONITORED_APPS,
                    isRequired = true,
                    estimatedTimeSeconds = 45
                )
            ),
            warningMessage = "Samsung's battery optimization can delay or prevent alarms. Follow these steps to ensure reliable wake-ups.",
            estimatedSetupTimeMinutes = 2
        )
    }

    private fun createXiaomiConfig(): OEMConfiguration {
        return OEMConfiguration(
            oemType = OEMType.XIAOMI,
            displayName = "Xiaomi MIUI/HyperOS",
            aggressionLevel = AggressionLevel.EXTREME,
            requiredPermissions = listOf(),
            requiredSystemSettings = listOf(
                SystemSetting.BATTERY_OPTIMIZATION,
                SystemSetting.AUTO_START,
                SystemSetting.LOCK_IN_RECENTS,
                SystemSetting.BACKGROUND_ACTIVITY
            ),
            specialWorkarounds = listOf(
                WorkaroundType.LOCK_IN_RECENTS,
                WorkaroundType.AUTO_START_PERMISSION,
                WorkaroundType.BATTERY_OPTIMIZATION,
                WorkaroundType.IGNORE_BACKGROUND_LIMITS
            ),
            dontkillmyappUrl = "https://dontkillmyapp.com/xiaomi",
            setupSteps = listOf(
                SetupStep(
                    id = "xiaomi_lock_recents",
                    title = "Lock WakeUp in Recent Apps",
                    description = "Open recent apps, find WakeUp, and tap the lock icon to prevent MIUI from killing it.",
                    deepLinkAction = null,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.LOCK_IN_RECENTS,
                    isRequired = true,
                    estimatedTimeSeconds = 15
                ),
                SetupStep(
                    id = "xiaomi_auto_start",
                    title = "Enable Auto-Start",
                    description = "Go to Settings > Apps > Permissions > Auto-start and enable WakeUp.",
                    deepLinkAction = Settings.ACTION_APPLICATION_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.AUTO_START_PERMISSION,
                    isRequired = true,
                    estimatedTimeSeconds = 45
                ),
                SetupStep(
                    id = "xiaomi_battery_saver",
                    title = "Ignore Battery Saver",
                    description = "In Settings > Battery & performance > App battery saver, set WakeUp to 'No restrictions'.",
                    deepLinkAction = Settings.ACTION_BATTERY_SAVER_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.BATTERY_OPTIMIZATION,
                    isRequired = true,
                    estimatedTimeSeconds = 30
                ),
                SetupStep(
                    id = "xiaomi_background",
                    title = "Allow Background Activity",
                    description = "Ensure WakeUp can run in the background without being killed.",
                    deepLinkAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.IGNORE_BACKGROUND_LIMITS,
                    isRequired = true,
                    estimatedTimeSeconds = 20
                )
            ),
            warningMessage = "Xiaomi MIUI is extremely aggressive with battery optimization. ALL 4 steps are required for reliable alarms.",
            estimatedSetupTimeMinutes = 3
        )
    }

    private fun createOPPOConfig(): OEMConfiguration {
        return OEMConfiguration(
            oemType = OEMType.OPPO,
            displayName = "OPPO ColorOS",
            aggressionLevel = AggressionLevel.HIGH,
            requiredPermissions = listOf(),
            requiredSystemSettings = listOf(
                SystemSetting.BATTERY_OPTIMIZATION,
                SystemSetting.AUTO_START,
                SystemSetting.QUICK_FREEZE
            ),
            specialWorkarounds = listOf(
                WorkaroundType.AUTO_START_PERMISSION,
                WorkaroundType.BATTERY_OPTIMIZATION,
                WorkaroundType.QUICK_FREEZE_DISABLE
            ),
            dontkillmyappUrl = "https://dontkillmyapp.com/oppo",
            setupSteps = listOf(
                SetupStep(
                    id = "oppo_battery",
                    title = "Disable Battery Optimization",
                    description = "Go to Settings > Battery > Battery optimization and set WakeUp to 'Don't optimize'.",
                    deepLinkAction = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.BATTERY_OPTIMIZATION,
                    isRequired = true,
                    estimatedTimeSeconds = 30
                ),
                SetupStep(
                    id = "oppo_auto_start",
                    title = "Allow Auto-Start",
                    description = "In Settings > Apps > App management > WakeUp > Auto launch, enable this option.",
                    deepLinkAction = Settings.ACTION_APPLICATION_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.AUTO_START_PERMISSION,
                    isRequired = true,
                    estimatedTimeSeconds = 45
                ),
                SetupStep(
                    id = "oppo_quick_freeze",
                    title = "Disable Quick Freeze",
                    description = "Turn off quick freeze for WakeUp in battery settings to prevent automatic freezing.",
                    deepLinkAction = Settings.ACTION_BATTERY_SAVER_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.QUICK_FREEZE_DISABLE,
                    isRequired = false,
                    estimatedTimeSeconds = 30
                )
            ),
            warningMessage = "OPPO ColorOS aggressively kills background apps. Complete these steps to ensure your alarm works reliably.",
            estimatedSetupTimeMinutes = 2
        )
    }

    private fun createOnePlusConfig(): OEMConfiguration {
        return OEMConfiguration(
            oemType = OEMType.ONEPLUS,
            displayName = "OnePlus OxygenOS/ColorOS",
            aggressionLevel = AggressionLevel.MODERATE,
            requiredPermissions = listOf(),
            requiredSystemSettings = listOf(
                SystemSetting.BATTERY_OPTIMIZATION,
                SystemSetting.BACKGROUND_ACTIVITY
            ),
            specialWorkarounds = listOf(
                WorkaroundType.BATTERY_OPTIMIZATION,
                WorkaroundType.IGNORE_BACKGROUND_LIMITS
            ),
            dontkillmyappUrl = "https://dontkillmyapp.com/oneplus",
            setupSteps = listOf(
                SetupStep(
                    id = "oneplus_battery",
                    title = "Disable Battery Optimization",
                    description = "Settings > Battery > Battery optimization > WakeUp > Don't optimize",
                    deepLinkAction = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.BATTERY_OPTIMIZATION,
                    isRequired = true,
                    estimatedTimeSeconds = 30
                ),
                SetupStep(
                    id = "oneplus_background",
                    title = "Allow Background Activity",
                    description = "Ensure WakeUp can run in the background.",
                    deepLinkAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.IGNORE_BACKGROUND_LIMITS,
                    isRequired = true,
                    estimatedTimeSeconds = 20
                )
            ),
            warningMessage = "OnePlus devices have moderate battery optimization. These steps ensure reliable alarms.",
            estimatedSetupTimeMinutes = 1
        )
    }

    private fun createHuaweiConfig(): OEMConfiguration {
        return OEMConfiguration(
            oemType = OEMType.HUAWEI,
            displayName = "Huawei EMUI/HarmonyOS",
            aggressionLevel = AggressionLevel.EXTREME,
            requiredPermissions = listOf(),
            requiredSystemSettings = listOf(
                SystemSetting.BATTERY_OPTIMIZATION,
                SystemSetting.APP_LAUNCH,
                SystemSetting.BACKGROUND_ACTIVITY
            ),
            specialWorkarounds = listOf(
                WorkaroundType.APP_LAUNCH_SETTINGS,
                WorkaroundType.BATTERY_OPTIMIZATION,
                WorkaroundType.IGNORE_BACKGROUND_LIMITS
            ),
            dontkillmyappUrl = "https://dontkillmyapp.com/huawei",
            setupSteps = listOf(
                SetupStep(
                    id = "huawei_app_launch",
                    title = "Set App Launch to Manual",
                    description = "Go to Settings > Battery > App launch > WakeUp > Manage manually, enable all options.",
                    deepLinkAction = Settings.ACTION_BATTERY_SAVER_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.APP_LAUNCH_SETTINGS,
                    isRequired = true,
                    estimatedTimeSeconds = 45
                ),
                SetupStep(
                    id = "huawei_battery",
                    title = "Disable Battery Optimization",
                    description = "Settings > Battery > App launch > WakeUp > Manage manually > Secondary launch",
                    deepLinkAction = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.BATTERY_OPTIMIZATION,
                    isRequired = true,
                    estimatedTimeSeconds = 30
                ),
                SetupStep(
                    id = "huawei_background",
                    title = "Allow Background Activity",
                    description = "Ensure 'Run in background' is enabled in app launch settings.",
                    deepLinkAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.IGNORE_BACKGROUND_LIMITS,
                    isRequired = true,
                    estimatedTimeSeconds = 15
                )
            ),
            warningMessage = "Huawei EMUI is extremely aggressive. The App Launch setting is critical for alarm reliability.",
            estimatedSetupTimeMinutes = 2
        )
    }

    private fun createVIVOConfig(): OEMConfiguration {
        return OEMConfiguration(
            oemType = OEMType.VIVO,
            displayName = "VIVO Funtouch OS/OriginOS",
            aggressionLevel = AggressionLevel.HIGH,
            requiredPermissions = listOf(),
            requiredSystemSettings = listOf(
                SystemSetting.BATTERY_OPTIMIZATION,
                SystemSetting.AUTO_START,
                SystemSetting.HIGH_POWER_USAGE
            ),
            specialWorkarounds = listOf(
                WorkaroundType.AUTO_START_PERMISSION,
                WorkaroundType.BATTERY_OPTIMIZATION,
                WorkaroundType.HIGH_POWER_USAGE
            ),
            dontkillmyappUrl = "https://dontkillmyapp.com/vivo",
            setupSteps = listOf(
                SetupStep(
                    id = "vivo_auto_start",
                    title = "Enable Auto-Start",
                    description = "i Manager > App manager > Autostart manager > Allow WakeUp to auto-start",
                    deepLinkAction = null,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.AUTO_START_PERMISSION,
                    isRequired = true,
                    estimatedTimeSeconds = 30
                ),
                SetupStep(
                    id = "vivo_high_power",
                    title = "Allow High Background Power Usage",
                    description = "Settings > Battery > High background power consumption > Allow WakeUp",
                    deepLinkAction = Settings.ACTION_BATTERY_SAVER_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.HIGH_POWER_USAGE,
                    isRequired = true,
                    estimatedTimeSeconds = 30
                ),
                SetupStep(
                    id = "vivo_battery",
                    title = "Disable Battery Optimization",
                    description = "Settings > Battery > Battery optimization > WakeUp > Don't optimize",
                    deepLinkAction = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.BATTERY_OPTIMIZATION,
                    isRequired = true,
                    estimatedTimeSeconds = 30
                )
            ),
            warningMessage = "VIVO devices require special settings to allow high background power usage for alarm apps.",
            estimatedSetupTimeMinutes = 2
        )
    }

    private fun createMotorolaConfig(): OEMConfiguration {
        return OEMConfiguration(
            oemType = OEMType.MOTOROLA,
            displayName = "Motorola",
            aggressionLevel = AggressionLevel.LOW,
            requiredPermissions = listOf(),
            requiredSystemSettings = listOf(
                SystemSetting.BATTERY_OPTIMIZATION
            ),
            specialWorkarounds = listOf(
                WorkaroundType.BATTERY_OPTIMIZATION
            ),
            dontkillmyappUrl = "https://dontkillmyapp.com/motorola",
            setupSteps = listOf(
                SetupStep(
                    id = "motorola_battery",
                    title = "Disable Battery Optimization",
                    description = "Settings > Apps > WakeUp > Battery > Battery optimization > Don't optimize",
                    deepLinkAction = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.BATTERY_OPTIMIZATION,
                    isRequired = true,
                    estimatedTimeSeconds = 30
                )
            ),
            warningMessage = "Motorola devices have minimal battery optimization issues. One quick setting ensures reliability.",
            estimatedSetupTimeMinutes = 1
        )
    }

    private fun createRealmeConfig(): OEMConfiguration {
        return OEMConfiguration(
            oemType = OEMType.REALME,
            displayName = "Realme UI",
            aggressionLevel = AggressionLevel.HIGH,
            requiredPermissions = listOf(),
            requiredSystemSettings = listOf(
                SystemSetting.BATTERY_OPTIMIZATION,
                SystemSetting.AUTO_START,
                SystemSetting.QUICK_FREEZE
            ),
            specialWorkarounds = listOf(
                WorkaroundType.AUTO_START_PERMISSION,
                WorkaroundType.BATTERY_OPTIMIZATION,
                WorkaroundType.QUICK_FREEZE_DISABLE
            ),
            dontkillmyappUrl = "https://dontkillmyapp.com/realme",
            setupSteps = listOf(
                SetupStep(
                    id = "realme_battery",
                    title = "Disable Battery Optimization",
                    description = "Settings > Battery > App battery management > WakeUp > Don't optimize",
                    deepLinkAction = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.BATTERY_OPTIMIZATION,
                    isRequired = true,
                    estimatedTimeSeconds = 30
                ),
                SetupStep(
                    id = "realme_auto_start",
                    title = "Allow Auto-Start",
                    description = "Settings > Apps > App management > WakeUp > Auto launch > Enable",
                    deepLinkAction = Settings.ACTION_APPLICATION_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.AUTO_START_PERMISSION,
                    isRequired = true,
                    estimatedTimeSeconds = 45
                )
            ),
            warningMessage = "Realme UI (similar to OPPO ColorOS) requires background app whitelisting for reliable alarms.",
            estimatedSetupTimeMinutes = 2
        )
    }

    private fun createGoogleConfig(): OEMConfiguration {
        return OEMConfiguration(
            oemType = OEMType.GOOGLE,
            displayName = "Google Pixel",
            aggressionLevel = AggressionLevel.NONE,
            requiredPermissions = listOf(),
            requiredSystemSettings = listOf(),
            specialWorkarounds = listOf(),
            dontkillmyappUrl = "https://dontkillmyapp.com/stock-android",
            setupSteps = listOf(),
            warningMessage = "Google Pixel devices have minimal battery optimization. No special setup required!",
            estimatedSetupTimeMinutes = 0
        )
    }

    private fun createSonyConfig(): OEMConfiguration {
        return OEMConfiguration(
            oemType = OEMType.SONY,
            displayName = "Sony Xperia",
            aggressionLevel = AggressionLevel.LOW,
            requiredPermissions = listOf(),
            requiredSystemSettings = listOf(
                SystemSetting.STAMINA_MODE
            ),
            specialWorkarounds = listOf(
                WorkaroundType.STAMINA_WHITELIST
            ),
            dontkillmyappUrl = "https://dontkillmyapp.com/sony",
            setupSteps = listOf(
                SetupStep(
                    id = "sony_stamina",
                    title = "Add to Stamina Mode Exceptions",
                    description = "If using Stamina mode, add WakeUp to the exceptions list.",
                    deepLinkAction = Settings.ACTION_BATTERY_SAVER_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.STAMINA_WHITELIST,
                    isRequired = false,
                    estimatedTimeSeconds = 30
                )
            ),
            warningMessage = "Sony devices with Stamina mode may need WakeUp added to exceptions. Only needed if you use Stamina.",
            estimatedSetupTimeMinutes = 1
        )
    }

    private fun createNokiaConfig(): OEMConfiguration {
        return OEMConfiguration(
            oemType = OEMType.NOKIA,
            displayName = "Nokia",
            aggressionLevel = AggressionLevel.LOW,
            requiredPermissions = listOf(),
            requiredSystemSettings = listOf(
                SystemSetting.BATTERY_OPTIMIZATION
            ),
            specialWorkarounds = listOf(
                WorkaroundType.EVENWELL_WHITELIST
            ),
            dontkillmyappUrl = "https://dontkillmyapp.com/nokia",
            setupSteps = listOf(
                SetupStep(
                    id = "nokia_battery",
                    title = "Disable Evenwell Battery Optimization",
                    description = "Settings > Apps > See all apps > Menu > Show system > com.evenwell.powersaving > Disable",
                    deepLinkAction = Settings.ACTION_APPLICATION_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.EVENWELL_WHITELIST,
                    isRequired = false,
                    estimatedTimeSeconds = 45
                )
            ),
            warningMessage = "Nokia devices use Evenwell battery optimization. Disabling it or whitelisting WakeUp ensures reliability.",
            estimatedSetupTimeMinutes = 1
        )
    }

    private fun createGenericConfig(): OEMConfiguration {
        return OEMConfiguration(
            oemType = OEMType.UNKNOWN,
            displayName = "Unknown Device",
            aggressionLevel = AggressionLevel.MODERATE,
            requiredPermissions = listOf(),
            requiredSystemSettings = listOf(
                SystemSetting.BATTERY_OPTIMIZATION
            ),
            specialWorkarounds = listOf(
                WorkaroundType.BATTERY_OPTIMIZATION
            ),
            dontkillmyappUrl = "https://dontkillmymyapp.com",
            setupSteps = listOf(
                SetupStep(
                    id = "generic_battery",
                    title = "Disable Battery Optimization",
                    description = "Go to Settings > Battery and disable battery optimization for WakeUp to ensure reliable alarms.",
                    deepLinkAction = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS,
                    deepLinkUri = null,
                    workaroundType = WorkaroundType.BATTERY_OPTIMIZATION,
                    isRequired = true,
                    estimatedTimeSeconds = 30
                )
            ),
            warningMessage = "Your device manufacturer may have battery optimizations that affect alarm reliability.",
            estimatedSetupTimeMinutes = 1
        )
    }
}

/**
 * Helper extension to create deep link intents for setup steps.
 */
fun SetupStep.createDeepLinkIntent(): Intent? {
    return when {
        deepLinkAction != null -> {
            Intent(deepLinkAction).apply {
                deepLinkUri?.let { data = Uri.parse(it) }
            }
        }
        deepLinkUri != null -> {
            Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUri))
        }
        else -> null
    }
}
