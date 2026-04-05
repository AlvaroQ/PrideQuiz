package com.quiz.pride.ui.select

import com.quiz.pride.managers.AnalyticsManager
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SelectGameViewModelTest {

    private val analyticsManager: AnalyticsManager = mockk(relaxed = true)

    private lateinit var viewModel: SelectGameViewModel

    @Before
    fun setup() {
        viewModel = SelectGameViewModel(analyticsManager = analyticsManager)
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
    fun `init no registra otras pantallas que no sean SelectGame`() {
        verify(exactly = 0) {
            analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT)
        }
        verify(exactly = 0) {
            analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_GAME)
        }
        verify(exactly = 0) {
            analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_RESULT)
        }
    }

    @Test
    fun `SelectGameViewModel y SelectViewModel registran pantallas distintas`() {
        val sharedAnalytics: AnalyticsManager = mockk(relaxed = true)

        SelectViewModel(analyticsManager = sharedAnalytics)
        SelectGameViewModel(analyticsManager = sharedAnalytics)

        verify(exactly = 1) {
            sharedAnalytics.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT)
        }
        verify(exactly = 1) {
            sharedAnalytics.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT_GAME)
        }
    }
}
