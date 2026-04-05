package com.quiz.data.repository

import arrow.core.Either
import com.quiz.data.datasource.FirestoreDataSource
import com.quiz.domain.User

class RankingRepositoryImpl(private val firestoreDataSource: FirestoreDataSource) : RankingRepository {

    override suspend fun addRecord(user: User): Either<RepositoryException, User> =
        firestoreDataSource.addRecord(user)

    override suspend fun getRanking(): List<User> = firestoreDataSource.getRanking()

    override suspend fun getWorldRecords(limit: Long): String = firestoreDataSource.getWorldRecords(limit)

    override suspend fun addTimedRecord(user: User): Either<RepositoryException, User> =
        firestoreDataSource.addTimedRecord(user)

    override suspend fun getTimedRanking(): List<User> = firestoreDataSource.getTimedRanking()

    override suspend fun getTimedWorldRecords(limit: Long): String = firestoreDataSource.getTimedWorldRecords(limit)
}
