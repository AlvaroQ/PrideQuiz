package com.quiz.data.datasource

import arrow.core.Either
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.XpLeaderboardEntry

interface XpLeaderboardDataSource {
    suspend fun syncUserXp(entry: XpLeaderboardEntry): Either<RepositoryException, XpLeaderboardEntry>
    suspend fun getUserXpEntry(uid: String): Either<RepositoryException, XpLeaderboardEntry?>
    suspend fun getXpLeaderboard(limit: Int = 100): List<XpLeaderboardEntry>
    suspend fun getUserRank(uid: String, userXp: Long): Either<RepositoryException, Int>
}
