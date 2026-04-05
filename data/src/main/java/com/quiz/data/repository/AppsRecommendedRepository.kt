package com.quiz.data.repository

import arrow.core.Either
import com.quiz.domain.App

interface AppsRecommendedRepository {
    suspend fun getAppsRecommended(): Either<RepositoryException, List<App>>
}
