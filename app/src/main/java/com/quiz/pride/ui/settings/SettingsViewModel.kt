package com.quiz.pride.ui.settings

import androidx.lifecycle.viewModelScope
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.ThemeManager
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.SetPaymentDone
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val showAds: Boolean = true,
    val isPurchasing: Boolean = false
)

sealed class SettingsEvent {
    object LaunchBillingFlow : SettingsEvent()
    object PurchaseSuccess : SettingsEvent()
    object PurchaseError : SettingsEvent()
}

class SettingsViewModel(
    private val setPaymentDone: SetPaymentDone,
    private val getPaymentDone: GetPaymentDone,
    private val themeManager: ThemeManager
) : ComposeViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    // Theme settings from ThemeManager
    val isDarkMode: StateFlow<Boolean> = themeManager.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isSoundEnabled: StateFlow<Boolean> = themeManager.isSoundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isDynamicColorsEnabled: StateFlow<Boolean> = themeManager.isDynamicColorsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SETTINGS)
        loadPaymentStatus()
    }

    private fun loadPaymentStatus() {
        val showAds = !getPaymentDone()
        _uiState.update { it.copy(showAds = showAds) }
    }

    fun onRemoveAdsClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true) }
            _events.emit(SettingsEvent.LaunchBillingFlow)
        }
    }

    fun onPurchaseComplete() {
        viewModelScope.launch {
            setPaymentDone.invoke(true)
            _uiState.update { it.copy(showAds = false, isPurchasing = false) }
            _events.emit(SettingsEvent.PurchaseSuccess)
        }
    }

    fun onPurchaseCancelled() {
        _uiState.update { it.copy(isPurchasing = false) }
    }

    fun onPurchaseError() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = false) }
            _events.emit(SettingsEvent.PurchaseError)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setDarkMode(enabled)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setSoundEnabled(enabled)
        }
    }

    fun setDynamicColorsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setDynamicColorsEnabled(enabled)
        }
    }
}