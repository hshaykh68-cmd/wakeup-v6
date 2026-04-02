package com.wakeup.app.presentation.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.wakeup.app.core.theme.WakeUpColors

/**
 * Light Effects - Moving highlights, dynamic glow, chromatic aberration
 * Creates next-level polish with simulated light physics
 */

/**
 * Moving highlight overlay - subtle white gradient traveling across surface
 * Simulates light source moving across the glass
 */
@Composable
fun MovingHighlight(
    modifier: Modifier = Modifier,
    highlightWidth: Float = 0.3f,
    duration: Int = 6000,
    intensity: Float = 0.25f,
    angle: Float = 30f // degrees
) {
    val infiniteTransition = rememberInfiniteTransition(label = "highlight")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "highlight_progress"
    )

    Box(
        modifier = modifier.drawBehind {
            val angleRad = Math.toRadians(angle.toDouble())
            val cos = kotlin.math.cos(angleRad).toFloat()
            val sin = kotlin.math.sin(angleRad).toFloat()

            // Calculate highlight position along the angled path
            val pathLength = size.width * kotlin.math.abs(cos) + size.height * kotlin.math.abs(sin)
            val currentPos = progress * pathLength

            // Highlight center point
            val centerX = when {
                cos > 0 -> currentPos * cos
                else -> size.width + currentPos * cos
            }
            val centerY = when {
                sin > 0 -> currentPos * sin
                else -> size.height + currentPos * sin
            }

            // Draw the highlight gradient
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0f),
                        Color.White.copy(alpha = intensity * 0.5f),
                        Color.White.copy(alpha = intensity),
                        Color.White.copy(alpha = intensity * 0.5f),
                        Color.White.copy(alpha = 0f),
                        Color.Transparent
                    ),
                    start = Offset(
                        centerX - highlightWidth * size.width * cos,
                        centerY - highlightWidth * size.width * sin
                    ),
                    end = Offset(
                        centerX + highlightWidth * size.width * cos,
                        centerY + highlightWidth * size.width * sin
                    )
                ),
                blendMode = BlendMode.Screen
            )
        }
    )
}

/**
 * Dynamic glow based on interaction state
 * Glows brighter when element is active/pressed
 */
@Composable
fun DynamicGlow(
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    glowColor: Color = WakeUpColors.iosBlue,
    baseIntensity: Float = 0.15f,
    activeIntensity: Float = 0.4f,
    pulseEnabled: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dynamic_glow")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    val intensity by animateFloatAsState(
        targetValue = if (isActive) activeIntensity else baseIntensity,
        animationSpec = tween(300),
        label = "glow_intensity"
    )

    val finalIntensity = if (pulseEnabled) intensity * pulse else intensity

    Box(
        modifier = modifier.drawBehind {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = finalIntensity),
                        glowColor.copy(alpha = finalIntensity * 0.5f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.maxDimension * 0.7f
                ),
                blendMode = BlendMode.Screen
            )
        }
    )
}

/**
 * Edge glow effect - glows along the edges of the container
 * Creates a premium "light bleed" effect
 */
@Composable
fun EdgeGlow(
    modifier: Modifier = Modifier,
    glowColor: Color = WakeUpColors.iosBlue,
    intensity: Float = 0.3f,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "edge_glow")

    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Restart
        ),
        label = "edge_shimmer"
    )

    Box(
        modifier = modifier.drawBehind {
            val strokeWidth = 3.dp.toPx()

            // Top edge glow
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        glowColor.copy(alpha = intensity * (0.5f + shimmer * 0.5f)),
                        Color.Transparent
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f)
                ),
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(size.width, strokeWidth),
                blendMode = BlendMode.Screen
            )

            // Left edge glow
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        glowColor.copy(alpha = intensity * 0.7f),
                        glowColor.copy(alpha = intensity * 0.3f),
                        Color.Transparent
                    ),
                    startX = 0f,
                    endX = 0f,
                    startY = 0f,
                    endY = size.height
                ),
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(strokeWidth, size.height),
                blendMode = BlendMode.Screen
            )

            // Right edge glow (dimmer)
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        glowColor.copy(alpha = intensity * 0.2f),
                        glowColor.copy(alpha = intensity * 0.4f)
                    ),
                    startX = size.width - strokeWidth,
                    endX = size.width,
                    startY = 0f,
                    endY = size.height
                ),
                topLeft = Offset(size.width - strokeWidth, 0f),
                size = androidx.compose.ui.geometry.Size(strokeWidth, size.height),
                blendMode = BlendMode.Screen
            )
        }
    )
}

/**
 * Inner light source - creates a subtle light from within the element
 * Like there's a light source inside the glass
 */
@Composable
fun InnerLightSource(
    modifier: Modifier = Modifier,
    lightColor: Color = WakeUpColors.iosBlue,
    intensity: Float = 0.2f,
    pulseEnabled: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "inner_light")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "light_pulse"
    )

    val finalIntensity = if (pulseEnabled) intensity * pulse else intensity

    Box(
        modifier = modifier.drawBehind {
            // Central glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        lightColor.copy(alpha = finalIntensity),
                        lightColor.copy(alpha = finalIntensity * 0.5f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * 0.4f
                ),
                center = center,
                radius = size.minDimension * 0.4f,
                blendMode = BlendMode.Screen
            )

            // Soft ambient glow
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        lightColor.copy(alpha = finalIntensity * 0.3f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.maxDimension * 0.8f
                ),
                blendMode = BlendMode.Screen
            )
        }
    )
}

/**
 * Chromatic aberration effect - RGB split at edges
 * Simulates a high-end lens distortion
 */
@Composable
fun ChromaticAberrationEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    isActive: Boolean = true
) {
    if (!isActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "chromatic")

    val shift by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chromatic_shift"
    )

    Box(
        modifier = modifier.drawBehind {
            val maxShift = 2.dp.toPx() * intensity

            // Red channel offset
            drawRect(
                color = Color.Red.copy(alpha = 0.03f * kotlin.math.abs(shift)),
                topLeft = Offset(-maxShift * shift, 0f),
                blendMode = BlendMode.Screen
            )

            // Blue channel offset (opposite direction)
            drawRect(
                color = Color.Blue.copy(alpha = 0.03f * kotlin.math.abs(shift)),
                topLeft = Offset(maxShift * shift, 0f),
                blendMode = BlendMode.Screen
            )
        }
    )
}

/**
 * Ambient light overlay - subtle lighting that reacts to "time of day"
 * Warmer at "night", cooler during "day" (simulated)
 */
@Composable
fun AmbientLightOverlay(
    modifier: Modifier = Modifier,
    isNight: Boolean = true
) {
    val lightColor = if (isNight) {
        Color(0xFF4A3F6B).copy(alpha = 0.15f) // Warm purple night
    } else {
        Color(0xFFB8D4E3).copy(alpha = 0.1f) // Cool blue day
    }

    Box(
        modifier = modifier.drawBehind {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lightColor,
                        Color.Transparent,
                        lightColor.copy(alpha = lightColor.alpha * 0.5f)
                    )
                ),
                blendMode = BlendMode.Overlay
            )
        }
    )
}

/**
 * Star field effect - subtle twinkling dots for night mode
 */
@Composable
fun StarFieldOverlay(
    modifier: Modifier = Modifier,
    starCount: Int = 30,
    baseAlpha: Float = 0.3f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")

    // Multiple stars with different twinkle phases
    val twinkle1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle1"
    )

    val twinkle2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle2"
    )

    val twinkle3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle3"
    )

    val twinkles = listOf(twinkle1, twinkle2, twinkle3)

    Canvas(modifier = modifier.fillMaxSize()) {
        // Fixed star positions (deterministic pseudo-random)
        val seedPositions = listOf(
            Offset(size.width * 0.1f, size.height * 0.15f),
            Offset(size.width * 0.25f, size.height * 0.08f),
            Offset(size.width * 0.7f, size.height * 0.12f),
            Offset(size.width * 0.85f, size.height * 0.2f),
            Offset(size.width * 0.15f, size.height * 0.35f),
            Offset(size.width * 0.6f, size.height * 0.3f),
            Offset(size.width * 0.9f, size.height * 0.4f),
            Offset(size.width * 0.05f, size.height * 0.55f),
            Offset(size.width * 0.4f, size.height * 0.5f),
            Offset(size.width * 0.75f, size.height * 0.6f),
            Offset(size.width * 0.2f, size.height * 0.75f),
            Offset(size.width * 0.55f, size.height * 0.8f),
            Offset(size.width * 0.8f, size.height * 0.9f),
            Offset(size.width * 0.35f, size.height * 0.95f),
            Offset(size.width * 0.92f, size.height * 0.75f)
        )

        seedPositions.take(starCount.coerceAtMost(seedPositions.size)).forEachIndexed { index, position ->
            val twinkle = twinkles[index % twinkles.size]
            val starAlpha = baseAlpha * twinkle
            val starSize = 1.5f + (index % 3) * 0.5f

            drawCircle(
                color = Color.White.copy(alpha = starAlpha),
                radius = starSize.dp.toPx(),
                center = position,
                blendMode = BlendMode.Screen
            )
        }
    }
}

/**
 * Glass shimmer effect - traveling light across glass surface
 * Like light reflecting off a curved glass surface
 */
@Composable
fun GlassShimmer(
    modifier: Modifier = Modifier,
    shimmerWidth: Float = 0.25f,
    duration: Int = 5000,
    angle: Float = -45f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")

    val progress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    Box(
        modifier = modifier.drawBehind {
            val angleRad = Math.toRadians(angle.toDouble())
            val cos = kotlin.math.cos(angleRad).toFloat()
            val sin = kotlin.math.sin(angleRad).toFloat()

            // Shimmer line position
            val shimmerCenterX = size.width * progress
            val shimmerCenterY = size.height * 0.5f

            // Shimmer gradient
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0f),
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0f),
                        Color.Transparent
                    ),
                    start = Offset(
                        shimmerCenterX - shimmerWidth * size.width * cos,
                        shimmerCenterY - shimmerWidth * size.width * sin
                    ),
                    end = Offset(
                        shimmerCenterX + shimmerWidth * size.width * cos,
                        shimmerCenterY + shimmerWidth * size.width * sin
                    )
                ),
                blendMode = BlendMode.Screen
            )
        }
    )
}
