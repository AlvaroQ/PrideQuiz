package com.quiz.pride.ui.info

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.quiz.domain.Pride
import com.quiz.pride.common.startActivity
import com.quiz.pride.databinding.InfoFragmentBinding
import com.quiz.pride.ui.select.SelectActivity
import com.quiz.pride.utils.glideLoadingGif
import org.koin.android.scope.lifecycleScope
import org.koin.android.viewmodel.scope.viewModel


class InfoFragment : Fragment() {
    private lateinit var binding: InfoFragmentBinding
    private val infoViewModel: InfoViewModel by lifecycleScope.viewModel(this)

    companion object {
        fun newInstance() = InfoFragment()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = InfoFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        infoViewModel.navigation.observe(viewLifecycleOwner, Observer(::navigate))
        infoViewModel.prideList.observe(viewLifecycleOwner, Observer(::fillRanking))
        infoViewModel.progress.observe(viewLifecycleOwner, Observer(::updateProgress))
        infoViewModel.showingAds.observe(viewLifecycleOwner, Observer(::loadAd))
    }

    private fun loadAd(model: InfoViewModel.UiModel) {
        if (model is InfoViewModel.UiModel.ShowAd)
            (activity as InfoActivity).showAd(model.show)
    }

    private fun updateProgress(model: InfoViewModel.UiModel?) {
        if (model is InfoViewModel.UiModel.Loading && model.show) {
            glideLoadingGif(activity as InfoActivity, binding.imagenLoading)
            binding.imagenLoading.visibility = View.VISIBLE
        } else {
            binding.imagenLoading.visibility = View.GONE
        }
    }

    private fun fillRanking(prideList: MutableList<Pride>) {
        binding.recyclerviewInfo.adapter = InfoListAdapter(requireContext(), prideList)
    }

    private fun navigate(navigation: InfoViewModel.Navigation?) {
        when (navigation) {
            InfoViewModel.Navigation.Select -> {
                activity?.startActivity<SelectActivity> {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
        }
    }
}
