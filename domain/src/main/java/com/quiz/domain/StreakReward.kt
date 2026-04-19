package com.quiz.domain

/**
 * Recompensa obtenida al completar una partida con racha activa.
 *
 * Incluye el XP bonus del ciclo diario, posibles tokens de freeze ganados
 * y el multiplicador que se aplica al XP base de la partida.
 */
data class StreakReward(
    val xpBonus: Int,
    val freezeTokens: Int = 0,
    val isMilestone: Boolean = false,
    val milestoneDay: Int = 0,
    val cycleDay: Int,
    val streakMultiplier: Float
)
