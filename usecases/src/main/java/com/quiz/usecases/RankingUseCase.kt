package com.quiz.usecases

import arrow.core.Either
import com.quiz.data.repository.RankingRepository
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.User

enum class RankingMode { NORMAL, TIMED }

class GetRankingScore(private val rankingRepository: RankingRepository) {
    suspend operator fun invoke(mode: RankingMode = RankingMode.NORMAL): Either<RepositoryException, List<User>> =
        when (mode) {
            RankingMode.NORMAL -> rankingRepository.getRanking()
            RankingMode.TIMED -> rankingRepository.getTimedRanking()
        }
}

class GetRecordScore(private val rankingRepository: RankingRepository) {
    // gameMode: filtra records por modo de juego dentro de la coleccion clasica ("NORMAL", "ADVANCE", "EXPERT").
    // String vacio = sin filtro (backward compatible con entradas legacy sin gameMode).
    // Para RankingMode.TIMED el gameMode se ignora (usa coleccion separada ranking-pride-timed).
    suspend operator fun invoke(
        limit: Int,
        mode: RankingMode = RankingMode.NORMAL,
        gameMode: String = ""
    ): Either<RepositoryException, String> =
        when (mode) {
            RankingMode.NORMAL -> rankingRepository.getWorldRecords(limit, gameMode)
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
