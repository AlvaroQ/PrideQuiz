package com.quiz.domain.challenge

data class DailyChallenge(
    val id: String,           // "{date}_{difficulty}_{type}"
    val type: ChallengeType,
    val difficulty: ChallengeDifficulty,
    val descriptionKey: String, // Clave logica para resolver el texto en UI (ej: "games_played")
    val targetValue: Int,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val reward: ChallengeReward,
    val extraParam: String = "" // ej: nombre de modo para PLAY_MODE
)
