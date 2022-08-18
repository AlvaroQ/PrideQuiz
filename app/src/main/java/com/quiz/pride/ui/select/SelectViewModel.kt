package com.quiz.pride.ui.select

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quiz.pride.common.ScopedViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.usecases.GetPaymentDone

class SelectViewModel(private val getPaymentDone: GetPaymentDone) : ScopedViewModel() {

    private val _navigation = MutableLiveData<Navigation>()
    val navigation: LiveData<Navigation> = _navigation

    init {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT_GAME)
    }

    private fun getPaymentDone(): Boolean {
        return getPaymentDone.invoke()
    }

    fun navigateToGame() {
        _navigation.value = Navigation.Game
    }

    fun navigateToSettings() {
        _navigation.value = Navigation.Settings
    }

    fun navigateToInfo() {
        _navigation.value = Navigation.Info
    }

    sealed class Navigation {
        object Game : Navigation()
        object Info : Navigation()
        object Settings : Navigation()
    }
}