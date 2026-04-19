package com.quiz.usecases

import com.quiz.data.datasource.GameResultProcessorDataSource
import com.quiz.domain.GameResult
import com.quiz.domain.ProcessedGameResult

/**
 * Caso de uso que orquesta el procesamiento completo de una partida finalizada.
 *
 * Responsabilidades:
 * 1. Registra estadisticas, calcula XP ganado y procesa racha diaria
 * 2. Verifica y desbloquea logros basados en las stats actualizadas
 * 3. Dispara sincronizacion del XP con el leaderboard remoto
 *
 * Reemplaza el uso directo de GameStatsManager, AchievementManager y XpSyncManager
 * en ResultViewModel, reduciendo sus dependencias de 12 a 9.
 */
class ProcessGameResultUseCase(
    private val processor: GameResultProcessorDataSource
) {
    suspend operator fun invoke(result: GameResult): ProcessedGameResult {
        // Orden importante: primero actualizar stats, XP y racha, luego evaluar logros
        val xpGainResult = processor.recordGameAndComputeXp(result)
        val newAchievements = processor.checkAndUnlockAchievements()

        // Fire-and-forget: no bloquear al usuario esperando sincronizacion remota
        processor.triggerXpSync()

        return ProcessedGameResult(
            xpGainResult = xpGainResult,
            newAchievements = newAchievements,
            streakCheckResult = processor.getLastStreakResult(),
            streakXpBonus = processor.getLastStreakXpBonus(),
            challengeCompletionResult = processor.getLastChallengeResult(),
            coinsEarned = processor.getLastCoinsEarned(),
            gemsEarned = processor.getLastGemsEarned()
        )
    }
}
