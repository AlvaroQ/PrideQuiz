package com.quiz.pride.ui.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quiz.domain.Pride
import com.quiz.pride.common.ScopedViewModel
import com.quiz.pride.managers.Analytics
import com.quiz.usecases.GetSymbolFlagList
import kotlinx.coroutines.launch

class InfoViewModel(private val getSymbolFlagList: GetSymbolFlagList) : ScopedViewModel() {

    private val _progress = MutableLiveData<UiModel>()
    val progress: LiveData<UiModel> = _progress

    private val _navigation = MutableLiveData<Navigation>()
    val navigation: LiveData<Navigation> = _navigation

    private val _prideList = MutableLiveData<MutableList<Pride>>()
    val prideList: LiveData<MutableList<Pride>> = _prideList

    init {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_INFO)
        launch {
            _progress.value = UiModel.Loading(true)
            _prideList.value = getPrideList()
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
    }
}