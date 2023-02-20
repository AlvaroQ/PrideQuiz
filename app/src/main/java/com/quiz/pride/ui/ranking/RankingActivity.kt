package com.quiz.pride.ui.ranking

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.common.viewBinding
import com.quiz.pride.databinding.RankingActivityBinding
import com.quiz.pride.utils.setSafeOnClickListener
import com.quiz.pride.utils.showBonificado

class RankingActivity : BaseActivity() {
    private var rewardedAd: RewardedAd? = null

    private val binding by viewBinding(RankingActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerResult, RankingFragment.newInstance())
                .commitNow()
        }

        MobileAds.initialize(this)
        RewardedAd.load(this, getString(R.string.BONIFICADO_GAME_OVER), AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("GameActivity", adError.toString())
                FirebaseCrashlytics.getInstance().recordException(Throwable(adError.message))
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d("GameActivity", "Ad was loaded.")
                rewardedAd = ad
            }
        })

        with(binding.appBar) {
            btnBack.setSafeOnClickListener { finish() }
            toolbarTitle.text = getString(R.string.best_points)
            layoutLife.visibility = View.GONE
        }
    }

    fun showRewardedAd(show: Boolean){
        showBonificado(this, show, rewardedAd)
    }
}