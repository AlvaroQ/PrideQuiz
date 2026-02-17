package com.quiz.usecases

import arrow.core.Either
import com.quiz.data.repository.RankingRepository
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.User

enum class RankingMode { NORMAL, TIMED }

class GetRankingScore(private val rankingRepository: RankingRepository) {
    suspend operator fun invoke(mode: RankingMode = RankingMode.NORMAL): List<User> =
        when (mode) {
            RankingMode.NORMAL -> rankingRepository.getRanking()
            RankingMode.TIMED -> rankingRepository.getTimedRanking()
        }
}

class GetRecordScore(private val rankingRepository: RankingRepository) {
    suspend operator fun invoke(limit: Long, mode: RankingMode = RankingMode.NORMAL): String =
        when (mode) {
            RankingMode.NORMAL -> rankingRepository.getWorldRecords(limit)
            RankingMode.TIMED -> rankingRepository.getTimedWorldRecords(limit)
        }
}

class SaveTopScore(private val rankingRepository: RankingRepository) {
    suspend operator fun invoke(user: User, mode: RankingMode = RankingMode.NORMAL): Either<RepositoryException, User> =
        when (mode) {
            RankingMode.NORMAL -> rankingRepository.addRecord(user)
            RankingMode.TIMED -> rankingRepository.addTimedRecord(user)
        }
}
