package com.quiz.data.repository

import com.quiz.data.datasource.FirestoreDataSource
import com.quiz.domain.User

class RankingRepository(private val firestoreDataSource: FirestoreDataSource) {

    suspend fun addRecord(user: User) = firestoreDataSource.addRecord(user)

    suspend fun getRanking(): MutableList<User> = firestoreDataSource.getRanking()

    suspend fun getWorldRecords(limit: Long): String = firestoreDataSource.getWorldRecords(limit)
}
