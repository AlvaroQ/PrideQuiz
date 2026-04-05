package com.quiz.data.repository

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.quiz.data.datasource.DataBaseSource
import com.quiz.domain.App

class AppsRecommendedRepositoryImpl(private val dataBaseSource: DataBaseSource) : AppsRecommendedRepository {

    // Cache simple: las apps recomendadas no cambian frecuentemente en una sesion
    private var cachedApps: List<App>? = null

    override suspend fun getAppsRecommended(): Either<RepositoryException, List<App>> {
        cachedApps?.let { return it.right() }

        return try {
            val result = dataBaseSource.getAppsRecommended()
            cachedApps = result
            result.right()
        } catch (e: Exception) {
            RepositoryException.DataNotFoundException.left()
        }
    }
}
