package com.quiz.pride.ui.game

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.utils.loadBonificado
import com.quiz.pride.utils.setSafeOnClickListener
import com.quiz.pride.utils.showBanner
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.game_activity.*


class GameActivity : BaseActivity() {
    private lateinit var rewardedAd: RewardedAd
    private lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.containerGame, GameFragment.newInstance())
                    .commitNow()
        }
        activity = this

        MobileAds.initialize(this)
        rewardedAd = RewardedAd(this, getString(R.string.BONIFICADO_GAME))

        btnBack.setSafeOnClickListener {
            finishAfterTransition()
        }

        layoutExtendedTitle.background = null
        layoutLife.visibility = View.VISIBLE
        writePoints(0)
    }

    fun writePoints(points: Int) {
        runOnUiThread {
            toolbarTitle.text = getString(R.string.points, points)
            toolbarTitle.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_xy_collapse))
        }
    }

    fun writeLife(life: Int) {
        runOnUiThread {
            when (life) {
                2 -> {
                    lifeSecond.setImageDrawable(getDrawable(R.drawable.ic_life_on))
                    lifeFirst.setImageDrawable(getDrawable(R.drawable.ic_life_on))
                }
                1 -> {
                    lifeSecond.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.scale_xy_collapse))
                    lifeSecond.setImageDrawable(getDrawable(R.drawable.ic_life_off))
                    lifeFirst.setImageDrawable(getDrawable(R.drawable.ic_life_on))
                }
                0 -> {
                    lifeFirst.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.scale_xy_collapse))

                    // GAME OVER
                    lifeSecond.setImageDrawable(getDrawable(R.drawable.ic_life_off))
                    lifeFirst.setImageDrawable(getDrawable(R.drawable.ic_life_off))
                }
            }
        }
    }

    fun showBannerAd(show: Boolean){
        showBanner(show, adViewGame)
    }

    fun showRewardedAd(show: Boolean){
        loadBonificado(this, show, rewardedAd)
    }
}