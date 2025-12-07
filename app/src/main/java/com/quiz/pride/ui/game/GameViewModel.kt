package com.quiz.pride.ui.game

import androidx.lifecycle.viewModelScope
import com.quiz.domain.Pride
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.utils.Constants.TOTAL_PRIDES
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetPrideById
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Consolidated UI State for Game Screen
 */
data class GameUiState(
    val isLoading: Boolean = true,
    val question: Pride? = null,
    val options: List<Pride> = emptyList(),
    val correctOptionIndex: Int = -1,
    val showBannerAd: Boolean = true,
    val showRewardedAd: Boolean = false
)

/**
 * One-time events for Game Screen
 */
sealed class GameEvent {
    object NavigateToResult : GameEvent()
    object ShowExtraLifeDialog : GameEvent()
    object PlaySuccessSound : GameEvent()
    object PlayFailSound : GameEvent()
}

class GameViewModel(
    private val getPrideById: GetPrideById,
    private val getPaymentDone: GetPaymentDone
) : ComposeViewModel() {

    private var randomCountries = mutableListOf<Int>()
    private lateinit var currentPride: Pride

    // Consolidated UI State
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // One-time events
    private val _events = MutableSharedFlow<GameEvent>()
    val events = _events.asSharedFlow()

    init {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_GAME)
        _uiState.value = _uiState.value.copy(
            showBannerAd = !getPaymentDone()
        )
        generateNewStage()
    }

    fun generateNewStage() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Generate question
            val numRandomMain = generateRandomWithExclusion(TOTAL_PRIDES, randomCountries)
            randomCountries.add(numRandomMain)

            currentPride = getPrideById.invoke(numRandomMain)

            // Generate response options
            val correctPosition = (0..3).random()
            val usedIds = mutableListOf(numRandomMain)
            val optionList = mutableListOf<Pride>()

            for (i in 0..3) {
                if (i == correctPosition) {
                    optionList.add(currentPride)
                } else {
                    val randomId = generateRandomWithExclusion(TOTAL_PRIDES, usedIds)
                    usedIds.add(randomId)
                    optionList.add(getPrideById.invoke(randomId))
                }
            }

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    question = currentPride,
                    options = optionList,
                    correctOptionIndex = correctPosition
                )
            }
        }
    }

    fun showRewardedAd() {
        if (!getPaymentDone()) {
            _uiState.update { it.copy(showRewardedAd = true) }
        }
    }

    fun onRewardedAdShown() {
        _uiState.update { it.copy(showRewardedAd = false) }
    }

    fun navigateToResult(points: String) {
        AnalyticsManager.analyticsGameFinished(points)
        viewModelScope.launch {
            _events.emit(GameEvent.NavigateToResult)
        }
    }

    fun navigateToExtraLifeDialog() {
        viewModelScope.launch {
            if (!getPaymentDone()) {
                _events.emit(GameEvent.ShowExtraLifeDialog)
            } else {
                _events.emit(GameEvent.NavigateToResult)
            }
        }
    }

    fun playSuccessSound() {
        viewModelScope.launch {
            _events.emit(GameEvent.PlaySuccessSound)
        }
    }

    fun playFailSound() {
        viewModelScope.launch {
            _events.emit(GameEvent.PlayFailSound)
        }
    }

    fun getCurrentPride(): Pride = currentPride

    fun getCorrectOptionIndex(): Int = _uiState.value.correctOptionIndex

    private fun generateRandomWithExclusion(max: Int, exclude: List<Int>): Int {
        var num = (0..max).random()
        while (exclude.contains(num)) {
            num = (0..max).random()
        }
        return num
    }
}
