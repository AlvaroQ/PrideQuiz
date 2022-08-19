package com.quiz.pride.ui.ranking

import android.os.Bundle
import android.view.View
import com.google.android.gms.ads.rewarded.RewardedAd
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.utils.loadBonificado
import com.quiz.pride.utils.setSafeOnClickListener
import kotlinx.android.synthetic.main.app_bar_layout.*

class RankingActivity : BaseActivity() {
    private lateinit var rewardedAd: RewardedAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerResult, RankingFragment.newInstance())
                .commitNow()
        }

        rewardedAd = RewardedAd(this, getString(R.string.BONIFICADO_GAME_OVER))
        btnBack.setSafeOnClickListener { finish() }
        toolbarTitle.text = getString(R.string.best_points)
        layoutLife.visibility = View.GONE
    }

    fun showRewardedAd(show: Boolean){
        loadBonificado(this, show, rewardedAd)
    }
}