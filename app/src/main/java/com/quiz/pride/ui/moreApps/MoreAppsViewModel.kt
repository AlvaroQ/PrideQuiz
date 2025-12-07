package com.quiz.pride.ui.moreApps

import androidx.lifecycle.viewModelScope
import com.quiz.domain.App
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.usecases.GetAppsRecommended
import com.quiz.usecases.GetPaymentDone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MoreAppsUiState(
    val isLoading: Boolean = true,
    val appsList: List<App> = emptyList(),
    val showAd: Boolean = false
)

class MoreAppsViewModel(
    private val getAppsRecommended: GetAppsRecommended,
    private val getPaymentDone: GetPaymentDone
) : ComposeViewModel() {

    private val _uiState = MutableStateFlow(MoreAppsUiState())
    val uiState: StateFlow<MoreAppsUiState> = _uiState.asStateFlow()

    init {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_MORE_APPS)
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val apps = getAppsRecommended.invoke()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    appsList = apps,
                    showAd = !getPaymentDone()
                )
            }
        }
    }
}
