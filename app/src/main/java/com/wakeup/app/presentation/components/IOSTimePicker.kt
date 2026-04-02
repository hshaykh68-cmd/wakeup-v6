package com.wakeup.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.domain.service.HapticsController
import androidx.compose.ui.platform.LocalContext

/**
 * iOS-style time picker with large hour/minute display and AM/PM toggle
 */
@Composable
fun IOSTimePicker(
    hour: Int,
    minute: Int,
    is24Hour: Boolean = false,
    hapticsController: HapticsController,
    onTimeChange: (hour: Int, minute: Int) -> Unit
) {
    var displayHour by remember { mutableIntStateOf(if (is24Hour) hour else (hour % 12).let { if (it == 0) 12 else it }) }
    var displayMinute by remember { mutableIntStateOf(minute) }
    var isAm by remember { mutableStateOf(hour < 12) }
    
    // Convert display hour back to 24-hour format when needed
    fun getActualHour(): Int {
        return if (is24Hour) {
            displayHour
        } else {
            when {
                isAm && displayHour == 12 -> 0
                isAm -> displayHour
                !isAm && displayHour == 12 -> 12
                else -> displayHour + 12
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                color = Color.White.copy(alpha = 0.1f)
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Time Display Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Hour Column
                val context = LocalContext.current
                TimeColumn(
                    value = displayHour,
                    minValue = if (is24Hour) 0 else 1,
                    maxValue = if (is24Hour) 23 else 12,
                    onValueChange = { 
                        displayHour = it
                        hapticsController.performTick()
                        onTimeChange(getActualHour(), displayMinute)
                    },
                    label = "Hour"
                )
                
                // Colon separator
                Text(
                    text = ":",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                // Minute Column
                TimeColumn(
                    value = displayMinute,
                    minValue = 0,
                    maxValue = 59,
                    step = 1,
                    padLength = 2,
                    onValueChange = { 
                        displayMinute = it
                        hapticsController.performTick()
                        onTimeChange(getActualHour(), displayMinute)
                    },
                    label = "Minute"
                )
                
                // AM/PM Toggle (only for 12-hour format)
                if (!is24Hour) {
                    Spacer(modifier = Modifier.width(16.dp))
                    AmPmToggle(
                        isAm = isAm,
                        onToggle = {
                            isAm = !isAm
                            hapticsController.performTick()
                            onTimeChange(getActualHour(), displayMinute)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeColumn(
    value: Int,
    minValue: Int,
    maxValue: Int,
    step: Int = 1,
    padLength: Int = if (maxValue > 99) 3 else if (maxValue > 9) 2 else 1,
    onValueChange: (Int) -> Unit,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Up arrow
        IconButton(
            onClick = {
                val newValue = if (value >= maxValue) minValue else value + step
                onValueChange(newValue)
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Value display
        Text(
            text = value.toString().padStart(padLength, '0'),
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(if (padLength == 2) 80.dp else 100.dp)
        )
        
        // Down arrow
        IconButton(
            onClick = {
                val newValue = if (value <= minValue) maxValue else value - step
                onValueChange(newValue)
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Label
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun AmPmToggle(
    isAm: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(4.dp)
    ) {
        // AM
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isAm) WakeUpColors.iosBlue.copy(alpha = 0.3f)
                    else Color.Transparent
                )
                .clickable { if (!isAm) onToggle() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AM",
                fontSize = 18.sp,
                fontWeight = if (isAm) FontWeight.Bold else FontWeight.Normal,
                color = if (isAm) WakeUpColors.iosBlue else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // PM
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (!isAm) WakeUpColors.iosBlue.copy(alpha = 0.3f)
                    else Color.Transparent
                )
                .clickable { if (isAm) onToggle() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "PM",
                fontSize = 18.sp,
                fontWeight = if (!isAm) FontWeight.Bold else FontWeight.Normal,
                color = if (!isAm) WakeUpColors.iosBlue else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Alternative iOS-style wheel time picker (simplified version)
 */
@Composable
fun IOSWheelTimePicker(
    hour: Int,
    minute: Int,
    is24Hour: Boolean = false,
    onTimeChange: (hour: Int, minute: Int) -> Unit
) {
    val displayHour = if (is24Hour) hour else (hour % 12).let { if (it == 0) 12 else it }
    val isAm = hour < 12
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
    ) {
        // Center selection indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(Alignment.Center)
                .background(Color.White.copy(alpha = 0.1f))
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Hour wheel
            WheelSelector(
                value = displayHour,
                range = if (is24Hour) 0..23 else 1..12,
                onValueChange = { newHour ->
                    val actualHour = if (is24Hour) {
                        newHour
                    } else {
                        when {
                            isAm && newHour == 12 -> 0
                            isAm -> newHour
                            !isAm && newHour == 12 -> 12
                            else -> newHour + 12
                        }
                    }
                    onTimeChange(actualHour, minute)
                },
                modifier = Modifier.width(80.dp)
            )
            
            Text(
                text = ":",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            // Minute wheel
            WheelSelector(
                value = minute,
                range = 0..59,
                padLength = 2,
                onValueChange = { newMinute ->
                    onTimeChange(hour, newMinute)
                },
                modifier = Modifier.width(80.dp)
            )
            
            // AM/PM selector
            if (!is24Hour) {
                Spacer(modifier = Modifier.width(16.dp))
                AmPmToggle(
                    isAm = isAm,
                    onToggle = {
                        val newHour = if (isAm) hour + 12 else hour - 12
                        onTimeChange(newHour.coerceIn(0, 23), minute)
                    }
                )
            }
        }
    }
}

@Composable
private fun WheelSelector(
    value: Int,
    range: IntRange,
    padLength: Int = if (range.last > 99) 3 else if (range.last > 9) 2 else 1,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Previous value
        val prevValue = if (value > range.first) value - 1 else range.last
        Text(
            text = prevValue.toString().padStart(padLength, '0'),
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Current value with selection indicator
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString().padStart(padLength, '0'),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Next value
        val nextValue = if (value < range.last) value + 1 else range.first
        Text(
            text = nextValue.toString().padStart(padLength, '0'),
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
