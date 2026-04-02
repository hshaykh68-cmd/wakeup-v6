package com.wakeup.app.domain.model

/**
 * Represents the type of premium subscription or purchase.
 */
enum class PremiumType {
    NONE,       // Not a premium user
    MONTHLY,    // Monthly subscription
    YEARLY,     // Yearly subscription
    LIFETIME;   // One-time lifetime purchase

    fun displayName(): String = when (this) {
        NONE -> "Free"
        MONTHLY -> "Monthly Premium"
        YEARLY -> "Yearly Premium"
        LIFETIME -> "Lifetime Premium"
    }

    fun isPremium(): Boolean = this != NONE
}
