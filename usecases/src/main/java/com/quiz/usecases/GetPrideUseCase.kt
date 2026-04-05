package com.quiz.usecases

import arrow.core.Either
import com.quiz.data.repository.PrideByIdRepository
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.Pride

class GetPrideById(private val prideByIdRepository: PrideByIdRepository) {
    suspend operator fun invoke(id: Int): Either<RepositoryException, Pride> =
        prideByIdRepository.getPrideById(id)
}

class GetPrideList(private val prideByIdRepository: PrideByIdRepository) {
    suspend operator fun invoke(currentPage: Int): Either<RepositoryException, List<Pride>> =
        prideByIdRepository.getPrideList(currentPage)
}
