package com.wakeup.app.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import androidx.annotation.VisibleForTesting
import com.wakeup.app.domain.model.PremiumType
import com.wakeup.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Manages Google Play Billing operations including product queries,
 * purchase flows, purchase acknowledgment, and restore functionality.
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Product IDs - should match Play Console configuration
    companion object {
        const val PRODUCT_MONTHLY = "premium_monthly"
        const val PRODUCT_YEARLY = "premium_yearly"
        const val PRODUCT_LIFETIME = "premium_lifetime"
    }

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Disconnected)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val _products = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val products: StateFlow<Map<String, ProductDetails>> = _products.asStateFlow()

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    private var billingClient: BillingClient? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.let { handlePurchases(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingState.value = BillingState.Error("Purchase cancelled")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _billingState.value = BillingState.Error("Item already owned")
                queryExistingPurchases()
            }
            else -> {
                _billingState.value = BillingState.Error(
                    billingResult.debugMessage ?: "Purchase failed (code: ${billingResult.responseCode})"
                )
            }
        }
    }

    /**
     * Initialize and connect to Google Play Billing
     */
    fun startConnection() {
        if (billingClient?.isReady == true) {
            _billingState.value = BillingState.Connected
            queryProducts()
            queryExistingPurchases()
            return
        }

        _billingState.value = BillingState.Connecting

        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _billingState.value = BillingState.Connected
                    queryProducts()
                    queryExistingPurchases()
                } else {
                    _billingState.value = BillingState.Error(
                        billingResult.debugMessage ?: "Failed to connect (code: ${billingResult.responseCode})"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingState.value = BillingState.Disconnected
            }
        })
    }

    /**
     * Disconnect from billing service when not needed
     */
    fun endConnection() {
        scope.cancel()
        billingClient?.endConnection()
        billingClient = null
        _billingState.value = BillingState.Disconnected
    }

    /**
     * Query available products from Play Console
     */
    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productsMap = productDetailsList?.associateBy { it.productId } ?: emptyMap()
                _products.value = productsMap
            }
        }
    }

    /**
     * Query existing purchases (for restore and startup checks)
     * Accumulates both subscription and in-app results atomically before processing
     */
    fun queryExistingPurchases() {
        scope.launch {
            try {
                // Query both types of purchases concurrently and wait for both
                val subsResult = queryPurchases(BillingClient.ProductType.SUBS)
                val inAppResult = queryPurchases(BillingClient.ProductType.INAPP)
                
                // Combine results atomically
                val allPurchases = subsResult + inAppResult
                
                // Process combined purchases once
                handlePurchases(allPurchases)
            } catch (e: Exception) {
                _billingState.value = BillingState.Error("Failed to query purchases: ${e.message}")
            }
        }
    }
    
    /**
     * Helper to query purchases of a specific type using coroutines
     */
    private suspend fun queryPurchases(productType: String): List<Purchase> {
        return suspendCancellableCoroutine { continuation ->
            billingClient?.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(productType)
                    .build()
            ) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(purchasesList ?: emptyList())
                } else {
                    continuation.resume(emptyList())
                }
            } ?: continuation.resume(emptyList())
        }
    }

    /**
     * Launch purchase flow for a product
     */
    fun launchPurchase(activity: Activity, productId: String) {
        val productDetails = _products.value[productId] ?: run {
            _billingState.value = BillingState.Error("Product not available")
            return
        }

        val offerToken = if (productDetails.productType == BillingClient.ProductType.SUBS) {
            // Get the first offer token for subscriptions
            productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
        } else null

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .apply {
                offerToken?.let { setOfferToken(it) }
            }
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    /**
     * Handle purchase results - acknowledge if needed and update premium status
     */
    private fun handlePurchases(purchases: List<Purchase>) {
        _purchases.value = purchases

        scope.launch {
            var isPremium = false
            var premiumType = PremiumType.NONE

            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    // Acknowledge purchase if not already acknowledged
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }

                    // Determine premium type from purchase
                    val type = determinePremiumType(purchase)
                    if (type.isPremium()) {
                        isPremium = true
                        // Lifetime takes precedence, then yearly, then monthly
                        premiumType = when {
                            premiumType == PremiumType.LIFETIME -> premiumType
                            type == PremiumType.LIFETIME -> type
                            premiumType == PremiumType.YEARLY -> premiumType
                            else -> type
                        }
                    }
                }
            }

            // Update premium status
            settingsRepository.setPremiumUser(isPremium)
            settingsRepository.setPremiumType(premiumType)

            _billingState.value = if (isPremium) {
                BillingState.PremiumActive(premiumType)
            } else {
                BillingState.Connected
            }
        }
    }

    /**
     * Acknowledge a purchase to complete the transaction
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                _billingState.value = BillingState.Error("Failed to acknowledge purchase")
            }
        }
    }

    /**
     * Determine PremiumType from a list of purchases
     * Internal visibility for testing without R8 obfuscation issues
     */
    @VisibleForTesting
    internal fun determinePremiumTypeFromPurchases(purchases: List<Purchase>): PremiumType {
        var isPremium = false
        var premiumType = PremiumType.NONE

        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                val type = determinePremiumType(purchase)
                if (type.isPremium()) {
                    isPremium = true
                    // Lifetime takes precedence, then yearly, then monthly
                    premiumType = when {
                        premiumType == PremiumType.LIFETIME -> premiumType
                        type == PremiumType.LIFETIME -> type
                        premiumType == PremiumType.YEARLY -> premiumType
                        else -> type
                    }
                }
            }
        }

        return premiumType
    }

    /**
     * Determine PremiumType from purchase data
     * Internal visibility for testing without R8 obfuscation issues
     */
    @VisibleForTesting
    internal fun determinePremiumType(purchase: Purchase): PremiumType {
        return when {
            purchase.products.contains(PRODUCT_LIFETIME) -> PremiumType.LIFETIME
            purchase.products.contains(PRODUCT_YEARLY) -> PremiumType.YEARLY
            purchase.products.contains(PRODUCT_MONTHLY) -> PremiumType.MONTHLY
            else -> PremiumType.NONE
        }
    }

    /**
     * Check if user is currently premium - READS FROM DATASTORE (single source of truth)
     * Use SettingsRepository.getPremiumUserFlow() for UI instead of this method
     */
    suspend fun isPremiumUser(): Boolean {
        return settingsRepository.isPremiumUser()
    }

    /**
     * Get the current premium type - READS FROM DATASTORE (single source of truth)
     * Use SettingsRepository.getPremiumTypeFlow() for UI instead of this method
     */
    suspend fun getCurrentPremiumType(): PremiumType {
        return settingsRepository.getPremiumType()
    }
}

/**
 * Billing connection and purchase states
 */
sealed class BillingState {
    data object Disconnected : BillingState()
    data object Connecting : BillingState()
    data object Connected : BillingState()
    data class PremiumActive(val type: PremiumType) : BillingState()
    data class Error(val message: String) : BillingState()
}
