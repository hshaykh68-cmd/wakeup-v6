package com.wakeup.app.presentation.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.domain.service.AlarmLabelSuggestionsProvider
import com.wakeup.app.domain.service.HapticsController
import com.wakeup.app.domain.model.MissionDifficulty
import com.wakeup.app.domain.model.MissionType
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import com.wakeup.app.presentation.components.IOSTimePicker
import java.time.DayOfWeek

import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateAlarmScreen(
    viewModel: CreateAlarmViewModel = hiltViewModel(),
    alarmLabelSuggestionsProvider: AlarmLabelSuggestionsProvider,
    hapticsController: HapticsController,
    onNavigateBack: () -> Unit
) {
    var hour by remember { mutableStateOf(7) }
    var minute by remember { mutableStateOf(0) }
    var label by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<DayOfWeek>()) }
    var missionType by remember { mutableStateOf(MissionType.MATH) }
    var missionDifficulty by remember { mutableStateOf(MissionDifficulty.EASY) }
    var strictMode by remember { mutableStateOf(false) }
    var useVibration by remember { mutableStateOf(true) }
    var gradualVolume by remember { mutableStateOf(true) }
    var snoozeEnabled by remember { mutableStateOf(true) }
    var smartWakeEnabled by remember { mutableStateOf(false) }
    var smartWakeWindowMinutes by remember { mutableStateOf(30) }
    val isPremium by viewModel.isPremium.collectAsState()
    val showUpsell by viewModel.showUpsell.collectAsState()
    val alarmCount by viewModel.alarmCount.collectAsState()
    val canCreateAlarm by viewModel.canCreateAlarm.collectAsState()
    val useIOSStyleTimePicker by viewModel.useIOSStyleTimePicker.collectAsState()
    val showBatteryWarning by viewModel.showBatteryOptimizationWarning.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    // Barcode mission setup
    var scannedBarcodeValue by remember { mutableStateOf<String?>(null) }
    var scannedBarcodeFormat by remember { mutableStateOf<Int?>(null) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    // Photo mission setup
    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }
    var photoReferenceHash by remember { mutableStateOf<String?>(null) }
    var showPhotoPicker by remember { mutableStateOf(false) }
    
    // Smart label suggestions
    var showLabelSuggestions by remember { mutableStateOf(false) }
    val labelSuggestions = remember(hour) {
        alarmLabelSuggestionsProvider.getSuggestionsForHour(hour)
    }

    // Stable lambdas for performance
    val onTimeChange = remember { { h: Int, m: Int ->
        hour = h
        minute = m
    } }
    val onNavigateBackStable = remember { onNavigateBack }

    // Sound picker state
    var selectedSoundUri by remember { mutableStateOf<String?>(null) }
    var showSoundPicker by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Alarm",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBackStable) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Show alarm count for free users
                    if (!isPremium) {
                        Text(
                            text = "$alarmCount/${CreateAlarmViewModel.FREE_ALARM_LIMIT} alarms",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (alarmCount >= CreateAlarmViewModel.FREE_ALARM_LIMIT) 
                                WakeUpColors.iosOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    TextButton(
                        onClick = {
                            if (!viewModel.checkCanCreateAlarm()) {
                                return@TextButton
                            }
                            viewModel.createAlarm(
                                hour = hour,
                                minute = minute,
                                label = label.ifBlank { "Alarm" },
                                repeatDays = selectedDays.toList(),
                                missionType = missionType,
                                missionDifficulty = missionDifficulty,
                                strictMode = strictMode,
                                useVibration = useVibration,
                                gradualVolume = gradualVolume,
                                snoozeEnabled = snoozeEnabled,
                                soundUri = selectedSoundUri
                            )
                            onNavigateBack()
                        }
                    ) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelLarge,
                            color = WakeUpColors.iosBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Time Picker - Material3 by default, iOS-style as option
            if (useIOSStyleTimePicker) {
                IOSTimePicker(
                    hour = hour,
                    minute = minute,
                    is24Hour = false,
                    hapticsController = hapticsController,
                    onTimeChange = onTimeChange
                )
            } else {
                // Material3 TimePicker
                val timePickerState = rememberTimePickerState(
                    initialHour = hour,
                    initialMinute = minute,
                    is24Hour = false
                )
                
                // Update hour/minute when time picker state changes
                androidx.compose.runtime.LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                    if (timePickerState.hour != hour || timePickerState.minute != minute) {
                        onTimeChange(timePickerState.hour, timePickerState.minute)
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(
                        state = timePickerState,
                        layoutType = androidx.compose.material3.TimePickerLayoutType.Vertical
                    )
                }
            }

            // Battery optimization warning
            if (showBatteryWarning) {
                val batteryHelper = remember { com.wakeup.app.core.util.BatteryOptimizationHelper(context) }
                Card(
                    onClick = { 
                        if (context is android.app.Activity) {
                            batteryHelper.requestIgnoreBatteryOptimizationsDirect(context)
                        }
                        viewModel.dismissBatteryOptimizationWarning()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = WakeUpColors.iosOrange.copy(alpha = 0.15f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = WakeUpColors.iosOrange.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WakeUpColors.iosOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Battery optimization may prevent alarms",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = WakeUpColors.iosOrange
                            )
                            Text(
                                text = "Tap to disable battery optimization for reliable alarms",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { viewModel.dismissBatteryOptimizationWarning() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Glassmorphic sections
            GlassAlarmSection(title = "Label") {
                Column {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { 
                            label = it
                            showLabelSuggestions = it.isEmpty()
                        },
                        label = { Text("Label") },
                        placeholder = { Text("e.g., Work, Gym") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WakeUpColors.iosBlue.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = WakeUpColors.iosBlue,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    
                    // Smart label suggestions
                    if (label.isEmpty() || showLabelSuggestions) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Suggested for ${hour}:00:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            labelSuggestions.forEach { suggestion ->
                                SuggestionChip(
                                    suggestion = suggestion,
                                    onClick = { 
                                        label = suggestion
                                        showLabelSuggestions = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            GlassAlarmSection(title = "Repeat") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DayOfWeek.values().forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        val dayLabel = day.name.take(1)
                        val onDayClick = remember(day) { {
                            selectedDays = if (selectedDays.contains(day)) {
                                selectedDays - day
                            } else {
                                selectedDays + day
                            }
                        } }
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = onDayClick,
                            label = {
                                Text(
                                    dayLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WakeUpColors.iosBlue.copy(alpha = 0.3f),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.05f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 0.dp else 1.dp,
                                color = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }

            GlassAlarmSection(title = "Wake Mission") {
                Column {
                    Text(
                        text = "Mission Type",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MissionType.values().forEach { type ->
                            val selected = missionType == type
                            val onTypeClick = remember(type) { { missionType = type } }
                            GlassChip(
                                selected = selected,
                                onClick = onTypeClick,
                                label = type.displayName()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Difficulty",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MissionDifficulty.values().forEach { difficulty ->
                            val selected = missionDifficulty == difficulty
                            val color = when (difficulty) {
                                MissionDifficulty.EASY -> WakeUpColors.iosGreen
                                MissionDifficulty.MEDIUM -> WakeUpColors.iosOrange
                                MissionDifficulty.HARD -> WakeUpColors.iosRed
                            }
                            
                            GlassChip(
                                selected = selected,
                                onClick = { missionDifficulty = difficulty },
                                label = difficulty.displayName(),
                                selectedColor = color
                            )
                        }
                    }
                }
            }

            GlassAlarmSection(title = "Wake Mission") {
                Column {
                    Text(
                        text = "Mission Type",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MissionType.values().forEach { type ->
                            val selected = missionType == type
                            val isAvailable = viewModel.isMissionTypeAvailable(type)
                            val onMissionClick = remember(type, isAvailable) { {
                                if (isAvailable) {
                                    missionType = type
                                }
                            } }
                            GlassChip(
                                selected = selected,
                                onClick = onMissionClick,
                                label = type.displayName(),
                                enabled = isAvailable
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Difficulty",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MissionDifficulty.values().forEach { difficulty ->
                            val selected = missionDifficulty == difficulty
                            val color = when (difficulty) {
                                MissionDifficulty.EASY -> WakeUpColors.iosGreen
                                MissionDifficulty.MEDIUM -> WakeUpColors.iosOrange
                                MissionDifficulty.HARD -> WakeUpColors.iosRed
                            }
                            val onDifficultyClick = remember(difficulty) { { missionDifficulty = difficulty } }
                            
                            GlassChip(
                                selected = selected,
                                onClick = onDifficultyClick,
                                label = difficulty.displayName(),
                                selectedColor = color
                            )
                        }
                    }
                    
                    // Barcode mission setup
                    if (missionType == MissionType.BARCODE && isPremium) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Barcode Setup",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (scannedBarcodeValue != null) {
                            val onBarcodeChipClick = remember { { showBarcodeScanner = true } }
                            GlassChip(
                                selected = true,
                                onClick = onBarcodeChipClick,
                                label = "Barcode: ${scannedBarcodeValue!!.take(15)}...",
                                selectedColor = WakeUpColors.iosGreen
                            )
                        } else {
                            val onScanClick = remember { { showBarcodeScanner = true } }
                            Button(
                                onClick = onScanClick,
                                colors = ButtonDefaults.buttonColors(containerColor = WakeUpColors.iosBlue)
                            ) {
                                Text("Scan Barcode to Save")
                            }
                        }
                    }

                    // Photo mission setup
                    if (missionType == MissionType.PHOTO && isPremium) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Photo Setup",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (selectedPhotoUri != null) {
                            val onPhotoChipClick = remember { { showPhotoPicker = true } }
                            GlassChip(
                                selected = true,
                                onClick = onPhotoChipClick,
                                label = "Photo Selected",
                                selectedColor = WakeUpColors.iosGreen
                            )
                        } else {
                            val onSelectPhotoClick = remember { { showPhotoPicker = true } }
                            Button(
                                onClick = onSelectPhotoClick,
                                colors = ButtonDefaults.buttonColors(containerColor = WakeUpColors.iosBlue)
                            ) {
                                Text("Select Reference Photo")
                            }
                        }
                    }
                }
            }

            GlassAlarmSection(title = "Options") {
                // Smart Wake Toggle
                val onSmartWakeChange = remember { { value: Boolean -> smartWakeEnabled = value } }
                GlassOptionSwitch(
                    label = "Smart Wake",
                    description = "Wake during light sleep (up to ${smartWakeWindowMinutes} min early)",
                    checked = smartWakeEnabled,
                    onCheckedChange = onSmartWakeChange
                )
                
                // Smart Wake Window Picker (only show if enabled)
                if (smartWakeEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Smart Wake Window",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15, 30, 45, 60).forEach { minutes ->
                                val selected = smartWakeWindowMinutes == minutes
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (selected) WakeUpColors.iosGreen.copy(alpha = 0.3f)
                                            else Color.White.copy(alpha = 0.1f)
                                        )
                                        .border(
                                            width = if (selected) 0.dp else 1.dp,
                                            color = if (selected) Color.Transparent else Color.White.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { smartWakeWindowMinutes = minutes }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${minutes}m",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) WakeUpColors.iosGreen else Color.White,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sound Picker
                val onSoundPickerClick = remember { { showSoundPicker = true } }
                GlassClickableOption(
                    label = "Alarm Sound",
                    description = selectedSoundUri ?: "Default",
                    onClick = onSoundPickerClick
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val onStrictModeChange = remember { { value: Boolean -> strictMode = value } }
                GlassOptionSwitch(
                    label = "Strict Mode",
                    description = "Must complete mission to dismiss",
                    checked = strictMode,
                    onCheckedChange = onStrictModeChange
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val onVibrationChange = remember { { value: Boolean -> useVibration = value } }
                GlassOptionSwitch(
                    label = "Vibration",
                    description = "Vibrate when alarm rings",
                    checked = useVibration,
                    onCheckedChange = onVibrationChange
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val onGradualVolumeChange = remember { { value: Boolean -> gradualVolume = value } }
                GlassOptionSwitch(
                    label = "Gradual Volume",
                    description = "Volume increases slowly",
                    checked = gradualVolume,
                    onCheckedChange = onGradualVolumeChange
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val onSnoozeChange = remember { { value: Boolean -> snoozeEnabled = value } }
                GlassOptionSwitch(
                    label = "Snooze",
                    description = "Allow snoozing the alarm",
                    checked = snoozeEnabled,
                    onCheckedChange = onSnoozeChange
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button with glass effect
            val onSaveAlarm = remember {
                {
                    viewModel.createAlarm(
                        hour = hour,
                        minute = minute,
                        label = label.ifBlank { "Alarm" },
                        repeatDays = selectedDays.toList(),
                        missionType = missionType,
                        missionDifficulty = missionDifficulty,
                        strictMode = strictMode,
                        useVibration = useVibration,
                        gradualVolume = gradualVolume,
                        snoozeEnabled = snoozeEnabled,
                        soundUri = selectedSoundUri,
                        smartWakeEnabled = smartWakeEnabled,
                        smartWakeWindowMinutes = smartWakeWindowMinutes,
                        barcodeValue = if (missionType == MissionType.BARCODE) scannedBarcodeValue else null,
                        barcodeFormat = if (missionType == MissionType.BARCODE) scannedBarcodeFormat else null,
                        photoReferencePath = if (missionType == MissionType.PHOTO) selectedPhotoUri else null,
                        photoReferenceHash = if (missionType == MissionType.PHOTO) photoReferenceHash else null
                    )
                    onNavigateBack()
                }
            }
            Button(
                onClick = onSaveAlarm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WakeUpColors.iosBlue
                )
            ) {
                Text(
                    text = "Create Alarm",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Sound Picker Dialog
            if (showSoundPicker) {
                val onDismissSoundPicker = remember { { showSoundPicker = false } }
                AlertDialog(
                    onDismissRequest = onDismissSoundPicker,
                    title = { Text("Select Alarm Sound") },
                    text = {
                        Column {
                            listOf(
                                "Default" to null,
                                "Classic Alarm" to "content://settings/alarm_alert",
                                "Gentle Wake" to "content://settings/alarm_alert",
                                "Digital Beep" to "content://settings/alarm_alert"
                            ).forEach { (name, uri) ->
                                val onSoundSelected = remember(name) { {
                                    selectedSoundUri = uri
                                    showSoundPicker = false
                                } }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = onSoundSelected)
                                        .padding(vertical = 12.dp, horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (selectedSoundUri == uri) WakeUpColors.iosBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = onDismissSoundPicker) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Barcode Scanner Dialog
            if (showBarcodeScanner) {
                val onDismissBarcode = remember { { showBarcodeScanner = false } }
                val onBarcodeScanned = remember {
                    { value: String, format: Int ->
                        scannedBarcodeValue = value
                        scannedBarcodeFormat = format
                        showBarcodeScanner = false
                    }
                }
                AlertDialog(
                    onDismissRequest = onDismissBarcode,
                    title = { Text("Scan Barcode") },
                    text = {
                        BarcodeSetupDialog(
                            onBarcodeScanned = onBarcodeScanned
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = onDismissBarcode) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Photo Picker Dialog
            if (showPhotoPicker) {
                val onDismissPhoto = remember { { showPhotoPicker = false } }
                val onPhotoSelected = remember {
                    { uri: String, hash: String ->
                        selectedPhotoUri = uri
                        photoReferenceHash = hash
                        showPhotoPicker = false
                    }
                }
                AlertDialog(
                    onDismissRequest = onDismissPhoto,
                    title = { Text("Select Reference Photo") },
                    text = {
                        PhotoSetupDialog(
                            onPhotoSelected = onPhotoSelected
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = onDismissPhoto) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Upsell Dialog
            if (showUpsell) {
                val onDismissUpsell = remember { { viewModel.dismissUpsell() } }
                val onUpgrade = remember {
                    {
                        viewModel.dismissUpsell()
                        // Navigate to premium screen would happen here
                    }
                }
                UpsellDialog(
                    onDismiss = onDismissUpsell,
                    onUpgrade = onUpgrade
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    suggestion: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = WakeUpColors.iosBlue.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = WakeUpColors.iosBlue.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = suggestion,
            style = MaterialTheme.typography.labelMedium,
            color = WakeUpColors.iosBlue,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun GlassAlarmSection(
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
private fun GlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    selectedColor: Color = WakeUpColors.iosBlue,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    !enabled -> Color.White.copy(alpha = 0.02f)
                    selected -> selectedColor.copy(alpha = 0.3f)
                    else -> Color.White.copy(alpha = 0.05f)
                }
            )
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = when {
                    !enabled -> Color.White.copy(alpha = 0.1f)
                    selected -> Color.Transparent
                    else -> Color.White.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!enabled) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Premium required",
                    modifier = Modifier.size(12.dp),
                    tint = Color.White.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = when {
                    !enabled -> Color.White.copy(alpha = 0.4f)
                    selected -> Color.White
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun GlassClickableOption(
    label: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Select",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun GlassOptionSwitch(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun UpsellDialog(
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = WakeUpColors.iosOrange,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Unlock Premium",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "You've reached the free limit of 3 alarms.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Upgrade to Premium for:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(
                    modifier = Modifier.padding(start = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("\u2022 Unlimited alarms", style = MaterialTheme.typography.bodyMedium)
                    Text("\u2022 Photo missions", style = MaterialTheme.typography.bodyMedium)
                    Text("\u2022 Barcode missions", style = MaterialTheme.typography.bodyMedium)
                    Text("\u2022 Advanced stats", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WakeUpColors.iosOrange
                )
            ) {
                Text("Upgrade Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Maybe Later")
            }
        }
    )
}
