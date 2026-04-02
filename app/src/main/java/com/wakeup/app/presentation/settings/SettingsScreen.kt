package com.wakeup.app.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.domain.model.PremiumType
import com.wakeup.app.domain.repository.ThemeMode
import com.wakeup.app.presentation.oem.OEMSetupViewModel
import com.wakeup.app.presentation.oem.OEMCertificationBadge
import androidx.compose.material.icons.filled.Security

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    oemViewModel: OEMSetupViewModel = hiltViewModel(),
    onNavigateToPremium: () -> Unit,
    onNavigateToOEMSetup: () -> Unit
) {
    val isPremium by viewModel.isPremium.collectAsState()
    val premiumType by viewModel.premiumType.collectAsState()
    val use24Hour by viewModel.use24Hour.collectAsState()
    val defaultVibration by viewModel.defaultVibration.collectAsState()
    val gradualVolume by viewModel.gradualVolume.collectAsState()
    val snoozeDuration by viewModel.snoozeDuration.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val useIOSStyleTimePicker by viewModel.useIOSStyleTimePicker.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Premium Card
            if (!isPremium) {
                GlassPremiumCard(onNavigateToPremium)
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                GlassPremiumActiveCard(premiumType)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // OEM Certification Section
            OEMCertificationSection(
                onNavigateToOEMSetup = onNavigateToOEMSetup
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Appearance Section with glassmorphism
            GlassSettingsSection(title = "Appearance") {
                // Theme Mode Selector
                ThemeModeSelector(
                    currentMode = themeMode,
                    onModeSelected = { viewModel.setThemeMode(it) }
                )
                
                SettingsDivider()
                
                SettingsSwitchItem(
                    icon = Icons.Default.WatchLater,
                    title = "24-Hour Format",
                    subtitle = "Display time in 24h format",
                    checked = use24Hour,
                    onCheckedChange = { viewModel.setUse24HourFormat(it) }
                )
                
                SettingsDivider()
                
                SettingsSwitchItem(
                    icon = Icons.Default.Palette,
                    title = "iOS-Style Time Picker",
                    subtitle = "Use scroll-wheel time picker (Material3 by default)",
                    checked = useIOSStyleTimePicker,
                    onCheckedChange = { viewModel.setUseIOSStyleTimePicker(it) }
                )
            }

            // Alarm Settings Section with glassmorphism
            GlassSettingsSection(title = "Alarm Settings") {
                SettingsSwitchItem(
                    icon = Icons.Default.Vibration,
                    title = "Default Vibration",
                    subtitle = "Vibrate on all alarms",
                    checked = defaultVibration,
                    onCheckedChange = { viewModel.setDefaultVibration(it) }
                )
                
                SettingsDivider()
                
                SettingsSwitchItem(
                    icon = Icons.Default.VolumeUp,
                    title = "Gradual Volume",
                    subtitle = "Slowly increase alarm volume",
                    checked = gradualVolume,
                    onCheckedChange = { viewModel.setGradualVolume(it) }
                )
                
                SettingsDivider()
                
                // Snooze Duration
                SnoozeDurationSelector(
                    duration = snoozeDuration,
                    onDurationChange = { viewModel.setSnoozeDuration(it) }
                )
            }

            // About Section with glassmorphism
            GlassSettingsSection(title = "About") {
                // Rate App
                SettingsClickableItem(
                    icon = Icons.Default.ThumbUp,
                    title = "Rate WakeUp",
                    subtitle = "Love the app? Rate us on Play Store",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                        context.startActivity(intent)
                    }
                )
                
                SettingsDivider()
                
                // Privacy Policy
                SettingsClickableItem(
                    icon = Icons.Default.Info,
                    title = "Privacy Policy",
                    subtitle = "Read our privacy policy",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wakeup.app/privacy"))
                        context.startActivity(intent)
                    }
                )
                
                SettingsDivider()
                
                // Share App
                SettingsClickableItem(
                    icon = Icons.Default.Share,
                    title = "Share WakeUp",
                    subtitle = "Tell your friends about us",
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "WakeUp Alarm App")
                            putExtra(Intent.EXTRA_TEXT, "Check out WakeUp - the best alarm app! https://play.google.com/store/apps/details?id=${context.packageName}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share WakeUp"))
                    }
                )
                
                SettingsDivider()
                
                // App Version
                SettingsClickableItem(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "1.0.0",
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GlassPremiumCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WakeUpColors.iosPurple.copy(alpha = 0.2f),
                        WakeUpColors.iosPurple.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = WakeUpColors.iosPurple.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = WakeUpColors.iosPurple.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Premium",
                        tint = WakeUpColors.iosPurple,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text(
                        text = "WakeUp Premium",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = WakeUpColors.iosPurple
                    )
                    Text(
                        text = "Unlock all missions & features",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate to Premium",
                tint = WakeUpColors.iosPurple
            )
        }
    }
}

@Composable
private fun OEMCertificationSection(
    onNavigateToOEMSetup: () -> Unit
) {
    val oemViewModel: OEMSetupViewModel = hiltViewModel()
    val uiState by oemViewModel.uiState.collectAsState()
    val certification = uiState.oemCertification
    
    if (uiState.deviceProfile == null) return
    
    val isAggressive = uiState.deviceProfile?.isAggressive == true
    val hasCertification = certification?.isCertified == true
    
    // Only show section for aggressive OEMs or if already certified
    if (!isAggressive && !hasCertification) return
    
    GlassSettingsSection(title = "Device Protection") {
        Column {
            // Certification status card
            OEMCertificationBadge(
                oemType = uiState.deviceProfile?.oemType ?: com.wakeup.app.core.oem.OEMType.UNKNOWN,
                isCertified = hasCertification,
                certificationDate = certification?.certificationDate,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToOEMSetup() }
            )
            
            if (!hasCertification) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onNavigateToOEMSetup,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WakeUpColors.iosBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Device Setup")
                }
            }
        }
    }
}

@Composable
private fun GlassPremiumActiveCard(premiumType: PremiumType) {
    val headerColor = when (premiumType) {
        PremiumType.LIFETIME -> WakeUpColors.iosGold
        else -> WakeUpColors.iosGreen
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        headerColor.copy(alpha = 0.3f),
                        headerColor.copy(alpha = 0.15f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = headerColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = headerColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Premium Active",
                        tint = headerColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text(
                        text = "Premium Active",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = headerColor
                    )
                    Text(
                        text = premiumType.displayName(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassSettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.06f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun ThemeModeSelector(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val modeDisplayName = when (currentMode) {
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
        ThemeMode.SYSTEM -> "System Default"
    }
    
    val modeIcon = when (currentMode) {
        ThemeMode.LIGHT -> Icons.Default.Palette
        ThemeMode.DARK -> Icons.Default.DarkMode
        ThemeMode.SYSTEM -> Icons.Default.Notifications
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = modeIcon,
                contentDescription = "Theme",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = modeDisplayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Select Theme",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ThemeMode.values().forEach { mode ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            when (mode) {
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                                ThemeMode.SYSTEM -> "System Default"
                            }
                        ) 
                    },
                    onClick = {
                        onModeSelected(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SnoozeDurationSelector(
    duration: Int,
    onDurationChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Snooze,
                contentDescription = "Snooze Duration",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Snooze Duration",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "$duration minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = duration.toFloat(),
            onValueChange = { onDurationChange(it.toInt()) },
            valueRange = 1f..30f,
            steps = 29,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun SettingsDivider() {
    Spacer(modifier = Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.1f))
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
