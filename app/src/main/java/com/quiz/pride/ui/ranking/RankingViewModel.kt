package com.quiz.pride.ui.ranking

import arrow.core.getOrElse
import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.quiz.domain.User
import com.quiz.domain.XpLeaderboardEntry
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetRankingScore
import com.quiz.usecases.GetXpLeaderboard
import com.quiz.usecases.RankingMode
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class RankingUiState(
    val isLoading: Boolean = true,
    val rankingList: List<User> = emptyList(),
    val timedRankingList: List<User> = emptyList(),
    val xpLeaderboardList: List<XpLeaderboardEntry> = emptyList(),
    val selectedTabIndex: Int = 0,
    val showRewardedAd: Boolean = false,
    val showBannerAd: Boolean = false,
    val hasError: Boolean = false
)

class RankingViewModel(
    private val getRankingScore: GetRankingScore,
    private val getPaymentDone: GetPaymentDone,
    private val getXpLeaderboard: GetXpLeaderboard,
    private val analyticsManager: AnalyticsManager
) : ComposeViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init {
        analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_RANKING)
        loadRanking()
    }

    private fun loadRanking() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hasError = false) }

            val rankingDeferred = async { getRankingScore(RankingMode.NORMAL) }
            val timedRankingDeferred = async { getRankingScore(RankingMode.TIMED) }
            val xpLeaderboardDeferred = async { getXpLeaderboard() }

            val rankingResult = rankingDeferred.await()
            val timedRankingResult = timedRankingDeferred.await()
            val xpLeaderboard = xpLeaderboardDeferred.await()
            val showAd = !getPaymentDone()

            // Los rankings usan getOrElse para degradar con lista vacia en caso de error
            // sin bloquear toda la pantalla — el usuario igual ve lo que cargo
            val ranking = rankingResult.getOrElse { emptyList() }
            val timedRanking = timedRankingResult.getOrElse { emptyList() }
            val hasError = rankingResult.isLeft() || timedRankingResult.isLeft()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    rankingList = ranking,
                    timedRankingList = timedRanking,
                    xpLeaderboardList = xpLeaderboard,
                    showRewardedAd = showAd,
                    showBannerAd = showAd,
                    hasError = hasError
                )
            }
        }
    }

    fun onTabSelected(tabIndex: Int) {
        val tabName = when (tabIndex) {
            0 -> "normal"
            1 -> "timed"
            2 -> "xp"
            else -> "unknown"
        }
        analyticsManager.analyticsRankingTabSelected(tabName)
        _uiState.update { it.copy(selectedTabIndex = tabIndex) }
    }

    fun refreshRanking() {
        loadRanking()
    }
}
