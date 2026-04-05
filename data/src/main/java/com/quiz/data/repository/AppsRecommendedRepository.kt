package com.quiz.data.repository

import com.quiz.domain.App

interface AppsRecommendedRepository {
    suspend fun getAppsRecommended(): List<App>
}
