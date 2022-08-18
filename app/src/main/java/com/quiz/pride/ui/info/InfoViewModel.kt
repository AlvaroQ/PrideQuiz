package com.quiz.pride.ui.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quiz.domain.Pride
import com.quiz.pride.BuildConfig
import com.quiz.pride.common.ScopedViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetPrideList
import kotlinx.coroutines.launch

class InfoViewModel(private val getPrideList: GetPrideList,
                    private val getPaymentDone: GetPaymentDone) : ScopedViewModel() {
    private var list = mutableListOf<Pride>()

    private val _progress = MutableLiveData<UiModel>()
    val progress: LiveData<UiModel> = _progress

    private val _navigation = MutableLiveData<Navigation>()
    val navigation: LiveData<Navigation> = _navigation

    private val _prideList = MutableLiveData<MutableList<Pride>>()
    val prideList: LiveData<MutableList<Pride>> = _prideList

    private val _updatePrideList = MutableLiveData<MutableList<Pride>>()
    val updatePrideList: LiveData<MutableList<Pride>> = _updatePrideList

    private val _showingAds = MutableLiveData<UiModel>()
    val showingAds: LiveData<UiModel> = _showingAds

    init {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_INFO)
        launch {
            _progress.value = UiModel.Loading(true)
            _prideList.value = getPrideList(0)
            _showingAds.value = UiModel.ShowBannerAd(!getPaymentDone())
            _progress.value = UiModel.Loading(false)
        }
    }

    fun loadMorePrideList(currentPage: Int) {
        launch {
            _progress.value = UiModel.Loading(true)
            _updatePrideList.value = getPrideList(currentPage)
            if(currentPage % 3 == 0) {
                _showingAds.value = UiModel.ShowReewardAd(!getPaymentDone())
            }
            _progress.value = UiModel.Loading(false)
        }
    }

    private suspend fun getPrideList(currentPage: Int): MutableList<Pride> {
        list = (list + getPrideList.invoke(currentPage)) as MutableList<Pride>
        return list
    }

    fun navigateToSelect() {
        _navigation.value = Navigation.Select
    }

    sealed class Navigation {
        object Select : Navigation()
    }

    sealed class UiModel {
        data class Loading(val show: Boolean) : UiModel()
        data class ShowBannerAd(val show: Boolean) : UiModel()
        data class ShowReewardAd(val show: Boolean) : UiModel()
    }
}