package com.quiz.pride.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quiz.pride.managers.Achievement
import com.quiz.pride.managers.LevelInfo
import com.quiz.pride.managers.PlayerStatistics
import com.quiz.pride.managers.ProgressionManager
import com.quiz.pride.managers.UserProfile
import com.quiz.pride.managers.XpSyncManager
import com.quiz.usecases.GetUserGlobalRank
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile = UserProfile(),
    val isEditingProfile: Boolean = false,
    val levelInfo: LevelInfo? = null,
    val statistics: PlayerStatistics? = null,
    val unlockedAchievements: Set<Achievement> = emptySet(),
    val allAchievements: List<Achievement> = Achievement.entries,
    val globalRank: Int? = null,
    val isLoadingRank: Boolean = false
)

class ProfileViewModel(
    private val progressionManager: ProgressionManager,
    private val xpSyncManager: XpSyncManager,
    private val getUserGlobalRank: GetUserGlobalRank
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isLoadingRank = true)

            // Load user profile first
            val userProfile = progressionManager.getUserProfile()

            // Get total XP and level info
            progressionManager.totalXp.collect { xp ->
                val levelInfo = progressionManager.getLevelInfo(xp)
                val statistics = progressionManager.getStatistics()
                val unlockedAchievements = progressionManager.getUnlockedAchievements()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userProfile = userProfile,
                    levelInfo = levelInfo,
                    statistics = statistics,
                    unlockedAchievements = unlockedAchievements
                )

                // Load global rank
                loadGlobalRank(xp)
            }
        }
    }

    private fun loadGlobalRank(currentXp: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRank = true)

            val uid = xpSyncManager.getCurrentUserId()
            if (uid != null && currentXp > 0) {
                getUserGlobalRank.invoke(uid, currentXp).fold(
                    ifLeft = {
                        _uiState.value = _uiState.value.copy(
                            globalRank = null,
                            isLoadingRank = false
                        )
                    },
                    ifRight = { rank ->
                        _uiState.value = _uiState.value.copy(
                            globalRank = rank,
                            isLoadingRank = false
                        )
                    }
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    globalRank = null,
                    isLoadingRank = false
                )
            }
        }
    }

    fun setEditingProfile(editing: Boolean) {
        _uiState.value = _uiState.value.copy(isEditingProfile = editing)
    }

    fun saveNickname(nickname: String) {
        viewModelScope.launch {
            progressionManager.saveNickname(nickname)
            _uiState.value = _uiState.value.copy(
                userProfile = _uiState.value.userProfile.copy(nickname = nickname)
            )
        }
    }

    fun saveUserImage(imageBase64: String) {
        viewModelScope.launch {
            progressionManager.saveUserImage(imageBase64)
            _uiState.value = _uiState.value.copy(
                userProfile = _uiState.value.userProfile.copy(imageBase64 = imageBase64)
            )
        }
    }

    fun saveUserProfile(nickname: String, imageBase64: String) {
        viewModelScope.launch {
            progressionManager.saveUserProfile(nickname, imageBase64)
            _uiState.value = _uiState.value.copy(
                userProfile = UserProfile(nickname, imageBase64),
                isEditingProfile = false
            )
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            val userProfile = progressionManager.getUserProfile()
            progressionManager.totalXp.collect { xp ->
                val levelInfo = progressionManager.getLevelInfo(xp)
                val statistics = progressionManager.getStatistics()
                val unlockedAchievements = progressionManager.getUnlockedAchievements()

                _uiState.value = _uiState.value.copy(
                    userProfile = userProfile,
                    levelInfo = levelInfo,
                    statistics = statistics,
                    unlockedAchievements = unlockedAchievements
                )

                // Refresh global rank
                loadGlobalRank(xp)
            }
        }
    }
}
