package com.wakeup.app.presentation.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.app.R
import com.wakeup.app.core.theme.WakeUpColors

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onContinue: () -> Unit
) {
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "background_zoom")
    
    // Background slow zoom animation (20s loop)
    val backgroundScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_scale"
    )

    // Logo animation: Scale 0.8→1.0, fade 0→1 (800ms)
    val logoAnimation = remember {
        Animatable(0f)
    }

    // Tagline animation: Slide up + fade (500ms delay)
    val taglineAnimation = remember {
        Animatable(0f)
    }

    // Button animation: Fade in (800ms delay)
    val buttonAnimation = remember {
        Animatable(0f)
    }

    // Trigger animations
    androidx.compose.runtime.LaunchedEffect(Unit) {
        logoAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = EaseOutCubic)
        )
        taglineAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, delayMillis = 500, easing = EaseOutCubic)
        )
        buttonAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, delayMillis = 800, easing = EaseOutCubic)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Static gradient background (replaces video to avoid ExoPlayer overhead on short screen)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0D0D2B), Color(0xFF1A0A3A))
                    )
                )
                .scale(backgroundScale)
        )

        // Dark overlay gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top spacer for balance
            Spacer(modifier = Modifier.height(40.dp))

            // Center content: Logo and tagline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Logo
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(0.8f + (logoAnimation.value * 0.2f))
                        .alpha(logoAnimation.value)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    WakeUpColors.iosBlue.copy(alpha = 0.6f),
                                    WakeUpColors.iosPurple.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        WakeUpColors.iosBlue,
                                        WakeUpColors.iosPurple
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // App name
                Text(
                    text = "WakeUp",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Glassmorphism tagline card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = ((1f - taglineAnimation.value) * 50).dp)
                        .alpha(taglineAnimation.value)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            )
                        )
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "The alarm that makes you\nactually wake up",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.95f),
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                }
            }

            // Bottom buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(buttonAnimation.value)
                    .padding(bottom = 16.dp)
            ) {
                // Shimmer Get Started button
                ShimmerButton(
                    onClick = onGetStarted,
                    text = "Get Started"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Continue link
                TextButton(
                    onClick = onContinue,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Continue to app →",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShimmerButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    
    // Shimmer animation
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    // Create shimmer brush
    val shimmerBrush = remember(shimmerOffset) {
        val gradient = listOf(
            Color.White.copy(alpha = 0.0f),
            Color.White.copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.0f)
        )
        
        val start = Offset(shimmerOffset * 1000f, 0f)
        val end = Offset(shimmerOffset * 1000f + 200f, 0f)
        
        Brush.linearGradient(
            colors = gradient,
            start = start,
            end = end,
            tileMode = TileMode.Clamp
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(WakeUpColors.iosBlue)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Shimmer overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(shimmerBrush)
        )
        
        // Button content
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}
