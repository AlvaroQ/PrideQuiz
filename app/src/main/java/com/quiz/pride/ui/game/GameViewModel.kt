package com.quiz.pride.ui.game

import androidx.lifecycle.viewModelScope
import com.quiz.domain.Pride
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AnalyticsManager
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val streakMessage: String = ""
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
    object PlaySuccessSound : GameEvent()
    object PlayFailSound : GameEvent()
}

class GameViewModel(
    private val getPrideById: GetPrideById,
    private val getPaymentDone: GetPaymentDone,
    private val analyticsManager: AnalyticsManager
) : ComposeViewModel() {

    private var randomCountries = mutableListOf<Int>()
    private var startTime = System.currentTimeMillis()
    private var timerJob: Job? = null

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>()
    val events = _events.asSharedFlow()

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

            val numRandomMain = generateRandomWithExclusion(TOTAL_PRIDES, randomCountries)
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
            val allPrides = allIds.map { id -> async { getPrideById.invoke(id) } }.awaitAll()

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
        }
    }

    fun onAnswerSelected(index: Int, streakOnFire: String, streakUnstoppable: String, streakLegendary: String, streakComboFormat: String) {
        val state = _uiState.value
        if (state.selectedAnswer != null) return

        val isCorrect = index == state.correctOptionIndex

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
                        _uiState.update { it.copy(showExtraLifeDialog = true) }
                    } else {
                        emitNavigateToResult()
                    }
                }
                currentState.stage >= TOTAL_PRIDES || (!currentState.isTimedMode && currentState.lives < 1) -> {
                    emitNavigateToResult()
                }
                else -> {
                    _uiState.update { it.copy(stage = it.stage + 1) }
                    generateNewStage()
                }
            }
        }
    }

    fun onExtraLifeAccepted() {
        _uiState.update {
            it.copy(
                showExtraLifeDialog = false,
                lives = 1,
                extraLivesUsed = it.extraLivesUsed + 1
            )
        }
        generateNewStage()
    }

    fun onExtraLifeDeclined() {
        _uiState.update { it.copy(showExtraLifeDialog = false) }
        viewModelScope.launch { emitNavigateToResult() }
    }

    fun showExitDialog() {
        _uiState.update { it.copy(showExitDialog = true) }
    }

    fun dismissExitDialog() {
        _uiState.update { it.copy(showExitDialog = false) }
    }

    private suspend fun emitNavigateToResult() {
        val state = _uiState.value
        analyticsManager.analyticsGameFinished(state.points.toString())
        timerJob?.cancel()
        val timePlayed = System.currentTimeMillis() - startTime
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

    private fun generateRandomWithExclusion(max: Int, exclude: List<Int>): Int {
        var num = (0..max).random()
        while (exclude.contains(num)) {
            num = (0..max).random()
        }
        return num
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
