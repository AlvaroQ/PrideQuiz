package com.quiz.pride.ui.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.quiz.pride.R
import com.quiz.pride.common.startActivity
import com.quiz.pride.databinding.RankingFragmentBinding
import com.quiz.pride.ui.game.GameActivity
import com.quiz.pride.utils.glideLoadingGif
import com.quiz.domain.User
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import org.koin.android.scope.lifecycleScope
import org.koin.android.viewmodel.scope.viewModel


class RankingFragment : Fragment() {
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

        val cardScore: CardView = root.findViewById(R.id.cardScore)
        cardScore.alpha = 0f

        loadAd(root.findViewById(R.id.adViewRanking))

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rankingViewModel.rankingList.observe(viewLifecycleOwner, Observer(::fillRanking))
        rankingViewModel.navigation.observe(viewLifecycleOwner, Observer(::navigate))
        rankingViewModel.progress.observe(viewLifecycleOwner, Observer(::updateProgress))
    }

    private fun fillRanking(userList: MutableList<User>) {
        binding.recyclerviewRanking.adapter = RankingListAdapter(userList)
        binding.cardScore.animate().alpha(1f).duration = 500
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

    private fun loadAd(mAdView: AdView) {
        MobileAds.initialize(activity)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }
}
