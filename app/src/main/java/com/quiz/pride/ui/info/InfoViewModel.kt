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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InfoUiState(
    val isLoading: Boolean = true,
    val prideList: List<Pride> = emptyList(),
    val showBannerAd: Boolean = true,
    val currentPage: Int = 0,
    val hasError: Boolean = false
)

class InfoViewModel(
    private val getPrideList: GetPrideList,
    private val getPaymentDone: GetPaymentDone,
    private val analyticsManager: AnalyticsManager
) : ComposeViewModel() {

    private val _uiState = MutableStateFlow(InfoUiState())
    val uiState: StateFlow<InfoUiState> = _uiState.asStateFlow()

    init {
        analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_INFO)
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hasError = false) }

            val showAd = !getPaymentDone()

            getPrideList.invoke(0).fold(
                ifLeft = {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            showBannerAd = showAd,
                            hasError = true
                        )
                    }
                },
                ifRight = { initialList ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            prideList = initialList,
                            showBannerAd = showAd,
                            currentPage = 0,
                            hasError = false
                        )
                    }
                }
            )
        }
    }

    fun loadMorePrideList() {
        viewModelScope.launch {
            // Lectura del estado DENTRO del launch para evitar race condition:
            // si se llama dos veces rapido, ambas corutinas leen el estado actualizado
            // y la segunda detecta isLoading = true y retorna sin cargar la misma pagina.
            val currentState = _uiState.value
            if (currentState.isLoading) return@launch

            _uiState.update { it.copy(isLoading = true, hasError = false) }

            val nextPage = currentState.currentPage + 1

            getPrideList.invoke(nextPage).fold(
                ifLeft = {
                    _uiState.update { state ->
                        state.copy(isLoading = false, hasError = true)
                    }
                },
                ifRight = { newItems ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            prideList = state.prideList + newItems,
                            currentPage = nextPage,
                            hasError = false
                        )
                    }
                }
            )
        }
    }
}
