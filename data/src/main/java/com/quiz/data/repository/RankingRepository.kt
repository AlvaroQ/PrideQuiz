package com.quiz.data.repository

import com.quiz.data.datasource.FirestoreDataSource
import com.quiz.domain.User

class RankingRepository(private val firestoreDataSource: FirestoreDataSource) {

    suspend fun addRecord(user: User) = firestoreDataSource.addRecord(user)

    suspend fun getRanking(): MutableList<User> = firestoreDataSource.getRanking()

    suspend fun getWorldRecords(limit: Long): String = firestoreDataSource.getWorldRecords(limit)

    // Timed ranking methods
    suspend fun addTimedRecord(user: User) = firestoreDataSource.addTimedRecord(user)

    suspend fun getTimedRanking(): MutableList<User> = firestoreDataSource.getTimedRanking()

    suspend fun getTimedWorldRecords(limit: Long): String = firestoreDataSource.getTimedWorldRecords(limit)
}
