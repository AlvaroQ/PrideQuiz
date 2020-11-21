package com.quiz.pride.ui.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.quiz.domain.User
import com.quiz.pride.R
import com.quiz.pride.databinding.RankingFragmentBinding
import com.quiz.pride.utils.glideLoadingGif
import org.koin.android.scope.lifecycleScope
import org.koin.android.viewmodel.scope.viewModel


class RankingFragment : Fragment() {
    private lateinit var adViewRanking: AdView
    private lateinit var binding: RankingFragmentBinding
    private val rankingViewModel: RankingViewModel by lifecycleScope.viewModel(this)

    companion object {
        fun newInstance() = RankingFragment()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = RankingFragmentBinding.inflate(inflater)
        val root = binding.root

        adViewRanking = root.findViewById(R.id.adViewRanking)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rankingViewModel.rankingList.observe(viewLifecycleOwner, Observer(::fillRanking))
        rankingViewModel.navigation.observe(viewLifecycleOwner, Observer(::navigate))
        rankingViewModel.progress.observe(viewLifecycleOwner, Observer(::updateProgress))
        rankingViewModel.showingAds.observe(viewLifecycleOwner, Observer(::loadAd))
    }

    private fun fillRanking(userList: MutableList<User>) {
        binding.recyclerviewRanking.adapter = RankingListAdapter(requireContext(), userList)
    }

    private fun updateProgress(model: RankingViewModel.UiModel?) {
        if (model is RankingViewModel.UiModel.Loading && model.show) {
            glideLoadingGif(activity as RankingActivity, binding.imagenLoading)
            binding.imagenLoading.visibility = View.VISIBLE
        } else {
            binding.imagenLoading.visibility = View.GONE
        }
    }

    private fun navigate(navigation: RankingViewModel.Navigation?) {
        when (navigation) {
            RankingViewModel.Navigation.Result -> {
                activity?.finish()
            }
        }
    }

    private fun loadAd(model: RankingViewModel.UiModel) {
        if (model is RankingViewModel.UiModel.ShowAd && model.show) {
            MobileAds.initialize(activity)
            val adRequest = AdRequest.Builder().build()
            adViewRanking.loadAd(adRequest)
        } else {
            adViewRanking.visibility = View.GONE
        }
    }
}
