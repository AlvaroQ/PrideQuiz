package com.quiz.pride.ui.result

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.quiz.domain.App
import com.quiz.domain.User
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.usecases.GetAppsRecommended
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetPersonalRecord
import com.quiz.usecases.GetRecordScore
import com.quiz.usecases.SaveTopScore
import com.quiz.usecases.SetPersonalRecord
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResultUiState(
    val isLoading: Boolean = true,
    val appsList: List<App> = emptyList(),
    val personalRecord: String = "0",
    val worldRecord: String = "0",
    val photoUrl: String = ""
)

sealed class ResultEvent {
    object ShowWorldRecordDialog : ResultEvent()
}

class ResultViewModel(
    private val getAppsRecommended: GetAppsRecommended,
    private val saveTopScore: SaveTopScore,
    private val getRecordScore: GetRecordScore,
    private val getPersonalRecord: GetPersonalRecord,
    private val setPersonalRecord: SetPersonalRecord,
    private val getPaymentDone: GetPaymentDone
) : ComposeViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ResultEvent>()
    val events = _events.asSharedFlow()

    init {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_RESULT)
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val apps = getAppsRecommended.invoke()
            val worldRecord = getRecordScore.invoke(1)

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    appsList = apps,
                    worldRecord = worldRecord
                )
            }
        }
    }

    fun checkPersonalRecord(points: Int) {
        val currentRecord = getPersonalRecord.invoke()
        if (points > currentRecord) {
            setPersonalRecord.invoke(points)
            _uiState.update { it.copy(personalRecord = points.toString()) }
        } else {
            _uiState.update { it.copy(personalRecord = currentRecord.toString()) }
        }
    }

    fun checkWorldRecord(gamePoints: Int) {
        viewModelScope.launch {
            val pointsLastClassified = getRecordScore.invoke(50)
            if (pointsLastClassified.isNotEmpty() && gamePoints > pointsLastClassified.toInt()) {
                AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_DIALOG_SAVE_SCORE)
                _events.emit(ResultEvent.ShowWorldRecordDialog)
            }
        }
    }

    fun saveScore(user: User) {
        viewModelScope.launch {
            saveTopScore.invoke(user)
        }
    }

    fun setPhotoUrl(url: String) {
        _uiState.update { it.copy(photoUrl = url) }
    }

    fun rateApp(context: Context) {
        AnalyticsManager.analyticsClicked(AnalyticsManager.BTN_RATE)
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=${context.packageName}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
}
