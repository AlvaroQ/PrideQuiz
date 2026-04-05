package com.quiz.data.repository

import arrow.core.Either
import com.quiz.domain.Pride

interface PrideByIdRepository {
    suspend fun getPrideById(id: Int): Either<RepositoryException, Pride>
    suspend fun getPrideList(currentPage: Int): Either<RepositoryException, List<Pride>>
}
