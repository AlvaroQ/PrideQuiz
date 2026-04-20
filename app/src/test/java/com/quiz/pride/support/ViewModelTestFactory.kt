package com.quiz.pride.support

import androidx.lifecycle.SavedStateHandle
import com.quiz.pride.managers.AchievementManager
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.CurrencyManager
import com.quiz.pride.managers.DailyChallengeManager
import com.quiz.pride.managers.DailyRewardManager
import com.quiz.pride.managers.GameStatsManager
import com.quiz.pride.managers.ProgressionManager
import com.quiz.pride.managers.SeasonalEventManager
import com.quiz.pride.managers.StreakManager
import com.quiz.pride.managers.ThemeManager
import com.quiz.pride.managers.UnlockablesManager
import com.quiz.pride.managers.XpSyncManager
import com.quiz.pride.ui.game.GameViewModel
import com.quiz.pride.ui.profile.ProfileViewModel
import com.quiz.pride.ui.select.SelectGameViewModel
import com.quiz.pride.ui.select.SelectViewModel
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetPrideById
import com.quiz.usecases.GetUserGlobalRank
import io.mockk.mockk

/**
 * Factories centralizadas para construir ViewModels en tests.
 *
 * Cada factory declara todas las dependencias del ViewModel como parametros
 * con defaults `mockk(relaxed = true)`. Los tests solo sobrescriben los mocks
 * que necesitan configurar para su caso de prueba.
 *
 * SOLID aplicado:
 * - SRP: cada factory tiene una unica responsabilidad (construir un VM concreto).
 * - OCP: cuando el ViewModel añade una dependencia, solo se añade aqui con un
 *   default y los tests existentes siguen pasando sin cambios.
 * - DIP: los tests dependen de esta API de construccion, no del constructor
 *   concreto. Esto aisla los tests de refactors en los constructores.
 *
 * Uso:
 * ```
 * // Sin configuracion (todos los mocks relaxed):
 * val vm = createSelectViewModel()
 *
 * // Con overrides para el mock que interesa verificar:
 * val analyticsManager: AnalyticsManager = mockk(relaxed = true)
 * val vm = createSelectViewModel(analyticsManager = analyticsManager)
 * verify { analyticsManager.analyticsScreenViewed(any()) }
 * ```
 */

fun createSelectViewModel(
    analyticsManager: AnalyticsManager = mockk(relaxed = true),
    streakManager: StreakManager = mockk(relaxed = true),
    currencyManager: CurrencyManager = mockk(relaxed = true),
    dailyRewardManager: DailyRewardManager = mockk(relaxed = true),
    progressionManager: ProgressionManager = mockk(relaxed = true),
    seasonalEventManager: SeasonalEventManager = mockk(relaxed = true),
    @Suppress("UNUSED_PARAMETER") dailyChallengeManager: DailyChallengeManager = mockk(relaxed = true),
): SelectViewModel = SelectViewModel(
    analyticsManager = analyticsManager,
    streakManager = streakManager,
    currencyManager = currencyManager,
    dailyRewardManager = dailyRewardManager,
    progressionManager = progressionManager,
    seasonalEventManager = seasonalEventManager,
)

fun createSelectGameViewModel(
    analyticsManager: AnalyticsManager = mockk(relaxed = true),
): SelectGameViewModel = SelectGameViewModel(
    analyticsManager = analyticsManager,
)

fun createGameViewModel(
    getPrideById: GetPrideById = mockk(relaxed = true),
    getPaymentDone: GetPaymentDone = mockk(relaxed = true),
    analyticsManager: AnalyticsManager = mockk(relaxed = true),
    themeManager: ThemeManager = mockk(relaxed = true),
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
    dailyChallengeManager: DailyChallengeManager = mockk(relaxed = true),
): GameViewModel = GameViewModel(
    getPrideById = getPrideById,
    getPaymentDone = getPaymentDone,
    analyticsManager = analyticsManager,
    themeManager = themeManager,
    savedStateHandle = savedStateHandle,
    dailyChallengeManager = dailyChallengeManager,
)

fun createProfileViewModel(
    progressionManager: ProgressionManager = mockk(relaxed = true),
    gameStatsManager: GameStatsManager = mockk(relaxed = true),
    achievementManager: AchievementManager = mockk(relaxed = true),
    xpSyncManager: XpSyncManager = mockk(relaxed = true),
    getUserGlobalRank: GetUserGlobalRank = mockk(relaxed = true),
    analyticsManager: AnalyticsManager = mockk(relaxed = true),
    streakManager: StreakManager = mockk(relaxed = true),
    dailyChallengeManager: DailyChallengeManager = mockk(relaxed = true),
    currencyManager: CurrencyManager = mockk(relaxed = true),
    unlockablesManager: UnlockablesManager = mockk(relaxed = true),
    dailyRewardManager: DailyRewardManager = mockk(relaxed = true),
): ProfileViewModel = ProfileViewModel(
    progressionManager = progressionManager,
    gameStatsManager = gameStatsManager,
    achievementManager = achievementManager,
    xpSyncManager = xpSyncManager,
    getUserGlobalRank = getUserGlobalRank,
    analyticsManager = analyticsManager,
    streakManager = streakManager,
    dailyChallengeManager = dailyChallengeManager,
    currencyManager = currencyManager,
    unlockablesManager = unlockablesManager,
    dailyRewardManager = dailyRewardManager,
)
