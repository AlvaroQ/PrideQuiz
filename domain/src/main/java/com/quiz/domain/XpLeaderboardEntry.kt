package com.quiz.domain

data class XpLeaderboardEntry(
    val uid: String = "",
    val nickname: String = "",
    val imageBase64: String = "",
    val totalXp: Long = 0L,
    val level: Int = 1,
    val title: String = "Novice",
    val totalGamesPlayed: Int = 0,
    val accuracy: Float = 0f,
    val lastUpdated: Long = 0L,
    val createdAt: Long = 0L
)
