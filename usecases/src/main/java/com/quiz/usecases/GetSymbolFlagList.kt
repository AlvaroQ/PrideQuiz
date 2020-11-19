package com.quiz.usecases

import com.quiz.data.repository.PrideByIdRepository
import com.quiz.domain.Pride

class GetSymbolFlagList(private val prideByIdRepository: PrideByIdRepository) {

    suspend fun invoke(): MutableList<Pride> = prideByIdRepository.getSymbolFlagList()

}
