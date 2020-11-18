package com.quiz.pride.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quiz.domain.Pride
import com.quiz.pride.R
import com.quiz.pride.common.ResourceProvider
import com.quiz.pride.common.ScopedViewModel
import com.quiz.pride.managers.Analytics
import com.quiz.pride.utils.Constants.TOTAL_PRIDES
import com.quiz.usecases.GetPrideById
import kotlinx.coroutines.launch

class GameViewModel(private val getPrideById: GetPrideById,
                    private val resourceProvider: ResourceProvider) : ScopedViewModel() {
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

    init {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_GAME)
        generateNewStage()
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
            optionList[numRandomMainPosition] =  if(resourceProvider.getString(R.string.locale) == "en") pride.name?.EN!! else pride.name?.ES!!
            optionList[numRandomPosition1] = if(resourceProvider.getString(R.string.locale) == "en") option1.name?.EN!! else option1.name?.ES!!
            optionList[numRandomPosition2] = if(resourceProvider.getString(R.string.locale) == "en") option2.name?.EN!! else option2.name?.ES!!
            optionList[numRandomPosition3] = if(resourceProvider.getString(R.string.locale) == "en") option3.name?.EN!! else option3.name?.ES!!

            _responseOptions.value = optionList
            _question.value = pride
            _progress.value = UiModel.Loading(false)
        }
    }

    private suspend fun getPride(id: Int): Pride {
        return getPrideById.invoke(id)
    }

    fun navigateToResult(points: String) {
        Analytics.analyticsGameFinished(points)
        _navigation.value = Navigation.Result
    }

    fun getPride() : Pride? {
        return pride
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
    }

    sealed class Navigation {
        object Result : Navigation()
    }
}