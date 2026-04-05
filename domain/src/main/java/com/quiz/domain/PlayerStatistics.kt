package com.quiz.domain

data class PlayerStatistics(
    val totalGamesPlayed: Int,
    val gamesWon: Int,
    val totalCorrectAnswers: Int,
    val totalWrongAnswers: Int,
    val accuracy: Float,
    val bestStreakEver: Int,
    val perfectGames: Int,
    val totalTimePlayedMs: Long,
    val normalGamesPlayed: Int,
    val advanceGamesPlayed: Int,
    val expertGamesPlayed: Int,
    val timedGamesPlayed: Int
) {
    val totalTimePlayed: String
        get() {
            val hours = totalTimePlayedMs / (1000 * 60 * 60)
            val minutes = (totalTimePlayedMs / (1000 * 60)) % 60
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }
}
