package com.quiz.data.repository

import arrow.core.Either
import arrow.core.right
import com.quiz.data.datasource.FirestoreDataSource
import com.quiz.domain.User

class RankingRepositoryImpl(private val firestoreDataSource: FirestoreDataSource) : RankingRepository {

    private val rankingCache = TtlCache<List<User>>()
    private val timedRankingCache = TtlCache<List<User>>()

    override suspend fun addRecord(user: User): Either<RepositoryException, User> {
        rankingCache.invalidate()
        return firestoreDataSource.addRecord(user)
    }

    override suspend fun getRanking(): Either<RepositoryException, List<User>> {
        rankingCache.get()?.let { return it.right() }
        return firestoreDataSource.getRanking().also { result ->
            result.onRight { rankingCache.set(it) }
        }
    }

    override suspend fun getWorldRecords(limit: Int, gameMode: String): Either<RepositoryException, String> =
        firestoreDataSource.getWorldRecords(limit, gameMode)

    override suspend fun addTimedRecord(user: User): Either<RepositoryException, User> {
        timedRankingCache.invalidate()
        return firestoreDataSource.addTimedRecord(user)
    }

    override suspend fun getTimedRanking(): Either<RepositoryException, List<User>> {
        timedRankingCache.get()?.let { return it.right() }
        return firestoreDataSource.getTimedRanking().also { result ->
            result.onRight { timedRankingCache.set(it) }
        }
    }

    override suspend fun getTimedWorldRecords(limit: Int): Either<RepositoryException, String> =
        firestoreDataSource.getTimedWorldRecords(limit)

    /**
     * Cache con TTL de 5 minutos. @Volatile garantiza visibilidad entre hilos
     * (coroutines en Dispatchers.IO). El repo es single en Koin.
     */
    private class TtlCache<T>(private val ttlMs: Long = 5 * 60 * 1000L) {
        @Volatile private var value: T? = null
        @Volatile private var fetchTime: Long = 0

        fun get(): T? =
            if (value != null && System.currentTimeMillis() - fetchTime < ttlMs) value else null

        fun set(v: T) { value = v; fetchTime = System.currentTimeMillis() }
        fun invalidate() { value = null; fetchTime = 0 }
    }
}
