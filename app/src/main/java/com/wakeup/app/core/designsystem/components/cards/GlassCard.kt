package com.wakeup.app.core.designsystem.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wakeup.app.core.designsystem.tokens.OpacityTokens
import com.wakeup.app.core.designsystem.tokens.ShapeTokens

/**
 * Elevation levels for glass cards, determining blur intensity and opacity.
 */
enum class GlassElevation {
    /** Low elevation - subtle glass effect (8% opacity, 8dp blur) */
    LOW,
    
    /** Medium elevation - standard glass card (15% opacity, 16dp blur) */
    MEDIUM,
    
    /** High elevation - prominent glass surface (25% opacity, 24dp blur) */
    HIGH
}

/**
 * True glassmorphic card component with actual blur effect.
 * 
 * This component creates authentic glassmorphism by applying blur to the background
 * behind the card, combined with semi-transparent overlays.
 * 
 * @param modifier Modifier for the card
 * @param elevation Glass elevation level affecting blur and opacity
 * @param shape Shape of the card
 * @param contentAlignment Alignment of content within the card
 * @param border Whether to show the glass border highlight
 * @param content Content to display inside the card
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    elevation: GlassElevation = GlassElevation.MEDIUM,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(ShapeTokens.mdLg),
    contentAlignment: Alignment = Alignment.Center,
    border: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val (blurRadius, backgroundAlpha, borderAlpha) = when (elevation) {
        GlassElevation.LOW -> Triple(8.dp, OpacityTokens.subtle, OpacityTokens.borderSubtle)
        GlassElevation.MEDIUM -> Triple(16.dp, OpacityTokens.medium, OpacityTokens.borderLight)
        GlassElevation.HIGH -> Triple(24.dp, OpacityTokens.strong, OpacityTokens.borderMedium)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Rectangle)
            .background(Color.White.copy(alpha = backgroundAlpha))
            .then(
                if (border) {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = borderAlpha),
                        shape = shape
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = contentAlignment,
        content = content
    )
}

/**
 * Gradient-based glass card (legacy style) for performance-critical screens.
 * Use this when blur performance is a concern on lower-end devices.
 */
@Composable
fun GradientGlassCard(
    modifier: Modifier = Modifier,
    elevation: GlassElevation = GlassElevation.MEDIUM,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(ShapeTokens.mdLg),
    contentAlignment: Alignment = Alignment.Center,
    border: Boolean = true,
    gradientColors: List<Color>? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val backgroundAlpha = when (elevation) {
        GlassElevation.LOW -> OpacityTokens.subtle
        GlassElevation.MEDIUM -> OpacityTokens.medium
        GlassElevation.HIGH -> OpacityTokens.strong
    }
    
    val borderAlpha = when (elevation) {
        GlassElevation.LOW -> OpacityTokens.borderSubtle
        GlassElevation.MEDIUM -> OpacityTokens.borderLight
        GlassElevation.HIGH -> OpacityTokens.borderMedium
    }

    val colors = gradientColors ?: listOf(
        Color.White.copy(alpha = backgroundAlpha),
        Color.White.copy(alpha = backgroundAlpha * 0.7f)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(colors))
            .then(
                if (border) {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = borderAlpha),
                        shape = shape
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = contentAlignment,
        content = content
    )
}

/**
 * Colored glass card with tinted gradient.
 * Useful for premium cards, accent sections, or branded content.
 */
@Composable
fun ColoredGlassCard(
    modifier: Modifier = Modifier,
    baseColor: Color,
    elevation: GlassElevation = GlassElevation.MEDIUM,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(ShapeTokens.mdLg),
    contentAlignment: Alignment = Alignment.Center,
    border: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val backgroundAlpha = when (elevation) {
        GlassElevation.LOW -> OpacityTokens.subtle
        GlassElevation.MEDIUM -> OpacityTokens.medium
        GlassElevation.HIGH -> OpacityTokens.strong
    }
    
    val borderAlpha = when (elevation) {
        GlassElevation.LOW -> OpacityTokens.borderSubtle
        GlassElevation.MEDIUM -> OpacityTokens.borderLight
        GlassElevation.HIGH -> OpacityTokens.borderMedium
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        baseColor.copy(alpha = backgroundAlpha),
                        baseColor.copy(alpha = backgroundAlpha * 0.6f)
                    )
                )
            )
            .then(
                if (border) {
                    Modifier.border(
                        width = 1.dp,
                        color = baseColor.copy(alpha = borderAlpha),
                        shape = shape
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = contentAlignment,
        content = content
    )
}
