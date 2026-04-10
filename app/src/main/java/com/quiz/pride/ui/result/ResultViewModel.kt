package com.quiz.pride.ui.result

import arrow.core.getOrElse
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.compose.runtime.Immutable
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
import com.quiz.pride.managers.GameStatsManager
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

@Immutable
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
    val hasPaid: Boolean = false,
    // Puntos mostrados al usuario (pueden ser duplicados por ad recompensado)
    val displayedPoints: Int = 0
)

sealed class ResultEvent {
    data object ShowInterstitialAd : ResultEvent()
    data class SaveScoreResult(val success: Boolean, val message: String = "") : ResultEvent()
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
    private val gameStatsManager: GameStatsManager,
    private val analyticsManager: AnalyticsManager,
    private val adFrequencyManager: AdFrequencyManager,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ComposeViewModel() {

    // Fix 5: initialized persiste en SavedStateHandle para sobrevivir process death
    // y evitar que recordGameResult contabilice XP dos veces tras recreacion.
    private var initialized: Boolean
        get() = savedStateHandle.get<Boolean>("initialized") ?: false
        set(value) { savedStateHandle["initialized"] = value }

    // Fix 3: flag volatil para proteger contra double-submit antes de recomposicion
    @Volatile private var isSaveInProgress = false

    // Modo de juego actual, guardado para usarlo al construir el User al guardar score
    private var currentGameType: Constants.GameType = Constants.GameType.NORMAL

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
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun onScreenLoaded() {
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

        // Guardar el tipo de juego para usarlo al construir el User al guardar score
        currentGameType = gameType

        // Fix 2: inicializar displayedPoints en el uiState para que saveScore use el valor correcto
        _uiState.update { it.copy(displayedPoints = points) }

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
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun recordGameResult(
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

            // Actualizar user properties para segmentacion en Firebase
            val xp = processed.xpGainResult
            val accuracy = if (totalQuestions > 0) (correctAnswers.toFloat() / totalQuestions * 100) else 0f
            val totalGamesHistorical = gameStatsManager.getStatistics().totalGamesPlayed
            analyticsManager.updateUserProperties(
                level = xp.newLevel,
                totalGames = totalGamesHistorical,
                accuracy = accuracy,
                favoriteMode = gameMode.name
            )
        }
    }

    fun dismissLevelUpDialog() {
        _uiState.update { it.copy(showLevelUpDialog = false) }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val appsDeferred = async { getAppsRecommended.invoke() }
            // Fix 1: pasar el gameMode actual para mostrar el record del modo correcto.
            // En este punto currentGameType puede ser el default (NORMAL) si loadData se llama
            // antes de onScreenInitialized, pero se actualiza correctamente una vez inicializado.
            val worldRecordDeferred = async {
                getRecordScore(1, gameMode = currentGameType.name)
            }

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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun checkPersonalRecord(points: Int) {
        // Fix 2: separar record personal por modo de juego para que cada modo compita
        // solo contra si mismo. String vacio hubiera usado la clave legacy global.
        val gameMode = currentGameType.name
        val currentRecord = getPersonalRecord.invoke(gameMode)
        if (points > currentRecord) {
            setPersonalRecord.invoke(points, gameMode)
            _uiState.update { it.copy(personalRecord = points.toString()) }
        } else {
            _uiState.update { it.copy(personalRecord = currentRecord.toString()) }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun checkWorldRecord(gamePoints: Int) {
        viewModelScope.launch {
            // Fix 1: filtrar por gameMode para que NORMAL solo compita contra NORMAL,
            // ADVANCE contra ADVANCE, EXPERT contra EXPERT.
            // El indice compuesto (gameMode ASC, score DESC) en Firestore soporta este query.
            val pointsLastClassified = getRecordScore(50, gameMode = currentGameType.name).getOrElse { return@launch }
            if (pointsLastClassified.isNotEmpty() && gamePoints > (pointsLastClassified.toIntOrNull() ?: return@launch)) {
                val userProfile = progressionManager.getUserProfile()
                analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_DIALOG_SAVE_SCORE)
                analyticsManager.analyticsRankingQualified("world_record", gamePoints, "top_50")
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

    private fun com.quiz.data.repository.RepositoryException.toUserMessage(): String = when (this) {
        is com.quiz.data.repository.RepositoryException.NoConnectionException -> "Sin conexion a internet"
        is com.quiz.data.repository.RepositoryException.DataNotFoundException -> "Error al guardar el puntaje"
        else -> "Error inesperado"
    }

    private fun saveScoreInternal(
        nickname: String,
        imageBase64: String,
        rankingMode: RankingMode,
        rankingType: String
    ) {
        if (isSaveInProgress) return
        isSaveInProgress = true

        val currentPoints = _uiState.value.displayedPoints
        val gameMode = if (rankingMode == RankingMode.TIMED) Constants.GameType.TIMED.name else currentGameType.name
        analyticsManager.analyticsSaveScoreAttempt(rankingType, currentPoints, currentPoints != _uiState.value.worldRecordPoints)

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingWorldRecord = true) }
            progressionManager.saveUserProfile(nickname, imageBase64)
            val user = User(
                name = nickname,
                score = currentPoints,
                userImage = imageBase64,
                gameMode = gameMode,
                timestamp = System.currentTimeMillis()
            )
            saveTopScore(user, rankingMode).fold(
                ifLeft = { error ->
                    val msg = error.toUserMessage()
                    analyticsManager.analyticsSaveScoreResult(rankingType, currentPoints, false, msg)
                    _uiState.update { it.copy(isSavingWorldRecord = false) }
                    _events.emit(ResultEvent.SaveScoreResult(success = false, message = msg))
                },
                ifRight = {
                    analyticsManager.analyticsScoreSaved(rankingType, true)
                    analyticsManager.analyticsSaveScoreResult(rankingType, currentPoints, true)
                    _uiState.update { it.copy(
                        isSavingWorldRecord = false,
                        showWorldRecordDialog = false,
                        showTimedRankingDialog = false
                    )}
                    _events.emit(ResultEvent.SaveScoreResult(success = true))
                }
            )
            isSaveInProgress = false
        }
    }

    fun saveScore(nickname: String, imageBase64: String) =
        saveScoreInternal(nickname, imageBase64, RankingMode.NORMAL, "world_record")

    fun onWorldRecordDialogDismissed() {
        analyticsManager.analyticsScoreDialogDismissed("world_record")
        _uiState.update { it.copy(showWorldRecordDialog = false) }
    }

    fun setPhotoUrl(url: String) {
        _uiState.update { it.copy(photoUrl = url) }
    }

    /**
     * Check if the timed mode score qualifies for top 20
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun checkTimedRanking(score: Int) {
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
                analyticsManager.analyticsRankingQualified("timed", score, "top_$TOP_RANKING_LIMIT")
            }
        }
    }

    fun saveTimedScore(nickname: String, imageBase64: String) =
        saveScoreInternal(nickname, imageBase64, RankingMode.TIMED, "timed_ranking")

    private var pointsAlreadyDoubled = false

    /**
     * Fix 2 + Fix 4: duplica los puntos mostrados en la UI, actualiza el score a guardar
     * y el record personal en DataStore (para que el record refleje el valor real).
     * Protected against multiple invocations to prevent exponential score inflation.
     */
    fun onPointsDoubled() {
        if (pointsAlreadyDoubled) return
        pointsAlreadyDoubled = true

        val original = _uiState.value.displayedPoints
        val doubled = original * 2
        analyticsManager.analyticsPointsDoubled(original, doubled, currentGameType.name)
        _uiState.update { it.copy(
            displayedPoints = doubled,
            worldRecordPoints = doubled,
            timedScore = doubled
        )}
        checkPersonalRecord(doubled)
    }

    /**
     * Dismiss the timed ranking dialog
     */
    fun dismissTimedRankingDialog() {
        analyticsManager.analyticsScoreDialogDismissed("timed_ranking")
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

    fun onShareClicked() {
        analyticsManager.analyticsClicked(AnalyticsManager.BTN_SHARE)
    }

}
