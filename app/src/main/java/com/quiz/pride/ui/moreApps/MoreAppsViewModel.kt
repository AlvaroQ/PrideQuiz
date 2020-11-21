package com.quiz.pride.ui.moreApps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quiz.domain.App
import com.quiz.domain.User
import com.quiz.pride.common.ScopedViewModel
import com.quiz.pride.managers.Analytics
import com.quiz.pride.ui.result.ResultViewModel
import com.quiz.usecases.GetAppsRecommended
import com.quiz.usecases.GetRankingScore
import kotlinx.coroutines.launch

class MoreAppsViewModel(private val getAppsRecommended: GetAppsRecommended) : ScopedViewModel() {

    private val _progress = MutableLiveData<UiModel>()
    val progress: LiveData<UiModel> = _progress

    private val _navigation = MutableLiveData<Navigation>()
    val navigation: LiveData<Navigation> = _navigation

    private val _list = MutableLiveData<MutableList<App>>()
    val list: LiveData<MutableList<App>> = _list

    init {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_MORE_APPS)
        launch {
            _progress.value = UiModel.Loading(true)
            _list.value = appsRecommended()
            _progress.value = UiModel.Loading(false)
        }
    }

    private suspend fun appsRecommended(): MutableList<App> {
        return getAppsRecommended.invoke()
    }

    sealed class Navigation {
        object Result : Navigation()
    }

    sealed class UiModel {
        data class Loading(val show: Boolean) : UiModel()
    }
}