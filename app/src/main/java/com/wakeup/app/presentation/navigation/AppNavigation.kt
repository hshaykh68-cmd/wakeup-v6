package com.wakeup.app.presentation.navigation

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wakeup.app.domain.service.AlarmLabelSuggestionsProvider
import com.wakeup.app.domain.service.HapticsController
import com.wakeup.app.presentation.onboarding.PermissionSetupScreen
import com.wakeup.app.presentation.onboarding.SplashScreen
import com.wakeup.app.presentation.onboarding.VideoOnboardingScreen
import com.wakeup.app.presentation.onboarding.WelcomeScreen
import com.wakeup.app.presentation.alarm.CreateAlarmScreen
import com.wakeup.app.presentation.alarm.EditAlarmScreen
import com.wakeup.app.presentation.alarm.AlarmRingingActivity
import com.wakeup.app.presentation.alarm.MissionScreen
import com.wakeup.app.presentation.alarm.WakeSuccessScreen
import com.wakeup.app.presentation.premium.PremiumScreen
import com.wakeup.app.presentation.sleep.SleepSoundsScreen
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route,
    navigateToCreateAlarmFromWidget: Boolean = false,
    widgetNavigationFlow: StateFlow<String?>? = null
) {
    val appNavViewModel = hiltViewModel<AppNavViewModel>()
    val hapticsController = appNavViewModel.hapticsController
    val alarmLabelSuggestionsProvider = appNavViewModel.alarmLabelSuggestionsProvider
    
    // Handle initial widget navigation (when app is first launched)
    if (navigateToCreateAlarmFromWidget && startDestination == Screen.Main.route) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.CreateAlarm.route)
        }
    }
    
    // Handle widget navigation when app is already running (from onNewIntent)
    LaunchedEffect(widgetNavigationFlow) {
        widgetNavigationFlow?.collect { destination ->
            if (destination == "create_alarm") {
                // Navigate to Main first, then CreateAlarm
                if (navController.currentDestination?.route != Screen.Main.route) {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = false
                        }
                    }
                }
                // Now navigate to CreateAlarm
                navController.navigate(Screen.CreateAlarm.route)
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToPermissionSetup = {
                    navController.navigate(Screen.PermissionSetup.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onGetStarted = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onContinue = {
                    navController.navigate(Screen.PermissionSetup.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            VideoOnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.PermissionSetup.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(Screen.PermissionSetup.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PermissionSetup.route) {
            PermissionSetupScreen(
                onPermissionsGranted = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.PermissionSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToCreateAlarm = {
                    navController.navigate(Screen.CreateAlarm.route)
                },
                onNavigateToEditAlarm = { alarmId ->
                    navController.navigate(Screen.EditAlarm.createRoute(alarmId))
                },
                onNavigateToPremium = {
                    navController.navigate(Screen.Premium.route)
                },
                onNavigateToSleepSounds = {
                    navController.navigate(Screen.SleepSounds.route)
                },
                onNavigateToOEMSetup = {}
            )
        }

        composable(Screen.CreateAlarm.route) {
            CreateAlarmScreen(
                alarmLabelSuggestionsProvider = alarmLabelSuggestionsProvider,
                hapticsController = hapticsController,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditAlarm.route) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getString("alarmId")
            alarmId?.let {
                EditAlarmScreen(
                    alarmId = it,
                    hapticsController = hapticsController,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Screen.Premium.route) {
            PremiumScreen(
                hapticsController = hapticsController,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.SleepSounds.route) {
            SleepSoundsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@HiltViewModel
class AppNavViewModel @Inject constructor(
    val hapticsController: HapticsController,
    val alarmLabelSuggestionsProvider: AlarmLabelSuggestionsProvider
) : ViewModel()
