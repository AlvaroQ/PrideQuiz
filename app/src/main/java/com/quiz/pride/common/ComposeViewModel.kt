package com.quiz.pride.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * ViewModel base para la app. Extiende ViewModel con utilidades comunes.
 *
 * Todos los ViewModels de la app heredan de esta clase para:
 * - Tener acceso a [launchSafe] como alternativa a viewModelScope.launch con manejo de errores
 * - Punto unico para agregar comportamiento base futuro sin tocar cada ViewModel
 */
abstract class ComposeViewModel : ViewModel() {

    /**
     * Lanza una coroutine en viewModelScope con manejo de errores integrado.
     * Relanza [CancellationException] para respetar el ciclo de vida de las coroutines.
     *
     * Uso:
     * ```kotlin
     * launchSafe(onError = { e -> _uiState.update { it.copy(error = e.message) } }) {
     *     val data = repository.fetchData()
     *     _uiState.update { it.copy(data = data) }
     * }
     * ```
     */
    protected fun launchSafe(
        onError: (Throwable) -> Unit = {},
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e  // Siempre relanzar CancellationException
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }
}
