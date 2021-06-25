package com.quiz.pride.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quiz.domain.Pride
import com.quiz.pride.R
import com.quiz.pride.common.ResourceProvider
import com.quiz.pride.common.ScopedViewModel
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.ui.result.ResultViewModel
import com.quiz.pride.utils.Constants.TOTAL_PRIDES
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetPrideById
import kotlinx.coroutines.launch

class GameViewModel(private val getPrideById: GetPrideById,
                    private val resourceProvider: ResourceProvider,
                    private val getPaymentDone: GetPaymentDone) : ScopedViewModel() {
    private var randomCountries = mutableListOf<Int>()
    private lateinit var pride: Pride

    private val _question = MutableLiveData<Pride>()
    val question: LiveData<Pride> = _question

    private val _responseOptions = MutableLiveData<MutableList<String>>()
    val responseOptions: LiveData<MutableList<String>> = _responseOptions

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
            val numRandomMain = generateRandomWithExcusion(0, TOTAL_PRIDES, *randomCountries.toIntArray())
            randomCountries.add(numRandomMain)

            pride = getPride(numRandomMain)

            /** Generate responses */
            val numRandomMainPosition = generateRandomWithExcusion(0, 3)

            val numRandomOption1 = generateRandomWithExcusion(0, TOTAL_PRIDES, numRandomMain)
            val option1: Pride = getPride(numRandomOption1)
            val numRandomPosition1 = generateRandomWithExcusion(0, 3, numRandomMainPosition)

            val numRandomOption2 = generateRandomWithExcusion(0, TOTAL_PRIDES, numRandomMain, numRandomOption1)
            val option2: Pride = getPride(numRandomOption2)
            val numRandomPosition2 = generateRandomWithExcusion(0, 3, numRandomMainPosition, numRandomPosition1)

            val numRandomOption3 = generateRandomWithExcusion(0, TOTAL_PRIDES, numRandomMain, numRandomOption1, numRandomOption2)
            val option3: Pride = getPride(numRandomOption3)
            val numRandomPosition3 = generateRandomWithExcusion(0, 3, numRandomMainPosition, numRandomPosition1, numRandomPosition2)

            /** Save value */
            val optionList = mutableListOf("", "", "", "")
            optionList[numRandomMainPosition] = when {
                resourceProvider.getString(R.string.locale) == "es" -> pride.name?.ES!!
                resourceProvider.getString(R.string.locale) == "fr" -> pride.name?.FR!!
                resourceProvider.getString(R.string.locale) == "pt" -> pride.name?.PT!!
                resourceProvider.getString(R.string.locale) == "de" -> pride.name?.DE!!
                resourceProvider.getString(R.string.locale) == "it" -> pride.name?.IT!!
                else -> pride.name?.EN!!
            }

            optionList[numRandomPosition1] = when {
                resourceProvider.getString(R.string.locale) == "es" -> option1.name?.ES!!
                resourceProvider.getString(R.string.locale) == "fr" -> option1.name?.FR!!
                resourceProvider.getString(R.string.locale) == "pt" -> option1.name?.PT!!
                resourceProvider.getString(R.string.locale) == "de" -> option1.name?.DE!!
                resourceProvider.getString(R.string.locale) == "it" -> option1.name?.IT!!
                else -> option1.name?.EN!!
            }

            optionList[numRandomPosition2] = when {
                resourceProvider.getString(R.string.locale) == "es" -> option2.name?.ES!!
                resourceProvider.getString(R.string.locale) == "fr" -> option2.name?.FR!!
                resourceProvider.getString(R.string.locale) == "pt" -> option2.name?.PT!!
                resourceProvider.getString(R.string.locale) == "de" -> option2.name?.DE!!
                resourceProvider.getString(R.string.locale) == "it" -> option2.name?.IT!!
                else -> option2.name?.EN!!
            }

            optionList[numRandomPosition3] = when {
                resourceProvider.getString(R.string.locale) == "es" -> option3.name?.ES!!
                resourceProvider.getString(R.string.locale) == "fr" -> option3.name?.FR!!
                resourceProvider.getString(R.string.locale) == "pt" -> option3.name?.PT!!
                resourceProvider.getString(R.string.locale) == "de" -> option3.name?.DE!!
                resourceProvider.getString(R.string.locale) == "it" -> option3.name?.IT!!
                else -> option3.name?.EN!!
            }

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

    private fun generateRandomWithExcusion(start: Int, end: Int, vararg exclude: Int): Int {
        var numRandom = (start..end).random()
        while(exclude.contains(numRandom)){
            numRandom = (start..end).random()
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