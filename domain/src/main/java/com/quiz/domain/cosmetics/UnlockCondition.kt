package com.quiz.domain.cosmetics

sealed class UnlockCondition {
    data class PurchaseWithCoins(val price: Int) : UnlockCondition()
    data class PurchaseWithGems(val price: Int) : UnlockCondition()
    data class ReachLevel(val level: Int) : UnlockCondition()
    data class CompleteAchievement(val achievementId: String) : UnlockCondition()
    data class StreakMilestone(val days: Int) : UnlockCondition()
    data class ChallengeCount(val count: Int) : UnlockCondition()
    data object Free : UnlockCondition()
}
