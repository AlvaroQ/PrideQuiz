package com.quiz.domain

import com.quiz.domain.challenge.ChallengeCompletionResult

/**
 * Resultado del procesamiento de una partida completada.
 *
 * Encapsula toda la informacion generada al finalizar un juego:
 * XP ganado, level-up, logros nuevos desbloqueados, estado de racha diaria
 * y desafios diarios completados durante la sesion.
 * Es el output de ProcessGameResultUseCase.
 */
data class ProcessedGameResult(
    val xpGainResult: XpGainResult,
    val newAchievements: List<Achievement>,
    val streakCheckResult: StreakCheckResult? = null,
    val streakXpBonus: Int = 0,
    val challengeCompletionResult: ChallengeCompletionResult? = null,
    val coinsEarned: Int = 0,
    val gemsEarned: Int = 0
)
