package com.quiz.pride.ui.game

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.common.viewBinding
import com.quiz.pride.databinding.GameActivityBinding
import com.quiz.pride.utils.setSafeOnClickListener
import com.quiz.pride.utils.showBanner
import com.quiz.pride.utils.showBonificado


class GameActivity : BaseActivity() {
    private var rewardedAd: RewardedAd? = null
    private lateinit var activity: Activity

    private val binding by viewBinding(GameActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.containerGame, GameFragment.newInstance())
                    .commitNow()
        }
        activity = this

        MobileAds.initialize(this)
        RewardedAd.load(this, getString(R.string.BONIFICADO_GAME), AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
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

        binding.appBar.btnBack.setSafeOnClickListener { finishAfterTransition() }
        binding.appBar.layoutExtendedTitle.background = null
        binding.appBar.layoutLife.visibility = View.VISIBLE
        writePoints(0)
    }

    fun writePoints(points: Int) {
        runOnUiThread {
            binding.appBar.toolbarTitle.text = getString(R.string.points, points)
            binding.appBar.toolbarTitle.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_xy_collapse))
        }
    }

    fun writeLife(life: Int) {
        runOnUiThread {
            when (life) {
                2 -> {
                    binding.appBar.lifeSecond.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_life_on))
                    binding.appBar.lifeFirst.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_life_on))
                }
                1 -> {
                    binding.appBar.lifeSecond.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.scale_xy_collapse))
                    binding.appBar.lifeSecond.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_life_off))
                    binding.appBar.lifeFirst.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_life_on))
                }
                0 -> {
                    binding.appBar.lifeFirst.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.scale_xy_collapse))

                    // GAME OVER
                    binding.appBar.lifeSecond.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_life_off))
                    binding.appBar.lifeFirst.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_life_off))
                }
            }
        }
    }

    fun showBannerAd(show: Boolean){
        showBanner(show, binding.adViewGame)
    }

    fun showRewardedAd(show: Boolean){
        showBonificado(this, show, rewardedAd)
    }
}