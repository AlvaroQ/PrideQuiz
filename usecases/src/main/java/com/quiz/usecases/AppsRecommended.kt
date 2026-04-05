package com.quiz.usecases

import arrow.core.Either
import com.quiz.data.repository.AppsRecommendedRepository
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.App

class GetAppsRecommended(private val appsRecommendedRepository: AppsRecommendedRepository) {
    suspend operator fun invoke(): Either<RepositoryException, List<App>> =
        appsRecommendedRepository.getAppsRecommended()
}
