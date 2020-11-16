package com.quiz.usecases

import com.quiz.data.repository.RankingRepository
import com.quiz.domain.User

class GetRankingScore(private val rankingRepository: RankingRepository) {

    suspend fun invoke(): MutableList<User> = rankingRepository.getRanking()

}
