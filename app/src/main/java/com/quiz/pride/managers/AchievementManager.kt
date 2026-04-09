package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.quiz.domain.Achievement
import com.quiz.pride.common.DataStoreKeys
import kotlinx.coroutines.flow.first

/**
 * Manages the achievement system: unlocking, checking, and querying achievements
 */
class AchievementManager(
    private val context: Context,
    private val progressionManager: ProgressionManager,
    private val gameStatsManager: GameStatsManager
) {

    private val UNLOCKED_ACHIEVEMENTS = DataStoreKeys.ProgressionKeys.UNLOCKED_ACHIEVEMENTS
    private val TOTAL_XP = DataStoreKeys.ProgressionKeys.TOTAL_XP

    suspend fun getUnlockedAchievements(): Set<Achievement> {
        val prefs = context.progressionDataStore.data.first()
        return parseUnlockedAchievements(prefs)
    }

    /** Extrae los achievements desbloqueados de preferencias ya leidas (sin IO adicional). */
    private fun parseUnlockedAchievements(prefs: Preferences): Set<Achievement> {
        val achievementsStr = prefs[UNLOCKED_ACHIEVEMENTS] ?: ""
        if (achievementsStr.isEmpty()) return emptySet()
        return achievementsStr.split(",")
            .mapNotNull { id -> Achievement.entries.find { it.id == id } }
            .toSet()
    }

    suspend fun checkAndUnlockAchievements(): List<Achievement> {
        // Lectura unica del DataStore — incluye XP, achievements y estadisticas del jugador
        val prefs = context.progressionDataStore.data.first()
        val currentLevel = progressionManager.calculateLevel(prefs[TOTAL_XP] ?: 0L)
        val alreadyUnlocked = parseUnlockedAchievements(prefs)
        // Leer estadisticas usando las preferencias ya cargadas para evitar tercera lectura
        val stats = gameStatsManager.getStatisticsFromPrefs(prefs)

        // Fase 1: determinar cuales achievements deben desbloquearse
        val achievementsToUnlock = Achievement.entries.filter { achievement ->
            if (achievement in alreadyUnlocked) return@filter false
            when (achievement) {
                Achievement.FIRST_GAME -> stats.totalGamesPlayed >= 1
                Achievement.TEN_GAMES -> stats.totalGamesPlayed >= 10
                Achievement.FIFTY_GAMES -> stats.totalGamesPlayed >= 50
                Achievement.HUNDRED_GAMES -> stats.totalGamesPlayed >= 100
                Achievement.FIRST_PERFECT -> stats.perfectGames >= 1
                Achievement.FIVE_PERFECT -> stats.perfectGames >= 5
                Achievement.STREAK_5 -> stats.bestStreakEver >= 5
                Achievement.STREAK_10 -> stats.bestStreakEver >= 10
                Achievement.STREAK_15 -> stats.bestStreakEver >= 15
                Achievement.STREAK_20 -> stats.bestStreakEver >= 20
                Achievement.LEVEL_10 -> currentLevel >= 10
                Achievement.LEVEL_25 -> currentLevel >= 25
                Achievement.LEVEL_50 -> currentLevel >= 50
                Achievement.EXPERT_MASTER -> stats.expertGamesPlayed >= 25
                Achievement.SPEED_DEMON -> stats.timedGamesPlayed >= 10
                Achievement.DEDICATED -> stats.totalTimePlayedMs >= 3600000 // 1 hora
                Achievement.ACCURACY_80 -> stats.accuracy >= 80f && stats.totalGamesPlayed >= 10
                Achievement.ACCURACY_90 -> stats.accuracy >= 90f && stats.totalGamesPlayed >= 20
            }
        }

        if (achievementsToUnlock.isEmpty()) return emptyList()

        // Fase 2: marcar todos los achievements en DataStore de una sola vez
        val newSet = alreadyUnlocked + achievementsToUnlock
        val idsStr = newSet.joinToString(",") { it.id }
        context.progressionDataStore.edit { preferences ->
            preferences[UNLOCKED_ACHIEVEMENTS] = idsStr
        }

        // Fase 3: sumar todo el XP de una sola llamada (evita duplicacion por llamadas concurrentes)
        val totalBonusXp = achievementsToUnlock.sumOf { it.xpReward.toLong() }
        if (totalBonusXp > 0) {
            progressionManager.addXp(totalBonusXp)
        }

        return achievementsToUnlock
    }
}
