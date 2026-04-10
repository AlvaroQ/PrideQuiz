package com.quiz.data.repository

import arrow.core.Either
import com.quiz.domain.User

interface RankingRepository {
    suspend fun addRecord(user: User): Either<RepositoryException, User>
    suspend fun getRanking(): Either<RepositoryException, List<User>>
    // gameMode: filtro por modo de juego para no mezclar records entre modos clasicos.
    // String vacio = sin filtro (backward compatible con entradas legacy).
    suspend fun getWorldRecords(limit: Int, gameMode: String = ""): Either<RepositoryException, String>
    suspend fun addTimedRecord(user: User): Either<RepositoryException, User>
    suspend fun getTimedRanking(): Either<RepositoryException, List<User>>
    suspend fun getTimedWorldRecords(limit: Int): Either<RepositoryException, String>
}
