package com.quiz.data.datasource

import arrow.core.Either
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.User

interface FirestoreDataSource {
    suspend fun addRecord(user: User): Either<RepositoryException, User>
    suspend fun getRanking(): Either<RepositoryException, List<User>>
    // gameMode: filtro por modo de juego ("NORMAL", "ADVANCE").
    // String vacio = sin filtro (compatibilidad con entradas legacy sin gameMode).
    suspend fun getWorldRecords(limit: Int, gameMode: String = ""): Either<RepositoryException, String>

    // Timed ranking methods
    suspend fun addTimedRecord(user: User): Either<RepositoryException, User>
    suspend fun getTimedRanking(): Either<RepositoryException, List<User>>
    suspend fun getTimedWorldRecords(limit: Int): Either<RepositoryException, String>
}