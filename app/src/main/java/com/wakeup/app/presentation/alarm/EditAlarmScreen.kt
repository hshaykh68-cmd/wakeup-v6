package com.wakeup.app.presentation.alarm

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
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
import com.wakeup.app.domain.model.Alarm
import com.wakeup.app.domain.model.MissionDifficulty
import com.wakeup.app.domain.model.MissionType
import com.wakeup.app.domain.service.HapticsController
import com.wakeup.app.presentation.components.IOSTimePicker
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAlarmScreen(
    alarmId: String,
    viewModel: EditAlarmViewModel = hiltViewModel(),
    hapticsController: HapticsController,
    onNavigateBack: () -> Unit
) {
    val alarm by viewModel.alarm.collectAsState()

    LaunchedEffect(alarmId) {
        viewModel.loadAlarm(alarmId)
    }

    alarm?.let { currentAlarm ->
        EditAlarmContent(
            alarm = currentAlarm,
            hapticsController = hapticsController,
            onSave = { updatedAlarm ->
                viewModel.updateAlarm(updatedAlarm)
                onNavigateBack()
            },
            onDelete = {
                viewModel.deleteAlarm(alarmId)
                onNavigateBack()
            },
            onNavigateBack = onNavigateBack
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAlarmContent(
    alarm: Alarm,
    hapticsController: HapticsController,
    onSave: (Alarm) -> Unit,
    onDelete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var hour by remember { mutableStateOf(alarm.hour) }
    var minute by remember { mutableStateOf(alarm.minute) }
    var label by remember { mutableStateOf(alarm.label) }
    var selectedDays by remember { mutableStateOf(alarm.repeatDays.toSet()) }
    var missionType by remember { mutableStateOf(alarm.missionType) }
    var missionDifficulty by remember { mutableStateOf(alarm.missionDifficulty) }
    var strictMode by remember { mutableStateOf(alarm.strictMode) }
    var useVibration by remember { mutableStateOf(alarm.useVibration) }
    var gradualVolume by remember { mutableStateOf(alarm.gradualVolume) }
    var snoozeEnabled by remember { mutableStateOf(alarm.snoozeEnabled) }
    var isEnabled by remember { mutableStateOf(alarm.isEnabled) }

    // Barcode mission setup
    var scannedBarcodeValue by remember { mutableStateOf(alarm.barcodeValue) }
    var scannedBarcodeFormat by remember { mutableStateOf(alarm.barcodeFormat) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    // Photo mission setup
    var selectedPhotoUri by remember { mutableStateOf(alarm.photoReferencePath) }
    var photoReferenceHash by remember { mutableStateOf(alarm.photoReferenceHash) }
    var showPhotoPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Alarm",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onSave(
                                alarm.copy(
                                    hour = hour,
                                    minute = minute,
                                    label = label,
                                    repeatDays = selectedDays.toList(),
                                    missionType = missionType,
                                    missionDifficulty = missionDifficulty,
                                    strictMode = strictMode,
                                    useVibration = useVibration,
                                    gradualVolume = gradualVolume,
                                    snoozeEnabled = snoozeEnabled,
                                    isEnabled = isEnabled,
                                    barcodeValue = if (missionType == MissionType.BARCODE) scannedBarcodeValue else null,
                                    barcodeFormat = if (missionType == MissionType.BARCODE) scannedBarcodeFormat else null,
                                    photoReferencePath = if (missionType == MissionType.PHOTO) selectedPhotoUri else null,
                                    photoReferenceHash = if (missionType == MissionType.PHOTO) photoReferenceHash else null
                                )
                            )
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
            // Enabled Switch with glass effect
            GlassAlarmEditSection {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alarm Enabled",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }
            }

            // iOS-style Time Picker
            IOSTimePicker(
                hour = hour,
                minute = minute,
                is24Hour = false,
                hapticsController = hapticsController,
                onTimeChange = { newHour, newMinute ->
                    hour = newHour
                    minute = newMinute
                }
            )

            // Glassmorphic sections
            GlassAlarmEditSection(title = "Label") {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
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
            }

            GlassAlarmEditSection(title = "Repeat") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DayOfWeek.values().forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        val dayLabel = day.name.take(1)
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDays = if (selectedDays.contains(day)) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                            },
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

            GlassAlarmEditSection(title = "Wake Mission") {
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
                            EditGlassChip(
                                selected = selected,
                                onClick = { missionType = type },
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
                            
                            EditGlassChip(
                                selected = selected,
                                onClick = { missionDifficulty = difficulty },
                                label = difficulty.displayName(),
                                selectedColor = color
                            )
                        }
                    }
                    
                    // Barcode mission setup
                    if (missionType == MissionType.BARCODE) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Barcode Setup",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (scannedBarcodeValue != null) {
                            EditGlassChip(
                                selected = true,
                                onClick = { showBarcodeScanner = true },
                                label = "Barcode: ${scannedBarcodeValue!!.take(15)}...",
                                selectedColor = WakeUpColors.iosGreen
                            )
                        } else {
                            Button(
                                onClick = { showBarcodeScanner = true },
                                colors = ButtonDefaults.buttonColors(containerColor = WakeUpColors.iosBlue)
                            ) {
                                Text("Scan Barcode to Save")
                            }
                        }
                    }

                    // Photo mission setup
                    if (missionType == MissionType.PHOTO) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Photo Setup",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (selectedPhotoUri != null) {
                            EditGlassChip(
                                selected = true,
                                onClick = { showPhotoPicker = true },
                                label = "Photo Selected",
                                selectedColor = WakeUpColors.iosGreen
                            )
                        } else {
                            Button(
                                onClick = { showPhotoPicker = true },
                                colors = ButtonDefaults.buttonColors(containerColor = WakeUpColors.iosBlue)
                            ) {
                                Text("Select Reference Photo")
                            }
                        }
                    }
                }
            }

            GlassAlarmEditSection(title = "Options") {
                EditGlassOptionSwitch(
                    label = "Strict Mode",
                    description = "Must complete mission to dismiss",
                    checked = strictMode,
                    onCheckedChange = { strictMode = it }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                EditGlassOptionSwitch(
                    label = "Vibration",
                    description = "Vibrate when alarm rings",
                    checked = useVibration,
                    onCheckedChange = { useVibration = it }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                EditGlassOptionSwitch(
                    label = "Gradual Volume",
                    description = "Volume increases slowly",
                    checked = gradualVolume,
                    onCheckedChange = { gradualVolume = it }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                EditGlassOptionSwitch(
                    label = "Snooze",
                    description = "Allow snoozing the alarm",
                    checked = snoozeEnabled,
                    onCheckedChange = { snoozeEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Delete Button
            Button(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WakeUpColors.iosRed.copy(alpha = 0.1f),
                    contentColor = WakeUpColors.iosRed
                )
            ) {
                Text(
                    text = "Delete Alarm",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Barcode Scanner Dialog
        if (showBarcodeScanner) {
            AlertDialog(
                onDismissRequest = { showBarcodeScanner = false },
                title = { Text("Scan Barcode") },
                text = {
                    BarcodeSetupDialog(
                        onBarcodeScanned = { value, format ->
                            scannedBarcodeValue = value
                            scannedBarcodeFormat = format
                            showBarcodeScanner = false
                        }
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showBarcodeScanner = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Photo Picker Dialog
        if (showPhotoPicker) {
            AlertDialog(
                onDismissRequest = { showPhotoPicker = false },
                title = { Text("Select Reference Photo") },
                text = {
                    PhotoSetupDialog(
                        onPhotoSelected = { uri, hash ->
                            selectedPhotoUri = uri
                            photoReferenceHash = hash
                            showPhotoPicker = false
                        }
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showPhotoPicker = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun GlassAlarmEditSection(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
        
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
private fun EditGlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    selectedColor: Color = WakeUpColors.iosBlue
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) selectedColor.copy(alpha = 0.3f)
                else Color.White.copy(alpha = 0.05f)
            )
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = if (selected) Color.Transparent else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun EditGlassOptionSwitch(
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
