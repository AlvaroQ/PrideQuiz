package com.quiz.data.repository

import arrow.core.Either
import com.quiz.domain.User

interface RankingRepository {
    suspend fun addRecord(user: User): Either<RepositoryException, User>
    suspend fun getRanking(): List<User>
    suspend fun getWorldRecords(limit: Long): String
    suspend fun addTimedRecord(user: User): Either<RepositoryException, User>
    suspend fun getTimedRanking(): List<User>
    suspend fun getTimedWorldRecords(limit: Long): String
}
