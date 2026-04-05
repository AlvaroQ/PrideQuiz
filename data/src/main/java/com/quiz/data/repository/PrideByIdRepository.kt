package com.quiz.data.repository

import com.quiz.domain.Pride

interface PrideByIdRepository {
    suspend fun getPrideById(id: Int): Pride
    suspend fun getPrideList(currentPage: Int): List<Pride>
}
