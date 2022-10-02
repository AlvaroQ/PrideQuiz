package com.quiz.pride.ui.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.quiz.domain.User
import com.quiz.pride.databinding.RankingFragmentBinding
import com.quiz.pride.utils.glideLoadingGif
import org.koin.androidx.viewmodel.ext.android.viewModel


class RankingFragment : Fragment() {
    private val rankingViewModel: RankingViewModel by viewModel()
    private lateinit var binding: RankingFragmentBinding

    companion object {
        fun newInstance() = RankingFragment()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = RankingFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rankingViewModel.rankingList.observe(viewLifecycleOwner, Observer(::fillRanking))
        rankingViewModel.navigation.observe(viewLifecycleOwner, Observer(::navigate))
        rankingViewModel.progress.observe(viewLifecycleOwner, Observer(::loadAdAndProgress))
        rankingViewModel.showingAds.observe(viewLifecycleOwner, Observer(::loadAdAndProgress))
    }

    private fun fillRanking(userList: MutableList<User>) {
        binding.recyclerviewRanking.adapter = RankingListAdapter(requireContext(), userList)
    }

    private fun navigate(navigation: RankingViewModel.Navigation) {
        when (navigation) {
            RankingViewModel.Navigation.Result -> activity?.finish()
        }
    }

    private fun loadAdAndProgress(model: RankingViewModel.UiModel) {
        when(model) {
            is RankingViewModel.UiModel.ShowReewardAd -> {
                (activity as RankingActivity).showRewardedAd(model.show)
            }
            is RankingViewModel.UiModel.Loading -> updateProgress(model.show)
        }
    }

    private fun updateProgress(isShowing: Boolean) {
        if (isShowing) {
            glideLoadingGif(activity as RankingActivity, binding.imagenLoading)
            binding.imagenLoading.visibility = View.VISIBLE
        } else {
            binding.imagenLoading.visibility = View.GONE
        }
    }
}
