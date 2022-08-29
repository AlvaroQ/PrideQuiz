package com.quiz.pride.ui.ranking

import android.os.Bundle
import android.view.View
import com.google.android.gms.ads.rewarded.RewardedAd
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.common.viewBinding
import com.quiz.pride.databinding.RankingActivityBinding
import com.quiz.pride.utils.loadBonificado
import com.quiz.pride.utils.setSafeOnClickListener

class RankingActivity : BaseActivity() {
    private lateinit var rewardedAd: RewardedAd

    private val binding by viewBinding(RankingActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerResult, RankingFragment.newInstance())
                .commitNow()
        }

        rewardedAd = RewardedAd(this, getString(R.string.BONIFICADO_GAME_OVER))

        with(binding.appBar) {
            btnBack.setSafeOnClickListener { finish() }
            toolbarTitle.text = getString(R.string.best_points)
            layoutLife.visibility = View.GONE
        }
    }

    fun showRewardedAd(show: Boolean){
        loadBonificado(this, show, rewardedAd)
    }
}