package com.quiz.pride.ui.result

import app.cash.turbine.test
import arrow.core.Either
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.App
import com.quiz.domain.ProcessedGameResult
import com.quiz.usecases.ProcessGameResultUseCase
import com.quiz.domain.GameMode
import com.quiz.domain.UserProfile
import com.quiz.domain.XpGainResult
import com.quiz.pride.MainDispatcherRule
import com.quiz.pride.managers.AdFrequencyManager
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.ProgressionManager
import com.quiz.pride.utils.Constants
import com.quiz.usecases.GetAppsRecommended
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetPersonalRecord
import com.quiz.usecases.GetRecordScore
import com.quiz.usecases.RankingMode
import com.quiz.usecases.SaveTopScore
import com.quiz.usecases.SetPersonalRecord
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
class ResultViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ResultViewModel

    private val getAppsRecommended: GetAppsRecommended = mockk()
    private val saveTopScore: SaveTopScore = mockk()
    private val getRecordScore: GetRecordScore = mockk()
    private val getPersonalRecord: GetPersonalRecord = mockk()
    private val setPersonalRecord: SetPersonalRecord = mockk()
    private val getPaymentDone: GetPaymentDone = mockk()
    private val processGameResult: ProcessGameResultUseCase = mockk()
    private val progressionManager: ProgressionManager = mockk()
    private val analyticsManager: AnalyticsManager = mockk(relaxed = true)
    private val adFrequencyManager: AdFrequencyManager = mockk()

    private fun buildApp(name: String) = App(
        image = "img_$name",
        localeName = com.quiz.domain.Name(ES = name, EN = name),
        url = "https://example.com/$name"
    )

    private fun buildXpGainResult(leveledUp: Boolean = false) = XpGainResult(
        xpGained = 100L,
        totalXp = 200L,
        oldLevel = 1,
        newLevel = if (leveledUp) 2 else 1,
        leveledUp = leveledUp
    )

    private fun setupDefaultMocks() {
        val apps = listOf(buildApp("App 1"), buildApp("App 2"))
        coEvery { getAppsRecommended.invoke() } returns Either.Right(apps)
        coEvery { getRecordScore.invoke(1L) } returns Either.Right("50")
        coEvery { getRecordScore.invoke(1L, RankingMode.NORMAL) } returns Either.Right("50")
        every { getPaymentDone.invoke() } returns false
        every { getPersonalRecord.invoke() } returns 0
        every { setPersonalRecord.invoke(any()) } just runs
        coEvery { adFrequencyManager.recordGameCompleted() } just runs
        coEvery { adFrequencyManager.shouldShowInterstitial() } returns false
    }

    @Before
    fun setup() {
        setupDefaultMocks()

        viewModel = ResultViewModel(
            getAppsRecommended = getAppsRecommended,
            saveTopScore = saveTopScore,
            getRecordScore = getRecordScore,
            getPersonalRecord = getPersonalRecord,
            setPersonalRecord = setPersonalRecord,
            getPaymentDone = getPaymentDone,
            processGameResult = processGameResult,
            progressionManager = progressionManager,
            analyticsManager = analyticsManager,
            adFrequencyManager = adFrequencyManager
        )
    }

    // =========================================================
    // loadData (llamado desde init)
    // =========================================================

    @Test
    fun `loadData carga apps y world record correctamente`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.appsList.size)
        assertEquals("50", state.worldRecord)
    }

    @Test
    fun `loadData con Either Left en apps usa lista vacia por degradacion graceful`() = runTest {
        coEvery { getAppsRecommended.invoke() } returns Either.Left(RepositoryException.NoConnectionException)

        viewModel = ResultViewModel(
            getAppsRecommended = getAppsRecommended,
            saveTopScore = saveTopScore,
            getRecordScore = getRecordScore,
            getPersonalRecord = getPersonalRecord,
            setPersonalRecord = setPersonalRecord,
            getPaymentDone = getPaymentDone,
            processGameResult = processGameResult,
            progressionManager = progressionManager,
            analyticsManager = analyticsManager,
            adFrequencyManager = adFrequencyManager
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.appsList.isEmpty())
    }

    @Test
    fun `loadData con Either Left en worldRecord usa valor por defecto`() = runTest {
        coEvery { getRecordScore.invoke(1L) } returns Either.Left(RepositoryException.NoConnectionException)
        coEvery { getRecordScore.invoke(1L, RankingMode.NORMAL) } returns Either.Left(RepositoryException.NoConnectionException)

        viewModel = ResultViewModel(
            getAppsRecommended = getAppsRecommended,
            saveTopScore = saveTopScore,
            getRecordScore = getRecordScore,
            getPersonalRecord = getPersonalRecord,
            setPersonalRecord = setPersonalRecord,
            getPaymentDone = getPaymentDone,
            processGameResult = processGameResult,
            progressionManager = progressionManager,
            analyticsManager = analyticsManager,
            adFrequencyManager = adFrequencyManager
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("0", state.worldRecord)
    }

    // =========================================================
    // checkPersonalRecord
    // =========================================================

    @Test
    fun `checkPersonalRecord actualiza el record cuando el nuevo es mayor`() = runTest {
        every { getPersonalRecord.invoke() } returns 10

        viewModel.checkPersonalRecord(25)

        verify { setPersonalRecord.invoke(25) }
        assertEquals("25", viewModel.uiState.value.personalRecord)
    }

    @Test
    fun `checkPersonalRecord no actualiza el record cuando el nuevo es menor`() = runTest {
        every { getPersonalRecord.invoke() } returns 100

        viewModel.checkPersonalRecord(30)

        verify(exactly = 0) { setPersonalRecord.invoke(any()) }
        assertEquals("100", viewModel.uiState.value.personalRecord)
    }

    @Test
    fun `checkPersonalRecord no actualiza el record cuando son iguales`() = runTest {
        every { getPersonalRecord.invoke() } returns 50

        viewModel.checkPersonalRecord(50)

        verify(exactly = 0) { setPersonalRecord.invoke(any()) }
        assertEquals("50", viewModel.uiState.value.personalRecord)
    }

    // =========================================================
    // checkWorldRecord
    // =========================================================

    @Test
    fun `checkWorldRecord muestra dialog cuando el puntaje califica`() = runTest {
        // Posicion 50 tiene score 20, usuario tiene 30 — califica
        coEvery { getRecordScore.invoke(50L) } returns Either.Right("20")
        coEvery { getRecordScore.invoke(50L, RankingMode.NORMAL) } returns Either.Right("20")
        coEvery { progressionManager.getUserProfile() } returns UserProfile(nickname = "Test", imageBase64 = "")

        viewModel.checkWorldRecord(30)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showWorldRecordDialog)
        assertEquals(30, viewModel.uiState.value.worldRecordPoints)
    }

    @Test
    fun `checkWorldRecord no muestra dialog cuando el puntaje no califica`() = runTest {
        // Posicion 50 tiene score 100, usuario tiene 30 — no califica
        coEvery { getRecordScore.invoke(50L) } returns Either.Right("100")
        coEvery { getRecordScore.invoke(50L, RankingMode.NORMAL) } returns Either.Right("100")

        viewModel.checkWorldRecord(30)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showWorldRecordDialog)
    }

    @Test
    fun `checkWorldRecord no muestra dialog cuando getRecordScore falla`() = runTest {
        coEvery { getRecordScore.invoke(50L) } returns Either.Left(RepositoryException.NoConnectionException)
        coEvery { getRecordScore.invoke(50L, RankingMode.NORMAL) } returns Either.Left(RepositoryException.NoConnectionException)

        viewModel.checkWorldRecord(999)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showWorldRecordDialog)
    }

    // =========================================================
    // recordGameResult
    // =========================================================

    @Test
    fun `recordGameResult actualiza xpGainResult en el estado`() = runTest {
        val xpResult = buildXpGainResult(leveledUp = false)
        coEvery { processGameResult.invoke(any()) } returns ProcessedGameResult(
            xpGainResult = xpResult,
            newAchievements = emptyList()
        )

        viewModel.recordGameResult(
            gameMode = GameMode.NORMAL,
            correctAnswers = 5,
            totalQuestions = 10,
            bestStreak = 3,
            timePlayedMs = 60_000L,
            completedAllQuestions = false
        )
        advanceUntilIdle()

        assertEquals(xpResult, viewModel.uiState.value.xpGainResult)
        assertFalse(viewModel.uiState.value.showLevelUpDialog)
    }

    @Test
    fun `recordGameResult activa showLevelUpDialog cuando hay level up`() = runTest {
        val xpResult = buildXpGainResult(leveledUp = true)
        coEvery { processGameResult.invoke(any()) } returns ProcessedGameResult(
            xpGainResult = xpResult,
            newAchievements = emptyList()
        )

        viewModel.recordGameResult(
            gameMode = GameMode.NORMAL,
            correctAnswers = 10,
            totalQuestions = 10,
            bestStreak = 10,
            timePlayedMs = 120_000L,
            completedAllQuestions = true
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showLevelUpDialog)
    }

    // =========================================================
    // dismissLevelUpDialog
    // =========================================================

    @Test
    fun `dismissLevelUpDialog desactiva el flag en el estado`() = runTest {
        val xpResult = buildXpGainResult(leveledUp = true)
        coEvery { processGameResult.invoke(any()) } returns ProcessedGameResult(
            xpGainResult = xpResult,
            newAchievements = emptyList()
        )

        viewModel.recordGameResult(GameMode.NORMAL, 10, 10, 5, 60_000L, true)
        advanceUntilIdle()

        viewModel.dismissLevelUpDialog()

        assertFalse(viewModel.uiState.value.showLevelUpDialog)
    }

    // =========================================================
    // setPhotoUrl
    // =========================================================

    @Test
    fun `setPhotoUrl actualiza photoUrl en el estado`() = runTest {
        viewModel.setPhotoUrl("https://example.com/avatar.jpg")

        assertEquals("https://example.com/avatar.jpg", viewModel.uiState.value.photoUrl)
    }

    // =========================================================
    // onScreenLoaded — interstitial
    // =========================================================

    @Test
    fun `onScreenLoaded emite ShowInterstitialAd cuando shouldShowInterstitial es true`() = runTest {
        coEvery { adFrequencyManager.shouldShowInterstitial() } returns true
        coEvery { adFrequencyManager.recordGameCompleted() } just runs

        viewModel.events.test {
            viewModel.onScreenLoaded()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ResultEvent.ShowInterstitialAd)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onScreenLoaded no emite evento cuando shouldShowInterstitial es false`() = runTest {
        coEvery { adFrequencyManager.shouldShowInterstitial() } returns false
        coEvery { adFrequencyManager.recordGameCompleted() } just runs

        viewModel.events.test {
            viewModel.onScreenLoaded()
            advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onScreenLoaded no emite interstitial cuando usuario pago`() = runTest {
        every { getPaymentDone.invoke() } returns true
        coEvery { adFrequencyManager.shouldShowInterstitial() } returns true
        coEvery { adFrequencyManager.recordGameCompleted() } just runs

        viewModel = ResultViewModel(
            getAppsRecommended = getAppsRecommended,
            saveTopScore = saveTopScore,
            getRecordScore = getRecordScore,
            getPersonalRecord = getPersonalRecord,
            setPersonalRecord = setPersonalRecord,
            getPaymentDone = getPaymentDone,
            processGameResult = processGameResult,
            progressionManager = progressionManager,
            analyticsManager = analyticsManager,
            adFrequencyManager = adFrequencyManager
        )

        viewModel.events.test {
            viewModel.onScreenLoaded()
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // =========================================================
    // onScreenInitialized
    // =========================================================

    @Test
    fun `onScreenInitialized es idempotente — segunda llamada no reejecuta`() = runTest {
        val xpResult = buildXpGainResult()
        coEvery { processGameResult.invoke(any()) } returns ProcessedGameResult(xpResult, emptyList())
        coEvery { getRecordScore.invoke(50L) } returns Either.Right("100")
        coEvery { getRecordScore.invoke(50L, RankingMode.NORMAL) } returns Either.Right("100")

        viewModel.onScreenInitialized(Constants.GameType.NORMAL, 10, 10, 8, 5, 60_000L)
        advanceUntilIdle()
        viewModel.onScreenInitialized(Constants.GameType.NORMAL, 10, 10, 8, 5, 60_000L)
        advanceUntilIdle()

        // processGameResult should be called only once
        coVerify(exactly = 1) { processGameResult.invoke(any()) }
    }

    @Test
    fun `onScreenInitialized TIMED llama checkTimedRanking y no checkWorldRecord`() = runTest {
        val xpResult = buildXpGainResult()
        coEvery { processGameResult.invoke(any()) } returns ProcessedGameResult(xpResult, emptyList())
        coEvery { getRecordScore.invoke(any(), RankingMode.TIMED) } returns Either.Right("")
        coEvery { progressionManager.getUserProfile() } returns UserProfile(nickname = "Test", imageBase64 = "")

        viewModel.onScreenInitialized(Constants.GameType.TIMED, 15, 10, 8, 5, 60_000L)
        advanceUntilIdle()

        // Timed mode should show timed ranking dialog (empty position = qualifies)
        assertTrue(viewModel.uiState.value.showTimedRankingDialog)
        // Should NOT show world record dialog
        assertFalse(viewModel.uiState.value.showWorldRecordDialog)
    }

    @Test
    fun `onScreenInitialized NORMAL llama checkWorldRecord y checkPersonalRecord`() = runTest {
        val xpResult = buildXpGainResult()
        coEvery { processGameResult.invoke(any()) } returns ProcessedGameResult(xpResult, emptyList())
        coEvery { getRecordScore.invoke(50L) } returns Either.Right("5")
        coEvery { getRecordScore.invoke(50L, RankingMode.NORMAL) } returns Either.Right("5")
        coEvery { progressionManager.getUserProfile() } returns UserProfile(nickname = "Test", imageBase64 = "")
        every { getPersonalRecord.invoke() } returns 5

        viewModel.onScreenInitialized(Constants.GameType.NORMAL, 20, 10, 8, 5, 60_000L)
        advanceUntilIdle()

        // Score 20 > position 50 score 5 → should show world record dialog
        assertTrue(viewModel.uiState.value.showWorldRecordDialog)
        // Personal record should be updated (20 > 5)
        assertEquals("20", viewModel.uiState.value.personalRecord)
    }

    // =========================================================
    // hasPaid state
    // =========================================================

    @Test
    fun `init setea hasPaid true cuando getPaymentDone retorna true`() = runTest {
        every { getPaymentDone.invoke() } returns true
        viewModel = ResultViewModel(
            getAppsRecommended = getAppsRecommended,
            saveTopScore = saveTopScore,
            getRecordScore = getRecordScore,
            getPersonalRecord = getPersonalRecord,
            setPersonalRecord = setPersonalRecord,
            getPaymentDone = getPaymentDone,
            processGameResult = processGameResult,
            progressionManager = progressionManager,
            analyticsManager = analyticsManager,
            adFrequencyManager = adFrequencyManager
        )
        assertTrue(viewModel.uiState.value.hasPaid)
    }

    // =========================================================
    // checkTimedRanking
    // =========================================================

    @Test
    fun `checkTimedRanking muestra dialog cuando califica`() = runTest {
        coEvery { getRecordScore.invoke(20L, RankingMode.TIMED) } returns Either.Right("10")
        coEvery { progressionManager.getUserProfile() } returns UserProfile(nickname = "Player", imageBase64 = "img")

        viewModel.checkTimedRanking(15) // 15 > 10 → qualifies
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showTimedRankingDialog)
        assertEquals(15, viewModel.uiState.value.timedScore)
    }

    @Test
    fun `checkTimedRanking no muestra dialog cuando no califica`() = runTest {
        coEvery { getRecordScore.invoke(20L, RankingMode.TIMED) } returns Either.Right("100")

        viewModel.checkTimedRanking(15) // 15 < 100 → doesn't qualify
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showTimedRankingDialog)
    }

    // =========================================================
    // saveScore / saveTimedScore
    // =========================================================

    @Test
    fun `saveTimedScore guarda y cierra dialog`() = runTest {
        coEvery { progressionManager.saveUserProfile(any(), any()) } just runs
        coEvery { saveTopScore.invoke(any(), any()) } returns Either.Right(mockk())

        // Setup: show dialog first
        coEvery { getRecordScore.invoke(20L, RankingMode.TIMED) } returns Either.Right("")
        coEvery { progressionManager.getUserProfile() } returns UserProfile(nickname = "P", imageBase64 = "")
        viewModel.checkTimedRanking(20)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showTimedRankingDialog)

        // Act: save
        viewModel.saveTimedScore("Nick", "img64")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showTimedRankingDialog)
        assertFalse(viewModel.uiState.value.isSavingTimedScore)
        verify { analyticsManager.analyticsScoreSaved("timed_ranking", true) }
    }

    @Test
    fun `dismissTimedRankingDialog cierra dialog y trackea analytics`() {
        viewModel.dismissTimedRankingDialog()
        assertFalse(viewModel.uiState.value.showTimedRankingDialog)
        verify { analyticsManager.analyticsScoreSaved("timed_ranking", false) }
    }

    // =========================================================
    // Analytics clicks
    // =========================================================

    @Test
    fun `onPlayAgainClicked trackea analytics`() {
        viewModel.onPlayAgainClicked()
        verify { analyticsManager.analyticsClicked(AnalyticsManager.BTN_PLAY_AGAIN) }
    }

    @Test
    fun `onRankingClicked trackea analytics`() {
        viewModel.onRankingClicked()
        verify { analyticsManager.analyticsClicked(AnalyticsManager.BTN_RANKING) }
    }

    @Test
    fun `onInterstitialShown registra en adFrequencyManager`() = runTest {
        coEvery { adFrequencyManager.recordInterstitialShown() } just runs
        viewModel.onInterstitialShown()
        advanceUntilIdle()
        coVerify { adFrequencyManager.recordInterstitialShown() }
    }

    // =========================================================
    // saveScore (world record)
    // =========================================================

    @Test
    fun `saveScore guarda perfil y cierra dialog`() = runTest {
        coEvery { progressionManager.saveUserProfile(any(), any()) } just runs
        coEvery { saveTopScore.invoke(any()) } returns Either.Right(mockk())

        viewModel.saveScore("WorldChamp", "avatar64")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showWorldRecordDialog)
        assertFalse(viewModel.uiState.value.isSavingWorldRecord)
        coVerify { progressionManager.saveUserProfile("WorldChamp", "avatar64") }
        verify { analyticsManager.analyticsScoreSaved("world_record", true) }
    }

    @Test
    fun `onWorldRecordDialogDismissed cierra dialog y trackea analytics`() {
        viewModel.onWorldRecordDialogDismissed()
        assertFalse(viewModel.uiState.value.showWorldRecordDialog)
        verify { analyticsManager.analyticsScoreSaved("world_record", false) }
    }

    @Test
    fun `onRateClicked trackea analytics`() {
        viewModel.onRateClicked()
        verify { analyticsManager.analyticsClicked(AnalyticsManager.BTN_RATE) }
    }
}
