package com.quiz.usecases

import com.quiz.data.repository.AppsRecommendedRepository
import com.quiz.domain.App

class GetAppsRecommended(private val appsRecommendedRepository: AppsRecommendedRepository) {
    suspend operator fun invoke(): List<App> = appsRecommendedRepository.getAppsRecommended()
}
