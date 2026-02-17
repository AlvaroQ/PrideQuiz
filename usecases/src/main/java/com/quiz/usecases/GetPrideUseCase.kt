package com.quiz.usecases

import com.quiz.data.repository.PrideByIdRepository
import com.quiz.domain.Pride

class GetPrideById(private val prideByIdRepository: PrideByIdRepository) {
    suspend operator fun invoke(id: Int): Pride = prideByIdRepository.getPrideById(id)
}

class GetPrideList(private val prideByIdRepository: PrideByIdRepository) {
    suspend operator fun invoke(currentPage: Int): List<Pride> = prideByIdRepository.getPrideList(currentPage)
}
