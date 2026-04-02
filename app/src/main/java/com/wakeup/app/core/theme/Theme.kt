package com.wakeup.app.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// iOS-inspired Glassmorphic Color Palette
object WakeUpColors {
    // Light Theme - iOS style
    val iosBackground = Color(0xFFF2F2F7)
    val iosSurface = Color(0xFFFFFFFF)
    val iosCard = Color(0xCCFFFFFF) // 80% opacity for glass effect
    val iosCardBorder = Color(0x40FFFFFF)
    val iosBlue = Color(0xFF007AFF)
    val iosGreen = Color(0xFF34C759)
    val iosOrange = Color(0xFFFF9500)
    val iosRed = Color(0xFFFF3B30)
    val iosPurple = Color(0xFF5856D6)
    val iosPink = Color(0xFFFF2D55)
    val iosGold = Color(0xFFFFD700)
    val iosTeal = Color(0xFF5AC8FA)
    val iosYellow = Color(0xFFFFCC00)
    val iosGray = Color(0xFF8E8E93)
    val iosGray2 = Color(0xFFAEAEB2)
    val iosGray3 = Color(0xFFC7C7CC)
    val iosGray4 = Color(0xFFD1D1D6)
    val iosGray5 = Color(0xFFE5E5EA)
    val iosGray6 = Color(0xFFF2F2F7)
    val iosTextPrimary = Color(0xFF000000)
    val iosTextSecondary = Color(0xFF8E8E93)
    
    // Dark Theme - iOS dark mode
    val iosDarkBackground = Color(0xFF000000)
    val iosDarkSurface = Color(0xFF1C1C1E)
    val iosDarkCard = Color(0xCC1C1C1E) // Glass effect on dark
    val iosDarkCardBorder = Color(0x40FFFFFF)
    val iosDarkTextPrimary = Color(0xFFFFFFFF)
    val iosDarkTextSecondary = Color(0xFF8E8E93)
    val iosDarkElevated = Color(0xFF2C2C2E)
}

private val LightColorScheme = lightColorScheme(
    primary = WakeUpColors.iosBlue,
    onPrimary = Color.White,
    primaryContainer = WakeUpColors.iosBlue.copy(alpha = 0.1f),
    onPrimaryContainer = WakeUpColors.iosBlue,
    secondary = WakeUpColors.iosPurple,
    onSecondary = Color.White,
    secondaryContainer = WakeUpColors.iosPurple.copy(alpha = 0.1f),
    onSecondaryContainer = WakeUpColors.iosPurple,
    tertiary = WakeUpColors.iosTeal,
    onTertiary = Color.Black,
    tertiaryContainer = WakeUpColors.iosTeal.copy(alpha = 0.1f),
    onTertiaryContainer = WakeUpColors.iosTeal,
    background = WakeUpColors.iosBackground,
    onBackground = WakeUpColors.iosTextPrimary,
    surface = WakeUpColors.iosSurface,
    onSurface = WakeUpColors.iosTextPrimary,
    surfaceVariant = WakeUpColors.iosGray6,
    onSurfaceVariant = WakeUpColors.iosTextSecondary,
    error = WakeUpColors.iosRed,
    onError = Color.White,
    errorContainer = WakeUpColors.iosRed.copy(alpha = 0.1f),
    onErrorContainer = WakeUpColors.iosRed,
    outline = WakeUpColors.iosGray4,
    outlineVariant = WakeUpColors.iosGray5,
    inverseSurface = WakeUpColors.iosTextPrimary,
    inverseOnSurface = WakeUpColors.iosSurface,
    inversePrimary = WakeUpColors.iosBlue,
    surfaceTint = WakeUpColors.iosBlue.copy(alpha = 0.05f)
)

private val DarkColorScheme = darkColorScheme(
    primary = WakeUpColors.iosBlue,
    onPrimary = Color.White,
    primaryContainer = WakeUpColors.iosBlue.copy(alpha = 0.2f),
    onPrimaryContainer = WakeUpColors.iosBlue,
    secondary = WakeUpColors.iosPurple,
    onSecondary = Color.White,
    secondaryContainer = WakeUpColors.iosPurple.copy(alpha = 0.2f),
    onSecondaryContainer = WakeUpColors.iosPurple,
    tertiary = WakeUpColors.iosTeal,
    onTertiary = Color.Black,
    tertiaryContainer = WakeUpColors.iosTeal.copy(alpha = 0.2f),
    onTertiaryContainer = WakeUpColors.iosTeal,
    background = WakeUpColors.iosDarkBackground,
    onBackground = WakeUpColors.iosDarkTextPrimary,
    surface = WakeUpColors.iosDarkSurface,
    onSurface = WakeUpColors.iosDarkTextPrimary,
    surfaceVariant = WakeUpColors.iosDarkElevated,
    onSurfaceVariant = WakeUpColors.iosDarkTextSecondary,
    error = WakeUpColors.iosRed,
    onError = Color.White,
    errorContainer = WakeUpColors.iosRed.copy(alpha = 0.2f),
    onErrorContainer = WakeUpColors.iosRed,
    outline = WakeUpColors.iosGray,
    outlineVariant = WakeUpColors.iosGray2,
    inverseSurface = WakeUpColors.iosSurface,
    inverseOnSurface = WakeUpColors.iosTextPrimary,
    inversePrimary = WakeUpColors.iosBlue,
    surfaceTint = WakeUpColors.iosBlue.copy(alpha = 0.1f)
)

@Composable
fun WakeUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
