package com.wakeup.app.presentation.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeup.app.core.billing.BillingManager
import com.wakeup.app.core.billing.BillingState
import com.wakeup.app.domain.model.PremiumType
import com.wakeup.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for PremiumScreen that manages billing operations and premium status.
 */
@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val billingState: StateFlow<BillingState> = billingManager.billingState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BillingState.Disconnected
        )

    val premiumType: StateFlow<PremiumType> = settingsRepository.getPremiumTypeFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PremiumType.NONE
        )

    val products = billingManager.products

    init {
        // Start billing connection when ViewModel is created
        billingManager.startConnection()
    }

    /**
     * Initiate purchase for a subscription or lifetime plan
     */
    fun purchase(activity: android.app.Activity, productId: String) {
        billingManager.launchPurchase(activity, productId)
    }

    /**
     * Restore existing purchases from Google Play
     */
    fun restorePurchases() {
        billingManager.queryExistingPurchases()
    }

    /**
     * Refresh premium status from local storage
     */
    fun refreshPremiumStatus() {
        viewModelScope.launch {
            val type = settingsRepository.getPremiumType()
            val isPremium = settingsRepository.isPremiumUser()
            // State will be updated through the flow
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}
