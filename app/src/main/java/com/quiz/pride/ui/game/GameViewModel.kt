package com.quiz.pride.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quiz.domain.Pride
import com.quiz.pride.common.ScopedViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.utils.Constants.TOTAL_PRIDES
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetPrideById
import kotlinx.coroutines.launch

class GameViewModel(private val getPrideById: GetPrideById,
                    private val getPaymentDone: GetPaymentDone) : ScopedViewModel() {

    private var randomCountries = mutableListOf<Int>()
    private lateinit var pride: Pride

    private val _question = MutableLiveData<Pride>()
    val question: LiveData<Pride> = _question

    private val _responseOptions = MutableLiveData<MutableList<Pride>>()
    val responseOptions: LiveData<MutableList<Pride>> = _responseOptions

    private val _progress = MutableLiveData<UiModel>()
    val progress: LiveData<UiModel> = _progress

    private val _navigation = MutableLiveData<Navigation>()
    val navigation: LiveData<Navigation> = _navigation

    private val _showingAds = MutableLiveData<UiModel>()
    val showingAds: LiveData<UiModel> = _showingAds

    init {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_GAME)
        generateNewStage()
        _showingAds.value = UiModel.ShowBannerAd(!getPaymentDone())
    }

    fun showRewardedAd() {
        _showingAds.value = UiModel.ShowReewardAd(!getPaymentDone())
    }

    fun generateNewStage() {
        launch {
            _progress.value = UiModel.Loading(true)

            /** Generate question */
            val numRandomMain = generateRandomWithExcusion(TOTAL_PRIDES, *randomCountries.toIntArray())
            randomCountries.add(numRandomMain)

            pride = getPride(numRandomMain)

            /** Generate responses */
            val numRandomMainPosition = generateRandomWithExcusion(3)

            val numRandomOption1 = generateRandomWithExcusion(TOTAL_PRIDES, numRandomMain)
            val option1: Pride = getPride(numRandomOption1)
            val numRandomPosition1 = generateRandomWithExcusion( 3, numRandomMainPosition)

            val numRandomOption2 = generateRandomWithExcusion(TOTAL_PRIDES, numRandomMain, numRandomOption1)
            val option2: Pride = getPride(numRandomOption2)
            val numRandomPosition2 = generateRandomWithExcusion(3, numRandomMainPosition, numRandomPosition1)

            val numRandomOption3 = generateRandomWithExcusion(TOTAL_PRIDES, numRandomMain, numRandomOption1, numRandomOption2)
            val option3: Pride = getPride(numRandomOption3)
            val numRandomPosition3 = generateRandomWithExcusion(3, numRandomMainPosition, numRandomPosition1, numRandomPosition2)

            /** Save value */
            val optionList = mutableListOf(Pride(), Pride(), Pride(), Pride())
            optionList[numRandomMainPosition] = pride
            optionList[numRandomPosition1] = option1
            optionList[numRandomPosition2] = option2
            optionList[numRandomPosition3] = option3

            _responseOptions.value = optionList
            _question.value = pride
            _progress.value = UiModel.Loading(false)
        }
    }

    private suspend fun getPride(id: Int): Pride {
        return getPrideById.invoke(id)
    }

    fun navigateToResult(points: String) {
        AnalyticsManager.analyticsGameFinished(points)
        _navigation.value = Navigation.Result
    }

    fun getPride() : Pride {
        return pride
    }

    fun navigateToExtraLifeDialog() {
        if(!getPaymentDone()) _navigation.value = Navigation.ExtraLifeDialog
        else _navigation.value = Navigation.Result
    }

    private fun generateRandomWithExcusion(end: Int, vararg exclude: Int): Int {
        var numRandom = (0..end).random()
        while(exclude.contains(numRandom)){
            numRandom = (0..end).random()
        }
        return numRandom
    }

    sealed class UiModel {
        data class Loading(val show: Boolean) : UiModel()
        data class ShowBannerAd(val show: Boolean) : UiModel()
        data class ShowReewardAd(val show: Boolean) : UiModel()
    }

    sealed class Navigation {
        object Result : Navigation()
        object ExtraLifeDialog : Navigation()
    }
}