package com.quiz.pride.ui.settings

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.google.android.gms.ads.MobileAds
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.common.viewBinding
import com.quiz.pride.databinding.SettingsActivityBinding
import com.quiz.pride.utils.setSafeOnClickListener
import com.quiz.pride.utils.showBanner


class SettingsActivity : BaseActivity() {
    private val binding by viewBinding(SettingsActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupToolbar()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        MobileAds.initialize(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                finishAfterTransition()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupToolbar() {
        with(binding.appBar) {
            toolbarTitle.text = getString(R.string.settings)
            layoutLife.visibility = View.GONE
            layoutExtendedTitle.background = null
            btnBack.setSafeOnClickListener { finishAfterTransition() }
        }
    }

    fun showAd(show: Boolean){
        showBanner(show, binding.adViewSettings)
    }
}