package com.quiz.pride.ui.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
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
import com.quiz.pride.utils.setSafeOnClickListener
import org.koin.android.scope.lifecycleScope
import org.koin.android.viewmodel.scope.viewModel


class SelectFragment : Fragment() {
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

        val cardStart: CardView = root.findViewById(R.id.cardStart)
        cardStart.setSafeOnClickListener {
            selectViewModel.navigateToGame()
        }

        val cardLearn: CardView = root.findViewById(R.id.cardLearn)
        cardLearn.setSafeOnClickListener {
            selectViewModel.navigateToInfo()
        }

        val cardSettings: CardView = root.findViewById(R.id.cardSettings)
        cardSettings.setSafeOnClickListener {
            selectViewModel.navigateToSettings()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectViewModel.navigation.observe(viewLifecycleOwner, Observer(::navigate))
    }

    private fun navigate(navigation: SelectViewModel.Navigation?) {
        when (navigation) {
            SelectViewModel.Navigation.Game -> activity?.startActivity<GameActivity> {}
            SelectViewModel.Navigation.Settings -> activity?.startActivity<SettingsActivity> {}
            SelectViewModel.Navigation.Info -> activity?.startActivity<InfoActivity> {}
            else -> {}
        }
    }
}
