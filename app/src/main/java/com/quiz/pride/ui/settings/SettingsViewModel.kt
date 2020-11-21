package com.quiz.pride.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quiz.pride.common.ScopedViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.SetPaymentDone
import kotlinx.coroutines.launch

class SettingsViewModel(private val setPaymentDone: SetPaymentDone,
                        private val getPaymentDone: GetPaymentDone) : ScopedViewModel() {

    private val _showingAds = MutableLiveData<UiModel>()
    val showingAds: LiveData<UiModel> = _showingAds

    init {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SETTINGS)
        _showingAds.value = UiModel.ShowAd(!getPaymentDone())
    }

    fun savePaymentDone() {
        launch {
            setPaymentDone.invoke(true)
            _showingAds.value = UiModel.ShowAd(false)
        }
    }

    private fun getPaymentDone(): Boolean {
        return getPaymentDone.invoke()
    }

    sealed class UiModel {
        data class ShowAd(val show: Boolean) : UiModel()
    }
}