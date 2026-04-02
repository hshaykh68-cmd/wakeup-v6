package com.wakeup.app.presentation.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.app.R
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.core.designsystem.tokens.OpacityTokens
import com.wakeup.app.core.designsystem.tokens.SpacingTokens
import com.wakeup.app.core.designsystem.tokens.ShapeTokens
import com.wakeup.app.core.designsystem.tokens.IconSizeTokens
import com.wakeup.app.core.designsystem.components.buttons.WakeUpButton
import com.wakeup.app.core.designsystem.components.buttons.ButtonVariant
import com.wakeup.app.core.designsystem.components.buttons.ButtonSize
import com.wakeup.app.core.designsystem.components.buttons.WakeUpTextButton
import kotlinx.coroutines.launch
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoOnboardingScreen(
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated Background based on current page
        when (pagerState.currentPage) {
            0 -> AnimatedBackground(lottieResId = R.raw.alarm_forcing)
            1 -> AnimatedBackground(lottieResId = R.raw.supercharge_your_work)
            2 -> AnimatedBackground(lottieResId = R.raw.working_attentively)
            3 -> AnimatedBackground(lottieResId = R.raw.stats_analytics)
            4 -> AnimatedBackground(lottieResId = R.raw.premium_unlocked)
        }

        // Dark overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // Skip button (top right) - with status bar padding
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(SpacingTokens.md),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = onSkip,
                modifier = Modifier
                    .size(IconSizeTokens.touchTarget)
                    .background(Color.White.copy(alpha = OpacityTokens.prominent), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Skip onboarding",
                    tint = Color.White
                )
            }
        }

        // Page content - with safe area padding
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 180.dp) // Reserve space for bottom controls
                .padding(horizontal = SpacingTokens.screenEdge)
        ) { page ->
            when (page) {
                0 -> ProblemPage()
                1 -> SuperchargePage()
                2 -> MissionsShowcasePage()
                3 -> StatsPreviewPage()
                4 -> FinalCTAPage(onGetStarted = onComplete)
            }
        }

        // Bottom controls - with navigation bar padding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = SpacingTokens.lg, vertical = SpacingTokens.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = SpacingTokens.lg)
            ) {
                repeat(5) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == pagerState.currentPage) 24.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(ShapeTokens.xs))
                            .background(
                                if (index == pagerState.currentPage)
                                    WakeUpColors.iosBlue
                                else
                                    Color.White.copy(alpha = OpacityTokens.borderLight)
                            )
                            .animateContentSize()
                    )
                }
            }

            // Navigation buttons - using unified WakeUpButton
            if (pagerState.currentPage < 4) {
                WakeUpButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    text = "Continue",
                    variant = ButtonVariant.PRIMARY,
                    size = ButtonSize.LARGE,
                    icon = {
                        Spacer(modifier = Modifier.width(SpacingTokens.sm))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null
                        )
                    }
                )
            } else {
                WakeUpButton(
                    onClick = onComplete,
                    text = "Get Started",
                    variant = ButtonVariant.PREMIUM,
                    size = ButtonSize.LARGE,
                    icon = {
                        Spacer(modifier = Modifier.width(SpacingTokens.sm))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }
                )

                Spacer(modifier = Modifier.height(SpacingTokens.sm))

                WakeUpTextButton(
                    onClick = onComplete,
                    text = "Continue Free"
                )
            }
        }
    }
}

// Screen 1: Problem - "Tired of Sleeping Through Alarms?"
@Composable
private fun ProblemPage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SpacingTokens.screenEdge)
    ) {
        // Lottie animation takes top 60% - no text overlay
        // (Animation is handled by AnimatedBackground in parent)
        
        // Text content in bottom 40% with solid gradient background for contrast
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.0f),
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
                .padding(horizontal = SpacingTokens.lg, vertical = SpacingTokens.xl)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tired of Sleeping\nThrough Alarms?",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    lineHeight = 40.sp
                )

                Spacer(modifier = Modifier.height(SpacingTokens.sm))

                Text(
                    text = "60% of people snooze through their alarm. Not anymore.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = OpacityTokens.border)
                )

                Spacer(modifier = Modifier.height(SpacingTokens.md))

                Text(
                    text = "Swipe to explore →",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = OpacityTokens.strong)
                )
            }
        }
    }
}

// Screen 2: Solution - "Supercharge Your Morning"
@Composable
private fun SuperchargePage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SpacingTokens.screenEdge)
    ) {
        // Text content in bottom 40% with solid gradient background for contrast
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.0f),
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
                .padding(horizontal = SpacingTokens.lg, vertical = SpacingTokens.xl)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Supercharge Your\nMorning",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    lineHeight = 40.sp
                )

                Spacer(modifier = Modifier.height(SpacingTokens.sm))

                Text(
                    text = "Missions that force you to get up and move. No snoozing allowed.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = OpacityTokens.border)
                )

                Spacer(modifier = Modifier.height(SpacingTokens.md))

                // Mission type pills - simplified
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MissionTypePill("Math", WakeUpColors.iosBlue)
                    MissionTypePill("Memory", WakeUpColors.iosPurple)
                    MissionTypePill("Shake", WakeUpColors.iosOrange)
                    MissionTypePill("Photo", WakeUpColors.iosPink)
                }
            }
        }
    }
}

@Composable
private fun MissionTypePill(
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(ShapeTokens.xl))
            .background(color.copy(alpha = OpacityTokens.strong))
            .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.sm)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Screen 3: Missions - "Missions That Actually Work"
@Composable
private fun MissionsShowcasePage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SpacingTokens.screenEdge)
    ) {
        // Text content in bottom 40% with solid gradient background for contrast
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.0f),
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
                .padding(horizontal = SpacingTokens.lg, vertical = SpacingTokens.xl)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Missions That\nActually Work",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    lineHeight = 40.sp
                )

                Spacer(modifier = Modifier.height(SpacingTokens.sm))

                Text(
                    text = "Complete challenges to dismiss. Easy, Medium, or Hard.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = OpacityTokens.border)
                )

                Spacer(modifier = Modifier.height(SpacingTokens.md))

                // Difficulty chips only - removed mission cards
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DifficultyChip("Easy", WakeUpColors.iosGreen)
                    DifficultyChip("Medium", WakeUpColors.iosOrange)
                    DifficultyChip("Hard", WakeUpColors.iosRed)
                }
            }
        }
    }
}

@Composable
private fun DifficultyChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(ShapeTokens.md))
            .background(color.copy(alpha = OpacityTokens.light))
            .padding(horizontal = SpacingTokens.mdLg, vertical = SpacingTokens.smMd)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Screen 4: Stats - "Watch Your Progress Grow"
@Composable
private fun StatsPreviewPage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SpacingTokens.screenEdge)
    ) {
        // Text content in bottom 40% with solid gradient background for contrast
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.0f),
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
                .padding(horizontal = SpacingTokens.lg, vertical = SpacingTokens.xl)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Watch Your\nProgress Grow",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    lineHeight = 40.sp
                )

                Spacer(modifier = Modifier.height(SpacingTokens.sm))

                Text(
                    text = "Build streaks, track success rates, and celebrate your wins.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = OpacityTokens.border)
                )

                Spacer(modifier = Modifier.height(SpacingTokens.md))

                // Simple stats summary - removed complex cards
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "7",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = WakeUpColors.iosOrange
                        )
                        Text(
                            text = "Day Streak",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = OpacityTokens.strong)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "85%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = WakeUpColors.iosGreen
                        )
                        Text(
                            text = "Success Rate",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = OpacityTokens.strong)
                        )
                    }
                }
            }
        }
    }
}

// Screen 5: Final CTA - "Ready to Wake Up?"
@Composable
private fun FinalCTAPage(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SpacingTokens.screenEdge)
    ) {
        // Text content in bottom 40% with solid gradient background for contrast
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.0f),
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.9f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
                .padding(horizontal = SpacingTokens.lg, vertical = SpacingTokens.xl)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Premium badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(ShapeTokens.smMd))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    WakeUpColors.iosGold,
                                    WakeUpColors.iosYellow
                                )
                            )
                        )
                        .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.sm)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(IconSizeTokens.sm)
                        )
                        Spacer(modifier = Modifier.width(SpacingTokens.xs))
                        Text(
                            text = "PREMIUM",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(SpacingTokens.md))

                Text(
                    text = "Ready to\nWake Up?",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    lineHeight = 40.sp
                )

                Spacer(modifier = Modifier.height(SpacingTokens.sm))

                Text(
                    text = "Join thousands who've transformed their mornings.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = OpacityTokens.border)
                )

                Spacer(modifier = Modifier.height(SpacingTokens.md))

                // Simplified feature list
                Column(
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    PremiumFeatureItem("All Mission Types")
                    PremiumFeatureItem("Advanced Analytics")
                    PremiumFeatureItem("No Ads")
                }
            }
        }
    }
}

@Composable
private fun PremiumFeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(WakeUpColors.iosGreen.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = WakeUpColors.iosGreen,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}

// Simple FlowRow implementation
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val hGapPx = with(density) { 12.dp.roundToPx() }
        val vGapPx = with(density) { 12.dp.roundToPx() }
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()

        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)

            if (currentRow.isNotEmpty() && currentRowWidth + hGapPx + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                rowWidths.add(currentRowWidth)
                rowHeights.add(currentRowHeight)
                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }

            if (currentRow.isNotEmpty()) {
                currentRowWidth += hGapPx
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentRowWidth)
            rowHeights.add(currentRowHeight)
        }

        val totalHeight = rowHeights.sum() + (rowHeights.size - 1).coerceAtLeast(0) * vGapPx
        val width = constraints.maxWidth

        layout(width, totalHeight) {
            var y = 0
            rows.forEachIndexed { rowIndex, row ->
                var x = when (horizontalArrangement) {
                    Arrangement.Center -> (width - rowWidths[rowIndex]) / 2
                    Arrangement.End -> width - rowWidths[rowIndex]
                    else -> 0
                }
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + hGapPx
                }
                y += rowHeights[rowIndex] + vGapPx
            }
        }
    }
}

private fun Modifier.animateContentSize(): Modifier = this
