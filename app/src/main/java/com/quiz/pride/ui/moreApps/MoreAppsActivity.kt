package com.quiz.pride.ui.moreApps

import android.os.Bundle
import android.view.View
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.common.viewBinding
import com.quiz.pride.databinding.MoreAppsActivityBinding
import com.quiz.pride.utils.setSafeOnClickListener
import com.quiz.pride.utils.showBanner

class MoreAppsActivity : BaseActivity() {
    private val binding by viewBinding(MoreAppsActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerResult, MoreAppsFragment.newInstance())
                .commitNow()
        }

        with(binding.appBar) {
            btnBack.setSafeOnClickListener { finish() }
            layoutExtendedTitle.background = null
            toolbarTitle.text = getString(R.string.more_apps)
            layoutLife.visibility = View.GONE
        }
    }

    fun showBannerAd(show: Boolean) {
        showBanner(show, binding.adViewMoreApps)
    }
}