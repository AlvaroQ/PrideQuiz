package com.quiz.pride.ui.components

import android.app.Activity
import android.content.Context
import timber.log.Timber
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.quiz.pride.R
import com.quiz.pride.managers.AnalyticsManager
import org.koin.java.KoinJavaComponent.inject

/**
 * State holder for Interstitial Ad
 */
class InterstitialAdState(
    private val context: Context,
    private val adUnitId: String
) {
    private val analyticsManager: AnalyticsManager by inject(AnalyticsManager::class.java)
    private var interstitialAd: InterstitialAd? = null
    var isLoading by mutableStateOf(false)
        private set
    var isReady by mutableStateOf(false)
        private set
    var loadError by mutableStateOf<String?>(null)
        private set

    fun loadAd() {
        if (isLoading || isReady) return

        isLoading = true
        loadError = null

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Timber.e("Interstitial ad failed to load: ${error.message}")
                    interstitialAd = null
                    isLoading = false
                    isReady = false
                    loadError = error.message
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Timber.d("Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isLoading = false
                    isReady = true
                    loadError = null
                }
            }
        )
    }

    fun showAd(
        activity: Activity,
        onAdDismissed: () -> Unit,
        onAdFailed: (String) -> Unit
    ) {
        val ad = interstitialAd
        if (ad == null) {
            onAdFailed("Ad not ready")
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Timber.d("Interstitial ad dismissed")
                analyticsManager.analyticsAdEvent("interstitial", "dismissed")
                interstitialAd = null
                isReady = false
                onAdDismissed()
                // Pre-load next ad
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Timber.e("Interstitial ad failed to show: ${error.message}")
                interstitialAd = null
                isReady = false
                onAdFailed(error.message)
                // Try to load again
                loadAd()
            }

            override fun onAdShowedFullScreenContent() {
                Timber.d("Interstitial ad showed")
                analyticsManager.analyticsAdEvent("interstitial", "shown")
            }
        }

        ad.show(activity)
    }

    fun release() {
        interstitialAd = null
        isReady = false
        isLoading = false
    }

}

/**
 * Remember an InterstitialAdState that automatically loads the ad
 */
@Composable
fun rememberInterstitialAdState(
    adUnitId: String = LocalContext.current.getString(R.string.BONIFICADO_GAME_OVER)
): InterstitialAdState {
    val context = LocalContext.current.applicationContext

    val adState = remember(adUnitId) {
        InterstitialAdState(context, adUnitId)
    }

    DisposableEffect(adState) {
        adState.loadAd()
        onDispose {
            adState.release()
        }
    }

    return adState
}
