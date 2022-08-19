package com.quiz.pride.ui.info

import android.os.Bundle
import android.view.View
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.utils.loadBonificado
import com.quiz.pride.utils.setSafeOnClickListener
import com.quiz.pride.utils.showBanner
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.info_activity.*

class InfoActivity : BaseActivity() {
    private lateinit var rewardedAd: RewardedAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerInfo, InfoFragment.newInstance())
                .commitNow()
        }

        MobileAds.initialize(this)
        rewardedAd = RewardedAd(this, getString(R.string.BONIFICADO_SHOW_INFO))

        MobileAds.initialize(this)
        btnBack.setSafeOnClickListener { finishAfterTransition() }
        toolbarTitle.text = getString(R.string.info_title)
        layoutLife.visibility = View.GONE
    }

    fun showAd(show: Boolean){
        showBanner(show, adViewInfo)
    }
    fun loadRewardedAd(show: Boolean) {
        loadBonificado(this, show, rewardedAd)
    }
}