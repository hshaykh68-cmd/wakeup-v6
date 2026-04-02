package com.wakeup.app.presentation.navigation

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wakeup.app.domain.service.AlarmLabelSuggestionsProvider
import com.wakeup.app.domain.service.HapticsController
import com.wakeup.app.presentation.onboarding.PermissionSetupScreen
import com.wakeup.app.presentation.onboarding.SplashState
import com.wakeup.app.presentation.onboarding.SplashViewModel
import com.wakeup.app.presentation.onboarding.VideoOnboardingScreen
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

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    navigateToCreateAlarmFromWidget: Boolean = false,
    widgetNavigationFlow: StateFlow<String?>? = null
) {
    val splashViewModel: SplashViewModel = hiltViewModel()
    val appNavViewModel = hiltViewModel<AppNavViewModel>()
    val hapticsController = appNavViewModel.hapticsController
    val alarmLabelSuggestionsProvider = appNavViewModel.alarmLabelSuggestionsProvider
    
    val splashState by splashViewModel.state.collectAsState()
    
    // Handle initial widget navigation (when app is first launched)
    LaunchedEffect(navigateToCreateAlarmFromWidget, splashState) {
        if (navigateToCreateAlarmFromWidget && splashState is SplashState.Completed) {
            navController.navigate(Screen.CreateAlarm.route)
        }
    }
    
    // Handle widget navigation when app is already running (from onNewIntent)
    LaunchedEffect(widgetNavigationFlow, splashState) {
        if (splashState is SplashState.Completed) {
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
    }
    
    // Show loading or navigate based on splash state
    when (val state = splashState) {
        is SplashState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        
        is SplashState.FirstLaunch -> {
            VideoOnboardingScreen(
                onComplete = {
                    splashViewModel.completeOnboarding()
                },
                onSkip = {
                    splashViewModel.completeOnboarding()
                }
            )
        }
        
        is SplashState.NeedsPermissions -> {
            PermissionSetupScreen(
                onPermissionsGranted = {
                    splashViewModel.completePermissions()
                }
            )
        }
        
        is SplashState.Completed -> {
            MainNavigationGraph(
                navController = navController,
                hapticsController = hapticsController,
                alarmLabelSuggestionsProvider = alarmLabelSuggestionsProvider
            )
        }
    }
}

@Composable
private fun MainNavigationGraph(
    navController: NavHostController,
    hapticsController: HapticsController,
    alarmLabelSuggestionsProvider: AlarmLabelSuggestionsProvider
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
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
