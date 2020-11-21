package com.quiz.pride.ui.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.quiz.pride.R
import com.quiz.pride.common.startActivity
import com.quiz.pride.databinding.SelectFragmentBinding
import com.quiz.pride.ui.game.GameActivity
import com.quiz.pride.ui.info.InfoActivity
import com.quiz.pride.ui.settings.SettingsActivity
import com.quiz.pride.ui.settings.SettingsViewModel
import com.quiz.pride.utils.log
import com.quiz.pride.utils.setSafeOnClickListener
import org.koin.android.scope.lifecycleScope
import org.koin.android.viewmodel.scope.viewModel


class SelectFragment : Fragment() {
    private var loadAd: Boolean = true
    private lateinit var rewardedAd: RewardedAd
    private lateinit var binding: SelectFragmentBinding
    private val selectViewModel: SelectViewModel by lifecycleScope.viewModel(this)

    companion object {
        fun newInstance() = SelectFragment()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = SelectFragmentBinding.inflate(inflater)
        val root = binding.root

        val imageSettings: ImageView = root.findViewById(R.id.imageSettings)
        imageSettings.setSafeOnClickListener {
            selectViewModel.navigateToSettings()
        }

        val btnStart: Button = root.findViewById(R.id.btnStart)
        btnStart.setSafeOnClickListener {
            selectViewModel.navigateToGame()
        }

        val btnInfo: Button = root.findViewById(R.id.btnInfo)
        btnInfo.setSafeOnClickListener {
            selectViewModel.navigateToInfo()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectViewModel.navigation.observe(viewLifecycleOwner, Observer(::navigate))
        selectViewModel.showingAds.observe(viewLifecycleOwner, Observer(::loadAd))
    }

    private fun navigate(navigation: SelectViewModel.Navigation?) {
        when (navigation) {
            SelectViewModel.Navigation.Game -> activity?.startActivity<GameActivity> {}
            SelectViewModel.Navigation.Settings -> activity?.startActivity<SettingsActivity> {}
            SelectViewModel.Navigation.Info -> {
                if(loadAd) loadRewardedAd()
                activity?.startActivity<InfoActivity> {}
            }
        }
    }

    private fun loadAd(model: SelectViewModel.UiModel) {
        if (model is SelectViewModel.UiModel.ShowAd)
            loadAd = model.show
    }


    private fun loadRewardedAd() {
        rewardedAd = RewardedAd(requireContext(), getString(R.string.admob_bonificado_test_id))
        val adLoadCallback: RewardedAdLoadCallback = object: RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                rewardedAd.show(activity, null)
            }
            override fun onRewardedAdFailedToLoad(adError: LoadAdError) {
                FirebaseCrashlytics.getInstance().recordException(Throwable(adError.message))
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
    }
}
