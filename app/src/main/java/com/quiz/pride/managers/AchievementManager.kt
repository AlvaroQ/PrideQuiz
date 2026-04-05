package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

/**
 * Manages the achievement system: unlocking, checking, and querying achievements
 */
class AchievementManager(
    private val context: Context,
    private val progressionManager: ProgressionManager,
    private val gameStatsManager: GameStatsManager
) {

    companion object {
        private val UNLOCKED_ACHIEVEMENTS = stringPreferencesKey("unlocked_achievements")
        private val TOTAL_XP = longPreferencesKey("total_xp")
    }

    suspend fun getUnlockedAchievements(): Set<Achievement> {
        val prefs = context.progressionDataStore.data.first()
        val achievementsStr = prefs[UNLOCKED_ACHIEVEMENTS] ?: ""
        if (achievementsStr.isEmpty()) return emptySet()

        return achievementsStr.split(",")
            .mapNotNull { id -> Achievement.entries.find { it.id == id } }
            .toSet()
    }

    suspend fun unlockAchievement(achievement: Achievement): Boolean {
        val current = getUnlockedAchievements()
        if (achievement in current) return false

        val newSet = current + achievement
        val idsStr = newSet.joinToString(",") { it.id }

        context.progressionDataStore.edit { preferences ->
            preferences[UNLOCKED_ACHIEVEMENTS] = idsStr
        }

        // Grant XP for achievement
        progressionManager.addXp(achievement.xpReward.toLong())

        return true
    }

    suspend fun checkAndUnlockAchievements(): List<Achievement> {
        val stats = gameStatsManager.getStatistics()
        val prefs = context.progressionDataStore.data.first()
        val currentLevel = progressionManager.calculateLevel(prefs[TOTAL_XP] ?: 0L)
        val unlockedNow = mutableListOf<Achievement>()

        Achievement.entries.forEach { achievement ->
            val shouldUnlock = when (achievement) {
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
                Achievement.DEDICATED -> stats.totalTimePlayedMs >= 3600000 // 1 hour
                Achievement.ACCURACY_80 -> stats.accuracy >= 80f && stats.totalGamesPlayed >= 10
                Achievement.ACCURACY_90 -> stats.accuracy >= 90f && stats.totalGamesPlayed >= 20
            }

            if (shouldUnlock && unlockAchievement(achievement)) {
                unlockedNow.add(achievement)
            }
        }

        return unlockedNow
    }
}
