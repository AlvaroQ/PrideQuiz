package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first

/**
 * Manages game statistics: recording results and retrieving player stats
 */
class GameStatsManager(
    private val context: Context,
    private val progressionManager: ProgressionManager
) {

    companion object {
        // Statistics Keys
        internal val TOTAL_GAMES_PLAYED = intPreferencesKey("total_games_played")
        internal val TOTAL_CORRECT_ANSWERS = intPreferencesKey("total_correct_answers")
        internal val TOTAL_WRONG_ANSWERS = intPreferencesKey("total_wrong_answers")
        internal val BEST_STREAK_EVER = intPreferencesKey("best_streak_ever")
        internal val TOTAL_TIME_PLAYED_MS = longPreferencesKey("total_time_played_ms")
        internal val PERFECT_GAMES = intPreferencesKey("perfect_games")
        internal val GAMES_WON = intPreferencesKey("games_won")

        // Mode-specific stats
        internal val NORMAL_GAMES = intPreferencesKey("normal_games")
        internal val ADVANCE_GAMES = intPreferencesKey("advance_games")
        internal val EXPERT_GAMES = intPreferencesKey("expert_games")
        internal val TIMED_GAMES = intPreferencesKey("timed_games")
    }

    suspend fun recordGameResult(result: GameResult): XpGainResult {
        val prefs = context.progressionDataStore.data.first()

        // Calculate XP earned
        var xpEarned = 0L

        // Base XP for correct answers
        xpEarned += result.correctAnswers * ProgressionManager.XP_PER_CORRECT_ANSWER

        // Streak bonus
        if (result.bestStreak >= 5) {
            xpEarned += (result.bestStreak / 5) * ProgressionManager.XP_PER_STREAK_BONUS
        }

        // Perfect game bonus
        if (result.correctAnswers == result.totalQuestions && result.totalQuestions > 0) {
            xpEarned += ProgressionManager.XP_PER_PERFECT_GAME
        }

        // Win bonus (completed all questions)
        if (result.completedAllQuestions) {
            xpEarned += ProgressionManager.XP_PER_WIN
        }

        // Mode multipliers
        xpEarned = when (result.gameMode) {
            GameMode.ADVANCE -> (xpEarned * ProgressionManager.XP_MULTIPLIER_ADVANCE).toLong()
            GameMode.EXPERT -> (xpEarned * ProgressionManager.XP_MULTIPLIER_EXPERT).toLong()
            GameMode.TIMED -> (xpEarned * ProgressionManager.XP_MULTIPLIER_TIMED).toLong()
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
        return progressionManager.addXp(xpEarned)
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
}
