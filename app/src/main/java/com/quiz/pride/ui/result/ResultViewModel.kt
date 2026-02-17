package com.quiz.pride.ui.result

import androidx.lifecycle.viewModelScope
import com.quiz.domain.App
import com.quiz.domain.User
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.Achievement
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.GameMode
import com.quiz.pride.managers.GameResult
import com.quiz.pride.managers.ProgressionManager
import com.quiz.pride.managers.XpGainResult
import com.quiz.pride.managers.XpSyncManager
import com.quiz.pride.managers.UserProfile
import com.quiz.pride.utils.Constants.TOP_RANKING_LIMIT
import com.quiz.usecases.GetAppsRecommended
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetPersonalRecord
import com.quiz.usecases.GetRecordScore
import com.quiz.usecases.RankingMode
import com.quiz.usecases.SaveTopScore
import com.quiz.usecases.SetPersonalRecord
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResultUiState(
    val isLoading: Boolean = true,
    val appsList: List<App> = emptyList(),
    val personalRecord: String = "0",
    val worldRecord: String = "0",
    val photoUrl: String = "",
    val xpGainResult: XpGainResult? = null,
    val newAchievements: List<Achievement> = emptyList(),
    val showLevelUpDialog: Boolean = false,
    // Timed ranking dialog state
    val showTimedRankingDialog: Boolean = false,
    val timedScore: Int = 0,
    val userProfile: UserProfile = UserProfile(),
    val isSavingTimedScore: Boolean = false
)

sealed class ResultEvent {
    object ShowWorldRecordDialog : ResultEvent()
    object TimedScoreSaved : ResultEvent()
}

class ResultViewModel(
    private val getAppsRecommended: GetAppsRecommended,
    private val saveTopScore: SaveTopScore,
    private val getRecordScore: GetRecordScore,
    private val getPersonalRecord: GetPersonalRecord,
    private val setPersonalRecord: SetPersonalRecord,
    private val getPaymentDone: GetPaymentDone,
    private val progressionManager: ProgressionManager,
    private val xpSyncManager: XpSyncManager,
    private val analyticsManager: AnalyticsManager
) : ComposeViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ResultEvent>()
    val events = _events.asSharedFlow()

    init {
        analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_RESULT)
        loadData()
    }

    /**
     * Record game result and award XP
     */
    fun recordGameResult(
        gameMode: GameMode,
        correctAnswers: Int,
        totalQuestions: Int,
        bestStreak: Int,
        timePlayedMs: Long,
        completedAllQuestions: Boolean
    ) {
        viewModelScope.launch {
            val result = GameResult(
                gameMode = gameMode,
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                bestStreak = bestStreak,
                timePlayedMs = timePlayedMs,
                completedAllQuestions = completedAllQuestions
            )

            // Record result and get XP
            val xpResult = progressionManager.recordGameResult(result)

            // Check for new achievements
            val newAchievements = progressionManager.checkAndUnlockAchievements()

            // Sync XP to Firestore leaderboard
            xpSyncManager.triggerSync()

            _uiState.update { state ->
                state.copy(
                    xpGainResult = xpResult,
                    newAchievements = newAchievements,
                    showLevelUpDialog = xpResult.leveledUp
                )
            }
        }
    }

    fun dismissLevelUpDialog() {
        _uiState.update { it.copy(showLevelUpDialog = false) }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val apps = getAppsRecommended.invoke()
            val worldRecord = getRecordScore(1)

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    appsList = apps,
                    worldRecord = worldRecord
                )
            }
        }
    }

    fun checkPersonalRecord(points: Int) {
        val currentRecord = getPersonalRecord.invoke()
        if (points > currentRecord) {
            setPersonalRecord.invoke(points)
            _uiState.update { it.copy(personalRecord = points.toString()) }
        } else {
            _uiState.update { it.copy(personalRecord = currentRecord.toString()) }
        }
    }

    fun checkWorldRecord(gamePoints: Int) {
        viewModelScope.launch {
            val pointsLastClassified = getRecordScore(50)
            if (pointsLastClassified.isNotEmpty() && gamePoints > pointsLastClassified.toInt()) {
                analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_DIALOG_SAVE_SCORE)
                _events.emit(ResultEvent.ShowWorldRecordDialog)
            }
        }
    }

    fun saveScore(user: User) {
        viewModelScope.launch {
            saveTopScore(user)
        }
    }

    fun setPhotoUrl(url: String) {
        _uiState.update { it.copy(photoUrl = url) }
    }

    /**
     * Check if the timed mode score qualifies for top 20
     */
    fun checkTimedRanking(score: Int) {
        viewModelScope.launch {
            val position20Score = getRecordScore(TOP_RANKING_LIMIT, RankingMode.TIMED)
            val qualifies = position20Score.isEmpty() || score > (position20Score.toIntOrNull() ?: 0)

            if (qualifies) {
                // Load user profile
                val userProfile = progressionManager.getUserProfile()

                _uiState.update { state ->
                    state.copy(
                        showTimedRankingDialog = true,
                        timedScore = score,
                        userProfile = userProfile
                    )
                }
                analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_DIALOG_SAVE_SCORE)
            }
        }
    }

    /**
     * Save timed score to ranking
     */
    fun saveTimedScore(nickname: String, imageBase64: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingTimedScore = true) }

            // Save profile data locally
            progressionManager.saveUserProfile(nickname, imageBase64)

            // Create user and save to ranking
            val user = User(
                name = nickname,
                score = _uiState.value.timedScore,
                userImage = imageBase64,
                timestamp = System.currentTimeMillis()
            )

            saveTopScore(user, RankingMode.TIMED)

            _uiState.update { state ->
                state.copy(
                    isSavingTimedScore = false,
                    showTimedRankingDialog = false
                )
            }

            _events.emit(ResultEvent.TimedScoreSaved)
        }
    }

    /**
     * Dismiss the timed ranking dialog
     */
    fun dismissTimedRankingDialog() {
        _uiState.update { it.copy(showTimedRankingDialog = false) }
    }

}
