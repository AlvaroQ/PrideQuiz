package com.quiz.pride.ui.settings

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresPermission
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.utils.setSafeOnClickListener
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.settings_activity.*


class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setupToolbar()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
        toolbarTitle.text = getString(R.string.settings)
        layoutLife.visibility = View.GONE
        layoutExtendedTitle.background = null
        btnBack.setSafeOnClickListener { finishAfterTransition() }
    }

    @RequiresPermission("android.permission.INTERNET")
    fun showAd(show: Boolean){
        if(show) {
            MobileAds.initialize(this)
            val adRequest = AdRequest.Builder().build()
            adViewSettings.loadAd(adRequest)
        } else {
            adViewSettings.visibility = View.GONE
        }
    }
}