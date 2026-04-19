package com.quiz.domain.challenge

sealed class ChallengeEvent {
    data class AnswerGiven(
        val isCorrect: Boolean,
        val responseTimeMs: Long = 0L
    ) : ChallengeEvent()

    data class GameCompleted(
        val gameMode: String,
        val score: Int,
        val bestStreak: Int,
        val isPerfect: Boolean,
        val completedAll: Boolean,
        val correctAnswers: Int,
        val totalQuestions: Int
    ) : ChallengeEvent()
}
