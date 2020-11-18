package com.quiz.pride.ui.ranking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quiz.domain.User
import com.quiz.pride.common.ScopedViewModel
import com.quiz.pride.managers.Analytics
import com.quiz.usecases.GetRankingScore
import kotlinx.coroutines.launch

class RankingViewModel(private val getRankingScore: GetRankingScore) : ScopedViewModel() {

    private val _progress = MutableLiveData<UiModel>()
    val progress: LiveData<UiModel> = _progress

    private val _navigation = MutableLiveData<Navigation>()
    val navigation: LiveData<Navigation> = _navigation

    private val _rankingList = MutableLiveData<MutableList<User>>()
    val rankingList: LiveData<MutableList<User>> = _rankingList

    init {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_RANKING)
        launch {
            _progress.value = UiModel.Loading(true)
            _rankingList.value = getRanking()
            _progress.value = UiModel.Loading(false)
        }
    }

    private suspend fun getRanking(): MutableList<User> {
        return getRankingScore.invoke()
    }

    sealed class Navigation {
        object Result : Navigation()
    }

    sealed class UiModel {
        data class Loading(val show: Boolean) : UiModel()
    }
}