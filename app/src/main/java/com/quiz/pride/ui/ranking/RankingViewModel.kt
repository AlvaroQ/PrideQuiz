package com.quiz.pride.ui.ranking

import androidx.lifecycle.viewModelScope
import com.quiz.domain.User
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetRankingScore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RankingUiState(
    val isLoading: Boolean = true,
    val rankingList: List<User> = emptyList(),
    val showRewardedAd: Boolean = false
)

class RankingViewModel(
    private val getRankingScore: GetRankingScore,
    private val getPaymentDone: GetPaymentDone
) : ComposeViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_RANKING)
        loadRanking()
    }

    private fun loadRanking() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val ranking = getRankingScore.invoke()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    rankingList = ranking,
                    showRewardedAd = !getPaymentDone()
                )
            }
        }
    }

    fun refreshRanking() {
        loadRanking()
    }
}
