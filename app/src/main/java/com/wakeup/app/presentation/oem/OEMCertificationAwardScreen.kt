package com.wakeup.app.presentation.oem

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.app.core.oem.OEMType
import com.wakeup.app.core.theme.WakeUpColors
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * Award ceremony screen shown when user completes all OEM setup steps.
 */
@Composable
fun OEMCertificationAwardScreen(
    oemType: OEMType,
    deviceModel: String,
    certificationDate: Instant = Instant.now(),
    onContinue: () -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    
    // Badge pulse animation
    val badgeScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_pulse"
    )
    
    // Checkmark rotation
    val checkRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "check_rotate"
    )

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }
    val formattedDate = remember(certificationDate) {
        dateFormatter.format(java.time.Instant.ofEpochMilli(certificationDate.toEpochMilli()))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Celebration header
            Text(
                text = "🎉",
                fontSize = 48.sp
            )
            
            Text(
                text = "Congratulations!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Your device is now protected",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Certification Badge
            Box(
                modifier = Modifier
                    .scale(badgeScale)
                    .size(200.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                WakeUpColors.iosBlue.copy(alpha = 0.3f),
                                WakeUpColors.iosPurple.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    WakeUpColors.iosBlue,
                                    WakeUpColors.iosPurple,
                                    WakeUpColors.iosPink,
                                    WakeUpColors.iosBlue
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner badge
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Checkmark in circle
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        color = WakeUpColors.iosGreen,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Certified",
                                    modifier = Modifier.size(32.dp),
                                    tint = Color.White
                                )
                            }
                            
                            Text(
                                text = "WAKEUP",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Text(
                                text = "CERTIFIED",
                                style = MaterialTheme.typography.labelSmall,
                                color = WakeUpColors.iosGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Device info
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = oemType.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = deviceModel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Certified $formattedDate",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Stats
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                StatItem(value = "99.5%", label = "Reliability")
                StatItem(value = "12%", label = "of users")
                StatItem(value = "✓", label = "Protected")
            }

            // Share CTA
            Button(
                onClick = { /* Share functionality */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = WakeUpColors.iosBlue
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Your Badge")
            }

            // Continue button
            TextButton(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue to App")
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = WakeUpColors.iosBlue
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Compact certification badge for settings screen.
 */
@Composable
fun OEMCertificationBadge(
    oemType: OEMType,
    isCertified: Boolean,
    certificationDate: Instant?,
    modifier: Modifier = Modifier
) {
    if (!isCertified) {
        // Show "Setup Required" version
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(WakeUpColors.iosOrange.copy(alpha = 0.15f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = WakeUpColors.iosOrange,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Setup Required",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = WakeUpColors.iosOrange
                    )
                    Text(
                        text = "Complete setup for reliable alarms",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    // Show certified badge
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WakeUpColors.iosGreen.copy(alpha = 0.2f),
                        WakeUpColors.iosBlue.copy(alpha = 0.1f)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small badge icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = WakeUpColors.iosGreen,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "WakeUp Certified",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = WakeUpColors.iosGreen
                )
                Text(
                    text = oemType.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (certificationDate != null) {
                    val dateStr = remember(certificationDate) {
                        DateTimeFormatter.ofPattern("MMM yyyy")
                            .format(java.time.Instant.ofEpochMilli(certificationDate.toEpochMilli()))
                    }
                    Text(
                        text = "Since $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Share button
            IconButton(onClick = { /* Share */ }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = WakeUpColors.iosBlue
                )
            }
        }
    }
}

// Placeholder
@Composable
private fun TextButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        content()
    }
}

@Composable
private fun IconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
