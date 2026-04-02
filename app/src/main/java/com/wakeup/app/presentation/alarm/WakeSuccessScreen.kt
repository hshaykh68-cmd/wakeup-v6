package com.wakeup.app.presentation.alarm

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.core.util.MorningQuote
import com.wakeup.app.core.util.MorningQuotesHelper
import com.wakeup.app.core.util.StreakMilestone
import com.wakeup.app.core.util.StreakMilestoneHelper
import com.wakeup.app.domain.repository.StatsRepository
import com.wakeup.app.domain.usecase.RecordWakeAttemptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class WakeSuccessViewModel @Inject constructor(
    private val recordWakeAttemptUseCase: RecordWakeAttemptUseCase,
    private val statsRepository: StatsRepository,
    private val morningQuotesHelper: MorningQuotesHelper,
    private val streakMilestoneHelper: StreakMilestoneHelper
) : ViewModel() {
    
    val streakFlow: Flow<Int> = statsRepository.getStreakFlow()
    
    suspend fun recordSuccess(historyId: String? = null, missionCompleted: Boolean = true) {
        recordWakeAttemptUseCase(
            historyId = historyId ?: "", 
            snoozeCount = 0,
            missionCompleted = missionCompleted,
            success = true
        )
        statsRepository.updateStreak(success = true)
        statsRepository.incrementMissionCount()
    }
    
    /**
     * Get dynamic morning quote for the current time.
     */
    fun getMorningQuote(): MorningQuote {
        return morningQuotesHelper.getQuoteForCurrentTime()
    }
    
    /**
     * Check if current streak is a milestone.
     */
    fun checkMilestone(streak: Int): StreakMilestone? {
        return streakMilestoneHelper.checkMilestone(streak)
    }
    
    /**
     * Get streak celebration quote.
     */
    fun getStreakQuote(streak: Int): String {
        return morningQuotesHelper.getStreakQuote(streak)
    }
}

@Composable
fun WakeSuccessScreen(
    viewModel: WakeSuccessViewModel = hiltViewModel(),
    historyId: String? = null,
    onDismiss: () -> Unit
) {
    var showCelebration by remember { mutableStateOf(false) }
    var pulseAnimation by remember { mutableStateOf(false) }
    val streak by viewModel.streakFlow.collectAsState(initial = 0)
    
    // Get dynamic morning quote
    val morningQuote by remember { mutableStateOf(viewModel.getMorningQuote()) }
    
    // Check for milestone
    val milestone = remember(streak) { viewModel.checkMilestone(streak) }
    var showMilestoneCelebration by remember { mutableStateOf(milestone != null) }
    
    LaunchedEffect(Unit) {
        // Record stats on successful wake
        viewModel.recordSuccess(historyId, missionCompleted = true)
        
        delay(300)
        showCelebration = true
        pulseAnimation = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (pulseAnimation) 1.2f else 1f,
        label = "pulse"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WakeUpColors.iosGreen.copy(alpha = 0.3f),
                        WakeUpColors.iosDarkBackground
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Success Icon with glassmorphic effect
            GlassmorphicSuccessIcon(scale = scale)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Dynamic motivational quote based on time
            Text(
                text = morningQuote.text,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Time context with statistic
            Text(
                text = morningQuote.timeContext,
                fontSize = 16.sp,
                color = WakeUpColors.iosYellow.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Subtitle message
            Text(
                text = "You completed the mission and woke up on time!",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Streak Card with glassmorphism
            if (streak > 0) {
                val streakQuote = viewModel.getStreakQuote(streak)
                GlassmorphicStreakCard(
                    streak = streak,
                    streakQuote = streakQuote,
                    isMilestone = milestone != null
                )
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Continue button with glass effect
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WakeUpColors.iosGreen
                )
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        // Milestone Celebration Overlay (full-screen Lottie animation)
        if (showMilestoneCelebration && milestone != null) {
            MilestoneCelebrationOverlay(
                milestone = milestone,
                onDismiss = { showMilestoneCelebration = false }
            )
        }
    }
}

@Composable
private fun GlassmorphicSuccessIcon(scale: Float) {
    Box(
        modifier = Modifier
            .size(140.dp)
            .scale(scale)
            .background(
                color = Color.White.copy(alpha = 0.1f),
                shape = CircleShape
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        WakeUpColors.iosGreen.copy(alpha = 0.3f),
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
                    color = Color.White.copy(alpha = 0.15f),
                    shape = CircleShape
                )
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            WakeUpColors.iosGreen.copy(alpha = 0.8f),
                            WakeUpColors.iosGreen
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                modifier = Modifier.size(56.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun GlassmorphicStreakCard(
    streak: Int,
    streakQuote: String,
    isMilestone: Boolean
) {
    val cardColor = when {
        isMilestone -> WakeUpColors.iosPurple
        streak >= 30 -> WakeUpColors.iosOrange
        streak >= 7 -> WakeUpColors.iosYellow
        else -> WakeUpColors.iosYellow
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .background(
                color = cardColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = if (isMilestone) 2.dp else 1.dp,
                color = cardColor.copy(alpha = if (isMilestone) 0.5f else 0.2f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = cardColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMilestone) Icons.Default.Star else Icons.Default.EmojiEvents,
                    contentDescription = if (isMilestone) "Milestone" else "Streak",
                    tint = cardColor,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isMilestone) "🎉 $streak Day Milestone! 🎉" else "$streak Day Streak!",
                fontSize = if (isMilestone) 24.sp else 28.sp,
                fontWeight = FontWeight.Bold,
                color = cardColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = streakQuote,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            if (isMilestone) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to celebrate!",
                    fontSize = 12.sp,
                    color = cardColor.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Full-screen milestone celebration overlay with Lottie animation placeholder
 */
@Composable
private fun MilestoneCelebrationOverlay(
    milestone: StreakMilestone,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-dismiss after 4 seconds
    LaunchedEffect(Unit) {
        delay(4000)
        onDismiss()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated celebration ring
            val infiniteTransition = rememberInfiniteTransition(label = "celebration")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "celebration_pulse"
            )
            
            // Central celebration icon
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                WakeUpColors.iosYellow.copy(alpha = 0.4f),
                                WakeUpColors.iosOrange.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            color = WakeUpColors.iosYellow.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .border(
                            width = 3.dp,
                            color = WakeUpColors.iosYellow,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Milestone",
                        modifier = Modifier.size(80.dp),
                        tint = WakeUpColors.iosYellow
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Milestone title
            Text(
                text = milestone.title,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = WakeUpColors.iosYellow,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Milestone message
            Text(
                text = milestone.message,
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Share button
            Button(
                onClick = { 
                    // Share functionality would go here
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WakeUpColors.iosYellow
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share Achievement",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Tap anywhere to continue",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}
