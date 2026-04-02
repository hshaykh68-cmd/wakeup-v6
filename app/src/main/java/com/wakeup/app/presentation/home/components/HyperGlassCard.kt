package com.wakeup.app.presentation.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wakeup.app.core.theme.WakeUpColors

/**
 * HyperGlassCard - True glass with heavy blur + gradient tint + edge highlights
 * Creates an expensive, multi-layer refractive glass effect
 */
@Composable
fun HyperGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    blurAmount: Dp = 30.dp,
    backgroundAlpha: Float = 0.15f,
    gradientColors: List<Color> = listOf(
        Color.White.copy(alpha = 0.12f),
        Color.White.copy(alpha = 0.06f),
        Color.White.copy(alpha = 0.03f)
    ),
    borderAlpha: Float = 0.3f,
    enableEdgeHighlight: Boolean = true,
    enableInnerShadow: Boolean = true,
    enableGlow: Boolean = true,
    glowColor: Color = WakeUpColors.iosBlue.copy(alpha = 0.2f),
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glass_shimmer")

    // Animated shimmer for edge highlight
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    // Pulsing glow animation
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .then(
                if (enableGlow) {
                    Modifier.drawBehind {
                        // Outer glow layer
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    glowColor.copy(alpha = 0.15f * glowPulse),
                                    glowColor.copy(alpha = 0.05f * glowPulse),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2, size.height / 2),
                                radius = size.maxDimension * 0.6f
                            )
                        )
                    }
                } else Modifier
            )
            .blur(blurAmount)
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
            .then(
                if (enableInnerShadow) {
                    Modifier.drawWithContent {
                        drawContent()
                        // Inner shadow overlay
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.08f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.04f)
                                )
                            ),
                            blendMode = BlendMode.SrcOver
                        )
                    }
                } else Modifier
            )
            .then(
                if (enableEdgeHighlight) {
                    Modifier.drawWithContent {
                        drawContent()
                        // Top edge highlight
                        val highlightWidth = size.width * 0.6f
                        val highlightX = size.width * 0.2f + (shimmerOffset - 0.5f) * size.width * 0.2f
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.25f),
                                    Color.Transparent
                                ),
                                startX = highlightX - highlightWidth / 2,
                                endX = highlightX + highlightWidth / 2
                            ),
                            topLeft = Offset(0f, 0f),
                            size = Size(size.width, 2.dp.toPx()),
                            blendMode = BlendMode.Screen
                        )
                    }
                } else Modifier
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = borderAlpha * 1.5f),
                        Color.White.copy(alpha = borderAlpha),
                        Color.White.copy(alpha = borderAlpha * 0.5f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        // Content container without blur
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp)
                .clip(RoundedCornerShape(cornerRadius - 1.dp)),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

/**
 * Glass card with interactive press effect
 * Scales and glows on press
 */
@Composable
fun InteractiveHyperGlassCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val glowIntensity by animateFloatAsState(
        targetValue = if (isPressed) 1.5f else 1f,
        animationSpec = tween(200),
        label = "glow"
    )

    HyperGlassCard(
        modifier = modifier
            .scale(scale)
            .graphicsLayer {
                shadowElevation = if (isPressed) 20f else 8f
                spotShadowColor = WakeUpColors.iosBlue.copy(alpha = 0.3f * glowIntensity)
            }
            .drawBehind {
                if (isPressed) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                WakeUpColors.iosBlue.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = size.maxDimension * 0.5f
                        )
                    )
                }
            },
        cornerRadius = cornerRadius,
        blurAmount = if (isPressed) 35.dp else 30.dp,
        glowColor = WakeUpColors.iosBlue.copy(alpha = 0.3f * glowIntensity),
        content = content
    )
}

/**
 * Specialized glass card for alarm display
 * Has animated time glow effect
 */
@Composable
fun AlarmHyperGlassCard(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "alarm_glass")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val gradientColors = if (isActive) {
        listOf(
            WakeUpColors.iosBlue.copy(alpha = 0.15f + pulse * 0.03f),
            WakeUpColors.iosPurple.copy(alpha = 0.1f),
            Color.White.copy(alpha = 0.05f)
        )
    } else {
        listOf(
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = 0.04f),
            Color.White.copy(alpha = 0.02f)
        )
    }

    HyperGlassCard(
        modifier = modifier,
        cornerRadius = 28.dp,
        blurAmount = 35.dp,
        gradientColors = gradientColors,
        glowColor = if (isActive) WakeUpColors.iosBlue.copy(alpha = 0.25f + pulse * 0.1f) else Color.Transparent,
        borderAlpha = if (isActive) 0.4f else 0.2f,
        content = content
    )
}

/**
 * Stats card with glass effect and icon glow
 */
@Composable
fun StatHyperGlassCard(
    modifier: Modifier = Modifier,
    accentColor: Color = WakeUpColors.iosBlue,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stat_glass")

    val breathe by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    HyperGlassCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        blurAmount = 25.dp,
        gradientColors = listOf(
            accentColor.copy(alpha = 0.08f + breathe * 0.02f),
            Color.White.copy(alpha = 0.06f),
            Color.White.copy(alpha = 0.03f)
        ),
        glowColor = accentColor.copy(alpha = 0.15f),
        enableGlow = true,
        content = content
    )
}

/**
 * Glass button with liquid press effect
 */
@Composable
fun GlassButtonContainer(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_scale"
    )

    val blurAmount by animateDpAsState(
        targetValue = if (isPressed) 20.dp else 15.dp,
        animationSpec = tween(150),
        label = "button_blur"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .blur(blurAmount)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WakeUpColors.iosBlue.copy(alpha = 0.25f),
                        WakeUpColors.iosPurple.copy(alpha = 0.15f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = if (isPressed) 0.5f else 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Floating action button with glass effect
 */
@Composable
fun GlassFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fab_scale"
    )

    val glowRadius by animateDpAsState(
        targetValue = if (isPressed) 30.dp else 20.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "fab_glow"
    )

    Box(
        modifier = modifier
            .size(64.dp)
            .scale(scale)
            .drawBehind {
                // Outer glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            WakeUpColors.iosBlue.copy(alpha = 0.4f),
                            WakeUpColors.iosPurple.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    radius = (32.dp + glowRadius).toPx()
                )
            }
            .clip(RoundedCornerShape(20.dp))
            .blur(20.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WakeUpColors.iosBlue.copy(alpha = 0.3f),
                        WakeUpColors.iosPurple.copy(alpha = 0.2f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
