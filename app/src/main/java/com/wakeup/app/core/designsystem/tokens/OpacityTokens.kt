package com.wakeup.app.core.designsystem.tokens

import androidx.compose.ui.unit.dp

/**
 * Opacity scale for glassmorphism effects and translucency.
 * 8-step scale for consistent transparency across the app.
 */
object OpacityTokens {
    /** 5% - Subtle hints, disabled states */
    val ghost = 0.05f
    
    /** 8% - Very subtle backgrounds */
    val subtle = 0.08f
    
    /** 12% - Light glass cards, secondary backgrounds */
    val light = 0.12f
    
    /** 15% - Standard glass card backgrounds */
    val medium = 0.15f
    
    /** 20% - Prominent glass surfaces */
    val prominent = 0.20f
    
    /** 25% - Strong glass effect, elevated cards */
    val strong = 0.25f
    
    /** 30% - Intense glass, overlays */
    val intense = 0.30f
    
    /** 50% - Borders, dividers, separators */
    val border = 0.50f
    
    /** Border alpha variants */
    val borderSubtle = 0.15f
    val borderLight = 0.20f
    val borderMedium = 0.30f
    val borderStrong = 0.40f
}
