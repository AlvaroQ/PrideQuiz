package com.quiz.pride.ui.profile

import arrow.core.Either
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.Achievement
import com.quiz.domain.LevelInfo
import com.quiz.domain.PlayerStatistics
import com.quiz.domain.UserProfile
import com.quiz.pride.MainDispatcherRule
import com.quiz.pride.managers.AchievementManager
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.GameStatsManager
import com.quiz.pride.managers.ProgressionManager
import com.quiz.pride.managers.XpSyncManager
import com.quiz.usecases.GetUserGlobalRank
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ProfileViewModel

    private val progressionManager: ProgressionManager = mockk()
    private val gameStatsManager: GameStatsManager = mockk()
    private val achievementManager: AchievementManager = mockk()
    private val xpSyncManager: XpSyncManager = mockk()
    private val getUserGlobalRank: GetUserGlobalRank = mockk()
    private val analyticsManager: AnalyticsManager = mockk(relaxed = true)

    private fun buildLevelInfo(level: Int = 5, xp: Long = 1000L) = LevelInfo(
        level = level,
        title = "Explorer",
        totalXp = xp,
        xpInCurrentLevel = 200L,
        xpNeededForNextLevel = 500L,
        progressPercent = 0.4f
    )

    private fun buildStatistics() = PlayerStatistics(
        totalGamesPlayed = 50,
        gamesWon = 30,
        totalCorrectAnswers = 200,
        totalWrongAnswers = 50,
        accuracy = 80f,
        bestStreakEver = 10,
        perfectGames = 5,
        totalTimePlayedMs = 3_600_000L,
        normalGamesPlayed = 30,
        advanceGamesPlayed = 10,
        expertGamesPlayed = 5,
        timedGamesPlayed = 5
    )

    private fun setupDefaultMocks() {
        coEvery { progressionManager.getUserProfile() } returns UserProfile(nickname = "TestUser", imageBase64 = "")
        every { progressionManager.totalXp } returns MutableStateFlow(1000L)
        every { progressionManager.getLevelInfo(any()) } returns buildLevelInfo()
        coEvery { gameStatsManager.getStatistics() } returns buildStatistics()
        coEvery { achievementManager.getUnlockedAchievements() } returns setOf(Achievement.FIRST_GAME)
        every { xpSyncManager.getCurrentUserId() } returns "user-uid-123"
        coEvery { getUserGlobalRank.invoke(any(), any()) } returns Either.Right(42)
    }

    @Before
    fun setup() {
        setupDefaultMocks()

        viewModel = ProfileViewModel(
            progressionManager = progressionManager,
            gameStatsManager = gameStatsManager,
            achievementManager = achievementManager,
            xpSyncManager = xpSyncManager,
            getUserGlobalRank = getUserGlobalRank,
            analyticsManager = analyticsManager
        )
    }

    // =========================================================
    // loadProfileData (llamado desde init)
    // =========================================================

    @Test
    fun `init carga el perfil correctamente y pone isLoading en false`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("TestUser", state.userProfile.nickname)
    }

    @Test
    fun `init carga el levelInfo correctamente`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.levelInfo)
        assertEquals(5, state.levelInfo?.level)
    }

    @Test
    fun `init carga las estadisticas correctamente`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.statistics)
        assertEquals(50, state.statistics?.totalGamesPlayed)
        assertEquals(80f, state.statistics?.accuracy)
    }

    @Test
    fun `init carga los logros desbloqueados correctamente`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.unlockedAchievements.size)
        assertTrue(state.unlockedAchievements.contains(Achievement.FIRST_GAME))
    }

    @Test
    fun `init carga el rank global cuando hay uid y xp mayor a 0`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingRank)
        assertEquals(42, state.globalRank)
    }

    @Test
    fun `init no carga rank cuando el uid es null`() = runTest {
        every { xpSyncManager.getCurrentUserId() } returns null

        viewModel = ProfileViewModel(
            progressionManager = progressionManager,
            gameStatsManager = gameStatsManager,
            achievementManager = achievementManager,
            xpSyncManager = xpSyncManager,
            getUserGlobalRank = getUserGlobalRank,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingRank)
        assertNull(state.globalRank)
    }

    @Test
    fun `init no carga rank cuando el XP es 0`() = runTest {
        every { progressionManager.totalXp } returns MutableStateFlow(0L)
        every { progressionManager.getLevelInfo(0L) } returns buildLevelInfo(xp = 0L)

        viewModel = ProfileViewModel(
            progressionManager = progressionManager,
            gameStatsManager = gameStatsManager,
            achievementManager = achievementManager,
            xpSyncManager = xpSyncManager,
            getUserGlobalRank = getUserGlobalRank,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.globalRank)
    }

    @Test
    fun `globalRank es null cuando getUserGlobalRank retorna Either Left`() = runTest {
        coEvery { getUserGlobalRank.invoke(any(), any()) } returns Either.Left(RepositoryException.NoConnectionException)

        viewModel = ProfileViewModel(
            progressionManager = progressionManager,
            gameStatsManager = gameStatsManager,
            achievementManager = achievementManager,
            xpSyncManager = xpSyncManager,
            getUserGlobalRank = getUserGlobalRank,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.globalRank)
        assertFalse(state.isLoadingRank)
    }

    // =========================================================
    // setEditingProfile
    // =========================================================

    @Test
    fun `setEditingProfile con true activa el flag de edicion`() = runTest {
        viewModel.setEditingProfile(true)

        assertTrue(viewModel.uiState.value.isEditingProfile)
    }

    @Test
    fun `setEditingProfile con false desactiva el flag de edicion`() = runTest {
        viewModel.setEditingProfile(true)
        viewModel.setEditingProfile(false)

        assertFalse(viewModel.uiState.value.isEditingProfile)
    }

    // =========================================================
    // saveNickname
    // =========================================================

    @Test
    fun `saveNickname actualiza el nickname en el estado`() = runTest {
        coEvery { progressionManager.saveNickname(any()) } returns Unit

        viewModel.saveNickname("NuevoNombre")
        advanceUntilIdle()

        assertEquals("NuevoNombre", viewModel.uiState.value.userProfile.nickname)
    }

    @Test
    fun `saveNickname delega la persistencia al progressionManager`() = runTest {
        coEvery { progressionManager.saveNickname(any()) } returns Unit

        viewModel.saveNickname("NombrePersistido")
        advanceUntilIdle()

        coVerify(exactly = 1) { progressionManager.saveNickname("NombrePersistido") }
    }

    // =========================================================
    // saveUserImage
    // =========================================================

    @Test
    fun `saveUserImage actualiza imageBase64 en el estado`() = runTest {
        coEvery { progressionManager.saveUserImage(any()) } returns Unit

        viewModel.saveUserImage("base64encodedimage==")
        advanceUntilIdle()

        assertEquals("base64encodedimage==", viewModel.uiState.value.userProfile.imageBase64)
    }

    @Test
    fun `saveUserImage delega la persistencia al progressionManager`() = runTest {
        coEvery { progressionManager.saveUserImage(any()) } returns Unit

        viewModel.saveUserImage("img_data")
        advanceUntilIdle()

        coVerify(exactly = 1) { progressionManager.saveUserImage("img_data") }
    }

    // =========================================================
    // saveUserProfile
    // =========================================================

    @Test
    fun `saveUserProfile actualiza nickname e imagen y desactiva isEditingProfile`() = runTest {
        viewModel.setEditingProfile(true)
        coEvery { progressionManager.saveUserProfile(any(), any()) } returns Unit

        viewModel.saveUserProfile("NuevoNick", "nuevaImagen")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("NuevoNick", state.userProfile.nickname)
        assertEquals("nuevaImagen", state.userProfile.imageBase64)
        assertFalse(state.isEditingProfile)
    }

    @Test
    fun `saveUserProfile delega al progressionManager con los datos correctos`() = runTest {
        coEvery { progressionManager.saveUserProfile(any(), any()) } returns Unit

        viewModel.saveUserProfile("Nick123", "img123")
        advanceUntilIdle()

        coVerify(exactly = 1) { progressionManager.saveUserProfile("Nick123", "img123") }
    }

    // =========================================================
    // allAchievements (state inmutable)
    // =========================================================

    @Test
    fun `allAchievements contiene todos los logros del enum Achievement`() = runTest {
        val state = viewModel.uiState.value

        assertEquals(Achievement.entries.size, state.allAchievements.size)
    }

    // =========================================================
    // refreshData
    // =========================================================

    @Test
    fun `refreshData actualiza perfil sin activar isLoading`() = runTest {
        advanceUntilIdle()
        // Change mock data
        coEvery { progressionManager.getUserProfile() } returns UserProfile(nickname = "Updated", imageBase64 = "new")

        viewModel.refreshData()
        advanceUntilIdle()

        assertEquals("Updated", viewModel.uiState.value.userProfile.nickname)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // =========================================================
    // Analytics
    // =========================================================

    @Test
    fun `init registra SCREEN_PROFILE en analytics`() {
        verify { analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_PROFILE) }
    }
}
