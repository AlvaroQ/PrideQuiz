package com.quiz.pride.ui.leaderboard

import com.quiz.domain.XpLeaderboardEntry
import com.quiz.pride.MainDispatcherRule
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.XpSyncManager
import com.quiz.usecases.GetXpLeaderboard
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class XpLeaderboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: XpLeaderboardViewModel

    private val getXpLeaderboard: GetXpLeaderboard = mockk()
    private val xpSyncManager: XpSyncManager = mockk()
    private val analyticsManager: AnalyticsManager = mockk(relaxed = true)

    private fun buildEntry(uid: String, xp: Long = 1000L, level: Int = 5) = XpLeaderboardEntry(
        uid = uid,
        nickname = "User_$uid",
        imageBase64 = "",
        totalXp = xp,
        level = level,
        title = "Explorer",
        totalGamesPlayed = 10,
        accuracy = 80f
    )

    private fun setupDefaultMocks(currentUid: String? = "user-123") {
        val leaderboard = listOf(
            buildEntry("top-1", xp = 5000L, level = 10),
            buildEntry("user-123", xp = 3000L, level = 7),
            buildEntry("user-456", xp = 1000L, level = 3)
        )
        coEvery { getXpLeaderboard.invoke(any()) } returns leaderboard
        every { xpSyncManager.getCurrentUserId() } returns currentUid
    }

    @Before
    fun setup() {
        setupDefaultMocks()
        viewModel = XpLeaderboardViewModel(getXpLeaderboard, xpSyncManager, analyticsManager)
    }

    // =========================================================
    // init — analytics
    // =========================================================

    @Test
    fun `init registra screen viewed en analytics`() {
        verify { analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_XP_LEADERBOARD) }
    }

    // =========================================================
    // loadLeaderboard — positive cases
    // =========================================================

    @Test
    fun `init carga leaderboard correctamente`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.leaderboardList.size)
        assertEquals("user-123", state.currentUserUid)
    }

    @Test
    fun `loadLeaderboard calcula user rank correctamente`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.userRank) // Second in the list (index 1 + 1)
    }

    @Test
    fun `loadLeaderboard con usuario en primer lugar devuelve rank 1`() = runTest {
        setupDefaultMocks(currentUid = "top-1")
        viewModel = XpLeaderboardViewModel(getXpLeaderboard, xpSyncManager, analyticsManager)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.userRank)
    }

    // =========================================================
    // loadLeaderboard — edge cases
    // =========================================================

    @Test
    fun `loadLeaderboard sin uid devuelve userRank null`() = runTest {
        setupDefaultMocks(currentUid = null)
        viewModel = XpLeaderboardViewModel(getXpLeaderboard, xpSyncManager, analyticsManager)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.userRank)
        assertNull(state.currentUserUid)
    }

    @Test
    fun `loadLeaderboard con uid no presente en lista devuelve userRank null`() = runTest {
        setupDefaultMocks(currentUid = "unknown-user")
        viewModel = XpLeaderboardViewModel(getXpLeaderboard, xpSyncManager, analyticsManager)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.userRank)
    }

    @Test
    fun `loadLeaderboard con lista vacia devuelve lista vacia y rank null`() = runTest {
        coEvery { getXpLeaderboard.invoke(any()) } returns emptyList()
        viewModel = XpLeaderboardViewModel(getXpLeaderboard, xpSyncManager, analyticsManager)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.leaderboardList.isEmpty())
        assertNull(state.userRank)
    }

    // =========================================================
    // refresh
    // =========================================================

    @Test
    fun `refresh recarga el leaderboard`() = runTest {
        advanceUntilIdle()

        // Change the data
        val newLeaderboard = listOf(buildEntry("new-user", xp = 9000L, level = 15))
        coEvery { getXpLeaderboard.invoke(any()) } returns newLeaderboard

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.leaderboardList.size)
        assertEquals("new-user", viewModel.uiState.value.leaderboardList.first().uid)
    }
}
