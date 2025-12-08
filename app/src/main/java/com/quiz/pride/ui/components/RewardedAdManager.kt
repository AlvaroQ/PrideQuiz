package com.quiz.pride.ui.components

import android.app.Activity
import android.content.Context
import android.util.Log
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
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.quiz.pride.R

/**
 * State holder for Rewarded Ad
 */
class RewardedAdState(
    private val context: Context,
    private val adUnitId: String
) {
    private var rewardedAd: RewardedAd? = null
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

        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Rewarded ad failed to load: ${error.message}")
                    rewardedAd = null
                    isLoading = false
                    isReady = false
                    loadError = error.message
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully")
                    rewardedAd = ad
                    isLoading = false
                    isReady = true
                    loadError = null
                }
            }
        )
    }

    fun showAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissed: () -> Unit,
        onAdFailed: (String) -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            onAdFailed("Ad not ready")
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded ad dismissed")
                rewardedAd = null
                isReady = false
                onAdDismissed()
                // Pre-load next ad
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Rewarded ad failed to show: ${error.message}")
                rewardedAd = null
                isReady = false
                onAdFailed(error.message)
                // Try to load again
                loadAd()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad showed")
            }
        }

        ad.show(activity) { rewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            onRewardEarned()
        }
    }

    fun release() {
        rewardedAd = null
        isReady = false
        isLoading = false
    }

    companion object {
        private const val TAG = "RewardedAdState"
    }
}

/**
 * Remember a RewardedAdState that automatically loads the ad
 */
@Composable
fun rememberRewardedAdState(
    adUnitId: String = LocalContext.current.getString(R.string.BONIFICADO_GAME)
): RewardedAdState {
    val context = LocalContext.current

    val adState = remember(adUnitId) {
        RewardedAdState(context, adUnitId)
    }

    DisposableEffect(adState) {
        adState.loadAd()
        onDispose {
            adState.release()
        }
    }

    return adState
}

/**
 * Extension to get Activity from Context
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is android.content.ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
