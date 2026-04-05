package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.quiz.domain.LevelInfo
import com.quiz.domain.PlayerStatistics
import com.quiz.domain.UserProfile
import com.quiz.domain.XpGainResult
import com.quiz.domain.XpLeaderboardEntry
import com.quiz.pride.R
import com.quiz.pride.common.DataStoreKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Manages player progression: XP, levels, and user profile
 */
class ProgressionManager(private val context: Context) {

    companion object {
        private val USER_NICKNAME = DataStoreKeys.ProgressionKeys.USER_NICKNAME
        private val USER_IMAGE = DataStoreKeys.ProgressionKeys.USER_IMAGE
        private val TOTAL_XP = DataStoreKeys.ProgressionKeys.TOTAL_XP
        private val CURRENT_LEVEL = DataStoreKeys.ProgressionKeys.CURRENT_LEVEL

        // XP Configuration
        const val XP_PER_CORRECT_ANSWER = 10
        const val XP_PER_STREAK_BONUS = 5
        const val XP_PER_PERFECT_GAME = 100
        const val XP_PER_WIN = 25
        const val XP_MULTIPLIER_ADVANCE = 1.5
        const val XP_MULTIPLIER_EXPERT = 2.0
        const val XP_MULTIPLIER_TIMED = 1.3

        // Level thresholds (cumulative XP needed)
        val LEVEL_THRESHOLDS = listOf(
            0L,      // Level 1
            100L,    // Level 2
            250L,    // Level 3
            500L,    // Level 4
            800L,    // Level 5
            1200L,   // Level 6
            1700L,   // Level 7
            2300L,   // Level 8
            3000L,   // Level 9
            3800L,   // Level 10
            4700L,   // Level 11
            5700L,   // Level 12
            6800L,   // Level 13
            8000L,   // Level 14
            9500L,   // Level 15
            11000L,  // Level 16
            13000L,  // Level 17
            15500L,  // Level 18
            18500L,  // Level 19
            22000L,  // Level 20
            26000L,  // Level 21-25 (+4000 each)
            30000L,
            34000L,
            38000L,
            42000L,  // Level 25
            47000L,  // Level 26-30 (+5000 each)
            52000L,
            57000L,
            62000L,
            67000L,  // Level 30
            75000L,  // Level 31-40 (+8000 each)
            83000L,
            91000L,
            99000L,
            107000L,
            115000L,
            123000L,
            131000L,
            139000L,
            147000L, // Level 40
            160000L, // Level 41-50 (+13000 each)
            173000L,
            186000L,
            199000L,
            212000L,
            225000L,
            238000L,
            251000L,
            264000L,
            277000L  // Level 50
        )
    }

    // ==================== USER PROFILE ====================

    val userNickname: Flow<String> = context.progressionDataStore.data
        .map { it[USER_NICKNAME] ?: "" }

    val userImage: Flow<String> = context.progressionDataStore.data
        .map { it[USER_IMAGE] ?: "" }

    suspend fun getUserProfile(): UserProfile {
        val prefs = context.progressionDataStore.data.first()
        return UserProfile(
            nickname = prefs[USER_NICKNAME] ?: "",
            imageBase64 = prefs[USER_IMAGE] ?: ""
        )
    }

    suspend fun saveNickname(nickname: String) {
        context.progressionDataStore.edit { preferences ->
            preferences[USER_NICKNAME] = nickname
        }
    }

    suspend fun saveUserImage(imageBase64: String) {
        context.progressionDataStore.edit { preferences ->
            preferences[USER_IMAGE] = imageBase64
        }
    }

    suspend fun saveUserProfile(nickname: String, imageBase64: String) {
        context.progressionDataStore.edit { preferences ->
            preferences[USER_NICKNAME] = nickname
            preferences[USER_IMAGE] = imageBase64
        }
    }

    // ==================== XP & LEVELS ====================

    val totalXp: Flow<Long> = context.progressionDataStore.data
        .map { it[TOTAL_XP] ?: 0L }

    val currentLevel: Flow<Int> = context.progressionDataStore.data
        .map { it[TOTAL_XP] ?: 0L }
        .distinctUntilChanged()
        .map { calculateLevel(it) }

    fun getLevelInfo(xp: Long): LevelInfo {
        val level = calculateLevel(xp)
        val currentThreshold = LEVEL_THRESHOLDS.getOrElse(level - 1) { 0L }
        val nextThreshold = LEVEL_THRESHOLDS.getOrElse(level) {
            // For levels beyond defined thresholds, add 20000 per level
            LEVEL_THRESHOLDS.last() + (level - LEVEL_THRESHOLDS.size + 1) * 20000L
        }
        val xpInCurrentLevel = xp - currentThreshold
        val xpNeededForNextLevel = nextThreshold - currentThreshold

        return LevelInfo(
            level = level,
            title = getTitleForLevel(level),
            totalXp = xp,
            xpInCurrentLevel = xpInCurrentLevel,
            xpNeededForNextLevel = xpNeededForNextLevel,
            progressPercent = (xpInCurrentLevel.toFloat() / xpNeededForNextLevel.toFloat()).coerceIn(0f, 1f)
        )
    }

    fun calculateLevel(xp: Long): Int {
        for (i in LEVEL_THRESHOLDS.indices.reversed()) {
            if (xp >= LEVEL_THRESHOLDS[i]) {
                return i + 1
            }
        }
        return 1
    }

    fun getTitleForLevel(level: Int): String {
        val resId = when {
            level <= 5 -> R.string.level_title_novice
            level <= 10 -> R.string.level_title_explorer
            level <= 15 -> R.string.level_title_enthusiast
            level <= 20 -> R.string.level_title_connoisseur
            level <= 30 -> R.string.level_title_expert
            level <= 40 -> R.string.level_title_master
            level <= 50 -> R.string.level_title_grandmaster
            else -> R.string.level_title_legend
        }
        return context.getString(resId)
    }

    suspend fun addXp(amount: Long): XpGainResult {
        val prefs = context.progressionDataStore.data.first()
        val oldXp = prefs[TOTAL_XP] ?: 0L
        val oldLevel = calculateLevel(oldXp)

        val newXp = oldXp + amount
        val newLevel = calculateLevel(newXp)

        context.progressionDataStore.edit { preferences ->
            preferences[TOTAL_XP] = newXp
            preferences[CURRENT_LEVEL] = newLevel
        }

        return XpGainResult(
            xpGained = amount,
            totalXp = newXp,
            oldLevel = oldLevel,
            newLevel = newLevel,
            leveledUp = newLevel > oldLevel
        )
    }

    suspend fun getCurrentXp(): Long {
        val prefs = context.progressionDataStore.data.first()
        return prefs[TOTAL_XP] ?: 0L
    }

    /**
     * Creates an XpLeaderboardEntry for syncing to Firestore.
     * Receives stats as parameter to avoid circular dependency.
     */
    suspend fun getLeaderboardEntry(uid: String, stats: PlayerStatistics): XpLeaderboardEntry {
        val profile = getUserProfile()
        val prefs = context.progressionDataStore.data.first()
        val xp = prefs[TOTAL_XP] ?: 0L
        val level = calculateLevel(xp)

        return XpLeaderboardEntry(
            uid = uid,
            nickname = profile.nickname,
            imageBase64 = profile.imageBase64,
            totalXp = xp,
            level = level,
            title = getTitleForLevel(level),
            totalGamesPlayed = stats.totalGamesPlayed,
            accuracy = stats.accuracy
        )
    }
}
