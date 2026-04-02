package com.wakeup.app.presentation.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.wakeup.app.core.theme.WakeUpColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Aurora Background - Multi-layer animated gradient system
 * Creates a flowing northern lights effect with 5 floating color blobs
 * Each blob moves in a unique orbital path with varying speeds
 */
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    blobColors: List<Color> = listOf(
        WakeUpColors.iosPurple.copy(alpha = 0.6f),
        WakeUpColors.iosBlue.copy(alpha = 0.5f),
        Color(0xFF9D4EDD).copy(alpha = 0.5f), // Deep magenta
        WakeUpColors.iosTeal.copy(alpha = 0.4f),
        Color(0xFFC77DFF).copy(alpha = 0.4f) // Light purple
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")

    // Blob 1 - Large slow orbit
    val blob1X by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(45000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob1X"
    )
    val blob1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(38000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob1Y"
    )

    // Blob 2 - Medium orbit
    val blob2X by infiniteTransition.animateFloat(
        initialValue = PI.toFloat(),
        targetValue = 3 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(32000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob2X"
    )
    val blob2Y by infiniteTransition.animateFloat(
        initialValue = PI.toFloat() / 2,
        targetValue = 5 * PI.toFloat() / 2,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob2Y"
    )

    // Blob 3 - Fast small orbit
    val blob3X by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob3X"
    )
    val blob3Y by infiniteTransition.animateFloat(
        initialValue = PI.toFloat(),
        targetValue = 3 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob3Y"
    )

    // Blob 4 - Diagonal drift
    val blob4Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob4Offset"
    )

    // Blob 5 - Pulsing center blob
    val blob5Pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob5Pulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Base dark gradient
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0D1A),
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
        }

        // Layer 1: Large blurred blobs
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Blob 1 - Top left area
            drawCircle(
                color = blobColors[0],
                radius = size.minDimension * 0.4f,
                center = Offset(
                    centerX + cos(blob1X) * size.width * 0.3f - size.width * 0.1f,
                    centerY + sin(blob1Y) * size.height * 0.25f - size.height * 0.1f
                )
            )

            // Blob 2 - Bottom right area
            drawCircle(
                color = blobColors[1],
                radius = size.minDimension * 0.35f,
                center = Offset(
                    centerX + cos(blob2X) * size.width * 0.25f + size.width * 0.1f,
                    centerY + sin(blob2Y) * size.height * 0.3f + size.height * 0.1f
                )
            )
        }

        // Layer 2: Medium blur blobs
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(50.dp)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Blob 3 - Floating center-right
            drawCircle(
                color = blobColors[2],
                radius = size.minDimension * 0.25f,
                center = Offset(
                    centerX + cos(blob3X) * size.width * 0.15f + size.width * 0.15f,
                    centerY + sin(blob3Y) * size.height * 0.15f
                )
            )

            // Blob 4 - Diagonal drift
            drawCircle(
                color = blobColors[3],
                radius = size.minDimension * 0.3f,
                center = Offset(
                    centerX - size.width * 0.2f + blob4Offset * size.width * 0.3f,
                    centerY - size.height * 0.15f + blob4Offset * size.height * 0.2f
                )
            )
        }

        // Layer 3: Sharp center glow with pulse
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(30.dp)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2

            drawCircle(
                color = blobColors[4],
                radius = size.minDimension * 0.2f * blob5Pulse,
                center = Offset(centerX, centerY - size.height * 0.05f)
            )
        }

        // Layer 4: Noise texture overlay (subtle)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = Color.White.copy(alpha = 0.02f),
                blendMode = BlendMode.Overlay
            )
        }
    }
}

/**
 * Simplified Aurora background for older devices
 * Uses fewer layers and simpler animations
 */
@Composable
fun AuroraBackgroundLite(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_lite")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset2"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(60.dp)
        ) {
            drawCircle(
                color = WakeUpColors.iosPurple.copy(alpha = 0.5f),
                radius = size.minDimension * 0.5f,
                center = Offset(
                    size.width * 0.3f + offset1 * size.width * 0.2f,
                    size.height * 0.4f
                )
            )

            drawCircle(
                color = WakeUpColors.iosBlue.copy(alpha = 0.4f),
                radius = size.minDimension * 0.4f,
                center = Offset(
                    size.width * 0.7f - offset2 * size.width * 0.15f,
                    size.height * 0.6f
                )
            )
        }
    }
}

/**
 * Aurora with interactive parallax
 * Responds to scroll or touch position
 */
@Composable
fun AuroraBackgroundInteractive(
    modifier: Modifier = Modifier,
    scrollOffset: Float = 0f,
    touchOffset: Offset = Offset.Zero
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_interactive")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(70.dp)
        ) {
            val parallaxX = scrollOffset * 0.3f + touchOffset.x * 0.1f
            val parallaxY = scrollOffset * 0.2f + touchOffset.y * 0.1f

            // Primary blob with parallax
            drawCircle(
                color = WakeUpColors.iosPurple.copy(alpha = 0.5f),
                radius = size.minDimension * 0.4f,
                center = Offset(
                    size.width * 0.3f + cos(time) * size.width * 0.15f - parallaxX,
                    size.height * 0.35f + sin(time * 0.8f) * size.height * 0.1f - parallaxY
                )
            )

            // Secondary blob with inverse parallax
            drawCircle(
                color = WakeUpColors.iosBlue.copy(alpha = 0.4f),
                radius = size.minDimension * 0.35f,
                center = Offset(
                    size.width * 0.7f + cos(time * 1.2f + PI) * size.width * 0.12f + parallaxX * 0.5f,
                    size.height * 0.6f + sin(time * 0.9f) * size.height * 0.15f + parallaxY * 0.5f
                )
            )

            // Accent blob
            drawCircle(
                color = Color(0xFF9D4EDD).copy(alpha = 0.35f),
                radius = size.minDimension * 0.25f,
                center = Offset(
                    size.width * 0.5f + cos(time * 0.7f) * size.width * 0.2f,
                    size.height * 0.5f + sin(time) * size.height * 0.2f
                )
            )
        }
    }
}
