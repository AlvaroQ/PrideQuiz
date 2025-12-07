package com.quiz.pride.common

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Legacy ScopedViewModel - kept for backwards compatibility during migration
 */
abstract class ScopedViewModel(uiDispatcher: CoroutineDispatcher = Dispatchers.Main): ViewModel(), Scope by Scope.Impl(
    uiDispatcher
) {
    init {
        this.initScope()
    }

    @CallSuper
    override fun onCleared() {
        destroyScope()
        super.onCleared()
    }
}

/**
 * Modern base ViewModel for Compose with StateFlow support
 */
abstract class ComposeViewModel : ViewModel() {

    /**
     * Helper function to update StateFlow value
     */
    protected fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
        value = transform(value)
    }

    /**
     * Launch a coroutine in the ViewModel scope
     */
    protected fun launchInScope(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }

    /**
     * Launch a coroutine in IO dispatcher
     */
    protected fun launchInIO(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            block()
        }
    }
}