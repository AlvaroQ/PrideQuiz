package com.quiz.pride.ui.select

import com.quiz.pride.managers.AnalyticsManager
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SelectViewModelTest {

    private val analyticsManager: AnalyticsManager = mockk(relaxed = true)

    private lateinit var viewModel: SelectViewModel

    @Before
    fun setup() {
        viewModel = SelectViewModel(analyticsManager = analyticsManager)
    }

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
    fun `init registra la pantalla correcta y no otra`() {
        verify(exactly = 0) {
            analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT_GAME)
        }
        verify(exactly = 0) {
            analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_GAME)
        }
    }

    @Test
    fun `multiples instancias registran la pantalla cada una por separado`() {
        val analyticsManager2: AnalyticsManager = mockk(relaxed = true)
        SelectViewModel(analyticsManager = analyticsManager2)

        // Cada instancia registra exactamente una vez su propia pantalla
        verify(exactly = 1) {
            analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT)
        }
        verify(exactly = 1) {
            analyticsManager2.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT)
        }
    }
}
