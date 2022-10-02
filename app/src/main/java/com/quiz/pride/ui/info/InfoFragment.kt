package com.quiz.pride.ui.info

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quiz.domain.Pride
import com.quiz.pride.common.startActivity
import com.quiz.pride.databinding.InfoFragmentBinding
import com.quiz.pride.ui.select.SelectActivity
import com.quiz.pride.utils.Constants.TOTAL_ITEM_EACH_LOAD
import com.quiz.pride.utils.Constants.TOTAL_PRIDES
import com.quiz.pride.utils.glideLoadingGif
import org.koin.androidx.viewmodel.ext.android.viewModel


class InfoFragment : Fragment() {
    private val infoViewModel: InfoViewModel by viewModel()
    private lateinit var binding: InfoFragmentBinding

    private var currentPage = 0
    private lateinit var scrollListener: RecyclerView.OnScrollListener
    lateinit var adapter: InfoListAdapter

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
        infoViewModel.prideList.observe(viewLifecycleOwner, Observer(::fillPrideList))
        infoViewModel.updatePrideList.observe(viewLifecycleOwner, Observer(::updatePrideList))
        infoViewModel.progress.observe(viewLifecycleOwner, Observer(::updateProgress))
        infoViewModel.showingAds.observe(viewLifecycleOwner, Observer(::loadAd))
    }

    private fun loadAd(model: InfoViewModel.UiModel) {
        when(model) {
            is InfoViewModel.UiModel.ShowBannerAd -> (activity as InfoActivity).showAd(model.show)
            is InfoViewModel.UiModel.ShowReewardAd -> (activity as InfoActivity).loadRewardedAd(model.show)
            else -> {}
        }
    }

    private fun updateProgress(model: InfoViewModel.UiModel?) {
        if (model is InfoViewModel.UiModel.Loading && model.show) {
            glideLoadingGif(activity as InfoActivity, binding.imagenLoading)
            binding.imagenLoading.visibility = View.VISIBLE
        } else {
            binding.imagenLoading.visibility = View.GONE
        }
    }

    private fun fillPrideList(prideList: MutableList<Pride>) {
        adapter = InfoListAdapter(requireContext(), prideList)
        binding.recyclerviewInfo.adapter = adapter
        setRecyclerViewScrollListener()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updatePrideList(prideList: MutableList<Pride>) {
        adapter.update(prideList)
        adapter.notifyDataSetChanged()
        setRecyclerViewScrollListener()
    }

    private fun setRecyclerViewScrollListener() {
        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager?.itemCount
                val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                if (totalItemCount == lastVisibleItemPosition + 1) {
                    binding.recyclerviewInfo.removeOnScrollListener(scrollListener)

                    if(currentPage * TOTAL_ITEM_EACH_LOAD < TOTAL_PRIDES) {
                        Log.d("MyTAG", "Load new list")
                        currentPage++
                        infoViewModel.loadMorePrideList(currentPage)
                    }
                }
            }
        }
        binding.recyclerviewInfo.addOnScrollListener(scrollListener)
    }

    private fun navigate(navigation: InfoViewModel.Navigation?) {
        when (navigation) {
            InfoViewModel.Navigation.Select -> {
                activity?.startActivity<SelectActivity> {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            else -> {}
        }
    }
}
