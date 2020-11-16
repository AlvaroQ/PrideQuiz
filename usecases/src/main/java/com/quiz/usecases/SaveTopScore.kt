package com.quiz.usecases

import arrow.core.Either
import com.quiz.data.repository.RankingRepository
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.User

class SaveTopScore(private val rankingRepository: RankingRepository) {

    suspend fun invoke(user: User): Either<RepositoryException, User> = rankingRepository.addRecord(user)

}
