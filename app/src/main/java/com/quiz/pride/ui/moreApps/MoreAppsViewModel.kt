package com.quiz.pride.ui.moreApps

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.quiz.domain.App
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.usecases.GetAppsRecommended
import com.quiz.usecases.GetPaymentDone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class MoreAppsUiState(
    val isLoading: Boolean = true,
    val appsList: List<App> = emptyList(),
    val showAd: Boolean = false,
    val hasError: Boolean = false
)

class MoreAppsViewModel(
    private val getAppsRecommended: GetAppsRecommended,
    private val getPaymentDone: GetPaymentDone,
    private val analyticsManager: AnalyticsManager
) : ComposeViewModel() {

    private val _uiState = MutableStateFlow(MoreAppsUiState())
    val uiState: StateFlow<MoreAppsUiState> = _uiState.asStateFlow()

    init {
        analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_MORE_APPS)
        loadApps()
    }

    /** Recarga la lista de apps. Puede invocarse desde pull-to-refresh o retry. */
    fun refresh() {
        loadApps()
    }

    fun onAppClicked(appName: String) {
        analyticsManager.analyticsAppRecommendedOpen(appName)
    }

    private fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hasError = false) }

            val showAd = !getPaymentDone()

            getAppsRecommended.invoke().fold(
                ifLeft = {
                    _uiState.update { state ->
                        state.copy(isLoading = false, showAd = showAd, hasError = true)
                    }
                },
                ifRight = { apps ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            appsList = apps,
                            showAd = showAd,
                            hasError = false
                        )
                    }
                }
            )
        }
    }
}
