package com.quiz.pride.ui.info

import arrow.core.Either
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.Name
import com.quiz.domain.Pride
import com.quiz.pride.MainDispatcherRule
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetPrideList
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
class InfoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: InfoViewModel

    private val getPrideList: GetPrideList = mockk()
    private val getPaymentDone: GetPaymentDone = mockk()
    private val analyticsManager: AnalyticsManager = mockk(relaxed = true)

    private fun buildPridePage(pageIndex: Int, count: Int = 10): List<Pride> =
        (0 until count).map { i ->
            Pride(
                name = Name(ES = "Orgullo pagina${pageIndex}_$i"),
                flag = "flag_${pageIndex}_$i"
            )
        }

    @Before
    fun setup() {
        every { getPaymentDone.invoke() } returns false
        coEvery { getPrideList.invoke(0) } returns Either.Right(buildPridePage(0))

        viewModel = InfoViewModel(
            getPrideList = getPrideList,
            getPaymentDone = getPaymentDone,
            analyticsManager = analyticsManager
        )
    }

    // =========================================================
    // loadInitialData — llamado desde init
    // =========================================================

    @Test
    fun `loadInitialData carga la primera pagina correctamente`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.hasError)
        assertEquals(10, state.prideList.size)
        assertEquals(0, state.currentPage)
    }

    @Test
    fun `loadInitialData con error marca hasError en true`() = runTest {
        coEvery { getPrideList.invoke(0) } returns Either.Left(RepositoryException.NoConnectionException)

        viewModel = InfoViewModel(
            getPrideList = getPrideList,
            getPaymentDone = getPaymentDone,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.hasError)
        assertTrue(state.prideList.isEmpty())
    }

    // =========================================================
    // loadMorePrideList
    // =========================================================

    @Test
    fun `loadMorePrideList incrementa la pagina y acumula los items`() = runTest {
        advanceUntilIdle()

        coEvery { getPrideList.invoke(1) } returns Either.Right(buildPridePage(1, 10))

        viewModel.loadMorePrideList()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.currentPage)
        assertEquals(20, state.prideList.size) // pagina 0 + pagina 1
    }

    @Test
    fun `loadMorePrideList con error marca hasError pero mantiene los items previos`() = runTest {
        advanceUntilIdle()

        coEvery { getPrideList.invoke(1) } returns Either.Left(RepositoryException.NoConnectionException)

        viewModel.loadMorePrideList()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasError)
        // La lista previa de la pagina 0 se mantiene
        assertEquals(10, state.prideList.size)
    }

    @Test
    fun `loadMorePrideList llamadas secuenciales acumulan paginas correctamente`() = runTest {
        // Realizamos la carga inicial
        advanceUntilIdle()

        // Configuramos mocks para paginas 1 y 2
        coEvery { getPrideList.invoke(1) } returns Either.Right(buildPridePage(1))
        coEvery { getPrideList.invoke(2) } returns Either.Right(buildPridePage(2))

        // La primera llamada ejecuta y completa (UnconfinedTestDispatcher es sincrono)
        viewModel.loadMorePrideList()
        advanceUntilIdle()

        // En este punto currentPage = 1, isLoading = false
        assertEquals(1, viewModel.uiState.value.currentPage)

        // Segunda llamada ejecuta correctamente porque isLoading ya es false
        viewModel.loadMorePrideList()
        advanceUntilIdle()

        // Ahora tenemos pagina 0 + pagina 1 + pagina 2
        assertEquals(2, viewModel.uiState.value.currentPage)
        assertEquals(30, viewModel.uiState.value.prideList.size)
    }

    @Test
    fun `loadMorePrideList multiple veces incrementa pagina en cada llamada`() = runTest {
        advanceUntilIdle()

        coEvery { getPrideList.invoke(1) } returns Either.Right(buildPridePage(1, 5))
        coEvery { getPrideList.invoke(2) } returns Either.Right(buildPridePage(2, 5))

        viewModel.loadMorePrideList()
        advanceUntilIdle()

        viewModel.loadMorePrideList()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.currentPage)
        assertEquals(20, state.prideList.size) // 10 + 5 + 5
    }

    // =========================================================
    // Ads
    // =========================================================

    @Test
    fun `con payment done no muestra banner ad`() = runTest {
        every { getPaymentDone.invoke() } returns true

        viewModel = InfoViewModel(
            getPrideList = getPrideList,
            getPaymentDone = getPaymentDone,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showBannerAd)
    }

    @Test
    fun `sin payment done muestra banner ad`() = runTest {
        every { getPaymentDone.invoke() } returns false

        viewModel = InfoViewModel(
            getPrideList = getPrideList,
            getPaymentDone = getPaymentDone,
            analyticsManager = analyticsManager
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showBannerAd)
    }
}
