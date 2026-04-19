package com.quiz.domain.reward

/**
 * Recompensa diaria generada de forma determinista por fecha + installId.
 *
 * Modelo de dominio puro (sin dependencias Android). Se instancia desde el
 * DailyRewardManager y se consume desde ViewModels/composables.
 *
 * Distribucion aproximada de tiers: 70% COMMON, 25% UNCOMMON, 5% RARE.
 */
data class DailyReward(
    val date: String,
    val tier: RewardTier,
    val xpAmount: Int,
    val coinsAmount: Int,
    val gemsAmount: Int,
    val isClaimed: Boolean
)

enum class RewardTier { COMMON, UNCOMMON, RARE }
