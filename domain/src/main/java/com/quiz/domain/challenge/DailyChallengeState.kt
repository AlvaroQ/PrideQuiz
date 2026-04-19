package com.quiz.domain.challenge

data class DailyChallengeState(
    val date: String = "",                          // "yyyy-MM-dd"
    val challenges: List<DailyChallenge> = emptyList(), // 3 diarios + opcional semanal
    val allDailyCompleted: Boolean = false,
    val allDailyBonusClaimed: Boolean = false,
    val weekStartDate: String = "",                 // Lunes de la semana actual
    val weeklyChallenge: DailyChallenge? = null
)
