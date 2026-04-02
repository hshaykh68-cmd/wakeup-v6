package com.wakeup.app.core.designsystem.components.buttons

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wakeup.app.core.designsystem.tokens.ShapeTokens
import com.wakeup.app.core.designsystem.tokens.SpacingTokens
import com.wakeup.app.core.theme.WakeUpColors

/**
 * Button size variants with standardized heights.
 */
enum class ButtonSize(val height: Dp) {
    SMALL(40.dp),
    MEDIUM(48.dp),
    LARGE(56.dp)
}

/**
 * Button style variants with standardized colors and behaviors.
 */
enum class ButtonVariant {
    /** Primary action - iOS Blue background */
    PRIMARY,
    
    /** Secondary action - Subtle gray background */
    SECONDARY,
    
    /** Glass effect - Translucent with blur */
    GLASS,
    
    /** Destructive action - Red background */
    DESTRUCTIVE,
    
    /** Premium/Gold accent */
    PREMIUM
}

/**
 * Unified WakeUp button component with consistent styling.
 * 
 * Usage:
 * ```
 * WakeUpButton(
 *     onClick = { /* action */ },
 *     text = "Continue",
 *     variant = ButtonVariant.PRIMARY,
 *     size = ButtonSize.LARGE
 * )
 * ```
 */
@Composable
fun WakeUpButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    size: ButtonSize = ButtonSize.LARGE,
    enabled: Boolean = true,
    icon: @Composable (RowScope.() -> Unit)? = null
) {
    val (backgroundColor, contentColor) = when (variant) {
        ButtonVariant.PRIMARY -> WakeUpColors.iosBlue to Color.White
        ButtonVariant.SECONDARY -> WakeUpColors.iosGray5 to WakeUpColors.iosTextPrimary
        ButtonVariant.GLASS -> Color.White.copy(alpha = 0.15f) to Color.White
        ButtonVariant.DESTRUCTIVE -> WakeUpColors.iosRed to Color.White
        ButtonVariant.PREMIUM -> WakeUpColors.iosGold to Color.Black
    }

    val disabledBackground = backgroundColor.copy(alpha = 0.5f)
    val disabledContent = contentColor.copy(alpha = 0.6f)

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(size.height),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(ShapeTokens.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = disabledBackground,
            disabledContentColor = disabledContent
        ),
        enabled = enabled
    ) {
        if (icon != null) {
            icon()
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Compact button variant for inline actions.
 */
@Composable
fun WakeUpButtonCompact(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true
) {
    WakeUpButton(
        onClick = onClick,
        text = text,
        modifier = modifier,
        variant = variant,
        size = ButtonSize.SMALL,
        enabled = enabled
    )
}

/**
 * Text-only button for secondary actions.
 */
@Composable
fun WakeUpTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = WakeUpColors.iosBlue
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) color else color.copy(alpha = 0.5f)
        )
    }
}
