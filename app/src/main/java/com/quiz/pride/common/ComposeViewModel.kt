package com.quiz.pride.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

abstract class ComposeViewModel : ViewModel() {

    protected fun launchInScope(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }

    protected fun launchInIO(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            block()
        }
    }
}
