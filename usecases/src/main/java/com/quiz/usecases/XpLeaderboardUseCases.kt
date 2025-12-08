package com.quiz.usecases

import arrow.core.Either
import com.quiz.data.repository.RepositoryException
import com.quiz.data.repository.XpLeaderboardRepository
import com.quiz.domain.XpLeaderboardEntry

class SyncUserXp(private val repository: XpLeaderboardRepository) {
    suspend fun invoke(entry: XpLeaderboardEntry): Either<RepositoryException, XpLeaderboardEntry> =
        repository.syncUserXp(entry)
}

class GetXpLeaderboard(private val repository: XpLeaderboardRepository) {
    suspend fun invoke(limit: Int = 100): List<XpLeaderboardEntry> =
        repository.getXpLeaderboard(limit)
}

class GetUserGlobalRank(private val repository: XpLeaderboardRepository) {
    suspend fun invoke(uid: String, userXp: Long): Either<RepositoryException, Int> =
        repository.getUserRank(uid, userXp)
}

class GetUserXpEntry(private val repository: XpLeaderboardRepository) {
    suspend fun invoke(uid: String): Either<RepositoryException, XpLeaderboardEntry?> =
        repository.getUserXpEntry(uid)
}
