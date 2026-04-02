package com.wakeup.app.data.admob

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AdMob Manager for handling banner and interstitial ads
 * Uses test ad unit IDs for development
 */
@Singleton
class AdMobManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Test Ad Unit IDs from Google
        const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        
        // Production Ad Unit IDs (replace with your actual IDs)
        const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"
        const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"
    }
    
    private var interstitialAd: InterstitialAd? = null
    private var isInitialized = false
    
    /**
     * Initialize the Mobile Ads SDK
     */
    fun initialize() {
        if (!isInitialized) {
            MobileAds.initialize(context) {}
            isInitialized = true
        }
    }
    
    /**
     * Create a banner ad view
     * @param isTest Use test ad unit ID
     */
    fun createBannerAdView(isTest: Boolean = true): AdView {
        initialize()
        val adUnitId = if (isTest) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID
        
        val adView = AdView(context).apply {
            setAdSize(AdSize.BANNER)
            this.adUnitId = adUnitId
            loadAd(AdRequest.Builder().build())
        }
        return adView
    }
    
    /**
     * Create an adaptive banner ad view that adjusts to screen width
     * @param isTest Use test ad unit ID
     */
    fun createAdaptiveBannerAdView(isTest: Boolean = true): AdView {
        initialize()
        val adUnitId = if (isTest) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID
        
        val adView = AdView(context).apply {
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, context.resources.displayMetrics.widthPixels))
            this.adUnitId = adUnitId
            loadAd(AdRequest.Builder().build())
        }
        return adView
    }
    
    /**
     * Load an interstitial ad
     * @param isTest Use test ad unit ID
     * @param onLoaded Callback when ad is loaded
     * @param onFailed Callback when ad fails to load
     */
    fun loadInterstitialAd(
        isTest: Boolean = true,
        onLoaded: (() -> Unit)? = null,
        onFailed: ((LoadAdError) -> Unit)? = null
    ) {
        initialize()
        val adUnitId = if (isTest) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID
        
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    onLoaded?.invoke()
                }
                
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    onFailed?.invoke(error)
                }
            }
        )
    }
    
    /**
     * Show the loaded interstitial ad
     * @param activity The activity to show the ad in
     * @param onDismissed Callback when ad is dismissed
     * @param onFailedToShow Callback when ad fails to show
     */
    fun showInterstitialAd(
        activity: Activity,
        onDismissed: (() -> Unit)? = null,
        onFailedToShow: ((AdError) -> Unit)? = null
    ) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    onDismissed?.invoke()
                }
                
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    onFailedToShow?.invoke(error)
                }
                
                override fun onAdShowedFullScreenContent() {
                    // Ad is showing
                }
            }
            ad.show(activity)
        } ?: run {
            onDismissed?.invoke() // No ad available, continue
        }
    }
    
    /**
     * Check if an interstitial ad is ready to show
     */
    fun isInterstitialAdReady(): Boolean {
        return interstitialAd != null
    }
    
    /**
     * Preload an interstitial ad for later use (e.g., after alarm dismissal)
     */
    fun preloadInterstitialAd() {
        if (!isInterstitialAdReady()) {
            loadInterstitialAd()
        }
    }
}
