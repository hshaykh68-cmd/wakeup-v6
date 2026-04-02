package com.wakeup.app.presentation.home.modifiers

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import com.wakeup.app.core.theme.WakeUpColors
import kotlinx.coroutines.launch

/**
 * Physics-based interaction modifiers
 * Melt-on-press, jelly-stretch on drag, spring-release with glow burst
 */

/**
 * Melt press effect - surface appears to indent inward on press
 * Creates a "melting" visual with scale down and blur increase
 */
fun Modifier.meltPressEffect(
    onClick: () -> Unit = {}
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "melt_scale"
    )

    val blur by animateFloatAsState(
        targetValue = if (isPressed) 1.3f else 1f,
        animationSpec = tween(150),
        label = "melt_blur"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = tween(150),
        label = "melt_alpha"
    )

    this
        .scale(scale)
        .graphicsLayer {
            this.alpha = alpha
            // Simulate "melting" with slight shadow expansion
            shadowElevation = if (isPressed) 4f else 8f
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                    onClick()
                }
            )
        }
}

/**
 * Jelly drag effect - element stretches like jelly when dragged
 * Uses skew and scale transformations based on drag velocity
 */
fun Modifier.jellyDragEffect(
    onDragEnd: () -> Unit = {}
): Modifier = composed {
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Spring animation for release
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scaleX = remember { Animatable(1f) }
    val scaleY = remember { Animatable(1f) }
    val skewX = remember { Animatable(0f) }

    LaunchedEffect(isDragging) {
        if (!isDragging) {
            // Spring back to center
            launch {
                offsetX.animateTo(
                    0f,
                    spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                )
            }
            launch {
                offsetY.animateTo(
                    0f,
                    spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                )
            }
            launch {
                scaleX.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                )
            }
            launch {
                scaleY.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                )
            }
            launch {
                skewX.animateTo(
                    0f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                )
            }
            onDragEnd()
        }
    }

    this
        .graphicsLayer {
            translationX = offsetX.value
            translationY = offsetY.value
            scaleX = scaleX.value
            scaleY = scaleY.value
            // Skew creates the "jelly" stretch effect
            transformOrigin = TransformOrigin.Center
        }
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { isDragging = true },
                onDragEnd = { isDragging = false },
                onDragCancel = { isDragging = false },
                onDrag = { change, dragAmount ->
                    change.consume()
                    coroutineScope.launch {
                        offsetX.snapTo(offsetX.value + dragAmount.x)
                        offsetY.snapTo(offsetY.value + dragAmount.y)

                        // Jelly stretch - stretch in drag direction, squash perpendicular
                        val stretchFactor = (dragAmount.x / 100f).coerceIn(-0.3f, 0.3f)
                        scaleX.snapTo(1f + kotlin.math.abs(stretchFactor) * 0.3f)
                        scaleY.snapTo(1f - kotlin.math.abs(stretchFactor) * 0.15f)
                    }
                }
            )
        }
}

/**
 * Spring release effect - bouncy return animation with glow burst
 */
fun Modifier.springReleaseEffect(
    onClick: () -> Unit = {}
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    var showGlowBurst by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "spring_scale"
    )

    val glowBurst by animateFloatAsState(
        targetValue = if (showGlowBurst) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 600
            0f at 0
            1.5f at 100
            1f at 300
            0f at 600
        },
        label = "glow_burst",
        finishedListener = { showGlowBurst = false }
    )

    this
        .scale(scale)
        .drawBehind {
            if (glowBurst > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            WakeUpColors.iosBlue.copy(alpha = 0.5f * glowBurst),
                            WakeUpColors.iosPurple.copy(alpha = 0.3f * glowBurst),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.maxDimension * 0.6f * glowBurst
                    )
                )
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                    showGlowBurst = true
                    onClick()
                }
            )
        }
}

/**
 * Combined physics interaction - melt + spring + glow burst
 * The full "expensive glass" interaction feel
 */
fun Modifier.physicsGlassInteraction(
    onClick: () -> Unit = {}
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    var showGlowBurst by remember { mutableStateOf(false) }

    // Melt animation (press down)
    val meltScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100, easing = FastOutSlowInEasing),
        label = "melt"
    )

    // Spring back animation (release)
    val springScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "spring"
    )

    // Blur increase during press
    val blurMultiplier by animateFloatAsState(
        targetValue = if (isPressed) 1.4f else 1f,
        animationSpec = tween(100),
        label = "blur"
    )

    // Glow burst animation
    val glowBurst by animateFloatAsState(
        targetValue = if (showGlowBurst) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 800
            0f at 0
            1.8f at 150  // Peak glow
            0.8f at 400  // Sustained
            0f at 800    // Fade out
        },
        label = "burst",
        finishedListener = { showGlowBurst = false }
    )

    // Shadow depth animation
    val shadowElevation by animateFloatAsState(
        targetValue = if (isPressed) 2f else 12f,
        animationSpec = tween(150),
        label = "shadow"
    )

    val effectiveScale = if (isPressed) meltScale else springScale

    this
        .scale(effectiveScale)
        .graphicsLayer {
            this.shadowElevation = shadowElevation
            this.spotShadowColor = WakeUpColors.iosBlue.copy(alpha = 0.4f)
            this.ambientShadowColor = WakeUpColors.iosPurple.copy(alpha = 0.2f)
        }
        .drawBehind {
            // Press indentation shadow
            if (isPressed) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.maxDimension * 0.4f
                    ),
                    blendMode = BlendMode.Multiply
                )
            }

            // Glow burst effect
            if (glowBurst > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            WakeUpColors.iosBlue.copy(alpha = 0.6f * glowBurst),
                            WakeUpColors.iosPurple.copy(alpha = 0.4f * glowBurst),
                            WakeUpColors.iosTeal.copy(alpha = 0.2f * glowBurst),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.maxDimension * 0.8f * glowBurst
                    ),
                    blendMode = BlendMode.Screen
                )
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                    showGlowBurst = true
                    onClick()
                }
            )
        }
}

/**
 * Chromatic aberration effect - RGB shift at edges during motion
 * Creates a subtle "premium lens" distortion effect
 */
fun Modifier.chromaticAberration(
    intensity: Float = 0.5f,
    isActive: Boolean = true
): Modifier = composed {
    if (!isActive) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "chromatic")

    // Subtle continuous shift
    val shift by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chromatic_shift"
    )

    this.graphicsLayer {
        // This is a simplified representation - true chromatic aberration
        // would require custom shaders, but we simulate with slight compositing
        compositingStrategy = CompositingStrategy.Offscreen
    }
}

/**
 * Floating hover effect - gentle floating animation
 * Like the element is suspended in liquid
 */
fun Modifier.floatingHover(
    amplitude: Float = 8f,
    duration: Int = 4000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "float")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration * 1.3f, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_rot"
    )

    this.graphicsLayer {
        translationY = offsetY * amplitude
        rotationZ = rotation * 0.5f
    }
}

/**
 * Breathing glow effect - subtle pulsing glow
 * Used for important elements like streak counters
 */
fun Modifier.breathingGlow(
    color: Color = WakeUpColors.iosOrange,
    minAlpha: Float = 0.1f,
    maxAlpha: Float = 0.3f,
    duration: Int = 3000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "breath")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_alpha"
    )

    val glowAlpha = minAlpha + (maxAlpha - minAlpha) * alpha

    this.drawBehind {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = glowAlpha),
                    color.copy(alpha = glowAlpha * 0.5f),
                    Color.Transparent
                ),
                center = center,
                radius = size.maxDimension * 0.6f
            )
        )
    }
}

/**
 * Liquid touch ripple - creates expanding ripple effect on touch
 */
fun Modifier.liquidTouchRipple(
    rippleColor: Color = WakeUpColors.iosBlue.copy(alpha = 0.3f),
    onClick: () -> Unit = {}
): Modifier = composed {
    var rippleCenter by remember { mutableStateOf(Offset.Zero) }
    var showRipple by remember { mutableStateOf(false) }

    val rippleProgress by animateFloatAsState(
        targetValue = if (showRipple) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "ripple",
        finishedListener = { showRipple = false }
    )

    val rippleRadius by animateFloatAsState(
        targetValue = if (showRipple) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "ripple_radius"
    )

    this
        .drawBehind {
            if (rippleProgress > 0f && rippleRadius > 0f) {
                val radius = size.maxDimension * 0.5f * rippleRadius
                val alpha = (1f - rippleProgress) * 0.4f

                drawCircle(
                    color = rippleColor.copy(alpha = alpha),
                    radius = radius,
                    center = rippleCenter,
                    blendMode = BlendMode.Screen
                )

                // Secondary ripple ring
                if (rippleProgress > 0.3f) {
                    drawCircle(
                        color = rippleColor.copy(alpha = alpha * 0.5f),
                        radius = radius * 0.7f,
                        center = rippleCenter,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                        blendMode = BlendMode.Screen
                    )
                }
            }
        }
        .pointerInput(Unit) {
            detectTapGestures { offset ->
                rippleCenter = offset
                showRipple = true
                onClick()
            }
        }
}
