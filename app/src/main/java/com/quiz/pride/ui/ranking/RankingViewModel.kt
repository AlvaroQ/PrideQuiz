package com.quiz.pride.ui.ranking

import androidx.lifecycle.viewModelScope
import com.quiz.domain.User
import com.quiz.domain.XpLeaderboardEntry
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetRankingScore
import com.quiz.usecases.GetTimedRankingScore
import com.quiz.usecases.GetXpLeaderboard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RankingUiState(
    val isLoading: Boolean = true,
    val rankingList: List<User> = emptyList(),
    val timedRankingList: List<User> = emptyList(),
    val xpLeaderboardList: List<XpLeaderboardEntry> = emptyList(),
    val selectedTabIndex: Int = 0,
    val showRewardedAd: Boolean = false
)

class RankingViewModel(
    private val getRankingScore: GetRankingScore,
    private val getTimedRankingScore: GetTimedRankingScore,
    private val getPaymentDone: GetPaymentDone,
    private val getXpLeaderboard: GetXpLeaderboard
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
            val timedRanking = getTimedRankingScore.invoke()
            val xpLeaderboard = getXpLeaderboard.invoke()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    rankingList = ranking,
                    timedRankingList = timedRanking,
                    xpLeaderboardList = xpLeaderboard,
                    showRewardedAd = !getPaymentDone()
                )
            }
        }
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTabIndex = tabIndex) }
    }

    fun refreshRanking() {
        loadRanking()
    }
}
