package com.quiz.pride.ui.select

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.quiz.domain.StreakState
import com.quiz.domain.cosmetics.CurrencyBalance
import com.quiz.domain.reward.DailyReward
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.CurrencyManager
import com.quiz.pride.managers.DailyRewardManager
import com.quiz.pride.managers.ProgressionManager
import com.quiz.pride.managers.SeasonalEventManager
import com.quiz.pride.managers.StreakManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class SelectUiState(
    val streakState: StreakState = StreakState(),
    val isStreakAtRisk: Boolean = false,
    val hasPlayedToday: Boolean = false,
    val isLoadingStreak: Boolean = true,
    val isStreakWidgetDismissed: Boolean = false,
    val balance: CurrencyBalance = CurrencyBalance(),
    val dailyReward: DailyReward? = null,
    val isClaimingDailyReward: Boolean = false,
    val isDailyRewardDismissed: Boolean = false,
    val isPrideMonth: Boolean = false,
    val prideMultiplier: Float = 1.0f,
    val prideDaysRemaining: Int = 0
)

class SelectViewModel(
    private val analyticsManager: AnalyticsManager,
    private val streakManager: StreakManager,
    private val currencyManager: CurrencyManager,
    private val dailyRewardManager: DailyRewardManager,
    private val progressionManager: ProgressionManager,
    private val seasonalEventManager: SeasonalEventManager
) : ComposeViewModel() {

    private val _uiState = MutableStateFlow(SelectUiState())
    val uiState: StateFlow<SelectUiState> = _uiState.asStateFlow()

    init {
        analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT)
        loadStreakData()
        observeBalance()
        observeDailyReward()
        observeDailyRewardDismissed()
        observeStreakWidgetDismissed()
        loadSeasonalEvent()
    }

    private fun observeStreakWidgetDismissed() {
        viewModelScope.launch {
            streakManager.observeIsWidgetDismissedToday().collect { dismissed ->
                _uiState.update { it.copy(isStreakWidgetDismissed = dismissed) }
            }
        }
    }

    /**
     * Oculta el widget de racha tras un swipe-to-dismiss. Persiste hasta el
     * proximo dia. Si la racha esta en riesgo, el dismiss se ignora porque
     * el aviso es critico para evitar perderla.
     */
    fun dismissStreakWidget() {
        if (_uiState.value.isStreakAtRisk) return
        viewModelScope.launch { streakManager.dismissWidgetToday() }
    }

    private fun observeDailyRewardDismissed() {
        viewModelScope.launch {
            dailyRewardManager.observeIsDismissedToday().collect { dismissed ->
                _uiState.update { it.copy(isDailyRewardDismissed = dismissed) }
            }
        }
    }

    /**
     * Oculta la recompensa del dia tras un swipe-to-dismiss en el Card.
     * Solo tiene efecto cuando la recompensa ya esta reclamada; el estado se
     * restablece automaticamente al cambiar la fecha (nuevo dia).
     */
    fun dismissDailyReward() {
        if (_uiState.value.dailyReward?.isClaimed != true) return
        viewModelScope.launch { dailyRewardManager.dismissTodayReward() }
    }

    private fun observeBalance() {
        viewModelScope.launch {
            currencyManager.observeBalance().collect { balance ->
                _uiState.update { it.copy(balance = balance) }
            }
        }
    }

    fun loadStreakData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStreak = true) }

            val streakState = streakManager.getStreakState()
            val isAtRisk = streakManager.isStreakAtRisk()
            val hasPlayedToday = streakManager.hasPlayedToday()

            _uiState.update {
                it.copy(
                    streakState = streakState,
                    isStreakAtRisk = isAtRisk,
                    hasPlayedToday = hasPlayedToday,
                    isLoadingStreak = false
                )
            }
        }
    }

    private fun observeDailyReward() {
        viewModelScope.launch {
            // Paso 1: sembrar el state con el valor real antes del primer collect.
            // Esto evita que la UI muestre el placeholder no-clickable en el frame
            // inicial (si el Flow del DataStore emite null por timing de escritura).
            val initial = dailyRewardManager.getTodayReward()
            _uiState.update { it.copy(dailyReward = initial) }

            // Paso 2: seguir cambios del DataStore (p. ej. isClaimed al reclamar).
            // Se filtran los nulls transitorios del map del manager.
            dailyRewardManager.observeTodayReward()
                .filterNotNull()
                .collect { reward ->
                    _uiState.update { it.copy(dailyReward = reward) }
                }
        }
    }

    private fun loadSeasonalEvent() {
        val isPride = seasonalEventManager.isPrideMonth()
        _uiState.update {
            it.copy(
                isPrideMonth = isPride,
                prideMultiplier = seasonalEventManager.xpMultiplier(),
                prideDaysRemaining = seasonalEventManager.daysRemaining()
            )
        }
    }

    /**
     * Reclama la recompensa diaria. Acredita XP via ProgressionManager y
     * coins/gems via CurrencyManager.
     *
     * isClaimingDailyReward protege contra doble-tap: el Card observa este flag
     * para deshabilitarse mientras la coroutine acredita premios. El Flow de
     * observeTodayReward() refrescara el state cuando el DataStore marque la
     * recompensa como claimed.
     */
    fun claimDailyReward() {
        if (_uiState.value.isClaimingDailyReward) return
        if (_uiState.value.dailyReward?.isClaimed == true) return

        _uiState.update { it.copy(isClaimingDailyReward = true) }
        viewModelScope.launch {
            try {
                val claimed = dailyRewardManager.claimTodayReward() ?: return@launch

                if (claimed.xpAmount > 0) progressionManager.addXp(claimed.xpAmount.toLong())
                if (claimed.coinsAmount > 0) currencyManager.earnCoins(claimed.coinsAmount, source = "daily_reward")
                if (claimed.gemsAmount > 0) currencyManager.earnGems(claimed.gemsAmount, source = "daily_reward")
            } finally {
                _uiState.update { it.copy(isClaimingDailyReward = false) }
            }
        }
    }
}
