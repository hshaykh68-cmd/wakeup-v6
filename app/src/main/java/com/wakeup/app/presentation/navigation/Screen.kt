package com.wakeup.app.presentation.navigation

sealed class Screen(val route: String) {
    // Main Screens
    data object Splash : Screen("splash")
    data object Welcome : Screen("welcome")
    data object Onboarding : Screen("onboarding")
    data object Main : Screen("main")
    
    // Bottom Nav Tabs
    data object Home : Screen("home")
    data object Alarms : Screen("alarms")
    data object Stats : Screen("stats")
    data object Settings : Screen("settings")
    
    // Standalone Screens
    data object CreateAlarm : Screen("create_alarm")
    data object EditAlarm : Screen("edit_alarm/{alarmId}") {
        fun createRoute(alarmId: String) = "edit_alarm/$alarmId"
    }
    data object AlarmRinging : Screen("alarm_ringing")
    data object Mission : Screen("mission")
    data object WakeSuccess : Screen("wake_success")
    data object Premium : Screen("premium")
    data object PermissionSetup : Screen("permission_setup")
    data object SleepSounds : Screen("sleep_sounds")
    data object OEMSetup : Screen("oem_setup")
}
