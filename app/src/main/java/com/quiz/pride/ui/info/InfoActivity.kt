package com.quiz.pride.ui.info

import android.os.Bundle
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.utils.setSafeOnClickListener
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.info_activity.*

class InfoActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerInfo, InfoFragment.newInstance())
                .commitNow()
        }
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
}