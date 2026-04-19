package com.quiz.domain.challenge

data class ChallengeCompletionResult(
    val completedChallenges: List<DailyChallenge>,
    val allDailyJustCompleted: Boolean,
    val totalXpEarned: Int,
    val totalCoinsEarned: Int
)
