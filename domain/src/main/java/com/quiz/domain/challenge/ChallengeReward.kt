package com.quiz.domain.challenge

data class ChallengeReward(
    val xp: Int,
    val coins: Int
) {
    companion object {
        fun forDifficulty(difficulty: ChallengeDifficulty) = when (difficulty) {
            ChallengeDifficulty.EASY -> ChallengeReward(25, 5)
            ChallengeDifficulty.MEDIUM -> ChallengeReward(50, 10)
            ChallengeDifficulty.HARD -> ChallengeReward(100, 20)
            ChallengeDifficulty.WEEKLY -> ChallengeReward(200, 50)
        }

        val ALL_DAILY_COMPLETE_BONUS = ChallengeReward(75, 15)
    }
}
