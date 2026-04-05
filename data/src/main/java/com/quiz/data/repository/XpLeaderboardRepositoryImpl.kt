package com.quiz.data.repository

import arrow.core.Either
import com.quiz.data.datasource.XpLeaderboardDataSource
import com.quiz.domain.XpLeaderboardEntry

class XpLeaderboardRepositoryImpl(private val xpLeaderboardDataSource: XpLeaderboardDataSource) : XpLeaderboardRepository {

    override suspend fun syncUserXp(entry: XpLeaderboardEntry): Either<RepositoryException, XpLeaderboardEntry> =
        xpLeaderboardDataSource.syncUserXp(entry)

    override suspend fun getUserXpEntry(uid: String): Either<RepositoryException, XpLeaderboardEntry?> =
        xpLeaderboardDataSource.getUserXpEntry(uid)

    override suspend fun getXpLeaderboard(limit: Int): List<XpLeaderboardEntry> =
        xpLeaderboardDataSource.getXpLeaderboard(limit)

    override suspend fun getUserRank(uid: String, userXp: Long): Either<RepositoryException, Int> =
        xpLeaderboardDataSource.getUserRank(uid, userXp)
}
