package com.quiz.pride.ui.result

import arrow.core.getOrElse
import androidx.lifecycle.viewModelScope
import com.quiz.domain.Achievement
import com.quiz.domain.App
import com.quiz.domain.GameMode
import com.quiz.domain.GameResult
import com.quiz.domain.User
import com.quiz.domain.UserProfile
import com.quiz.domain.XpGainResult
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AdFrequencyManager
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.ProgressionManager
import com.quiz.pride.utils.Constants
import com.quiz.pride.utils.Constants.TOP_RANKING_LIMIT
import com.quiz.usecases.GetAppsRecommended
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetPersonalRecord
import com.quiz.usecases.GetRecordScore
import com.quiz.usecases.ProcessGameResultUseCase
import com.quiz.usecases.RankingMode
import com.quiz.usecases.SaveTopScore
import com.quiz.usecases.SetPersonalRecord
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    // World record dialog state
    val showWorldRecordDialog: Boolean = false,
    val worldRecordPoints: Int = 0,
    val isSavingWorldRecord: Boolean = false,
    // Timed ranking dialog state
    val showTimedRankingDialog: Boolean = false,
    val timedScore: Int = 0,
    val userProfile: UserProfile = UserProfile(),
    val isSavingTimedScore: Boolean = false,
    // Ad state
    val hasPaid: Boolean = false
)

sealed class ResultEvent {
    data object ShowInterstitialAd : ResultEvent()
}

class ResultViewModel(
    private val getAppsRecommended: GetAppsRecommended,
    private val saveTopScore: SaveTopScore,
    private val getRecordScore: GetRecordScore,
    private val getPersonalRecord: GetPersonalRecord,
    private val setPersonalRecord: SetPersonalRecord,
    private val getPaymentDone: GetPaymentDone,
    private val processGameResult: ProcessGameResultUseCase,
    private val progressionManager: ProgressionManager,
    private val analyticsManager: AnalyticsManager,
    private val adFrequencyManager: AdFrequencyManager
) : ComposeViewModel() {

    // Flag para garantizar idempotencia: onScreenInitialized solo ejecuta logica una vez
    private var initialized = false

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ResultEvent>()
    val events = _events.asSharedFlow()

    init {
        analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_RESULT)
        _uiState.update { it.copy(hasPaid = getPaymentDone()) }
        loadData()
    }

    /**
     * Debe llamarse una sola vez al cargar la pantalla.
     * Registra el juego completado en AdFrequencyManager y emite ShowInterstitialAd
     * si corresponde segun la frecuencia configurada.
     */
    fun onScreenLoaded() {
        viewModelScope.launch {
            adFrequencyManager.recordGameCompleted()
            if (!getPaymentDone() && adFrequencyManager.shouldShowInterstitial()) {
                _events.emit(ResultEvent.ShowInterstitialAd)
            }
        }
    }

    /**
     * Debe llamarse cuando el interstitial fue mostrado exitosamente.
     */
    fun onInterstitialShown() {
        viewModelScope.launch {
            adFrequencyManager.recordInterstitialShown()
        }
    }

    /**
     * Punto de entrada unico para inicializar la pantalla de resultado.
     * Es idempotente: solo ejecuta la logica la primera vez que se llama.
     * Agrupa: ad frequency tracking, XP/achievements, y timed ranking check.
     */
    fun onScreenInitialized(
        gameType: Constants.GameType,
        points: Int,
        totalQuestions: Int,
        correctAnswers: Int,
        bestStreak: Int,
        timePlayed: Long
    ) {
        if (initialized) return
        initialized = true

        // Ad frequency tracking
        onScreenLoaded()

        // XP, achievements y estadisticas
        val gameMode = when (gameType) {
            Constants.GameType.NORMAL -> GameMode.NORMAL
            Constants.GameType.ADVANCE -> GameMode.ADVANCE
            Constants.GameType.EXPERT -> GameMode.EXPERT
            Constants.GameType.TIMED -> GameMode.TIMED
        }
        recordGameResult(
            gameMode = gameMode,
            correctAnswers = correctAnswers,
            totalQuestions = totalQuestions,
            bestStreak = bestStreak,
            timePlayedMs = timePlayed,
            completedAllQuestions = totalQuestions >= Constants.TOTAL_PRIDES
        )

        // Check personal record para todos los modos
        checkPersonalRecord(points)

        // Check world record para modo NORMAL, timed ranking para TIMED
        if (gameType == Constants.GameType.TIMED) {
            checkTimedRanking(points)
        } else {
            checkWorldRecord(points)
        }
    }

    /**
     * Procesa el resultado del juego: stats, XP, logros y sincronizacion remota.
     * Delega en ProcessGameResultUseCase para mantener el ViewModel liviano.
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

            val processed = processGameResult(result)

            _uiState.update { state ->
                state.copy(
                    xpGainResult = processed.xpGainResult,
                    newAchievements = processed.newAchievements,
                    showLevelUpDialog = processed.xpGainResult.leveledUp
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

            val appsDeferred = async { getAppsRecommended.invoke() }
            val worldRecordDeferred = async { getRecordScore(1) }

            // getOrElse: degradacion graceful — si falla, muestra lista/valor vacio
            val apps = appsDeferred.await().getOrElse { emptyList() }
            val worldRecord = worldRecordDeferred.await().getOrElse { "0" }

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
            // Si falla, no mostrar el dialog — mejor degradar que crashear
            val pointsLastClassified = getRecordScore(50).getOrElse { return@launch }
            if (pointsLastClassified.isNotEmpty() && gamePoints > pointsLastClassified.toInt()) {
                val userProfile = progressionManager.getUserProfile()
                analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_DIALOG_SAVE_SCORE)
                _uiState.update { state ->
                    state.copy(
                        showWorldRecordDialog = true,
                        worldRecordPoints = gamePoints,
                        userProfile = userProfile
                    )
                }
            }
        }
    }

    fun saveScore(nickname: String, imageBase64: String) {
        analyticsManager.analyticsScoreSaved("world_record", true)
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingWorldRecord = true) }
            progressionManager.saveUserProfile(nickname, imageBase64)
            val user = User(
                name = nickname,
                score = _uiState.value.worldRecordPoints,
                userImage = imageBase64,
                timestamp = System.currentTimeMillis()
            )
            saveTopScore(user)
            _uiState.update { it.copy(isSavingWorldRecord = false, showWorldRecordDialog = false) }
        }
    }

    fun onWorldRecordDialogDismissed() {
        analyticsManager.analyticsScoreSaved("world_record", false)
        _uiState.update { it.copy(showWorldRecordDialog = false) }
    }

    fun setPhotoUrl(url: String) {
        _uiState.update { it.copy(photoUrl = url) }
    }

    /**
     * Check if the timed mode score qualifies for top 20
     */
    fun checkTimedRanking(score: Int) {
        viewModelScope.launch {
            // Si falla la consulta, asumir que califica (beneficio de la duda al usuario)
            val position20Score = getRecordScore(TOP_RANKING_LIMIT, RankingMode.TIMED).getOrElse { "" }
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
        analyticsManager.analyticsScoreSaved("timed_ranking", true)
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
        }
    }

    /**
     * Dismiss the timed ranking dialog
     */
    fun dismissTimedRankingDialog() {
        analyticsManager.analyticsScoreSaved("timed_ranking", false)
        _uiState.update { it.copy(showTimedRankingDialog = false) }
    }

    /**
     * Trackea el click en el boton de calificar la app
     */
    fun onRateClicked() {
        analyticsManager.analyticsClicked(AnalyticsManager.BTN_RATE)
    }

    fun onPlayAgainClicked() {
        analyticsManager.analyticsClicked(AnalyticsManager.BTN_PLAY_AGAIN)
    }

    fun onRankingClicked() {
        analyticsManager.analyticsClicked(AnalyticsManager.BTN_RANKING)
    }

}
