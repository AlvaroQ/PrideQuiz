package com.quiz.usecases

import com.quiz.domain.App
import com.quiz.data.repository.AppsRecommendedRepository

class GetAppsRecommended(private val appsRecommendedRepository: AppsRecommendedRepository) {

    suspend fun invoke(): MutableList<App> = appsRecommendedRepository.getAppsRecommended()

}
