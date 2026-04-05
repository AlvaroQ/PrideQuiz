package com.quiz.pride.ui.settings

import app.cash.turbine.test
import com.quiz.pride.MainDispatcherRule
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.BillingManager
import com.quiz.pride.managers.ConsentManager
import com.quiz.pride.managers.PurchaseResult
import com.quiz.pride.managers.ThemeManager
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.SetPaymentDone
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SettingsViewModel

    private val setPaymentDone: SetPaymentDone = mockk()
    private val getPaymentDone: GetPaymentDone = mockk()
    private val themeManager: ThemeManager = mockk()
    private val analyticsManager: AnalyticsManager = mockk(relaxed = true)
    private val billingManager: BillingManager = mockk(relaxed = true)
    private val consentManager: ConsentManager = mockk(relaxed = true)
    private val purchaseResultFlow = MutableSharedFlow<PurchaseResult>()

    private fun setupThemeManagerDefaults() {
        every { themeManager.isDarkMode } returns flowOf(false)
        every { themeManager.isSoundEnabled } returns flowOf(true)
        every { themeManager.isDynamicColorsEnabled } returns flowOf(true)
        every { themeManager.isHighContrastEnabled } returns flowOf(false)
        every { themeManager.isLargeTextEnabled } returns flowOf(false)
    }

    @Before
    fun setup() {
        setupThemeManagerDefaults()
        every { getPaymentDone.invoke() } returns false
        every { billingManager.purchaseResult } returns purchaseResultFlow

        viewModel = SettingsViewModel(
            setPaymentDone = setPaymentDone,
            getPaymentDone = getPaymentDone,
            themeManager = themeManager,
            analyticsManager = analyticsManager,
            billingManager = billingManager,
            consentManager = consentManager
        )
    }

    // =========================================================
    // Estado inicial
    // =========================================================

    @Test
    fun `estado inicial tiene showAds = true cuando payment no esta hecho`() {
        every { getPaymentDone.invoke() } returns false

        viewModel = SettingsViewModel(
            setPaymentDone = setPaymentDone,
            getPaymentDone = getPaymentDone,
            themeManager = themeManager,
            analyticsManager = analyticsManager,
            billingManager = billingManager,
            consentManager = consentManager
        )

        assertTrue(viewModel.uiState.value.showAds)
    }

    @Test
    fun `estado inicial tiene showAds = false cuando payment ya esta hecho`() {
        every { getPaymentDone.invoke() } returns true

        viewModel = SettingsViewModel(
            setPaymentDone = setPaymentDone,
            getPaymentDone = getPaymentDone,
            themeManager = themeManager,
            analyticsManager = analyticsManager,
            billingManager = billingManager,
            consentManager = consentManager
        )

        assertFalse(viewModel.uiState.value.showAds)
    }

    // =========================================================
    // Theme — setDarkMode
    // =========================================================

    @Test
    fun `setDarkMode en true llama a themeManager con true`() = runTest {
        coJustRun { themeManager.setDarkMode(true) }

        viewModel.setDarkMode(true)
        advanceUntilIdle()

        coVerify { themeManager.setDarkMode(true) }
    }

    @Test
    fun `setDarkMode en false llama a themeManager con false`() = runTest {
        coJustRun { themeManager.setDarkMode(false) }

        viewModel.setDarkMode(false)
        advanceUntilIdle()

        coVerify { themeManager.setDarkMode(false) }
    }

    // =========================================================
    // Sound — setSoundEnabled
    // =========================================================

    @Test
    fun `setSoundEnabled en false llama a themeManager con false`() = runTest {
        coJustRun { themeManager.setSoundEnabled(false) }

        viewModel.setSoundEnabled(false)
        advanceUntilIdle()

        coVerify { themeManager.setSoundEnabled(false) }
    }

    @Test
    fun `setSoundEnabled en true llama a themeManager con true`() = runTest {
        coJustRun { themeManager.setSoundEnabled(true) }

        viewModel.setSoundEnabled(true)
        advanceUntilIdle()

        coVerify { themeManager.setSoundEnabled(true) }
    }

    // =========================================================
    // DynamicColors
    // =========================================================

    @Test
    fun `setDynamicColorsEnabled delega al themeManager`() = runTest {
        coJustRun { themeManager.setDynamicColorsEnabled(false) }

        viewModel.setDynamicColorsEnabled(false)
        advanceUntilIdle()

        coVerify { themeManager.setDynamicColorsEnabled(false) }
    }

    // =========================================================
    // Accessibility
    // =========================================================

    @Test
    fun `setHighContrastEnabled delega al themeManager`() = runTest {
        coJustRun { themeManager.setHighContrastEnabled(true) }

        viewModel.setHighContrastEnabled(true)
        advanceUntilIdle()

        coVerify { themeManager.setHighContrastEnabled(true) }
    }

    @Test
    fun `setLargeTextEnabled delega al themeManager`() = runTest {
        coJustRun { themeManager.setLargeTextEnabled(true) }

        viewModel.setLargeTextEnabled(true)
        advanceUntilIdle()

        coVerify { themeManager.setLargeTextEnabled(true) }
    }

    // =========================================================
    // Billing — onRemoveAdsClick
    // =========================================================

    @Test
    fun `onRemoveAdsClick emite LaunchBillingFlow y activa isPurchasing`() = runTest {
        viewModel.events.test {
            viewModel.onRemoveAdsClick()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isPurchasing)
            val event = awaitItem()
            assertTrue(event is SettingsEvent.LaunchBillingFlow)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `PurchaseResult Success guarda payment, desactiva ads y emite PurchaseSuccess`() = runTest {
        every { setPaymentDone.invoke(true) } just runs

        viewModel.events.test {
            purchaseResultFlow.emit(PurchaseResult.Success)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.showAds)
            assertFalse(state.isPurchasing)

            val event = awaitItem()
            assertTrue(event is SettingsEvent.PurchaseSuccess)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `PurchaseResult Canceled desactiva isPurchasing`() = runTest {
        purchaseResultFlow.emit(PurchaseResult.Canceled)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isPurchasing)
    }

    @Test
    fun `PurchaseResult Error desactiva isPurchasing y emite PurchaseError`() = runTest {
        viewModel.events.test {
            purchaseResultFlow.emit(PurchaseResult.Error("test error"))
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isPurchasing)
            val event = awaitItem()
            assertTrue(event is SettingsEvent.PurchaseError)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
