package com.quiz.pride.ui.moreApps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.quiz.domain.App
import com.quiz.pride.R
import com.quiz.pride.databinding.MoreAppsFragmentBinding
import com.quiz.pride.utils.glideLoadingGif
import org.koin.android.scope.lifecycleScope
import org.koin.android.viewmodel.scope.viewModel


class MoreAppsFragment : Fragment() {
    private lateinit var binding: MoreAppsFragmentBinding
    private lateinit var adViewMoreApps: AdView
    private val moreAppsViewModel: MoreAppsViewModel by lifecycleScope.viewModel(this)

    companion object {
        fun newInstance() = MoreAppsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = MoreAppsFragmentBinding.inflate(inflater)
        val root = binding.root

        adViewMoreApps = root.findViewById(R.id.adViewMoreApps)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        moreAppsViewModel.list.observe(viewLifecycleOwner, Observer(::fillList))
        moreAppsViewModel.navigation.observe(viewLifecycleOwner, Observer(::navigate))
        moreAppsViewModel.progress.observe(viewLifecycleOwner, Observer(::updateProgress))
        moreAppsViewModel.showingAds.observe(viewLifecycleOwner, Observer(::loadAd))
    }

    private fun fillList(appList: MutableList<App>) {
        binding.recyclerviewMoreApps.adapter = MoreAppsListAdapter(requireContext(), appList)
    }

    private fun updateProgress(model: MoreAppsViewModel.UiModel?) {
        if (model is MoreAppsViewModel.UiModel.Loading && model.show) {
            glideLoadingGif(activity as MoreAppsActivity, binding.imagenLoading)
            binding.imagenLoading.visibility = View.VISIBLE
        } else {
            binding.imagenLoading.visibility = View.GONE
        }
    }

    private fun navigate(navigation: MoreAppsViewModel.Navigation?) {
        when (navigation) {
            MoreAppsViewModel.Navigation.Result -> { activity?.finishAfterTransition() }
        }
    }

    private fun loadAd(model: MoreAppsViewModel.UiModel) {
        if (model is MoreAppsViewModel.UiModel.ShowAd && model.show) {
            MobileAds.initialize(activity)
            val adRequest = AdRequest.Builder().build()
            adViewMoreApps.loadAd(adRequest)
        } else {
            adViewMoreApps.visibility = View.GONE
        }
    }
}
