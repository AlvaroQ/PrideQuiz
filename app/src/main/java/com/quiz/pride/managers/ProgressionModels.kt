package com.quiz.pride.managers

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.quiz.pride.R

internal val Context.progressionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "progression_preferences"
)

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
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val xpReward: Int,
    val icon: String
) {
    // Game milestones
    FIRST_GAME("first_game", R.string.achievement_first_game_title, R.string.achievement_first_game_desc, 50, "\uD83C\uDFAE"),
    TEN_GAMES("ten_games", R.string.achievement_ten_games_title, R.string.achievement_ten_games_desc, 100, "\uD83C\uDFAF"),
    FIFTY_GAMES("fifty_games", R.string.achievement_fifty_games_title, R.string.achievement_fifty_games_desc, 250, "\uD83C\uDFC5"),
    HUNDRED_GAMES("hundred_games", R.string.achievement_hundred_games_title, R.string.achievement_hundred_games_desc, 500, "\uD83C\uDF96\uFE0F"),

    // Perfect games
    FIRST_PERFECT("first_perfect", R.string.achievement_first_perfect_title, R.string.achievement_first_perfect_desc, 100, "\u2728"),
    FIVE_PERFECT("five_perfect", R.string.achievement_five_perfect_title, R.string.achievement_five_perfect_desc, 300, "\uD83D\uDC8E"),

    // Streaks
    STREAK_5("streak_5", R.string.achievement_streak_5_title, R.string.achievement_streak_5_desc, 50, "\uD83D\uDD25"),
    STREAK_10("streak_10", R.string.achievement_streak_10_title, R.string.achievement_streak_10_desc, 150, "\u26A1"),
    STREAK_15("streak_15", R.string.achievement_streak_15_title, R.string.achievement_streak_15_desc, 300, "\uD83C\uDF1F"),
    STREAK_20("streak_20", R.string.achievement_streak_20_title, R.string.achievement_streak_20_desc, 500, "\uD83D\uDC51"),

    // Levels
    LEVEL_10("level_10", R.string.achievement_level_10_title, R.string.achievement_level_10_desc, 200, "\u2B50"),
    LEVEL_25("level_25", R.string.achievement_level_25_title, R.string.achievement_level_25_desc, 500, "\uD83C\uDF08"),
    LEVEL_50("level_50", R.string.achievement_level_50_title, R.string.achievement_level_50_desc, 1000, "\uD83C\uDFC6"),

    // Special
    EXPERT_MASTER("expert_master", R.string.achievement_expert_master_title, R.string.achievement_expert_master_desc, 400, "\uD83C\uDF93"),
    SPEED_DEMON("speed_demon", R.string.achievement_speed_demon_title, R.string.achievement_speed_demon_desc, 300, "\u23F1\uFE0F"),
    DEDICATED("dedicated", R.string.achievement_dedicated_title, R.string.achievement_dedicated_desc, 200, "\u23F0"),
    ACCURACY_80("accuracy_80", R.string.achievement_accuracy_80_title, R.string.achievement_accuracy_80_desc, 250, "\uD83C\uDFAF"),
    ACCURACY_90("accuracy_90", R.string.achievement_accuracy_90_title, R.string.achievement_accuracy_90_desc, 500, "\uD83D\uDCAF")
}
