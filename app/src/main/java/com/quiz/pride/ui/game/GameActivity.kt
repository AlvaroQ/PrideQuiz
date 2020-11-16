package com.quiz.pride.ui.game

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.common.startActivity
import com.quiz.pride.managers.Analytics
import com.quiz.pride.ui.select.SelectActivity
import com.quiz.pride.utils.setSafeOnClickListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.game_activity.*


class GameActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerGame, GameFragment.newInstance())
                .commitNow()
        }

        loadAd(adView)
        btnBack.setSafeOnClickListener {
            startActivity<SelectActivity> {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        writeStage(1)
    }

    private fun loadAd(mAdView: AdView) {
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    fun writeStage(stage: Int) {
        toolbarTitle.text = stage.toString()
    }

    fun writeDeleteLife(life: Int) {
        when(life) {
            2 -> {
                lifeSecond.setImageDrawable(getDrawable(R.drawable.ic_life_on))
                lifeFirst.setImageDrawable(getDrawable(R.drawable.ic_life_on))
            }
            1 -> {
                lifeSecond.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_xy_collapse))

                lifeSecond.setImageDrawable(getDrawable(R.drawable.ic_life_off))
                lifeFirst.setImageDrawable(getDrawable(R.drawable.ic_life_on))
            }
            0 -> {
                lifeFirst.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_xy_collapse))

                // GAME OVER
                lifeSecond.setImageDrawable(getDrawable(R.drawable.ic_life_off))
                lifeFirst.setImageDrawable(getDrawable(R.drawable.ic_life_off))
            }
        }
    }
}