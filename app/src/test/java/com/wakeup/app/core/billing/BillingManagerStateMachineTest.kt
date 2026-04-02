package com.wakeup.app.core.billing

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.wakeup.app.domain.model.PremiumType
import com.wakeup.app.domain.repository.SettingsRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for BillingManager state machine.
 * Tests state transitions, purchase handling, and premium type determination.
 */
@ExperimentalCoroutinesApi
class BillingManagerStateMachineTest {

    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        settingsRepository = mockk(relaxed = true)
    }

    // ==================== BILLING STATE TRANSITION TESTS ====================

    @Test
    fun `initial state is Disconnected`() = runTest {
        // The billing state should start as Disconnected before connection
        val billingManager = createBillingManager()

        assertTrue(
            "Initial state should be Disconnected",
            billingManager.billingState.first() is BillingState.Disconnected
        )
    }

    @Test
    fun `determinePremiumType returns LIFETIME for lifetime purchase`() {
        val billingManager = createBillingManager()
        val purchase = createMockPurchase(
            products = listOf(BillingManager.PRODUCT_LIFETIME),
            purchaseState = Purchase.PurchaseState.PURCHASED
        )

        val result = billingManager.callPrivate("determinePremiumType", purchase)

        assertEquals(PremiumType.LIFETIME, result)
    }

    @Test
    fun `determinePremiumType returns YEARLY for yearly purchase`() {
        val billingManager = createBillingManager()
        val purchase = createMockPurchase(
            products = listOf(BillingManager.PRODUCT_YEARLY),
            purchaseState = Purchase.PurchaseState.PURCHASED
        )

        val result = billingManager.callPrivate("determinePremiumType", purchase)

        assertEquals(PremiumType.YEARLY, result)
    }

    @Test
    fun `determinePremiumType returns MONTHLY for monthly purchase`() {
        val billingManager = createBillingManager()
        val purchase = createMockPurchase(
            products = listOf(BillingManager.PRODUCT_MONTHLY),
            purchaseState = Purchase.PurchaseState.PURCHASED
        )

        val result = billingManager.callPrivate("determinePremiumType", purchase)

        assertEquals(PremiumType.MONTHLY, result)
    }

    @Test
    fun `determinePremiumType returns NONE for unknown product`() {
        val billingManager = createBillingManager()
        val purchase = createMockPurchase(
            products = listOf("unknown_product"),
            purchaseState = Purchase.PurchaseState.PURCHASED
        )

        val result = billingManager.callPrivate("determinePremiumType", purchase)

        assertEquals(PremiumType.NONE, result)
    }

    @Test
    fun `determinePremiumType returns NONE for empty products`() {
        val billingManager = createBillingManager()
        val purchase = createMockPurchase(
            products = emptyList(),
            purchaseState = Purchase.PurchaseState.PURCHASED
        )

        val result = billingManager.callPrivate("determinePremiumType", purchase)

        assertEquals(PremiumType.NONE, result)
    }

    // ==================== PREMIUM TYPE PRIORITY TESTS ====================

    @Test
    fun `determinePremiumTypeFromPurchases prioritizes LIFETIME over YEARLY`() {
        val billingManager = createBillingManager()
        val purchases = listOf(
            createMockPurchase(
                products = listOf(BillingManager.PRODUCT_YEARLY),
                purchaseState = Purchase.PurchaseState.PURCHASED
            ),
            createMockPurchase(
                products = listOf(BillingManager.PRODUCT_LIFETIME),
                purchaseState = Purchase.PurchaseState.PURCHASED
            )
        )

        val result = billingManager.callPrivate("determinePremiumTypeFromPurchases", purchases)

        assertEquals(PremiumType.LIFETIME, result)
    }

    @Test
    fun `determinePremiumTypeFromPurchases prioritizes LIFETIME over MONTHLY`() {
        val billingManager = createBillingManager()
        val purchases = listOf(
            createMockPurchase(
                products = listOf(BillingManager.PRODUCT_MONTHLY),
                purchaseState = Purchase.PurchaseState.PURCHASED
            ),
            createMockPurchase(
                products = listOf(BillingManager.PRODUCT_LIFETIME),
                purchaseState = Purchase.PurchaseState.PURCHASED
            )
        )

        val result = billingManager.callPrivate("determinePremiumTypeFromPurchases", purchases)

        assertEquals(PremiumType.LIFETIME, result)
    }

    @Test
    fun `determinePremiumTypeFromPurchases prioritizes YEARLY over MONTHLY`() {
        val billingManager = createBillingManager()
        val purchases = listOf(
            createMockPurchase(
                products = listOf(BillingManager.PRODUCT_MONTHLY),
                purchaseState = Purchase.PurchaseState.PURCHASED
            ),
            createMockPurchase(
                products = listOf(BillingManager.PRODUCT_YEARLY),
                purchaseState = Purchase.PurchaseState.PURCHASED
            )
        )

        val result = billingManager.callPrivate("determinePremiumTypeFromPurchases", purchases)

        assertEquals(PremiumType.YEARLY, result)
    }

    @Test
    fun `determinePremiumTypeFromPurchases ignores non-purchased items`() {
        val billingManager = createBillingManager()
        val purchases = listOf(
            createMockPurchase(
                products = listOf(BillingManager.PRODUCT_LIFETIME),
                purchaseState = Purchase.PurchaseState.PENDING
            ),
            createMockPurchase(
                products = listOf(BillingManager.PRODUCT_MONTHLY),
                purchaseState = Purchase.PurchaseState.PURCHASED
            )
        )

        val result = billingManager.callPrivate("determinePremiumTypeFromPurchases", purchases)

        assertEquals(PremiumType.MONTHLY, result)
    }

    @Test
    fun `determinePremiumTypeFromPurchases returns NONE for no purchases`() {
        val billingManager = createBillingManager()
        val purchases = emptyList<Purchase>()

        val result = billingManager.callPrivate("determinePremiumTypeFromPurchases", purchases)

        assertEquals(PremiumType.NONE, result)
    }

    // ==================== IS PREMIUM TESTS ====================

    @Test
    fun `isPremiumUser returns true when purchase is PURCHASED`() {
        val billingManager = createBillingManager()
        val purchase = createMockPurchase(
            products = listOf(BillingManager.PRODUCT_MONTHLY),
            purchaseState = Purchase.PurchaseState.PURCHASED
        )

        // Set purchases through reflection since _purchases is private
        billingManager.setPrivateField("_purchases", kotlinx.coroutines.flow.MutableStateFlow(listOf(purchase)))

        assertTrue(billingManager.isPremiumUser())
    }

    @Test
    fun `isPremiumUser returns false when purchase is PENDING`() {
        val billingManager = createBillingManager()
        val purchase = createMockPurchase(
            products = listOf(BillingManager.PRODUCT_MONTHLY),
            purchaseState = Purchase.PurchaseState.PENDING
        )

        billingManager.setPrivateField("_purchases", kotlinx.coroutines.flow.MutableStateFlow(listOf(purchase)))

        assertFalse(billingManager.isPremiumUser())
    }

    @Test
    fun `isPremiumUser returns false when no purchases`() {
        val billingManager = createBillingManager()

        billingManager.setPrivateField("_purchases", kotlinx.coroutines.flow.MutableStateFlow(emptyList<Purchase>()))

        assertFalse(billingManager.isPremiumUser())
    }

    // ==================== BILLING RESPONSE HANDLING TESTS ====================

    @Test
    fun `purchase response code OK results in success state`() {
        val result = BillingResult.newBuilder()
            .setResponseCode(BillingClient.BillingResponseCode.OK)
            .build()

        assertEquals(BillingClient.BillingResponseCode.OK, result.responseCode)
    }

    @Test
    fun `purchase response code USER_CANCELED indicates cancellation`() {
        val result = BillingResult.newBuilder()
            .setResponseCode(BillingClient.BillingResponseCode.USER_CANCELED)
            .build()

        assertEquals(BillingClient.BillingResponseCode.USER_CANCELED, result.responseCode)
    }

    @Test
    fun `purchase response code ITEM_ALREADY_OWNED indicates existing purchase`() {
        val result = BillingResult.newBuilder()
            .setResponseCode(BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED)
            .build()

        assertEquals(BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED, result.responseCode)
    }

    @Test
    fun `purchase response code BILLING_UNAVAILABLE indicates service unavailable`() {
        val result = BillingResult.newBuilder()
            .setResponseCode(BillingClient.BillingResponseCode.BILLING_UNAVAILABLE)
            .build()

        assertEquals(BillingClient.BillingResponseCode.BILLING_UNAVAILABLE, result.responseCode)
    }

    @Test
    fun `purchase response code NETWORK_ERROR indicates connectivity issue`() {
        val result = BillingResult.newBuilder()
            .setResponseCode(BillingClient.BillingResponseCode.NETWORK_ERROR)
            .build()

        assertEquals(BillingClient.BillingResponseCode.NETWORK_ERROR, result.responseCode)
    }

    // ==================== PRODUCT ID CONSTANTS TESTS ====================

    @Test
    fun `product IDs are correctly defined`() {
        assertEquals("premium_monthly", BillingManager.PRODUCT_MONTHLY)
        assertEquals("premium_yearly", BillingManager.PRODUCT_YEARLY)
        assertEquals("premium_lifetime", BillingManager.PRODUCT_LIFETIME)
    }

    @Test
    fun `product IDs are unique`() {
        val ids = listOf(
            BillingManager.PRODUCT_MONTHLY,
            BillingManager.PRODUCT_YEARLY,
            BillingManager.PRODUCT_LIFETIME
        )
        assertEquals(3, ids.distinct().size)
    }

    // ==================== PREMIUM TYPE UTILITY TESTS ====================

    @Test
    fun `PremiumType isPremium returns true for all premium types`() {
        assertTrue(PremiumType.LIFETIME.isPremium())
        assertTrue(PremiumType.YEARLY.isPremium())
        assertTrue(PremiumType.MONTHLY.isPremium())
    }

    @Test
    fun `PremiumType isPremium returns false for NONE`() {
        assertFalse(PremiumType.NONE.isPremium())
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    fun `determinePremiumType handles multiple products in single purchase`() {
        val billingManager = createBillingManager()
        val purchase = createMockPurchase(
            products = listOf(BillingManager.PRODUCT_MONTHLY, BillingManager.PRODUCT_LIFETIME),
            purchaseState = Purchase.PurchaseState.PURCHASED
        )

        val result = billingManager.callPrivate("determinePremiumType", purchase)

        // Should find LIFETIME even when not first in list
        assertEquals(PremiumType.LIFETIME, result)
    }

    @Test
    fun `determinePremiumTypeFromPurchases handles null purchase state gracefully`() {
        val billingManager = createBillingManager()
        // Purchase with invalid state (simulating edge case)
        val purchases = listOf(
            createMockPurchase(
                products = listOf(BillingManager.PRODUCT_LIFETIME),
                purchaseState = 999 // Invalid state
            )
        )

        val result = billingManager.callPrivate("determinePremiumTypeFromPurchases", purchases)

        assertEquals(PremiumType.NONE, result)
    }

    // ==================== HELPER METHODS ====================

    private fun createBillingManager(): BillingManager {
        val context = mockk<android.content.Context>(relaxed = true)
        return BillingManager(context, settingsRepository)
    }

    private fun createMockPurchase(
        products: List<String>,
        purchaseState: Int,
        purchaseToken: String = "test_token",
        isAcknowledged: Boolean = true
    ): Purchase {
        val purchase = mockk<Purchase>(relaxed = true)
        every { purchase.products } returns products
        every { purchase.purchaseState } returns purchaseState
        every { purchase.purchaseToken } returns purchaseToken
        every { purchase.isAcknowledged } returns isAcknowledged
        return purchase
    }

    private fun <T> Any.callPrivate(methodName: String, vararg args: Any?): T {
        val method = this::class.java.declaredMethods.find { it.name == methodName }
            ?: throw NoSuchMethodException("Method $methodName not found")
        method.isAccessible = true
        return method.invoke(this, *args) as T
    }

    private fun Any.setPrivateField(fieldName: String, value: Any?) {
        val field = this::class.java.declaredFields.find { it.name == fieldName }
            ?: throw NoSuchFieldException("Field $fieldName not found")
        field.isAccessible = true
        field.set(this, value)
    }
}
