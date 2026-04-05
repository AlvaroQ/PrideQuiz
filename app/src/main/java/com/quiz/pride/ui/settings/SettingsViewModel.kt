package com.quiz.pride.ui.settings

import androidx.lifecycle.viewModelScope
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.BillingManager
import com.quiz.pride.managers.ConsentManager
import com.quiz.pride.managers.PurchaseResult
import com.quiz.pride.managers.ThemeManager
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.SetPaymentDone
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val showAds: Boolean = true,
    val isPurchasing: Boolean = false
)

/**
 * Estado combinado de las 5 preferencias de tema/accesibilidad.
 * Se actualiza en un solo StateFlow para evitar 5 suscripciones independientes.
 */
data class SettingsPrefsState(
    val isDarkMode: Boolean = false,
    val isSoundEnabled: Boolean = true,
    val isDynamicColorsEnabled: Boolean = true,
    val isHighContrastEnabled: Boolean = false,
    val isLargeTextEnabled: Boolean = false
)

sealed class SettingsEvent {
    data object LaunchBillingFlow : SettingsEvent()
    data object PurchaseSuccess : SettingsEvent()
    data object PurchaseError : SettingsEvent()
    data object ConsentReset : SettingsEvent()
    data object RestoreSuccess : SettingsEvent()
    data object RestoreNoPurchases : SettingsEvent()
}

class SettingsViewModel(
    private val setPaymentDone: SetPaymentDone,
    private val getPaymentDone: GetPaymentDone,
    private val themeManager: ThemeManager,
    private val analyticsManager: AnalyticsManager,
    private val billingManager: BillingManager,
    private val consentManager: ConsentManager
) : ComposeViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    // Flag para distinguir restore de compra nueva en el handler de purchaseResult
    private var isRestoringPurchases = false

    // Todas las preferencias de tema y accesibilidad combinadas en un solo StateFlow.
    // Reduce 5 suscripciones independientes a 1 sola lectura del DataStore de ThemeManager.
    val prefsState: StateFlow<SettingsPrefsState> = combine(
        themeManager.isDarkMode,
        themeManager.isSoundEnabled,
        themeManager.isDynamicColorsEnabled,
        themeManager.isHighContrastEnabled,
        themeManager.isLargeTextEnabled
    ) { darkMode, sound, dynamicColors, highContrast, largeText ->
        SettingsPrefsState(
            isDarkMode = darkMode,
            isSoundEnabled = sound,
            isDynamicColorsEnabled = dynamicColors,
            isHighContrastEnabled = highContrast,
            isLargeTextEnabled = largeText
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsPrefsState()
    )

    init {
        analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SETTINGS)
        loadPaymentStatus()
        observePurchaseResults()
    }

    private fun loadPaymentStatus() {
        val showAds = !getPaymentDone()
        _uiState.update { it.copy(showAds = showAds) }
    }

    /**
     * Escucha los resultados de compra del BillingManager y los traduce
     * a eventos de UI y actualizaciones de estado.
     */
    private fun observePurchaseResults() {
        billingManager.purchaseResult
            .onEach { result ->
                when (result) {
                    is PurchaseResult.Success -> {
                        analyticsManager.analyticsPurchaseResult("success")
                        setPaymentDone.invoke(true)
                        _uiState.update { it.copy(showAds = false, isPurchasing = false) }
                        _events.emit(SettingsEvent.PurchaseSuccess)
                    }
                    is PurchaseResult.AlreadyOwned -> {
                        analyticsManager.analyticsPurchaseResult("already_owned")
                        setPaymentDone.invoke(true)
                        _uiState.update { it.copy(showAds = false, isPurchasing = false) }
                        if (isRestoringPurchases) {
                            isRestoringPurchases = false
                            _events.emit(SettingsEvent.RestoreSuccess)
                        } else {
                            _events.emit(SettingsEvent.PurchaseSuccess)
                        }
                    }
                    is PurchaseResult.Pending -> {
                        analyticsManager.analyticsPurchaseResult("pending")
                        _uiState.update { it.copy(isPurchasing = false) }
                    }
                    is PurchaseResult.Canceled -> {
                        analyticsManager.analyticsPurchaseResult("canceled")
                        _uiState.update { it.copy(isPurchasing = false) }
                    }
                    is PurchaseResult.Error -> {
                        _uiState.update { it.copy(isPurchasing = false) }
                        if (isRestoringPurchases) {
                            isRestoringPurchases = false
                            if (result.message == "NO_PURCHASES_FOUND") {
                                _events.emit(SettingsEvent.RestoreNoPurchases)
                            } else {
                                analyticsManager.analyticsPurchaseResult("error")
                                _events.emit(SettingsEvent.PurchaseError)
                            }
                        } else {
                            analyticsManager.analyticsPurchaseResult("error")
                            _events.emit(SettingsEvent.PurchaseError)
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun initBilling() {
        billingManager.initBilling()
    }

    fun releaseBilling() {
        billingManager.release()
    }

    fun launchBillingFlow(activity: android.app.Activity) {
        _uiState.update { it.copy(isPurchasing = true) }
        billingManager.launchBillingFlow(activity)
    }

    fun onRestorePurchases() {
        isRestoringPurchases = true
        billingManager.restorePurchases()
    }

    fun onRemoveAdsClick() {
        analyticsManager.analyticsPurchaseIntent("remove_ads")
        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true) }
            _events.emit(SettingsEvent.LaunchBillingFlow)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        analyticsManager.analyticsSettingChanged("dark_mode", enabled.toString())
        viewModelScope.launch {
            themeManager.setDarkMode(enabled)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        analyticsManager.analyticsSettingChanged("sound", enabled.toString())
        viewModelScope.launch {
            themeManager.setSoundEnabled(enabled)
        }
    }

    fun setDynamicColorsEnabled(enabled: Boolean) {
        analyticsManager.analyticsSettingChanged("dynamic_colors", enabled.toString())
        viewModelScope.launch {
            themeManager.setDynamicColorsEnabled(enabled)
        }
    }

    fun setHighContrastEnabled(enabled: Boolean) {
        analyticsManager.analyticsSettingChanged("high_contrast", enabled.toString())
        viewModelScope.launch {
            themeManager.setHighContrastEnabled(enabled)
        }
    }

    fun setLargeTextEnabled(enabled: Boolean) {
        analyticsManager.analyticsSettingChanged("large_text", enabled.toString())
        viewModelScope.launch {
            themeManager.setLargeTextEnabled(enabled)
        }
    }

    /**
     * Resetea el estado de consentimiento de ads.
     * Solo disponible en builds DEBUG para testing del flujo UMP/GDPR.
     * En produccion este metodo no hace nada.
     */
    fun resetAdConsent() {
        viewModelScope.launch {
            consentManager.resetConsent()
            _events.emit(SettingsEvent.ConsentReset)
        }
    }

    // --- Tracking de acciones de usuario ---

    fun onRateClicked() {
        analyticsManager.analyticsClicked(AnalyticsManager.BTN_RATE)
    }

    fun onShareClicked() {
        analyticsManager.analyticsClicked(AnalyticsManager.BTN_SHARE)
    }
}