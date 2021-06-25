package com.quiz.pride.ui.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quiz.domain.App
import com.quiz.domain.User
import com.quiz.pride.common.ScopedViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.usecases.*
import kotlinx.coroutines.launch

class ResultViewModel(private val getAppsRecommended: GetAppsRecommended,
                      private val saveTopScore: SaveTopScore,
                      private val getRecordScore: GetRecordScore,
                      private val getPersonalRecord: GetPersonalRecord,
                      private val setPersonalRecord: SetPersonalRecord,
                      private val getPaymentDone: GetPaymentDone
) : ScopedViewModel() {

    private val _progress = MutableLiveData<UiModel>()
    val progress: LiveData<UiModel> = _progress

    private val _navigation = MutableLiveData<Navigation>()
    val navigation: LiveData<Navigation> = _navigation

    private val _list = MutableLiveData<MutableList<App>>()
    val list: LiveData<MutableList<App>> = _list

    private val _personalRecord = MutableLiveData<String>()
    val personalRecord: LiveData<String> = _personalRecord

    private val _worldRecord = MutableLiveData<String>()
    val worldRecord: LiveData<String> = _worldRecord

    private val _photoUrl = MutableLiveData<String>()
    val photoUrl: LiveData<String> = _photoUrl

    private val _showingAds = MutableLiveData<UiModel>()
    val showingAds: LiveData<UiModel> = _showingAds

    init {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_RESULT)
        launch {
            _progress.value = UiModel.Loading(true)
            _list.value = appsRecommended()
            _worldRecord.value = getPointsWorldRecord()
            _showingAds.value = UiModel.ShowAd(!getPaymentDone())
            _progress.value = UiModel.Loading(false)
        }
    }

    private suspend fun appsRecommended(): MutableList<App> {
        return getAppsRecommended.invoke()
    }

    private suspend fun getPointsWorldRecord(): String {
        return getRecordScore.invoke(1)
    }

    fun setPersonalRecordOnServer(gamePoints: Int) {
        launch {
            val pointsLastClassified = getRecordScore.invoke(50)
            if(gamePoints > pointsLastClassified.toInt()) {
                showDialogToSaveGame(gamePoints.toString())
            }
        }
    }

    fun getPersonalRecord(points: Int) {
        val personalRecordPoints = getPersonalRecord.invoke()
        if(points > personalRecordPoints) {
            savePersonalRecord(points)
            _personalRecord.value = points.toString()
        } else {
            _personalRecord.value = personalRecordPoints.toString()
        }
    }

    private fun savePersonalRecord(record: Int) {
        setPersonalRecord.invoke(record)
    }

    private fun showDialogToSaveGame(points: String) {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_DIALOG_SAVE_SCORE)
        _navigation.value = Navigation.Dialog(points)
    }

    fun onAppClicked(url: String) {
        AnalyticsManager.analyticsAppRecommendedOpen(url)
        _navigation.value = Navigation.Open(url)
    }

    fun navigateToGame() {
        AnalyticsManager.analyticsClicked(AnalyticsManager.BTN_PLAY_AGAIN)
        _navigation.value = Navigation.Game
    }

    fun navigateToRate() {
        AnalyticsManager.analyticsClicked(AnalyticsManager.BTN_RATE)
        _navigation.value = Navigation.Rate
    }

    fun navigateToRanking() {
        AnalyticsManager.analyticsClicked(AnalyticsManager.BTN_RANKING)
        _navigation.value = Navigation.Ranking
    }

    fun navigateToShare(points: Int) {
        AnalyticsManager.analyticsClicked(AnalyticsManager.BTN_SHARE)
        _navigation.value = Navigation.Share(points)
    }
    fun saveTopScore(user: User) {
        launch {
            saveTopScore.invoke(user)
        }
    }

    fun clickOnPicker() {
        _navigation.value = Navigation.PickerImage
    }

    fun setImage(image: String?) {
        _photoUrl.value = image
    }

    sealed class Navigation {
        data class Share(val points: Int) : Navigation()
        object Rate : Navigation()
        object Game : Navigation()
        object Ranking : Navigation()
        data class Dialog(val points : String): Navigation()
        data class Open(val url : String): Navigation()
        object PickerImage : Navigation()
    }

    sealed class UiModel {
        data class Loading(val show: Boolean) : UiModel()
        data class ShowAd(val show: Boolean) : UiModel()
    }
}