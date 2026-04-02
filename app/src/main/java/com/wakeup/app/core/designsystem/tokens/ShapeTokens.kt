package com.wakeup.app.core.designsystem.tokens

import androidx.compose.ui.unit.dp

/**
 * Corner radius scale for consistent shaping across components.
 */
object ShapeTokens {
    /** 0dp - No rounding, sharp corners */
    val none = 0.dp
    
    /** 4dp - Minimal rounding, tags, pills */
    val xs = 4.dp
    
    /** 8dp - Small rounding, small buttons, chips */
    val sm = 8.dp
    
    /** 12dp - Medium-small rounding, icon containers */
    val smMd = 12.dp
    
    /** 16dp - Medium rounding, standard cards, buttons */
    val md = 16.dp
    
    /** 20dp - Medium-large rounding, large cards, sections */
    val mdLg = 20.dp
    
    /** 24dp - Large rounding, bottom sheets, dialogs */
    val lg = 24.dp
    
    /** 32dp - Extra large rounding, modals */
    val xl = 32.dp
    
    /** 50% - Fully rounded, pills, FABs, circular elements */
    val pill = 50.dp
    
    /** Full circle - 100% */
    val full = 100.dp
}
