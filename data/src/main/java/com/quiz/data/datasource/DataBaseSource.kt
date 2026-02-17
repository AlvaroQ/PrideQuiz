package com.quiz.data.datasource

import com.quiz.domain.App
import com.quiz.domain.Pride

interface DataBaseSource {
    suspend fun getPrideById(id: Int): Pride
    suspend fun getPrideList(currentPage: Int): List<Pride>
    suspend fun getAppsRecommended(): List<App>
}