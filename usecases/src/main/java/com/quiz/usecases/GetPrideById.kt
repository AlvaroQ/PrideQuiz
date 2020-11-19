package com.quiz.usecases

import com.quiz.data.repository.PrideByIdRepository
import com.quiz.domain.Pride

class GetPrideById(private val prideByIdRepository: PrideByIdRepository) {

    suspend fun invoke(id: Int): Pride = prideByIdRepository.getPrideById(id)

}
