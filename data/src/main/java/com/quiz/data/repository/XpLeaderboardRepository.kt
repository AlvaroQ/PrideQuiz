package com.quiz.data.repository

import com.quiz.data.datasource.XpLeaderboardDataSource
import com.quiz.domain.XpLeaderboardEntry

class XpLeaderboardRepository(private val xpLeaderboardDataSource: XpLeaderboardDataSource) {

    suspend fun syncUserXp(entry: XpLeaderboardEntry) = xpLeaderboardDataSource.syncUserXp(entry)

    suspend fun getUserXpEntry(uid: String) = xpLeaderboardDataSource.getUserXpEntry(uid)

    suspend fun getXpLeaderboard(limit: Int = 100) = xpLeaderboardDataSource.getXpLeaderboard(limit)

    suspend fun getUserRank(uid: String, userXp: Long) = xpLeaderboardDataSource.getUserRank(uid, userXp)
}
