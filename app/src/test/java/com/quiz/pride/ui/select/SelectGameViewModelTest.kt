package com.quiz.pride.ui.select

import com.quiz.domain.StreakState
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.StreakManager
import com.quiz.pride.support.createSelectGameViewModel
import com.quiz.pride.support.createSelectViewModel
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SelectGameViewModelTest {

    private val analyticsManager: AnalyticsManager = mockk(relaxed = true)

    private lateinit var viewModel: SelectGameViewModel

    @Before
    fun setup() {
        viewModel = createSelectGameViewModel(analyticsManager = analyticsManager)
    }

    // =========================================================
    // Inicializacion
    // =========================================================

    @Test
    fun `init registra la pantalla SelectGameScreen en Analytics`() {
        verify(exactly = 1) {
            analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT_GAME)
        }
    }

    @Test
    fun `init registra exactamente una pantalla y ninguna otra`() {
        verify(exactly = 1) { analyticsManager.analyticsScreenViewed(any()) }
    }

    @Test
    fun `SelectGameViewModel y SelectViewModel registran pantallas distintas`() {
        val sharedAnalytics: AnalyticsManager = mockk(relaxed = true)
        val streakManager: StreakManager = mockk(relaxed = true)
        coEvery { streakManager.getStreakState() } returns StreakState()
        coEvery { streakManager.isStreakAtRisk() } returns false
        coEvery { streakManager.hasPlayedToday() } returns false

        createSelectViewModel(analyticsManager = sharedAnalytics, streakManager = streakManager)
        createSelectGameViewModel(analyticsManager = sharedAnalytics)

        verify(exactly = 1) {
            sharedAnalytics.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT)
        }
        verify(exactly = 1) {
            sharedAnalytics.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT_GAME)
        }
    }
}
