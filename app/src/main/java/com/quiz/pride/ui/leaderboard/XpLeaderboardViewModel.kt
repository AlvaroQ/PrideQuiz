package com.quiz.pride.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quiz.domain.XpLeaderboardEntry
import com.quiz.pride.managers.XpSyncManager
import com.quiz.usecases.GetUserGlobalRank
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
    private val getUserGlobalRank: GetUserGlobalRank,
    private val xpSyncManager: XpSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(XpLeaderboardUiState())
    val uiState: StateFlow<XpLeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val currentUid = xpSyncManager.getCurrentUserId()
            val leaderboard = getXpLeaderboard.invoke()

            // Find user's rank if they have XP
            var userRank: Int? = null
            if (currentUid != null) {
                val userEntry = leaderboard.find { it.uid == currentUid }
                if (userEntry != null) {
                    userRank = leaderboard.indexOf(userEntry) + 1
                }
            }

            _uiState.value = XpLeaderboardUiState(
                isLoading = false,
                leaderboardList = leaderboard,
                userRank = userRank,
                currentUserUid = currentUid
            )
        }
    }

    fun refresh() {
        loadLeaderboard()
    }
}
