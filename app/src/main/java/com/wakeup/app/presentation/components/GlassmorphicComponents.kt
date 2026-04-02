package com.wakeup.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wakeup.app.core.theme.WakeUpColors

/**
 * Glassmorphic card container with iOS-style translucent background
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundAlpha: Float = 0.15f,
    borderAlpha: Float = 0.3f,
    cornerRadius: Int = 20,
    gradientColors: List<Color> = listOf(
        Color.White.copy(alpha = backgroundAlpha),
        Color.White.copy(alpha = backgroundAlpha * 0.5f)
    ),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .padding(1.dp),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Glassmorphic section container with title
 */
@Composable
fun GlassSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            content = {
                Column(
                    modifier = Modifier.padding(16.dp),
                    content = content
                )
            }
        )
    }
}

/**
 * Glassmorphic header with gradient background
 */
@Composable
fun GlassHeader(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(
        WakeUpColors.iosBlue.copy(alpha = 0.2f),
        WakeUpColors.iosPurple.copy(alpha = 0.15f)
    ),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Glassmorphic floating action button background
 */
@Composable
fun GlassFloatingBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Glassmorphic bottom sheet / container
 */
@Composable
fun GlassBottomContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            content = content
        )
    }
}

/**
 * Glassmorphic button container
 */
@Composable
fun GlassButtonContainer(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = WakeUpColors.iosBlue.copy(alpha = 0.2f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Glassmorphic toggle chip
 */
@Composable
fun GlassToggleChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val backgroundColor = if (selected) {
        WakeUpColors.iosBlue.copy(alpha = 0.3f)
    } else {
        Color.White.copy(alpha = 0.1f)
    }
    
    val borderColor = if (selected) {
        WakeUpColors.iosBlue.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.2f)
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Glassmorphic stat card
 */
@Composable
fun GlassStatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    GlassCard(
        modifier = modifier,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        icon()
                    }
                }
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    )
}
