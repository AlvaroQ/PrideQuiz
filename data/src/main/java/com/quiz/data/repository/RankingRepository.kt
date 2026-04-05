package com.quiz.data.repository

import arrow.core.Either
import com.quiz.domain.User

interface RankingRepository {
    suspend fun addRecord(user: User): Either<RepositoryException, User>
    suspend fun getRanking(): Either<RepositoryException, List<User>>
    suspend fun getWorldRecords(limit: Long): Either<RepositoryException, String>
    suspend fun addTimedRecord(user: User): Either<RepositoryException, User>
    suspend fun getTimedRanking(): Either<RepositoryException, List<User>>
    suspend fun getTimedWorldRecords(limit: Long): Either<RepositoryException, String>
}
