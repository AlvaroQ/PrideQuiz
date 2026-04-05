package com.quiz.pride.ui.moreApps

import arrow.core.Either
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.App
import com.quiz.domain.Name
import com.quiz.pride.MainDispatcherRule
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.usecases.GetAppsRecommended
import com.quiz.usecases.GetPaymentDone
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
class MoreAppsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: MoreAppsViewModel

    private val getAppsRecommended: GetAppsRecommended = mockk()
    private val getPaymentDone: GetPaymentDone = mockk()
    private val analyticsManager: AnalyticsManager = mockk(relaxed = true)

    private fun buildApp(name: String) = App(
        image = "img_$name",
        localeName = Name(ES = name, EN = name),
        url = "https://example.com/$name",
        priority = 1
    )

    @Before
    fun setup() {
        every { getPaymentDone.invoke() } returns false
        coEvery { getAppsRecommended.invoke() } returns Either.Right(
            listOf(buildApp("App1"), buildApp("App2"))
        )

        viewModel = MoreAppsViewModel(
            getAppsRecommended = getAppsRecommended,
            getPaymentDone = getPaymentDone,
            analyticsManager = analyticsManager
        )
    }

    // =========================================================
    // Estado inicial / carga
    // =========================================================

    @Test
    fun `init carga apps correctamente y pone isLoading en false`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.appsList.size)
        assertFalse(state.hasError)
    }

    @Test
    fun `init registra pantalla MoreApps en Analytics`() {
        verify(exactly = 1) {
            analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_MORE_APPS)
        }
    }

    @Test
    fun `init cuando el usuario no pago showAd es true`() = runTest {
        every { getPaymentDone.invoke() } returns false

        viewModel = MoreAppsViewModel(
            getAppsRecommended = getAppsRecommended,
            getPaymentDone = getPaymentDone,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showAd)
    }

    @Test
    fun `init cuando el usuario pago showAd es false`() = runTest {
        every { getPaymentDone.invoke() } returns true

        viewModel = MoreAppsViewModel(
            getAppsRecommended = getAppsRecommended,
            getPaymentDone = getPaymentDone,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAd)
    }

    // =========================================================
    // Error handling
    // =========================================================

    @Test
    fun `loadApps con error en repository activa hasError`() = runTest {
        coEvery { getAppsRecommended.invoke() } returns Either.Left(RepositoryException.NoConnectionException)

        viewModel = MoreAppsViewModel(
            getAppsRecommended = getAppsRecommended,
            getPaymentDone = getPaymentDone,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.hasError)
        assertTrue(state.appsList.isEmpty())
    }

    @Test
    fun `loadApps con error mantiene showAd segun el estado de pago`() = runTest {
        every { getPaymentDone.invoke() } returns false
        coEvery { getAppsRecommended.invoke() } returns Either.Left(RepositoryException.DataNotFoundException)

        viewModel = MoreAppsViewModel(
            getAppsRecommended = getAppsRecommended,
            getPaymentDone = getPaymentDone,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        // Aun con error, showAd refleja el estado de pago
        assertTrue(viewModel.uiState.value.showAd)
    }

    // =========================================================
    // refresh()
    // =========================================================

    @Test
    fun `refresh recarga las apps correctamente`() = runTest {
        advanceUntilIdle()

        // Cambiar el mock para devolver 3 apps
        val nuevasApps = listOf(buildApp("A"), buildApp("B"), buildApp("C"))
        coEvery { getAppsRecommended.invoke() } returns Either.Right(nuevasApps)

        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.appsList.size)
        assertFalse(state.hasError)
    }

    @Test
    fun `refresh limpia el error previo cuando la carga es exitosa`() = runTest {
        coEvery { getAppsRecommended.invoke() } returns Either.Left(RepositoryException.NoConnectionException)

        viewModel = MoreAppsViewModel(
            getAppsRecommended = getAppsRecommended,
            getPaymentDone = getPaymentDone,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasError)

        // Ahora la red esta disponible
        coEvery { getAppsRecommended.invoke() } returns Either.Right(listOf(buildApp("Recovered")))
        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasError)
        assertEquals(1, viewModel.uiState.value.appsList.size)
    }

    @Test
    fun `refresh con lista vacia actualiza appsList a vacio`() = runTest {
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.appsList.size)

        coEvery { getAppsRecommended.invoke() } returns Either.Right(emptyList())
        viewModel.refresh()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.appsList.isEmpty())
        assertFalse(viewModel.uiState.value.hasError)
    }
}
