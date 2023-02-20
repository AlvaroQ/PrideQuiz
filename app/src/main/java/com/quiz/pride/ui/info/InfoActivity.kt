package com.quiz.pride.ui.info

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
import com.quiz.pride.databinding.InfoActivityBinding
import com.quiz.pride.utils.setSafeOnClickListener
import com.quiz.pride.utils.showBanner
import com.quiz.pride.utils.showBonificado

class InfoActivity : BaseActivity() {
    private var rewardedAd: RewardedAd? = null
    private val binding by viewBinding(InfoActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerInfo, InfoFragment.newInstance())
                .commitNow()
        }

        MobileAds.initialize(this)
        RewardedAd.load(this, getString(R.string.BONIFICADO_SHOW_INFO), AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
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
            btnBack.setSafeOnClickListener { finishAfterTransition() }
            toolbarTitle.text = getString(R.string.info_title)
            layoutLife.visibility = View.GONE
        }
    }

    fun showAd(show: Boolean){
        showBanner(show, binding.adViewInfo)
    }
    fun loadRewardedAd(show: Boolean) {
        showBonificado(this, show, rewardedAd)
    }
}