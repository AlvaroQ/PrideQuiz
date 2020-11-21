package com.quiz.pride.ui.settings

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.quiz.pride.BuildConfig
import com.quiz.pride.R
import com.quiz.pride.base.BaseActivity
import com.quiz.pride.common.startActivity
import com.quiz.pride.managers.Analytics
import com.quiz.pride.ui.info.InfoActivity
import com.quiz.pride.ui.moreApps.MoreAppsActivity
import com.quiz.pride.ui.ranking.RankingActivity
import com.quiz.pride.utils.rateApp
import com.quiz.pride.utils.setSafeOnClickListener
import com.quiz.pride.utils.shareApp
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.settings_activity.*
import org.koin.core.parameter.parametersOf


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

        Analytics.analyticsScreenViewed(Analytics.SCREEN_SETTINGS)
        loadAd(adViewSettings)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // rate_app
            val rateApp: Preference? = findPreference("rate_app")
            rateApp?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                rateApp(requireContext())
                false
            }

            // share
            val share: Preference? = findPreference("share")
            share?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                shareApp(-1, requireContext())
                false
            }

            // Version
            val version: Preference? = findPreference("version")
            version?.summary = "${getString(R.string.settings_version)} ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"

            // more_apps
            val moreApps: Preference? = findPreference("more_apps")
            moreApps?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                activity?.startActivity<MoreAppsActivity> {}
                false
            }

            //TODO delete_ads
            val deleteAds: Preference? = findPreference("delete_ads")
            deleteAds?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                false
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                finish()
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

    private fun loadAd(mAdView: AdView) {
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }
}