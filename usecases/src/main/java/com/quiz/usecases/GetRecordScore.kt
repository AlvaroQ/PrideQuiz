package com.quiz.usecases

import com.quiz.data.repository.RankingRepository

class GetRecordScore(private val rankingRepository: RankingRepository) {

    suspend fun invoke(limit: Long): String = rankingRepository.getWorldRecords(limit)

}
