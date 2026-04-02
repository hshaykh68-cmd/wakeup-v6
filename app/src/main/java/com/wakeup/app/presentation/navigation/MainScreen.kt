package com.wakeup.app.presentation.navigation

import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.data.admob.AdMobManager
import com.wakeup.app.domain.service.HapticsController
import com.wakeup.app.presentation.alarms.AlarmListScreen
import com.wakeup.app.presentation.home.HomeScreen
import com.wakeup.app.presentation.settings.SettingsScreen
import com.wakeup.app.presentation.stats.StatsScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    onNavigateToCreateAlarm: () -> Unit,
    onNavigateToEditAlarm: (String) -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToSleepSounds: () -> Unit,
    onNavigateToOEMSetup: () -> Unit = {}
) {
    val mainViewModel = hiltViewModel<MainViewModel>()
    val adMobManager = mainViewModel.adMobManager
    val hapticsController = mainViewModel.hapticsController
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Alarms,
        BottomNavItem.Stats,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            Column {
                // Banner Ad
                BannerAdView(adMobManager = adMobManager)
                
                // Glassmorphic Bottom Navigation
                GlassmorphicBottomNav(
                    items = bottomNavItems,
                    currentDestination = currentDestination,
                    onItemClick = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onCreateAlarm = onNavigateToCreateAlarm,
                    onNavigateToAlarms = {
                        navController.navigate(Screen.Alarms.route)
                    },
                    onNavigateToSleepSounds = onNavigateToSleepSounds
                )
            }
            composable(Screen.Alarms.route) {
                AlarmListScreen(
                    hapticsController = hapticsController,
                    onCreateAlarm = onNavigateToCreateAlarm,
                    onEditAlarm = onNavigateToEditAlarm
                )
            }
            composable(Screen.Stats.route) {
                StatsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToPremium = onNavigateToPremium
                )
            }
        }
    }
}

@Composable
private fun BannerAdView(adMobManager: AdMobManager) {
    AndroidView(
        factory = { context ->
            val adView = adMobManager.createBannerAdView(isTest = true)
            adView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            adView
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@HiltViewModel
class MainViewModel @Inject constructor(
    val adMobManager: AdMobManager,
    val hapticsController: HapticsController
) : ViewModel()

@Composable
private fun GlassmorphicBottomNav(
    items: List<BottomNavItem>,
    currentDestination: androidx.navigation.NavDestination?,
    onItemClick: (BottomNavItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Glassmorphic container with blur
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
        )

        // Content layer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onItemClick(item) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Icon with glassmorphic background when selected
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            WakeUpColors.iosBlue.copy(alpha = 0.3f),
                                            WakeUpColors.iosPurple.copy(alpha = 0.2f)
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(Color.Transparent, Color.Transparent)
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : BottomNavItem(
        Screen.Home.route,
        "Home",
        Icons.Filled.Home,
        Icons.Outlined.Home
    )
    data object Alarms : BottomNavItem(
        Screen.Alarms.route,
        "Alarms",
        Icons.Filled.Alarm,
        Icons.Outlined.Alarm
    )
    data object Stats : BottomNavItem(
        Screen.Stats.route,
        "Stats",
        Icons.Filled.BarChart,
        Icons.Outlined.BarChart
    )
    data object Settings : BottomNavItem(
        Screen.Settings.route,
        "Settings",
        Icons.Filled.Settings,
        Icons.Outlined.Settings
    )
}
