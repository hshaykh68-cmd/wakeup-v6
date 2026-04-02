package com.wakeup.app.core.designsystem.tokens

import androidx.compose.ui.unit.dp

/**
 * 8-point grid spacing system.
 * All spacing values are multiples of 4dp for visual rhythm.
 */
object SpacingTokens {
    /** 0dp - No spacing */
    val none = 0.dp
    
    /** 2dp - Micro spacing (rarely used) */
    val micro = 2.dp
    
    /** 4dp - Extra small spacing, tight gaps */
    val xs = 4.dp
    
    /** 8dp - Small spacing, icon-text gaps */
    val sm = 8.dp
    
    /** 12dp - Small-medium spacing */
    val smMd = 12.dp
    
    /** 16dp - Medium spacing, standard padding */
    val md = 16.dp
    
    /** 20dp - Medium-large spacing */
    val mdLg = 20.dp
    
    /** 24dp - Large spacing, section gaps */
    val lg = 24.dp
    
    /** 32dp - Extra large spacing, major sections */
    val xl = 32.dp
    
    /** 48dp - 2x Extra large, major breaks */
    val xxl = 48.dp
    
    /** 64dp - 3x Extra large, screen padding */
    val xxxl = 64.dp
    
    /** Screen edge padding standard */
    val screenEdge = 20.dp
    
    /** Card internal padding standard */
    val cardPadding = 16.dp
    
    /** Section gap standard */
    val sectionGap = 16.dp
}
