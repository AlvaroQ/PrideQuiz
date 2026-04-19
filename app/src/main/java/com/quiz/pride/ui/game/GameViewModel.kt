package com.quiz.pride.ui.game

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.quiz.domain.Pride
import com.quiz.domain.challenge.ChallengeEvent
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.DailyChallengeManager
import com.quiz.pride.managers.ThemeManager
import com.quiz.pride.utils.Constants
import com.quiz.pride.utils.Constants.TOTAL_PRIDES
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetPrideById
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections

@Immutable
data class GameUiState(
    val isLoading: Boolean = true,
    val question: Pride? = null,
    val options: List<Pride> = emptyList(),
    val correctOptionIndex: Int = -1,
    val showBannerAd: Boolean = true,
    val showRewardedAd: Boolean = false,
    // Game state
    val points: Int = 0,
    val lives: Int = 3,
    val stage: Int = 1,
    val correctAnswers: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val extraLivesUsed: Int = 0,
    val selectedAnswer: Int? = null,
    val showExtraLifeDialog: Boolean = false,
    val showExitDialog: Boolean = false,
    // Timed mode
    val timeRemaining: Int = Constants.TIMED_MODE_TOTAL_SECONDS,
    val isTimedMode: Boolean = false,
    // Streak effects
    val showStreakEffect: Boolean = false,
    val streakMessage: String = "",
    // Error state
    val hasError: Boolean = false
) {
    val maxExtraLives: Int get() = 2
}

sealed class GameEvent {
    data class NavigateToResult(
        val points: Int,
        val stage: Int,
        val correctAnswers: Int,
        val bestStreak: Int,
        val timePlayed: Long
    ) : GameEvent()
    data object PlaySuccessSound : GameEvent()
    data object PlayFailSound : GameEvent()
}

class GameViewModel(
    private val getPrideById: GetPrideById,
    private val getPaymentDone: GetPaymentDone,
    private val analyticsManager: AnalyticsManager,
    private val themeManager: ThemeManager,
    private val savedStateHandle: SavedStateHandle,
    private val dailyChallengeManager: DailyChallengeManager
) : ComposeViewModel() {

    private val randomCountries: MutableSet<Int> = Collections.synchronizedSet(mutableSetOf())
    private var startTime = System.currentTimeMillis()
    private var questionStartTime = System.currentTimeMillis()
    private var currentGameMode: String = "NORMAL"
    private var timerJob: Job? = null

    // Restaurar estado critico del juego tras process death
    private val savedPoints = savedStateHandle.get<Int>("points") ?: 0
    private val savedLives = savedStateHandle.get<Int>("lives") ?: 3
    private val savedStage = savedStateHandle.get<Int>("stage") ?: 0
    private val savedCorrectAnswers = savedStateHandle.get<Int>("correctAnswers") ?: 0
    private val savedBestStreak = savedStateHandle.get<Int>("bestStreak") ?: 0

    private val _uiState = MutableStateFlow(
        GameUiState(
            points = savedPoints,
            lives = savedLives,
            stage = if (savedStage > 0) savedStage else 1,
            correctAnswers = savedCorrectAnswers,
            bestStreak = savedBestStreak
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    // Expuesto como StateFlow para que el composable lo observe sin koinInject
    val isSoundEnabled: StateFlow<Boolean> = themeManager.isSoundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /**
     * Persiste los campos criticos del juego en SavedStateHandle.
     * Se llama despues de cada update que modifique puntos, vidas, stage, etc.
     * Permite recuperar el estado tras un process death del sistema.
     */
    private fun saveState() {
        val state = _uiState.value
        savedStateHandle["points"] = state.points
        savedStateHandle["lives"] = state.lives
        savedStateHandle["stage"] = state.stage
        savedStateHandle["correctAnswers"] = state.correctAnswers
        savedStateHandle["bestStreak"] = state.bestStreak
    }

    init {
        analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_GAME)
        _uiState.update { it.copy(showBannerAd = !getPaymentDone()) }
    }

    fun initGame(gameType: Constants.GameType) {
        val isTimedMode = gameType == Constants.GameType.TIMED
        _uiState.update {
            it.copy(
                lives = if (isTimedMode) Int.MAX_VALUE else 3,
                isTimedMode = isTimedMode,
                timeRemaining = Constants.TIMED_MODE_TOTAL_SECONDS
            )
        }
        currentGameMode = gameType.name
        analyticsManager.analyticsGameModeSelected(gameType.name)
        startTime = System.currentTimeMillis()
        if (isTimedMode) startTimer()
        generateNewStage()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemaining > 0) {
                delay(1000)
                _uiState.update { it.copy(timeRemaining = it.timeRemaining - 1) }
            }
            // Time's up
            _events.emit(GameEvent.PlayFailSound)
            emitNavigateToResult()
        }
    }

    fun generateNewStage() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val excludeSnapshot = randomCountries.toSet()
            val numRandomMain = generateRandomWithExclusion(TOTAL_PRIDES, excludeSnapshot)
            randomCountries.add(numRandomMain)

            val correctPosition = (0..3).random()
            val usedIds = mutableListOf(numRandomMain)
            val wrongIds = mutableListOf<Int>()
            for (i in 0..3) {
                if (i != correctPosition) {
                    val randomId = generateRandomWithExclusion(TOTAL_PRIDES, usedIds)
                    usedIds.add(randomId)
                    wrongIds.add(randomId)
                }
            }

            val allIds = listOf(numRandomMain) + wrongIds
            val allPridesResults = allIds.map { id -> async { getPrideById.invoke(id) } }.awaitAll()

            // Si algun fetch fallo, mostrar estado de error y no avanzar
            val allPrides = mutableListOf<Pride>()
            for (result in allPridesResults) {
                result.fold(
                    ifLeft = {
                        analyticsManager.analyticsErrorAction("game", "error_shown", "load_error")
                        _uiState.update { state -> state.copy(isLoading = false, hasError = true) }
                        return@launch
                    },
                    ifRight = { pride -> allPrides.add(pride) }
                )
            }

            val optionList = mutableListOf<Pride>()
            var wrongIndex = 0
            for (i in 0..3) {
                if (i == correctPosition) {
                    optionList.add(allPrides[0])
                } else {
                    optionList.add(allPrides[1 + wrongIndex])
                    wrongIndex++
                }
            }

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    question = allPrides[0],
                    options = optionList,
                    correctOptionIndex = correctPosition
                )
            }
            questionStartTime = System.currentTimeMillis()
        }
    }

    fun retryCurrentStage() {
        _uiState.update { it.copy(hasError = false) }
        generateNewStage()
    }

    fun onAnswerSelected(index: Int, streakOnFire: String, streakUnstoppable: String, streakLegendary: String, streakComboFormat: String) {
        val state = _uiState.value
        if (state.selectedAnswer != null) return

        val isCorrect = index == state.correctOptionIndex
        val timeToAnswer = System.currentTimeMillis() - questionStartTime

        // Trackear cada respuesta para análisis de funnel y dificultad
        analyticsManager.analyticsQuestionAnswered(
            questionNumber = state.stage,
            correct = isCorrect,
            timeToAnswerMs = timeToAnswer,
            gameMode = currentGameMode,
            currentStreak = if (isCorrect) state.currentStreak + 1 else 0
        )

        // Notificar al sistema de desafios diarios (fire-and-forget, no bloquea el flujo del juego)
        viewModelScope.launch {
            dailyChallengeManager.processEvent(
                ChallengeEvent.AnswerGiven(
                    isCorrect = isCorrect,
                    responseTimeMs = timeToAnswer
                )
            )
        }

        if (isCorrect) {
            val newStreak = state.currentStreak + 1
            val newBestStreak = maxOf(newStreak, state.bestStreak)

            val (showEffect, message) = when {
                newStreak == 5 -> true to streakOnFire
                newStreak == 10 -> true to streakUnstoppable
                newStreak == 15 -> true to streakLegendary
                newStreak >= 3 && newStreak % 3 == 0 -> true to String.format(streakComboFormat, newStreak)
                else -> false to ""
            }

            _uiState.update {
                it.copy(
                    selectedAnswer = index,
                    points = it.points + 1,
                    correctAnswers = it.correctAnswers + 1,
                    currentStreak = newStreak,
                    bestStreak = newBestStreak,
                    showStreakEffect = showEffect,
                    streakMessage = message
                )
            }
            saveState()
            viewModelScope.launch { _events.emit(GameEvent.PlaySuccessSound) }
        } else {
            val newLives = if (state.isTimedMode) state.lives else state.lives - 1
            _uiState.update {
                it.copy(
                    selectedAnswer = index,
                    lives = newLives,
                    currentStreak = 0
                )
            }
            saveState()
            viewModelScope.launch { _events.emit(GameEvent.PlayFailSound) }
        }

        // Delay before next question
        viewModelScope.launch {
            delay(1000)
            _uiState.update { it.copy(selectedAnswer = null, showStreakEffect = false) }

            val currentState = _uiState.value
            when {
                !currentState.isTimedMode && currentState.lives < 1 && currentState.extraLivesUsed < currentState.maxExtraLives && currentState.stage < TOTAL_PRIDES -> {
                    if (!getPaymentDone()) {
                        analyticsManager.analyticsExtraLife("offered")
                        _uiState.update { it.copy(showExtraLifeDialog = true) }
                    } else {
                        emitNavigateToResult()
                    }
                }
                currentState.stage >= TOTAL_PRIDES || (!currentState.isTimedMode && currentState.lives < 1) || (currentState.isTimedMode && currentState.timeRemaining <= 0) -> {
                    emitNavigateToResult()
                }
                else -> {
                    _uiState.update { it.copy(stage = it.stage + 1) }
                    saveState()
                    generateNewStage()
                }
            }
        }
    }

    fun onExtraLifeAccepted() {
        analyticsManager.analyticsExtraLife("accepted")
        _uiState.update {
            it.copy(
                showExtraLifeDialog = false,
                lives = 1,
                extraLivesUsed = it.extraLivesUsed + 1
            )
        }
        saveState()
        generateNewStage()
    }

    fun onExtraLifeDeclined() {
        analyticsManager.analyticsExtraLife("declined")
        _uiState.update { it.copy(showExtraLifeDialog = false) }
        viewModelScope.launch { emitNavigateToResult() }
    }

    fun showExitDialog() {
        val state = _uiState.value
        analyticsManager.analyticsGameExit(state.stage, state.points)
        _uiState.update { it.copy(showExitDialog = true) }
    }

    fun dismissExitDialog() {
        _uiState.update { it.copy(showExitDialog = false) }
    }

    private suspend fun emitNavigateToResult() {
        val state = _uiState.value
        timerJob?.cancel()
        val timePlayed = System.currentTimeMillis() - startTime

        analyticsManager.analyticsGameCompleted(
            gameMode = currentGameMode,
            questionsAnswered = state.stage,
            correctAnswers = state.correctAnswers,
            points = state.points,
            livesRemaining = if (state.isTimedMode) -1 else state.lives,
            bestStreak = state.bestStreak,
            timePlayedMs = timePlayed,
            usedExtraLife = state.extraLivesUsed > 0
        )
        _events.emit(
            GameEvent.NavigateToResult(
                points = state.points,
                stage = state.stage,
                correctAnswers = state.correctAnswers,
                bestStreak = state.bestStreak,
                timePlayed = timePlayed
            )
        )
    }

    private fun generateRandomWithExclusion(max: Int, exclude: Collection<Int>): Int {
        // Si exclude ya es un Set (caso de randomCountries que es synchronizedSet),
        // reutilizarlo directamente en lugar de convertirlo de nuevo
        val excludeSet = if (exclude is Set) exclude else exclude.toSet()
        val available = (0..max).filter { it !in excludeSet }
        if (available.isEmpty()) {
            // Se agotaron todos los IDs disponibles: resetear y empezar de nuevo
            randomCountries.clear()
            return (0..max).random()
        }
        return available.random()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
