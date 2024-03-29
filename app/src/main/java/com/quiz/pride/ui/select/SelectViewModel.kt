package com.quiz.pride.ui.select

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quiz.pride.common.ScopedViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.utils.Constants.GameType

class SelectViewModel : ScopedViewModel() {

    private val _navigation = MutableLiveData<Navigation>()
    val navigation: LiveData<Navigation> = _navigation

    init {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT_GAME)
    }

    fun navigateToSelectGame() {
        _navigation.value = Navigation.SelectGame
    }

    fun navigateToSettings() {
        _navigation.value = Navigation.Settings
    }

    fun navigateToInfo() {
        _navigation.value = Navigation.Info
    }

    sealed class Navigation {
        object SelectGame : Navigation()
        object Info : Navigation()
        object Settings : Navigation()
    }
}