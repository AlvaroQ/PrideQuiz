package com.quiz.data.repository

import com.quiz.data.datasource.DataBaseSource
import com.quiz.domain.App

class AppsRecommendedRepository(private val dataBaseSource: DataBaseSource) {

    suspend fun getAppsRecommended(): MutableList<App> = dataBaseSource.getAppsRecommended()

}