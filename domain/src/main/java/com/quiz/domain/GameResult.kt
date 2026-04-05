package com.quiz.domain

data class GameResult(
    val gameMode: GameMode,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val bestStreak: Int,
    val timePlayedMs: Long,
    val completedAllQuestions: Boolean
)
