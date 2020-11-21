package com.quiz.pride.ui.game

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.utils.setSafeOnClickListener
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

        btnBack.setSafeOnClickListener {
            finishAfterTransition()
        }

        layoutExtendedTitle.background = null
        layoutLife.visibility = View.VISIBLE
        writePoints(0)
    }

    fun writePoints(points: Int) {
        toolbarTitle.text = getString(R.string.points, points)
        toolbarTitle.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_xy_collapse))
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

    fun showAd(show: Boolean){
        if(show) {
            MobileAds.initialize(this)
            val adRequest = AdRequest.Builder().build()
            adViewGame.loadAd(adRequest)
        } else {
            adViewGame.visibility = View.GONE
        }
    }
}