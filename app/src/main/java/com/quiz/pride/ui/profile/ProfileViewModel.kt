package com.quiz.pride.ui.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.quiz.domain.Achievement
import com.quiz.domain.LevelInfo
import com.quiz.domain.PlayerStatistics
import com.quiz.domain.StreakState
import com.quiz.domain.UserProfile
import com.quiz.domain.challenge.ChallengeStats
import com.quiz.domain.challenge.DailyChallengeState
import com.quiz.domain.cosmetics.CurrencyBalance
import com.quiz.domain.cosmetics.PlayerCosmetics
import com.quiz.domain.reward.DailyReward
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AchievementManager
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.CurrencyManager
import com.quiz.pride.managers.DailyChallengeManager
import com.quiz.pride.managers.DailyRewardManager
import com.quiz.pride.managers.GameStatsManager
import com.quiz.pride.managers.ProgressionManager
import com.quiz.pride.managers.StreakManager
import com.quiz.pride.managers.UnlockablesManager
import com.quiz.pride.managers.XpSyncManager
import com.quiz.usecases.GetUserGlobalRank
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ProfileUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile = UserProfile(),
    val isEditingProfile: Boolean = false,
    val levelInfo: LevelInfo? = null,
    val statistics: PlayerStatistics? = null,
    val unlockedAchievements: Set<Achievement> = emptySet(),
    val allAchievements: List<Achievement> = Achievement.entries,
    val globalRank: Int? = null,
    val isLoadingRank: Boolean = false,
    // Datos de racha diaria
    val streakState: StreakState = StreakState(),
    val isStreakAtRisk: Boolean = false,
    val hasPlayedToday: Boolean = false,
    // Estadisticas de desafios diarios
    val challengeStats: ChallengeStats = ChallengeStats(),
    // Desafios activos del dia
    val challengeState: DailyChallengeState = DailyChallengeState(),
    val isLoadingChallenges: Boolean = true,
    // Economia virtual
    val balance: CurrencyBalance = CurrencyBalance(),
    val playerCosmetics: PlayerCosmetics = PlayerCosmetics(),
    // Nombre legible del titulo cosmetico equipado (vacio si es el default)
    val equippedTitleName: String = "",
    // Estado de la recompensa del dia (siempre visible en perfil, incluso si se
    // hizo swipe-to-dismiss en Select).
    val dailyReward: DailyReward? = null
)

class ProfileViewModel(
    private val progressionManager: ProgressionManager,
    private val gameStatsManager: GameStatsManager,
    private val achievementManager: AchievementManager,
    private val xpSyncManager: XpSyncManager,
    private val getUserGlobalRank: GetUserGlobalRank,
    private val analyticsManager: AnalyticsManager,
    private val streakManager: StreakManager,
    private val dailyChallengeManager: DailyChallengeManager,
    private val currencyManager: CurrencyManager,
    private val unlockablesManager: UnlockablesManager,
    private val dailyRewardManager: DailyRewardManager
) : ComposeViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_PROFILE)
        loadProfileData()
        observeBalance()
        observeDailyReward()
    }

    private fun observeDailyReward() {
        viewModelScope.launch {
            val initial = dailyRewardManager.getTodayReward()
            _uiState.update { it.copy(dailyReward = initial) }

            dailyRewardManager.observeTodayReward()
                .filterNotNull()
                .collect { reward ->
                    _uiState.update { it.copy(dailyReward = reward) }
                }
        }
    }

    private fun observeBalance() {
        viewModelScope.launch {
            currencyManager.observeBalance().collect { balance ->
                _uiState.update { it.copy(balance = balance) }
            }
        }
    }

    fun loadProfileData() {
        viewModelScope.launch {
            fetchAndUpdateProfile(showLoading = true)
        }
    }

    /**
     * Logica comun de carga del perfil.
     * @param showLoading true para mostrar estado de carga (carga inicial), false para refresh silencioso
     */
    private suspend fun fetchAndUpdateProfile(showLoading: Boolean) {
        if (showLoading) {
            _uiState.update { it.copy(isLoading = true, isLoadingRank = true) }
        }

        val userProfile = progressionManager.getUserProfile()
        val xp = progressionManager.totalXp.first()
        val levelInfo = progressionManager.getLevelInfo(xp)
        val statistics = gameStatsManager.getStatistics()
        val unlockedAchievements = achievementManager.getUnlockedAchievements()

        // Datos de racha — cargar en paralelo con el resto del perfil
        val streakState = streakManager.getStreakState()
        val isAtRisk = streakManager.isStreakAtRisk()
        val hasPlayedToday = streakManager.hasPlayedToday()

        // Estadisticas de desafios diarios
        val challengeStats = dailyChallengeManager.getChallengeStats()

        // Desafios activos del dia (requieren nivel del jugador para escalado)
        val challengeState = dailyChallengeManager.getDailyChallengeState(levelInfo.level)

        // Cosmeticos equipados del jugador
        val playerCosmetics = unlockablesManager.getPlayerCosmetics()
        val equippedTitleName = if (playerCosmetics.equippedTitle != "title_default") {
            unlockablesManager.getCatalog()
                .find { it.id == playerCosmetics.equippedTitle }
                ?.name
                ?: ""
        } else ""

        _uiState.update { it.copy(
            isLoading = false,
            userProfile = userProfile,
            levelInfo = levelInfo,
            statistics = statistics,
            unlockedAchievements = unlockedAchievements,
            streakState = streakState,
            isStreakAtRisk = isAtRisk,
            hasPlayedToday = hasPlayedToday,
            challengeStats = challengeStats,
            challengeState = challengeState,
            isLoadingChallenges = false,
            playerCosmetics = playerCosmetics,
            equippedTitleName = equippedTitleName
        ) }

        loadGlobalRank(xp)
    }

    private fun loadGlobalRank(currentXp: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRank = true) }

            val uid = xpSyncManager.getCurrentUserId()
            if (uid != null && currentXp > 0) {
                getUserGlobalRank(uid, currentXp).fold(
                    ifLeft = {
                        _uiState.update { it.copy(globalRank = null, isLoadingRank = false) }
                    },
                    ifRight = { rank ->
                        _uiState.update { it.copy(globalRank = rank, isLoadingRank = false) }
                    }
                )
            } else {
                _uiState.update { it.copy(globalRank = null, isLoadingRank = false) }
            }
        }
    }

    fun setEditingProfile(editing: Boolean) {
        _uiState.update { it.copy(isEditingProfile = editing) }
    }

    fun saveNickname(nickname: String) {
        viewModelScope.launch {
            progressionManager.saveNickname(nickname)
            _uiState.update { it.copy(
                userProfile = it.userProfile.copy(nickname = nickname)
            ) }
        }
    }

    fun saveUserImage(imageBase64: String) {
        viewModelScope.launch {
            progressionManager.saveUserImage(imageBase64)
            _uiState.update { it.copy(
                userProfile = it.userProfile.copy(imageBase64 = imageBase64)
            ) }
        }
    }

    fun saveUserProfile(nickname: String, imageBase64: String) {
        viewModelScope.launch {
            progressionManager.saveUserProfile(nickname, imageBase64)
            _uiState.update { it.copy(
                userProfile = UserProfile(nickname, imageBase64),
                isEditingProfile = false
            ) }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            fetchAndUpdateProfile(showLoading = false)
        }
    }
}
