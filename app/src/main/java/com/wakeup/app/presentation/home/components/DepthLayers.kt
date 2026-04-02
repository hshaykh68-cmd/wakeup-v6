package com.wakeup.app.presentation.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.map
import kotlin.math.absoluteValue

/**
 * Depth Layers - 5-layer parallax system
 * Creates depth perception through differential movement speeds
 * Layers: Background → Blobs → Cards → Content → Glow (closest)
 */

/**
 * Parallax configuration for each depth layer
 */
data class ParallaxConfig(
    val speedMultiplier: Float, // 0.0 = no movement, 1.0 = full movement
    val verticalOffset: Float = 0f,
    val blurAmount: Float = 0f
)

/**
 * 5-layer depth configuration
 */
object DepthLayers {
    // Layer 0: Background (farthest, slowest)
    val Background = ParallaxConfig(speedMultiplier = 0.1f, blurAmount = 0f)

    // Layer 1: Floating blurred blobs (mid-depth, slow movement)
    val FloatingBlobs = ParallaxConfig(speedMultiplier = 0.25f, blurAmount = 20f)

    // Layer 2: Glass cards base
    val GlassCards = ParallaxConfig(speedMultiplier = 0.5f, blurAmount = 0f)

    // Layer 3: Floating content elements
    val FloatingContent = ParallaxConfig(speedMultiplier = 0.7f, verticalOffset = -10f)

    // Layer 4: Glow/highlight overlay (closest, subtle parallax)
    val GlowOverlay = ParallaxConfig(speedMultiplier = 0.85f, blurAmount = 0f)
}

/**
 * Parallax scroll state - tracks scroll position for parallax calculations
 */
@Composable
fun rememberParallaxScrollState(): ParallaxScrollState {
    return remember { ParallaxScrollState() }
}

class ParallaxScrollState {
    var scrollOffset by mutableStateOf(0f)
    var scrollVelocity by mutableStateOf(0f)
    var touchOffset by mutableStateOf(Offset.Zero)
}

/**
 * Depth layer container - applies parallax transformation based on layer config
 */
@Composable
fun DepthLayer(
    config: ParallaxConfig,
    scrollState: ParallaxScrollState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "depth_float")

    // Continuous subtle floating animation
    val floatY by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000 + (config.speedMultiplier * 4000).toInt(), easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "depth_float_${config.speedMultiplier}"
    )

    // Calculate parallax offset based on scroll and layer speed
    val parallaxOffset = scrollState.scrollOffset * config.speedMultiplier
    val floatOffset = floatY * config.verticalOffset

    val totalOffsetY = parallaxOffset + floatOffset

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = totalOffsetY
                // Slight scale for depth perception
                val depthScale = 1f - (1f - config.speedMultiplier) * 0.05f
                scaleX = depthScale
                scaleY = depthScale

                // Apply blur for atmospheric perspective
                if (config.blurAmount > 0) {
                    // Note: blur is applied via modifier chain, not graphicsLayer directly
                }
            }
            .then(
                if (config.blurAmount > 0) {
                    Modifier.blur(config.blurAmount.dp)
                } else Modifier
            )
    ) {
        content()
    }
}

/**
 * Complete depth system container with all 5 layers
 */
@Composable
fun DepthSystemContainer(
    scrollState: ParallaxScrollState,
    backgroundLayer: @Composable () -> Unit,
    blobLayer: @Composable () -> Unit,
    cardLayer: @Composable () -> Unit,
    contentLayer: @Composable () -> Unit,
    glowLayer: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Layer 0: Background (farthest)
        DepthLayer(
            config = DepthLayers.Background,
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            backgroundLayer()
        }

        // Layer 1: Floating blobs (mid-depth)
        DepthLayer(
            config = DepthLayers.FloatingBlobs,
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            blobLayer()
        }

        // Layer 2: Glass cards
        DepthLayer(
            config = DepthLayers.GlassCards,
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            cardLayer()
        }

        // Layer 3: Floating content
        DepthLayer(
            config = DepthLayers.FloatingContent,
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            contentLayer()
        }

        // Layer 4: Glow overlay (closest)
        DepthLayer(
            config = DepthLayers.GlowOverlay,
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            glowLayer()
        }
    }
}

/**
 * Nested scroll connection that provides parallax data
 */
fun Modifier.parallaxNestedScroll(
    scrollState: ParallaxScrollState
): Modifier = nestedScroll(
    object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            scrollState.scrollOffset += available.y
            scrollState.scrollVelocity = available.y
            return Offset.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            // Decay velocity after fling
            scrollState.scrollVelocity *= 0.9f
            return Velocity.Zero
        }
    }
)

/**
 * Touch-responsive parallax - reacts to finger position
 */
@Composable
fun TouchResponsiveParallax(
    scrollState: ParallaxScrollState,
    sensitivity: Float = 0.1f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val position = event.changes.firstOrNull()?.position
                    if (position != null) {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        scrollState.touchOffset = Offset(
                            (position.x - centerX) * sensitivity,
                            (position.y - centerY) * sensitivity
                        )
                    }
                }
            }
        }
    ) {
        content()
    }
}

/**
 * Floating elements layer - content that floats above cards
 */
@Composable
fun FloatingElementsLayer(
    scrollState: ParallaxScrollState,
    modifier: Modifier = Modifier,
    elements: @Composable () -> Unit
) {
    DepthLayer(
        config = DepthLayers.FloatingContent,
        scrollState = scrollState,
        modifier = modifier
    ) {
        elements()
    }
}

/**
 * Depth-aware card - individual card with depth effects
 */
@Composable
fun DepthAwareCard(
    depth: Float, // 0.0 = far, 1.0 = close
    scrollState: ParallaxScrollState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val config = ParallaxConfig(
        speedMultiplier = 0.3f + depth * 0.5f,
        verticalOffset = -5f - depth * 10f
    )

    DepthLayer(
        config = config,
        scrollState = scrollState,
        modifier = modifier
    ) {
        content()
    }
}

/**
 * 3D tilt effect - card tilts based on touch position
 */
@Composable
fun Tilt3DEffect(
    maxTiltDegrees: Float = 5f,
    content: @Composable () -> Unit
) {
    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }

    val animatedTiltX by animateFloatAsState(
        targetValue = tiltX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tilt_x"
    )

    val animatedTiltY by animateFloatAsState(
        targetValue = tiltY,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tilt_y"
    )

    Box(
        modifier = Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val position = event.changes.firstOrNull()?.position
                    val bounds = this.size

                    if (position != null && bounds.width > 0 && bounds.height > 0) {
                        val normalizedX = (position.x / bounds.width - 0.5f) * 2f
                        val normalizedY = (position.y / bounds.height - 0.5f) * 2f

                        tiltY = normalizedX * maxTiltDegrees // Y rotation based on X position
                        tiltX = -normalizedY * maxTiltDegrees // X rotation based on Y position
                    } else {
                        tiltX = 0f
                        tiltY = 0f
                    }

                    if (event.changes.none { it.pressed }) {
                        tiltX = 0f
                        tiltY = 0f
                    }
                }
            }
        }
        .graphicsLayer {
            rotationX = animatedTiltX
            rotationY = animatedTiltY
            cameraDistance = 12f
            transformOrigin = TransformOrigin.Center
        }
    ) {
        content()
    }
}

/**
 * Z-depth shadow - shadow that gets larger/closer with depth
 */
@Composable
fun ZDepthShadow(
    depth: Float, // 0.0 = far/small shadow, 1.0 = close/large shadow
    content: @Composable () -> Unit
) {
    val shadowElevation = 4.dp + (depth * 20).dp
    val shadowAlpha = 0.1f + depth * 0.2f

    Box(
        modifier = Modifier.graphicsLayer {
            this.shadowElevation = shadowElevation.value
            this.spotShadowColor = Color.Black.copy(alpha = shadowAlpha)
            this.ambientShadowColor = Color.Black.copy(alpha = shadowAlpha * 0.5f)
        }
    ) {
        content()
    }
}

/**
 * Foreground blur on scroll - blurs foreground when scrolling fast
 */
@Composable
fun ForegroundBlurOnScroll(
    scrollVelocity: Float,
    maxBlur: Float = 8f,
    content: @Composable () -> Unit
) {
    val blurAmount = kotlin.math.min(
        kotlin.math.abs(scrollVelocity) / 50f,
        maxBlur
    )

    val animatedBlur by animateFloatAsState(
        targetValue = blurAmount,
        animationSpec = tween(100),
        label = "scroll_blur"
    )

    Box(
        modifier = Modifier.blur(animatedBlur.dp)
    ) {
        content()
    }
}

/**
 * Perspective scroll container - creates 3D perspective during scroll
 */
@Composable
fun PerspectiveScrollContainer(
    scrollState: ParallaxScrollState,
    perspectiveAmount: Float = 0.02f,
    content: @Composable () -> Unit
) {
    val perspectiveScale = 1f - kotlin.math.abs(scrollState.scrollOffset) * perspectiveAmount / 1000f

    val animatedScale by animateFloatAsState(
        targetValue = perspectiveScale.coerceIn(0.95f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "perspective_scale"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale

            // Slight perspective tilt during scroll
            val tilt = (scrollState.scrollVelocity / 1000f).coerceIn(-3f, 3f)
            rotationX = tilt
        }
    ) {
        content()
    }
}
