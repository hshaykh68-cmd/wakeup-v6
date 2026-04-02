package com.wakeup.app.presentation.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakeup.app.core.billing.BillingManager
import com.wakeup.app.core.billing.BillingState
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.domain.service.HapticsController
import com.wakeup.app.domain.model.PremiumType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    hapticsController: HapticsController,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val billingState by viewModel.billingState.collectAsState()
    val premiumType by viewModel.premiumType.collectAsState()
    val products by viewModel.products.collectAsState()
    val context = LocalContext.current

    // Check if already premium
    val isAlreadyPremium = premiumType.isPremium()

    // Start connection when screen is shown
    LaunchedEffect(Unit) {
        viewModel.refreshPremiumStatus()
    }

    var selectedPlan by remember { mutableStateOf<PlanType>(PlanType.YEARLY) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WakeUp Premium",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Premium Header with glassmorphism
            if (isAlreadyPremium) {
                GlassPremiumActiveHeader(premiumType)
            } else {
                GlassPremiumHeader()
            }

            // Features with glassmorphism
            GlassFeaturesSection()

            Spacer(modifier = Modifier.height(16.dp))

            if (isAlreadyPremium) {
                // Show already premium UI
                AlreadyPremiumContent(
                    premiumType = premiumType,
                    onRestorePurchases = { viewModel.restorePurchases() },
                    billingState = billingState
                )
            } else {
                // Pricing with selectable plans
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SelectablePricingCard(
                        title = "Monthly",
                        price = products[BillingManager.PRODUCT_MONTHLY]?.let {
                            it.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                        } ?: "$4.99",
                        period = "/month",
                        isSelected = selectedPlan == PlanType.MONTHLY,
                        onSelect = { 
                            hapticsController.performLightImpact()
                            selectedPlan = PlanType.MONTHLY 
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SelectablePricingCard(
                        title = "Yearly",
                        price = products[BillingManager.PRODUCT_YEARLY]?.let {
                            it.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                        } ?: "$29.99",
                        period = "/year",
                        badge = "SAVE 50%",
                        isSelected = selectedPlan == PlanType.YEARLY,
                        onSelect = { 
                            hapticsController.performLightImpact()
                            selectedPlan = PlanType.YEARLY 
                        },
                        isRecommended = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Lifetime option
                SelectablePricingCard(
                    title = "Lifetime",
                    price = products[BillingManager.PRODUCT_LIFETIME]?.let {
                        it.oneTimePurchaseOfferDetails?.formattedPrice
                    } ?: "$59.99",
                    period = " one-time",
                    badge = "BEST VALUE",
                    isSelected = selectedPlan == PlanType.LIFETIME,
                    onSelect = { 
                        hapticsController.performLightImpact()
                        selectedPlan = PlanType.LIFETIME 
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Subscribe Button
                Button(
                    onClick = {
                        hapticsController.performSuccess()
                        val productId = when (selectedPlan) {
                            PlanType.MONTHLY -> BillingManager.PRODUCT_MONTHLY
                            PlanType.YEARLY -> BillingManager.PRODUCT_YEARLY
                            PlanType.LIFETIME -> BillingManager.PRODUCT_LIFETIME
                        }
                        viewModel.purchase(context as android.app.Activity, productId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WakeUpColors.iosPurple
                    ),
                    enabled = billingState !is BillingState.Error
                ) {
                    if (billingState is BillingState.Connecting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = when (selectedPlan) {
                                PlanType.MONTHLY -> "Subscribe Monthly"
                                PlanType.YEARLY -> "Subscribe Yearly"
                                PlanType.LIFETIME -> "Buy Lifetime"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Restore purchases link
                TextButton(
                    onClick = { 
                        hapticsController.performLightImpact()
                        viewModel.restorePurchases() 
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restore Purchases")
                }

                // Error message
                if (billingState is BillingState.Error) {
                    Text(
                        text = (billingState as BillingState.Error).message,
                        style = MaterialTheme.typography.bodySmall,
                        color = WakeUpColors.iosRed,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Text(
                    text = "Subscription auto-renews. Cancel anytime in Play Store.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

enum class PlanType {
    MONTHLY, YEARLY, LIFETIME
}

@Composable
private fun GlassPremiumHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WakeUpColors.iosPurple.copy(alpha = 0.8f),
                        WakeUpColors.iosBlue.copy(alpha = 0.6f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Go Premium",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Unlock the full power of waking up",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun GlassFeaturesSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.06f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Premium Features",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            PremiumFeatureItem("All Mission Types", "Math, Memory, Typing & more")
            PremiumFeatureItem("All Difficulty Levels", "Easy, Medium & Hard missions")
            PremiumFeatureItem("Advanced Analytics", "Detailed wake-up insights")
            PremiumFeatureItem("Strict Mode", "Force mission completion")
            PremiumFeatureItem("Custom Themes", "Personalize your experience")
            PremiumFeatureItem("No Ads", "Distraction-free experience")
        }
    }
}

@Composable
private fun SelectablePricingCard(
    title: String,
    price: String,
    period: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    badge: String? = null,
    isRecommended: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) {
                    WakeUpColors.iosPurple.copy(alpha = 0.15f)
                } else {
                    Color.White.copy(alpha = 0.08f)
                }
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) {
                    WakeUpColors.iosPurple
                } else {
                    Color.White.copy(alpha = 0.15f)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Selection indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (isSelected) WakeUpColors.iosPurple else Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(WakeUpColors.iosPurple)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) WakeUpColors.iosPurple else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = period,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PremiumFeatureItem(title: String, description: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(WakeUpColors.iosGreen.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = WakeUpColors.iosGreen,
                modifier = Modifier.size(16.dp)
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GlassPremiumActiveHeader(premiumType: PremiumType) {
    val headerColor = when (premiumType) {
        PremiumType.LIFETIME -> WakeUpColors.iosGold
        else -> WakeUpColors.iosGreen
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        headerColor.copy(alpha = 0.8f),
                        WakeUpColors.iosBlue.copy(alpha = 0.6f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Premium Active!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = premiumType.displayName(),
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AlreadyPremiumContent(
    premiumType: PremiumType,
    onRestorePurchases: () -> Unit,
    billingState: BillingState
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "You're enjoying all premium benefits!",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Restore purchases button for safety
        TextButton(
            onClick = onRestorePurchases,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            if (billingState is BillingState.Connecting) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Restore Purchases")
            }
        }

        if (billingState is BillingState.PremiumActive) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Premium status verified",
                style = MaterialTheme.typography.bodySmall,
                color = WakeUpColors.iosGreen
            )
        }
    }
}
