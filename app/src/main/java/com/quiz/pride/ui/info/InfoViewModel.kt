package com.quiz.pride.ui.info

import androidx.lifecycle.viewModelScope
import com.quiz.domain.Pride
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetPrideList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InfoUiState(
    val isLoading: Boolean = true,
    val prideList: List<Pride> = emptyList(),
    val showBannerAd: Boolean = true,
    val currentPage: Int = 0
)

class InfoViewModel(
    private val getPrideList: GetPrideList,
    private val getPaymentDone: GetPaymentDone
) : ComposeViewModel() {

    private val _uiState = MutableStateFlow(InfoUiState())
    val uiState: StateFlow<InfoUiState> = _uiState.asStateFlow()

    init {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_INFO)
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val initialList = getPrideList.invoke(0)

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    prideList = initialList,
                    showBannerAd = !getPaymentDone(),
                    currentPage = 0
                )
            }
        }
    }

    fun loadMorePrideList() {
        val currentState = _uiState.value
        if (currentState.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val nextPage = currentState.currentPage + 1
            val newItems = getPrideList.invoke(nextPage)

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    prideList = state.prideList + newItems,
                    currentPage = nextPage
                )
            }
        }
    }
}
