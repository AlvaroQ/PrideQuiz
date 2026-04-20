package com.quiz.pride.ui.select

import com.quiz.domain.LevelInfo
import com.quiz.domain.StreakState
import com.quiz.domain.XpGainResult
import com.quiz.domain.challenge.DailyChallengeState
import com.quiz.domain.cosmetics.CurrencyBalance
import com.quiz.domain.reward.DailyReward
import com.quiz.domain.reward.RewardTier
import com.quiz.pride.MainDispatcherRule
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.CurrencyManager
import com.quiz.pride.managers.DailyChallengeManager
import com.quiz.pride.managers.DailyRewardManager
import com.quiz.pride.managers.ProgressionManager
import com.quiz.pride.managers.StreakManager
import com.quiz.pride.support.createSelectViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SelectViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val analyticsManager: AnalyticsManager = mockk(relaxed = true)
    private val streakManager: StreakManager = mockk(relaxed = true)
    private val dailyChallengeManager: DailyChallengeManager = mockk(relaxed = true)
    // progressionManager se declara ESTRICTO (sin relaxed) porque mockk 1.14+ tiene
    // problemas al aplicar stubs sobre properties que retornan StateFlow cuando el
    // mock es relaxed (el default relaxed no se sobrescribe con `every { } returns`).
    // ProfileViewModelTest sigue el mismo patron.
    private val progressionManager: ProgressionManager = mockk()
    private val currencyManager: CurrencyManager = mockk(relaxed = true)
    private val dailyRewardManager: DailyRewardManager = mockk(relaxed = true)

    private lateinit var viewModel: SelectViewModel

    /**
     * Stubea el comportamiento por defecto que el init del ViewModel consulta:
     * streak, challenges y balance. Centralizar estos stubs evita duplicarlos
     * en cada test que reconstruya el VM con configuracion alterada.
     */
    private fun stubDefaults() {
        coEvery { streakManager.getStreakState() } returns StreakState()
        coEvery { streakManager.isStreakAtRisk() } returns false
        coEvery { streakManager.hasPlayedToday() } returns false

        every { progressionManager.totalXp } returns MutableStateFlow(0L)
        every { progressionManager.getLevelInfo(any()) } returns LevelInfo(
            level = 1,
            title = "Novato",
            totalXp = 0L,
            xpInCurrentLevel = 0L,
            xpNeededForNextLevel = 100L,
            progressPercent = 0f,
        )
        // addXp se invoca desde claimDailyReward. Al ser progressionManager estricto,
        // sin este stub la llamada lanza excepcion y aborta la cadena de acreditacion.
        coEvery { progressionManager.addXp(any()) } returns XpGainResult(
            xpGained = 0L,
            totalXp = 0L,
            oldLevel = 1,
            newLevel = 1,
            leveledUp = false,
        )
        coEvery { dailyChallengeManager.getDailyChallengeState(any()) } returns DailyChallengeState()

        every { currencyManager.observeBalance() } returns flowOf(CurrencyBalance())
    }

    @Before
    fun setup() {
        stubDefaults()

        viewModel = createSelectViewModel(
            analyticsManager = analyticsManager,
            streakManager = streakManager,
            dailyChallengeManager = dailyChallengeManager,
            progressionManager = progressionManager,
            currencyManager = currencyManager,
            dailyRewardManager = dailyRewardManager,
        )
    }

    private fun sampleReward(
        tier: RewardTier = RewardTier.COMMON,
        xp: Int = 15,
        coins: Int = 10,
        gems: Int = 0,
        isClaimed: Boolean = false,
    ) = DailyReward(
        date = "2026-04-18",
        tier = tier,
        xpAmount = xp,
        coinsAmount = coins,
        gemsAmount = gems,
        isClaimed = isClaimed,
    )

    // =========================================================
    // Inicializacion
    // =========================================================

    @Test
    fun `init registra la pantalla SelectScreen en Analytics`() {
        verify(exactly = 1) {
            analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT)
        }
    }

    @Test
    fun `init registra exactamente una pantalla y ninguna otra`() {
        verify(exactly = 1) { analyticsManager.analyticsScreenViewed(any()) }
    }

    @Test
    fun `multiples instancias registran la pantalla cada una por separado`() {
        // La segunda instancia reutiliza los mismos stubs que la primera: el init del VM
        // consulta streak, challenges y balance, por lo que reusamos los mocks ya
        // configurados via stubDefaults() en @Before.
        val analyticsManager2: AnalyticsManager = mockk(relaxed = true)

        createSelectViewModel(
            analyticsManager = analyticsManager2,
            streakManager = streakManager,
            dailyChallengeManager = dailyChallengeManager,
            progressionManager = progressionManager,
            currencyManager = currencyManager,
        )

        verify(exactly = 1) {
            analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT)
        }
        verify(exactly = 1) {
            analyticsManager2.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT)
        }
    }

    // =========================================================
    // loadStreakData
    // =========================================================

    @Test
    fun `loadStreakData carga el estado de racha correctamente`() = runTest {
        val expectedState = StreakState(currentStreak = 5, bestStreak = 10)
        coEvery { streakManager.getStreakState() } returns expectedState
        coEvery { streakManager.isStreakAtRisk() } returns true
        coEvery { streakManager.hasPlayedToday() } returns true

        viewModel.loadStreakData()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoadingStreak)
        assertTrue(uiState.isStreakAtRisk)
        assertTrue(uiState.hasPlayedToday)
    }

    // =========================================================
    // observeDailyReward
    // =========================================================

    @Test
    fun `observeDailyReward actualiza el uiState cuando el Flow emite una recompensa`() = runTest {
        val reward = sampleReward(tier = RewardTier.UNCOMMON, xp = 40, coins = 30)
        coEvery { dailyRewardManager.getTodayReward() } returns reward
        every { dailyRewardManager.observeTodayReward() } returns flowOf(reward)

        val vm = createSelectViewModel(
            analyticsManager = analyticsManager,
            streakManager = streakManager,
            dailyChallengeManager = dailyChallengeManager,
            progressionManager = progressionManager,
            currencyManager = currencyManager,
            dailyRewardManager = dailyRewardManager,
        )
        advanceUntilIdle()

        assertEquals(reward, vm.uiState.value.dailyReward)
    }

    // =========================================================
    // claimDailyReward
    // =========================================================

    @Test
    fun `claimDailyReward acredita XP coins y gems para un reward RARE`() = runTest {
        val rare = sampleReward(tier = RewardTier.RARE, xp = 100, coins = 90, gems = 1)
        coEvery { dailyRewardManager.claimTodayReward() } returns rare.copy(isClaimed = true)

        viewModel.claimDailyReward()
        advanceUntilIdle()

        coVerify(exactly = 1) { progressionManager.addXp(100L) }
        coVerify(exactly = 1) { currencyManager.earnCoins(90, source = "daily_reward") }
        coVerify(exactly = 1) { currencyManager.earnGems(1, source = "daily_reward") }
    }

    @Test
    fun `claimDailyReward no acredita gems cuando es COMMON`() = runTest {
        val common = sampleReward(tier = RewardTier.COMMON, xp = 15, coins = 10, gems = 0)
        coEvery { dailyRewardManager.claimTodayReward() } returns common.copy(isClaimed = true)

        viewModel.claimDailyReward()
        advanceUntilIdle()

        coVerify(exactly = 1) { progressionManager.addXp(15L) }
        coVerify(exactly = 1) { currencyManager.earnCoins(10, source = "daily_reward") }
        coVerify(exactly = 0) { currencyManager.earnGems(any(), any()) }
    }

    @Test
    fun `claimDailyReward no acredita nada si claimTodayReward retorna null`() = runTest {
        // Manager retorna null cuando ya estaba reclamada hoy.
        coEvery { dailyRewardManager.claimTodayReward() } returns null

        viewModel.claimDailyReward()
        advanceUntilIdle()

        coVerify(exactly = 0) { progressionManager.addXp(any()) }
        coVerify(exactly = 0) { currencyManager.earnCoins(any(), any()) }
        coVerify(exactly = 0) { currencyManager.earnGems(any(), any()) }
    }

    @Test
    fun `claimDailyReward doble-tap solo invoca claimTodayReward una vez mientras la primera esta en curso`() = runTest {
        // Con UnconfinedTestDispatcher la coroutine se ejecuta hasta suspender. Para
        // simular el escenario real de doble-tap, bloqueamos claimTodayReward con un
        // CompletableDeferred: asi la primera invocacion queda suspendida "en vuelo"
        // cuando llega la segunda, que debe ser rechazada por isClaimingDailyReward.
        val reward = sampleReward(tier = RewardTier.COMMON).copy(isClaimed = true)
        val gate = CompletableDeferred<DailyReward>()
        coEvery { dailyRewardManager.claimTodayReward() } coAnswers { gate.await() }

        viewModel.claimDailyReward() // (1) entra al launch y suspende en claimTodayReward
        viewModel.claimDailyReward() // (2) bloqueada por isClaimingDailyReward == true

        gate.complete(reward)
        advanceUntilIdle()

        // Solo una ejecucion efectiva; la segunda fue bloqueada por isClaimingDailyReward.
        coVerify(exactly = 1) { dailyRewardManager.claimTodayReward() }
    }

    @Test
    fun `claimDailyReward no re-invoca al manager si la recompensa ya estaba reclamada en el state`() = runTest {
        val claimedReward = sampleReward(isClaimed = true)
        coEvery { dailyRewardManager.getTodayReward() } returns claimedReward
        every { dailyRewardManager.observeTodayReward() } returns flowOf(claimedReward)

        val vm = createSelectViewModel(
            analyticsManager = analyticsManager,
            streakManager = streakManager,
            dailyChallengeManager = dailyChallengeManager,
            progressionManager = progressionManager,
            currencyManager = currencyManager,
            dailyRewardManager = dailyRewardManager,
        )
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.dailyReward)
        assertTrue(vm.uiState.value.dailyReward!!.isClaimed)

        vm.claimDailyReward()
        advanceUntilIdle()

        // El guard de isClaimed en el VM evita tocar el manager.
        coVerify(exactly = 0) { dailyRewardManager.claimTodayReward() }
    }
}
