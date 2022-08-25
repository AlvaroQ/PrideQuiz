package com.quiz.pride.ui.select

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quiz.pride.common.ScopedViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.utils.Constants.GameType

class SelectGameViewModel : ScopedViewModel() {

    private val _navigation = MutableLiveData<Navigation>()
    val navigation: LiveData<Navigation> = _navigation

    init {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT_GAME)
    }

    fun navigateToGame(type: GameType) {
        _navigation.value = Navigation.Game(type)
    }

    sealed class Navigation {
        data class Game(val type: GameType) : Navigation()
    }
}