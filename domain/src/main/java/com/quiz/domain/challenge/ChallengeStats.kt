package com.quiz.domain.challenge

data class ChallengeStats(
    val totalCompleted: Int = 0,
    val totalAllDailyCompleteDays: Int = 0,
    val currentAllDailyStreak: Int = 0,
    val bestAllDailyStreak: Int = 0
)
