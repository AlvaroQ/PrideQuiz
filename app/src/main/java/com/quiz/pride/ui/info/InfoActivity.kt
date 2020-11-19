package com.quiz.pride.ui.info

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.utils.log
import com.quiz.pride.utils.setSafeOnClickListener
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.info_activity.*

class InfoActivity : BaseActivity() {
    private lateinit var rewardedAd: RewardedAd
    private lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerInfo, InfoFragment.newInstance())
                .commitNow()
        }
        loadRewardedAd()
        loadAd(adViewInfo)

        btnBack.setSafeOnClickListener { finishAfterTransition() }
        layoutExtendedTitle.background = getDrawable(R.drawable.background_title_top_score)
        toolbarTitle.text = getString(R.string.info_title)
        layoutLife.visibility = View.GONE
    }

    private fun loadAd(mAdView: AdView) {
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun loadRewardedAd() {
        rewardedAd = RewardedAd(this, getString(R.string.admob_bonificado_test_id))
        val adLoadCallback: RewardedAdLoadCallback = object: RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                rewardedAd.show(activity, null)
            }
            override fun onRewardedAdFailedToLoad(adError: LoadAdError) {
                FirebaseCrashlytics.getInstance().recordException(Throwable(adError.message))
                log("ResultActivity - loadAd", "Ad failed to load.")
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
    }
}