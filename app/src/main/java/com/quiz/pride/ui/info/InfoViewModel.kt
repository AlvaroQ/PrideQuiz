package com.quiz.pride.ui.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quiz.domain.Pride
import com.quiz.pride.common.ScopedViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetSymbolFlagList
import kotlinx.coroutines.launch

class InfoViewModel(private val getSymbolFlagList: GetSymbolFlagList,
                    private val getPaymentDone: GetPaymentDone) : ScopedViewModel() {

    private val _progress = MutableLiveData<UiModel>()
    val progress: LiveData<UiModel> = _progress

    private val _navigation = MutableLiveData<Navigation>()
    val navigation: LiveData<Navigation> = _navigation

    private val _prideList = MutableLiveData<MutableList<Pride>>()
    val prideList: LiveData<MutableList<Pride>> = _prideList

    private val _showingAds = MutableLiveData<UiModel>()
    val showingAds: LiveData<UiModel> = _showingAds

    init {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_INFO)
        launch {
            _progress.value = UiModel.Loading(true)
            _prideList.value = getPrideList()
            _showingAds.value = UiModel.ShowAd(!getPaymentDone())
            _progress.value = UiModel.Loading(false)
        }
    }

    private suspend fun getPrideList(): MutableList<Pride> {
        return getSymbolFlagList.invoke()
    }

    fun navigateToSelect() {
        _navigation.value = Navigation.Select
    }

    sealed class Navigation {
        object Select : Navigation()
    }

    sealed class UiModel {
        data class Loading(val show: Boolean) : UiModel()
        data class ShowAd(val show: Boolean) : UiModel()
    }
}