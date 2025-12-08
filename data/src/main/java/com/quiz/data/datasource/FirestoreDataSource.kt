package com.quiz.data.datasource

import arrow.core.Either
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.User

interface FirestoreDataSource {
    suspend fun addRecord(user: User): Either<RepositoryException, User>
    suspend fun getRanking(): MutableList<User>
    suspend fun getWorldRecords(limit: Long): String

    // Timed ranking methods
    suspend fun addTimedRecord(user: User): Either<RepositoryException, User>
    suspend fun getTimedRanking(): MutableList<User>
    suspend fun getTimedWorldRecords(limit: Long): String
}