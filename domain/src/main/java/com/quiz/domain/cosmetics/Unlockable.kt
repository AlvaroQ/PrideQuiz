package com.quiz.domain.cosmetics

data class Unlockable(
    val id: String,
    val name: String,
    val description: String,
    val category: CosmeticCategory,
    val tier: CosmeticTier,
    val unlockCondition: UnlockCondition,
    val isDefault: Boolean = false
)
