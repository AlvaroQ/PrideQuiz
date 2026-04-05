package com.quiz.data.repository

import com.quiz.data.datasource.DataBaseSource
import com.quiz.domain.App

class AppsRecommendedRepositoryImpl(private val dataBaseSource: DataBaseSource) : AppsRecommendedRepository {

    override suspend fun getAppsRecommended(): List<App> = dataBaseSource.getAppsRecommended()
}
