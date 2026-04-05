package com.quiz.data.repository

import com.quiz.data.datasource.DataBaseSource
import com.quiz.domain.Pride

class PrideByIdRepositoryImpl(private val dataBaseSource: DataBaseSource) : PrideByIdRepository {

    override suspend fun getPrideById(id: Int): Pride = dataBaseSource.getPrideById(id)

    override suspend fun getPrideList(currentPage: Int): List<Pride> = dataBaseSource.getPrideList(currentPage)
}
