package com.quiz.data.repository

import arrow.core.Either
import arrow.core.right
import com.quiz.data.datasource.FirestoreDataSource
import com.quiz.domain.User

class RankingRepositoryImpl(private val firestoreDataSource: FirestoreDataSource) : RankingRepository {

    // Cache con TTL de 5 minutos para reducir fetches a Firestore
    private var cachedRanking: List<User>? = null
    private var cachedTimedRanking: List<User>? = null
    private var lastRankingFetchTime: Long = 0
    private var lastTimedRankingFetchTime: Long = 0

    companion object {
        private const val CACHE_TTL_MS = 5 * 60 * 1000L // 5 minutos
    }

    override suspend fun addRecord(user: User): Either<RepositoryException, User> {
        // Invalidar cache al agregar un nuevo record
        cachedRanking = null
        lastRankingFetchTime = 0
        return firestoreDataSource.addRecord(user)
    }

    override suspend fun getRanking(): Either<RepositoryException, List<User>> {
        val now = System.currentTimeMillis()
        val cached = cachedRanking
        if (cached != null && now - lastRankingFetchTime < CACHE_TTL_MS) {
            return cached.right()
        }
        return firestoreDataSource.getRanking().also { result ->
            result.onRight {
                cachedRanking = it
                lastRankingFetchTime = now
            }
        }
    }

    override suspend fun getWorldRecords(limit: Long): Either<RepositoryException, String> =
        firestoreDataSource.getWorldRecords(limit)

    override suspend fun addTimedRecord(user: User): Either<RepositoryException, User> {
        // Invalidar cache al agregar un nuevo record
        cachedTimedRanking = null
        lastTimedRankingFetchTime = 0
        return firestoreDataSource.addTimedRecord(user)
    }

    override suspend fun getTimedRanking(): Either<RepositoryException, List<User>> {
        val now = System.currentTimeMillis()
        val cached = cachedTimedRanking
        if (cached != null && now - lastTimedRankingFetchTime < CACHE_TTL_MS) {
            return cached.right()
        }
        return firestoreDataSource.getTimedRanking().also { result ->
            result.onRight {
                cachedTimedRanking = it
                lastTimedRankingFetchTime = now
            }
        }
    }

    override suspend fun getTimedWorldRecords(limit: Long): Either<RepositoryException, String> =
        firestoreDataSource.getTimedWorldRecords(limit)
}
