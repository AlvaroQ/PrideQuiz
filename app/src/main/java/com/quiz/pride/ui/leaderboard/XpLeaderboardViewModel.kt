package com.quiz.pride.ui.leaderboard

import androidx.lifecycle.viewModelScope
import com.quiz.domain.XpLeaderboardEntry
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.XpSyncManager
import com.quiz.usecases.GetXpLeaderboard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class XpLeaderboardUiState(
    val isLoading: Boolean = true,
    val leaderboardList: List<XpLeaderboardEntry> = emptyList(),
    val userRank: Int? = null,
    val currentUserUid: String? = null
)

class XpLeaderboardViewModel(
    private val getXpLeaderboard: GetXpLeaderboard,
    private val getUserGlobalRank: com.quiz.usecases.GetUserGlobalRank,
    private val xpSyncManager: XpSyncManager
) : ComposeViewModel() {

    private val _uiState = MutableStateFlow(XpLeaderboardUiState())
    val uiState: StateFlow<XpLeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentUid = xpSyncManager.getCurrentUserId()
            val leaderboard = getXpLeaderboard()

            var userRank: Int? = null
            if (currentUid != null) {
                val userEntry = leaderboard.find { it.uid == currentUid }
                if (userEntry != null) {
                    userRank = leaderboard.indexOf(userEntry) + 1
                }
            }

            _uiState.update {
                XpLeaderboardUiState(
                    isLoading = false,
                    leaderboardList = leaderboard,
                    userRank = userRank,
                    currentUserUid = currentUid
                )
            }
        }
    }

    fun refresh() {
        loadLeaderboard()
    }
}
