package com.quiz.domain

/**
 * Resultado del procesamiento de una partida completada.
 *
 * Encapsula toda la informacion generada al finalizar un juego:
 * XP ganado, level-up, y logros nuevos desbloqueados.
 * Es el output de ProcessGameResultUseCase.
 */
data class ProcessedGameResult(
    val xpGainResult: XpGainResult,
    val newAchievements: List<Achievement>
)
