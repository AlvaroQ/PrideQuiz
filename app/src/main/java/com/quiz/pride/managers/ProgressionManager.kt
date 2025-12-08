package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.quiz.domain.XpLeaderboardEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.progressionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "progression_preferences"
)

/**
 * Manages player progression: XP, levels, achievements, and detailed statistics
 */
class ProgressionManager(private val context: Context) {

    companion object {
        // User Profile Keys
        private val USER_NICKNAME = stringPreferencesKey("user_nickname")
        private val USER_IMAGE = stringPreferencesKey("user_image")

        // XP and Level Keys
        private val TOTAL_XP = longPreferencesKey("total_xp")
        private val CURRENT_LEVEL = intPreferencesKey("current_level")

        // Statistics Keys
        private val TOTAL_GAMES_PLAYED = intPreferencesKey("total_games_played")
        private val TOTAL_CORRECT_ANSWERS = intPreferencesKey("total_correct_answers")
        private val TOTAL_WRONG_ANSWERS = intPreferencesKey("total_wrong_answers")
        private val BEST_STREAK_EVER = intPreferencesKey("best_streak_ever")
        private val TOTAL_TIME_PLAYED_MS = longPreferencesKey("total_time_played_ms")
        private val PERFECT_GAMES = intPreferencesKey("perfect_games")
        private val GAMES_WON = intPreferencesKey("games_won")

        // Mode-specific stats
        private val NORMAL_GAMES = intPreferencesKey("normal_games")
        private val ADVANCE_GAMES = intPreferencesKey("advance_games")
        private val EXPERT_GAMES = intPreferencesKey("expert_games")
        private val TIMED_GAMES = intPreferencesKey("timed_games")

        // Achievements
        private val UNLOCKED_ACHIEVEMENTS = stringPreferencesKey("unlocked_achievements")

        // XP Configuration
        const val XP_PER_CORRECT_ANSWER = 10
        const val XP_PER_STREAK_BONUS = 5      // Extra per streak milestone
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
        .map { calculateLevel(it[TOTAL_XP] ?: 0L) }

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

    private fun calculateLevel(xp: Long): Int {
        for (i in LEVEL_THRESHOLDS.indices.reversed()) {
            if (xp >= LEVEL_THRESHOLDS[i]) {
                return i + 1
            }
        }
        return 1
    }

    fun getTitleForLevel(level: Int): String {
        return when {
            level <= 5 -> "Novice"
            level <= 10 -> "Explorer"
            level <= 15 -> "Enthusiast"
            level <= 20 -> "Connoisseur"
            level <= 30 -> "Expert"
            level <= 40 -> "Master"
            level <= 50 -> "Grandmaster"
            else -> "Legend"
        }
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

    /**
     * Creates an XpLeaderboardEntry for syncing to Firestore
     */
    suspend fun getLeaderboardEntry(uid: String): XpLeaderboardEntry {
        val profile = getUserProfile()
        val stats = getStatistics()
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

    /**
     * Returns current total XP value
     */
    suspend fun getCurrentXp(): Long {
        val prefs = context.progressionDataStore.data.first()
        return prefs[TOTAL_XP] ?: 0L
    }

    // ==================== GAME STATS ====================

    suspend fun recordGameResult(result: GameResult): XpGainResult {
        val prefs = context.progressionDataStore.data.first()

        // Calculate XP earned
        var xpEarned = 0L

        // Base XP for correct answers
        xpEarned += result.correctAnswers * XP_PER_CORRECT_ANSWER

        // Streak bonus
        if (result.bestStreak >= 5) {
            xpEarned += (result.bestStreak / 5) * XP_PER_STREAK_BONUS
        }

        // Perfect game bonus
        if (result.correctAnswers == result.totalQuestions && result.totalQuestions > 0) {
            xpEarned += XP_PER_PERFECT_GAME
        }

        // Win bonus (completed all questions)
        if (result.completedAllQuestions) {
            xpEarned += XP_PER_WIN
        }

        // Mode multipliers
        xpEarned = when (result.gameMode) {
            GameMode.ADVANCE -> (xpEarned * XP_MULTIPLIER_ADVANCE).toLong()
            GameMode.EXPERT -> (xpEarned * XP_MULTIPLIER_EXPERT).toLong()
            GameMode.TIMED -> (xpEarned * XP_MULTIPLIER_TIMED).toLong()
            else -> xpEarned
        }

        // Update statistics
        context.progressionDataStore.edit { preferences ->
            preferences[TOTAL_GAMES_PLAYED] = (prefs[TOTAL_GAMES_PLAYED] ?: 0) + 1
            preferences[TOTAL_CORRECT_ANSWERS] = (prefs[TOTAL_CORRECT_ANSWERS] ?: 0) + result.correctAnswers
            preferences[TOTAL_WRONG_ANSWERS] = (prefs[TOTAL_WRONG_ANSWERS] ?: 0) + (result.totalQuestions - result.correctAnswers)
            preferences[TOTAL_TIME_PLAYED_MS] = (prefs[TOTAL_TIME_PLAYED_MS] ?: 0L) + result.timePlayedMs

            // Update best streak
            val currentBestStreak = prefs[BEST_STREAK_EVER] ?: 0
            if (result.bestStreak > currentBestStreak) {
                preferences[BEST_STREAK_EVER] = result.bestStreak
            }

            // Update perfect games
            if (result.correctAnswers == result.totalQuestions && result.totalQuestions > 0) {
                preferences[PERFECT_GAMES] = (prefs[PERFECT_GAMES] ?: 0) + 1
            }

            // Update games won
            if (result.completedAllQuestions) {
                preferences[GAMES_WON] = (prefs[GAMES_WON] ?: 0) + 1
            }

            // Update mode-specific stats
            when (result.gameMode) {
                GameMode.NORMAL -> preferences[NORMAL_GAMES] = (prefs[NORMAL_GAMES] ?: 0) + 1
                GameMode.ADVANCE -> preferences[ADVANCE_GAMES] = (prefs[ADVANCE_GAMES] ?: 0) + 1
                GameMode.EXPERT -> preferences[EXPERT_GAMES] = (prefs[EXPERT_GAMES] ?: 0) + 1
                GameMode.TIMED -> preferences[TIMED_GAMES] = (prefs[TIMED_GAMES] ?: 0) + 1
            }
        }

        // Add XP and check for level up
        return addXp(xpEarned)
    }

    suspend fun getStatistics(): PlayerStatistics {
        val prefs = context.progressionDataStore.data.first()

        val totalGames = prefs[TOTAL_GAMES_PLAYED] ?: 0
        val totalCorrect = prefs[TOTAL_CORRECT_ANSWERS] ?: 0
        val totalWrong = prefs[TOTAL_WRONG_ANSWERS] ?: 0
        val totalQuestions = totalCorrect + totalWrong

        return PlayerStatistics(
            totalGamesPlayed = totalGames,
            gamesWon = prefs[GAMES_WON] ?: 0,
            totalCorrectAnswers = totalCorrect,
            totalWrongAnswers = totalWrong,
            accuracy = if (totalQuestions > 0) (totalCorrect.toFloat() / totalQuestions * 100) else 0f,
            bestStreakEver = prefs[BEST_STREAK_EVER] ?: 0,
            perfectGames = prefs[PERFECT_GAMES] ?: 0,
            totalTimePlayedMs = prefs[TOTAL_TIME_PLAYED_MS] ?: 0L,
            normalGamesPlayed = prefs[NORMAL_GAMES] ?: 0,
            advanceGamesPlayed = prefs[ADVANCE_GAMES] ?: 0,
            expertGamesPlayed = prefs[EXPERT_GAMES] ?: 0,
            timedGamesPlayed = prefs[TIMED_GAMES] ?: 0
        )
    }

    // ==================== ACHIEVEMENTS ====================

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
        addXp(achievement.xpReward.toLong())

        return true
    }

    suspend fun checkAndUnlockAchievements(): List<Achievement> {
        val stats = getStatistics()
        val prefs = context.progressionDataStore.data.first()
        val currentLevel = calculateLevel(prefs[TOTAL_XP] ?: 0L)
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

// ==================== DATA CLASSES ====================

data class UserProfile(
    val nickname: String = "",
    val imageBase64: String = ""
)

data class LevelInfo(
    val level: Int,
    val title: String,
    val totalXp: Long,
    val xpInCurrentLevel: Long,
    val xpNeededForNextLevel: Long,
    val progressPercent: Float
)

data class XpGainResult(
    val xpGained: Long,
    val totalXp: Long,
    val oldLevel: Int,
    val newLevel: Int,
    val leveledUp: Boolean
)

data class GameResult(
    val gameMode: GameMode,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val bestStreak: Int,
    val timePlayedMs: Long,
    val completedAllQuestions: Boolean
)

enum class GameMode {
    NORMAL, ADVANCE, EXPERT, TIMED
}

data class PlayerStatistics(
    val totalGamesPlayed: Int,
    val gamesWon: Int,
    val totalCorrectAnswers: Int,
    val totalWrongAnswers: Int,
    val accuracy: Float,
    val bestStreakEver: Int,
    val perfectGames: Int,
    val totalTimePlayedMs: Long,
    val normalGamesPlayed: Int,
    val advanceGamesPlayed: Int,
    val expertGamesPlayed: Int,
    val timedGamesPlayed: Int
) {
    val totalTimePlayed: String
        get() {
            val hours = totalTimePlayedMs / (1000 * 60 * 60)
            val minutes = (totalTimePlayedMs / (1000 * 60)) % 60
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }
}

enum class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val icon: String
) {
    // Game milestones
    FIRST_GAME("first_game", "First Steps", "Complete your first game", 50, "🎮"),
    TEN_GAMES("ten_games", "Getting Started", "Play 10 games", 100, "🎯"),
    FIFTY_GAMES("fifty_games", "Dedicated Player", "Play 50 games", 250, "🏅"),
    HUNDRED_GAMES("hundred_games", "Veteran", "Play 100 games", 500, "🎖️"),

    // Perfect games
    FIRST_PERFECT("first_perfect", "Perfectionist", "Complete a game with no mistakes", 100, "✨"),
    FIVE_PERFECT("five_perfect", "Flawless", "Complete 5 perfect games", 300, "💎"),

    // Streaks
    STREAK_5("streak_5", "On Fire", "Get a 5 answer streak", 50, "🔥"),
    STREAK_10("streak_10", "Unstoppable", "Get a 10 answer streak", 150, "⚡"),
    STREAK_15("streak_15", "Legendary", "Get a 15 answer streak", 300, "🌟"),
    STREAK_20("streak_20", "Godlike", "Get a 20 answer streak", 500, "👑"),

    // Levels
    LEVEL_10("level_10", "Rising Star", "Reach level 10", 200, "⭐"),
    LEVEL_25("level_25", "Pride Expert", "Reach level 25", 500, "🌈"),
    LEVEL_50("level_50", "Pride Legend", "Reach level 50", 1000, "🏆"),

    // Special
    EXPERT_MASTER("expert_master", "Expert Master", "Complete 25 expert games", 400, "🎓"),
    SPEED_DEMON("speed_demon", "Speed Demon", "Complete 10 timed mode games", 300, "⏱️"),
    DEDICATED("dedicated", "Dedicated", "Play for 1 hour total", 200, "⏰"),
    ACCURACY_80("accuracy_80", "Sharpshooter", "Maintain 80%+ accuracy over 10 games", 250, "🎯"),
    ACCURACY_90("accuracy_90", "Precision Master", "Maintain 90%+ accuracy over 20 games", 500, "💯")
}
