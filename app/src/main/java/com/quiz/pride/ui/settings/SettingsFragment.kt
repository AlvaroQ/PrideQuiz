package com.quiz.pride.ui.settings

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.quiz.pride.BuildConfig
import com.quiz.pride.R
import com.quiz.pride.common.startActivity
import com.quiz.pride.ui.moreApps.MoreAppsActivity
import com.quiz.pride.utils.rateApp
import com.quiz.pride.utils.shareApp
import org.koin.android.scope.lifecycleScope
import org.koin.android.viewmodel.scope.viewModel

class SettingsFragment : PreferenceFragmentCompat() {

    private val settingsViewModel: SettingsViewModel by lifecycleScope.viewModel(this)

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
            settingsViewModel.savePaymentDone()
            false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsViewModel.showingAds.observe(viewLifecycleOwner, Observer(::loadAd))
    }

    private fun loadAd(model: SettingsViewModel.UiModel) {
        if (model is SettingsViewModel.UiModel.ShowAd)
            (activity as SettingsActivity).showAd(model.show)
    }
}