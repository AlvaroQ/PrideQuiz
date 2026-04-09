package com.quiz.pride.ui.ranking

import arrow.core.Either
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.User
import com.quiz.domain.XpLeaderboardEntry
import com.quiz.pride.MainDispatcherRule
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetRankingScore
import com.quiz.usecases.GetXpLeaderboard
import com.quiz.usecases.RankingMode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RankingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: RankingViewModel

    private val getRankingScore: GetRankingScore = mockk()
    private val getPaymentDone: GetPaymentDone = mockk()
    private val getXpLeaderboard: GetXpLeaderboard = mockk()
    private val analyticsManager: AnalyticsManager = mockk(relaxed = true)

    private fun buildUser(name: String, score: Int) = User(
        name = name,
        score = score,
        userImage = "",
        timestamp = 0L
    )

    private fun buildXpEntry(uid: String, xp: Long) = XpLeaderboardEntry(
        uid = uid,
        nickname = "Player $uid",
        totalXp = xp,
        level = 1,
        title = "Novato"
    )

    private fun setupDefaultMocks(
        normalResult: Either<RepositoryException, List<User>> = Either.Right(listOf(buildUser("Alice", 100))),
        timedResult: Either<RepositoryException, List<User>> = Either.Right(listOf(buildUser("Bob", 200))),
        xpResult: List<XpLeaderboardEntry> = listOf(buildXpEntry("uid1", 500L))
    ) {
        coEvery { getRankingScore.invoke(RankingMode.NORMAL) } returns normalResult
        coEvery { getRankingScore.invoke(RankingMode.TIMED) } returns timedResult
        coEvery { getXpLeaderboard.invoke() } returns xpResult
        every { getPaymentDone.invoke() } returns false
    }

    @Before
    fun setup() {
        setupDefaultMocks()

        viewModel = RankingViewModel(
            getRankingScore = getRankingScore,
            getPaymentDone = getPaymentDone,
            getXpLeaderboard = getXpLeaderboard,
            analyticsManager = analyticsManager
        )
    }

    // =========================================================
    // loadRanking — llamado desde init
    // =========================================================

    @Test
    fun `loadRanking carga las tres listas en paralelo`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.rankingList.size)
        assertEquals("Alice", state.rankingList[0].name)
        assertEquals(1, state.timedRankingList.size)
        assertEquals("Bob", state.timedRankingList[0].name)
        assertEquals(1, state.xpLeaderboardList.size)
    }

    @Test
    fun `loadRanking cuando ranking normal falla muestra lista vacia y marca hasError`() = runTest {
        setupDefaultMocks(
            normalResult = Either.Left(RepositoryException.NoConnectionException)
        )

        viewModel = RankingViewModel(
            getRankingScore = getRankingScore,
            getPaymentDone = getPaymentDone,
            getXpLeaderboard = getXpLeaderboard,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.rankingList.isEmpty())
        assertTrue(state.hasError)
        // El ranking timed si cargo correctamente
        assertEquals(1, state.timedRankingList.size)
    }

    @Test
    fun `loadRanking cuando ranking timed falla muestra lista vacia y marca hasError`() = runTest {
        setupDefaultMocks(
            timedResult = Either.Left(RepositoryException.DataNotFoundException)
        )

        viewModel = RankingViewModel(
            getRankingScore = getRankingScore,
            getPaymentDone = getPaymentDone,
            getXpLeaderboard = getXpLeaderboard,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.timedRankingList.isEmpty())
        assertTrue(state.hasError)
        // El ranking normal si cargo correctamente
        assertEquals(1, state.rankingList.size)
    }

    @Test
    fun `loadRanking cuando ambos rankings fallan muestra hasError y listas vacias`() = runTest {
        setupDefaultMocks(
            normalResult = Either.Left(RepositoryException.NoConnectionException),
            timedResult = Either.Left(RepositoryException.NoConnectionException)
        )

        viewModel = RankingViewModel(
            getRankingScore = getRankingScore,
            getPaymentDone = getPaymentDone,
            getXpLeaderboard = getXpLeaderboard,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.rankingList.isEmpty())
        assertTrue(state.timedRankingList.isEmpty())
        assertTrue(state.hasError)
    }

    @Test
    fun `loadRanking exitoso no activa hasError`() = runTest {
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasError)
    }

    // =========================================================
    // onTabSelected
    // =========================================================

    @Test
    fun `onTabSelected actualiza selectedTabIndex`() = runTest {
        viewModel.onTabSelected(1)
        assertEquals(1, viewModel.uiState.value.selectedTabIndex)

        viewModel.onTabSelected(2)
        assertEquals(2, viewModel.uiState.value.selectedTabIndex)

        viewModel.onTabSelected(0)
        assertEquals(0, viewModel.uiState.value.selectedTabIndex)
    }

    // =========================================================
    // refreshRanking
    // =========================================================

    @Test
    fun `refreshRanking recarga las listas desde cero`() = runTest {
        advanceUntilIdle()

        // Cambiamos los mocks para la recarga
        val updatedList = listOf(buildUser("Charlie", 300))
        coEvery { getRankingScore.invoke(RankingMode.NORMAL) } returns Either.Right(updatedList)

        viewModel.refreshRanking()
        advanceUntilIdle()

        assertEquals("Charlie", viewModel.uiState.value.rankingList[0].name)
    }

    // =========================================================
    // Ads
    // =========================================================

    @Test
    fun `loadRanking con payment done desactiva el rewarded ad`() = runTest {
        every { getPaymentDone.invoke() } returns true

        viewModel = RankingViewModel(
            getRankingScore = getRankingScore,
            getPaymentDone = getPaymentDone,
            getXpLeaderboard = getXpLeaderboard,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showRewardedAd)
    }

    @Test
    fun `loadRanking sin payment done activa el rewarded ad`() = runTest {
        every { getPaymentDone.invoke() } returns false

        viewModel = RankingViewModel(
            getRankingScore = getRankingScore,
            getPaymentDone = getPaymentDone,
            getXpLeaderboard = getXpLeaderboard,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showRewardedAd)
    }

    // =========================================================
    // Analytics — onTabSelected
    // =========================================================

    @Test
    fun `onTabSelected trackea analytics con nombre de tab correcto`() {
        viewModel.onTabSelected(0)
        verify { analyticsManager.analyticsRankingTabSelected("normal") }

        viewModel.onTabSelected(1)
        verify { analyticsManager.analyticsRankingTabSelected("timed") }

        viewModel.onTabSelected(2)
        verify { analyticsManager.analyticsRankingTabSelected("xp") }
    }

    @Test
    fun `onTabSelected con indice invalido trackea unknown`() {
        viewModel.onTabSelected(99)
        verify { analyticsManager.analyticsRankingTabSelected("unknown") }
    }

    @Test
    fun `loadRanking con payment done tambien desactiva showBannerAd`() = runTest {
        every { getPaymentDone.invoke() } returns true
        viewModel = RankingViewModel(getRankingScore, getPaymentDone, getXpLeaderboard, analyticsManager)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showBannerAd)
    }
}
