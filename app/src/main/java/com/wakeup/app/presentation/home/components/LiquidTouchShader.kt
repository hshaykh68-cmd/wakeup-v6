package com.wakeup.app.presentation.home.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.wakeup.app.core.theme.WakeUpColors
import kotlinx.coroutines.launch

/**
 * Liquid Touch Shader Effects
 * Creates ripple distortion and liquid interaction effects
 * Uses RuntimeShader on Android 13+ with fallback for older versions
 */

// AGSL shader for liquid ripple effect
private const val LIQUID_RIPPLE_SHADER = """
    uniform float2 resolution;
    uniform float time;
    uniform float2 touchPos;
    uniform float rippleStrength;
    uniform float rippleRadius;
    
    float4 main(float2 coord) {
        float2 uv = coord / resolution;
        float2 touchUV = touchPos / resolution;
        
        float dist = distance(uv, touchUV);
        float ripple = sin(dist * 50.0 - time * 5.0) * exp(-dist * 3.0) * rippleStrength;
        
        float2 distortedUV = uv + normalize(uv - touchUV) * ripple * 0.02;
        
        // Sample from distorted position
        float4 color = float4(0.0);
        color.a = 1.0 - smoothstep(0.0, rippleRadius, dist);
        color.rgb = float3(ripple * 0.5 + 0.5);
        
        return color;
    }
"""

/**
 * Liquid ripple container - applies ripple distortion effect
 */
@Composable
fun LiquidRippleContainer(
    modifier: Modifier = Modifier,
    rippleColor: Color = WakeUpColors.iosBlue,
    content: @Composable () -> Unit
) {
    var touchPosition by remember { mutableStateOf(Offset.Zero) }
    var isPressed by remember { mutableStateOf(false) }
    var rippleTime by remember { mutableStateOf(0f) }

    val coroutineScope = rememberCoroutineScope()

    // Ripple animation
    val rippleProgress by animateFloatAsState(
        targetValue = if (isPressed) 0f else 1f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "ripple_progress"
    )

    val rippleRadius by animateFloatAsState(
        targetValue = if (isPressed) 0f else 300f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "ripple_radius"
    )

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val position = event.changes.first().position
                        touchPosition = position
                        isPressed = event.changes.any { it.pressed }

                        if (!isPressed) {
                            coroutineScope.launch {
                                // Trigger ripple release
                                rippleTime = 0f
                            }
                        }
                    }
                }
            }
    ) {
        content()

        // Ripple overlay
        if (rippleRadius > 0 && !isPressed) {
            LiquidRippleOverlay(
                touchPosition = touchPosition,
                rippleRadius = rippleRadius,
                rippleStrength = 1f - rippleProgress,
                color = rippleColor,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Liquid ripple overlay - draws the actual ripple effect
 */
@Composable
private fun LiquidRippleOverlay(
    touchPosition: Offset,
    rippleRadius: Float,
    rippleStrength: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple_wave")

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Canvas(modifier = modifier) {
        // Draw ripple rings
        val rings = 3
        for (i in 0 until rings) {
            val ringProgress = (rippleStrength - i * 0.2f).coerceIn(0f, 1f)
            if (ringProgress <= 0) continue

            val ringRadius = rippleRadius * (0.5f + i * 0.3f)
            val ringAlpha = ringProgress * (1f - i * 0.3f)
            val ringWidth = 2.dp.toPx() * (1f - i * 0.2f)

            // Wave distortion
            val wave = kotlin.math.sin(wavePhase + i) * 3f

            drawCircle(
                color = color.copy(alpha = ringAlpha * 0.4f),
                radius = ringRadius + wave,
                center = touchPosition,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = ringWidth
                ),
                blendMode = BlendMode.Screen
            )
        }

        // Central glow burst
        if (rippleStrength > 0.3f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = rippleStrength * 0.5f),
                        color.copy(alpha = rippleStrength * 0.2f),
                        Color.Transparent
                    ),
                    center = touchPosition,
                    radius = rippleRadius * 0.3f
                ),
                center = touchPosition,
                radius = rippleRadius * 0.3f,
                blendMode = BlendMode.Screen
            )
        }
    }
}

/**
 * Distortion surface - applies liquid distortion to content
 * Simulates refraction through liquid
 */
@Composable
fun DistortionSurface(
    modifier: Modifier = Modifier,
    distortionStrength: Float = 0.5f,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "distortion")

    val distortionPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "distortion_phase"
    )

    Box(
        modifier = modifier.graphicsLayer {
            // Apply subtle warping effect through graphics layer
            val wave = kotlin.math.sin(distortionPhase) * distortionStrength * 0.02f
            scaleX = 1f + wave
            scaleY = 1f - wave * 0.5f
        }
    ) {
        content()
    }
}

/**
 * Liquid button - button with liquid fill effect
 */
@Composable
fun LiquidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fillColor: Color = WakeUpColors.iosBlue,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var fillProgress by remember { mutableStateOf(0f) }

    val animatedFill by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = if (isPressed) {
            tween(400, easing = FastOutSlowInEasing)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "liquid_fill"
    )

    val wavePhase by animateFloatAsState(
        targetValue = if (isPressed) 10f else 0f,
        animationSpec = tween(2000, easing = LinearEasing),
        label = "wave_phase"
    )

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        isPressed = event.changes.any { it.pressed }
                        if (!isPressed) {
                            onClick()
                        }
                    }
                }
            }
            .drawBehind {
                if (animatedFill > 0) {
                    // Liquid fill with wave effect
                    val fillHeight = size.height * animatedFill
                    val waveAmplitude = 4.dp.toPx() * (1f - animatedFill * 0.5f)

                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, size.height)

                        // Wave surface
                        val points = 20
                        for (i in 0..points) {
                            val x = size.width * i / points
                            val wave = kotlin.math.sin(
                                (i.toFloat() / points) * 4 * Math.PI.toFloat() + wavePhase
                            ).toFloat() * waveAmplitude
                            val y = size.height - fillHeight + wave
                            lineTo(x, y.toFloat())
                        }

                        lineTo(size.width, size.height)
                        close()
                    }

                    drawPath(
                        path = path,
                        color = fillColor.copy(alpha = 0.6f),
                        blendMode = BlendMode.SrcOver
                    )

                    // Highlight on liquid surface
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        blendMode = BlendMode.Screen
                    )
                }
            }
    ) {
        content()
    }
}

/**
 * Water droplet effect - creates droplet-like distortion
 */
@Composable
fun WaterDropletEffect(
    modifier: Modifier = Modifier,
    dropletCount: Int = 3,
    baseColor: Color = WakeUpColors.iosBlue
) {
    val infiniteTransition = rememberInfiniteTransition(label = "droplets")

    val droplets = List(dropletCount) { index ->
        val delay = index * 2000
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(6000 + delay, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "droplet_$index"
        )

        val scale by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 6000 + delay
                    0.5f at 0
                    1f at 2000
                    0.8f at 4000
                    0f at 6000 + delay
                }
            ),
            label = "droplet_scale_$index"
        )

        Triple(offsetY, scale, index)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        droplets.forEach { (offsetY, scale, index) ->
            if (scale <= 0.01f) return@forEach

            val x = size.width * (0.2f + index * 0.3f)
            val y = size.height * offsetY
            val radius = 20.dp.toPx() * scale

            // Droplet body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.4f * scale),
                        baseColor.copy(alpha = 0.2f * scale),
                        Color.Transparent
                    )
                ),
                radius = radius,
                center = Offset(x, y),
                blendMode = BlendMode.Screen
            )

            // Highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.6f * scale),
                radius = radius * 0.2f,
                center = Offset(x - radius * 0.3f, y - radius * 0.3f),
                blendMode = BlendMode.Screen
            )
        }
    }
}

/**
 * Viscous drag effect - content feels like it's in thick liquid
 */
@Composable
fun ViscousDragContainer(
    modifier: Modifier = Modifier,
    viscosity: Float = 0.7f,
    content: @Composable () -> Unit
) {
    var dragVelocity by remember { mutableStateOf(Offset.Zero) }
    val coroutineScope = rememberCoroutineScope()

    // Smooth velocity decay (viscous damping)
    val decay = remember { exponentialDecay<Offset>(frictionMultiplier = 1f - viscosity * 0.3f) }

    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    LaunchedEffect(dragVelocity) {
        if (dragVelocity != Offset.Zero) {
            animatedOffset.animateDecay(dragVelocity, decay)
        }
    }

    Box(
        modifier = modifier
            .offset {
                androidx.compose.ui.unit.IntOffset(
                    animatedOffset.value.x.toInt(),
                    animatedOffset.value.y.toInt()
                )
            }
            .graphicsLayer {
                // Viscous deformation
                val velocityMagnitude = kotlin.math.sqrt(
                    dragVelocity.x * dragVelocity.x + dragVelocity.y * dragVelocity.y
                )
                val deformation = (velocityMagnitude / 1000f).coerceIn(0f, 0.1f) * viscosity

                scaleX = 1f - deformation * 0.3f
                scaleY = 1f + deformation * 0.5f
            }
    ) {
        content()
    }
}
