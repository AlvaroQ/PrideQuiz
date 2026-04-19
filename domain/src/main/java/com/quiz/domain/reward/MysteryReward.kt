package com.quiz.domain.reward

/**
 * Recompensa de la caja misteriosa que se desbloquea cada N partidas.
 *
 * Modelo de dominio puro (sin dependencias Android). El sorteo ocurre en
 * MysteryBoxManager.openBox() y la acreditacion final la hace el ViewModel.
 */
data class MysteryReward(
    val xpAmount: Int = 0,
    val coinsAmount: Int = 0,
    val freezeTokens: Int = 0,
    val type: MysteryRewardType
)

enum class MysteryRewardType { XP_BONUS, COINS_BONUS, FREEZE_TOKEN }
