package com.quiz.data.datasource

import arrow.core.Either
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.User

interface FirestoreDataSource {
    suspend fun addRecord(user: User): Either<RepositoryException, User>
    suspend fun getRanking(): Either<RepositoryException, List<User>>
    suspend fun getWorldRecords(limit: Long): Either<RepositoryException, String>

    // Timed ranking methods
    suspend fun addTimedRecord(user: User): Either<RepositoryException, User>
    suspend fun getTimedRanking(): Either<RepositoryException, List<User>>
    suspend fun getTimedWorldRecords(limit: Long): Either<RepositoryException, String>
}