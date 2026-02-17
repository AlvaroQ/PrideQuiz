package com.quiz.data.repository

import com.quiz.data.datasource.DataBaseSource
import com.quiz.domain.Pride

class PrideByIdRepository(private val dataBaseSource: DataBaseSource) {

    suspend fun getPrideById(id: Int): Pride = dataBaseSource.getPrideById(id)

    suspend fun getPrideList(currentPage: Int): List<Pride> = dataBaseSource.getPrideList(currentPage)

}