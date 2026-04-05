package com.quiz.pride.datasource

import com.quiz.data.datasource.GameResultProcessorDataSource
import com.quiz.domain.Achievement
import com.quiz.domain.GameResult
import com.quiz.domain.XpGainResult
import com.quiz.pride.managers.AchievementManager
import com.quiz.pride.managers.GameStatsManager
import com.quiz.pride.managers.XpSyncManager

/**
 * Implementacion concreta de GameResultProcessorDataSource.
 *
 * Delega en los managers de la capa app/ para ejecutar la logica de
 * estadisticas, logros y sincronizacion. Esta clase vive en app/ porque
 * los managers necesitan Context y DataStore, que son dependencias Android.
 *
 * Registrada en Koin como single<GameResultProcessorDataSource> { GameResultProcessorImpl(...) }
 */
class GameResultProcessorImpl(
    private val gameStatsManager: GameStatsManager,
    private val achievementManager: AchievementManager,
    private val xpSyncManager: XpSyncManager
) : GameResultProcessorDataSource {

    override suspend fun recordGameAndComputeXp(result: GameResult): XpGainResult {
        return gameStatsManager.recordGameResult(result)
    }

    override suspend fun checkAndUnlockAchievements(): List<Achievement> {
        return achievementManager.checkAndUnlockAchievements()
    }

    override suspend fun triggerXpSync() {
        xpSyncManager.triggerSync()
    }
}
