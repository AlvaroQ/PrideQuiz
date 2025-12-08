package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.adFrequencyDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ad_frequency_preferences"
)

/**
 * Manages ad frequency to avoid overwhelming users with ads
 * while maximizing revenue through strategic ad placement
 */
class AdFrequencyManager(private val context: Context) {

    companion object {
        // Keys for DataStore
        private val GAMES_SINCE_INTERSTITIAL = intPreferencesKey("games_since_interstitial")
        private val INTERSTITIALS_THIS_SESSION = intPreferencesKey("interstitials_this_session")
        private val SESSION_START_TIME = longPreferencesKey("session_start_time")
        private val LAST_INTERSTITIAL_TIME = longPreferencesKey("last_interstitial_time")
        private val TOTAL_GAMES_PLAYED = intPreferencesKey("total_games_played")

        // Configuration constants
        const val GAMES_BEFORE_FIRST_INTERSTITIAL = 2  // Show after 2nd game
        const val GAMES_BETWEEN_INTERSTITIALS = 3      // Then every 3 games
        const val MAX_INTERSTITIALS_PER_SESSION = 4    // Max 4 per session
        const val COOLDOWN_MINUTES = 2                  // Min 2 min between ads
        const val SESSION_TIMEOUT_MINUTES = 30          // New session after 30 min
    }

    /**
     * Flow of games played since last interstitial
     */
    val gamesSinceInterstitial: Flow<Int> = context.adFrequencyDataStore.data
        .map { preferences ->
            preferences[GAMES_SINCE_INTERSTITIAL] ?: 0
        }

    /**
     * Check if we should show an interstitial ad
     */
    suspend fun shouldShowInterstitial(): Boolean {
        val preferences = context.adFrequencyDataStore.data.first()

        val gamesSinceLast = preferences[GAMES_SINCE_INTERSTITIAL] ?: 0
        val interstitialsThisSession = preferences[INTERSTITIALS_THIS_SESSION] ?: 0
        val lastInterstitialTime = preferences[LAST_INTERSTITIAL_TIME] ?: 0L
        val totalGamesPlayed = preferences[TOTAL_GAMES_PLAYED] ?: 0

        // Check session reset
        checkAndResetSession()

        // Don't exceed max interstitials per session
        if (interstitialsThisSession >= MAX_INTERSTITIALS_PER_SESSION) {
            return false
        }

        // Check cooldown (minimum time between interstitials)
        val timeSinceLastAd = System.currentTimeMillis() - lastInterstitialTime
        val cooldownMs = COOLDOWN_MINUTES * 60 * 1000L
        if (timeSinceLastAd < cooldownMs && lastInterstitialTime > 0) {
            return false
        }

        // First time user - show after GAMES_BEFORE_FIRST_INTERSTITIAL games
        if (totalGamesPlayed < GAMES_BEFORE_FIRST_INTERSTITIAL) {
            return false
        }

        // For subsequent interstitials, check games between
        return gamesSinceLast >= GAMES_BETWEEN_INTERSTITIALS
    }

    /**
     * Record that a game was completed
     */
    suspend fun recordGameCompleted() {
        context.adFrequencyDataStore.edit { preferences ->
            val current = preferences[GAMES_SINCE_INTERSTITIAL] ?: 0
            val total = preferences[TOTAL_GAMES_PLAYED] ?: 0
            preferences[GAMES_SINCE_INTERSTITIAL] = current + 1
            preferences[TOTAL_GAMES_PLAYED] = total + 1
        }
    }

    /**
     * Record that an interstitial was shown
     */
    suspend fun recordInterstitialShown() {
        context.adFrequencyDataStore.edit { preferences ->
            val sessionCount = preferences[INTERSTITIALS_THIS_SESSION] ?: 0
            preferences[GAMES_SINCE_INTERSTITIAL] = 0
            preferences[INTERSTITIALS_THIS_SESSION] = sessionCount + 1
            preferences[LAST_INTERSTITIAL_TIME] = System.currentTimeMillis()
        }
    }

    /**
     * Check and reset session if needed
     */
    private suspend fun checkAndResetSession() {
        val preferences = context.adFrequencyDataStore.data.first()
        val sessionStart = preferences[SESSION_START_TIME] ?: 0L
        val now = System.currentTimeMillis()
        val sessionTimeoutMs = SESSION_TIMEOUT_MINUTES * 60 * 1000L

        if (now - sessionStart > sessionTimeoutMs || sessionStart == 0L) {
            // Reset session
            context.adFrequencyDataStore.edit { prefs ->
                prefs[SESSION_START_TIME] = now
                prefs[INTERSTITIALS_THIS_SESSION] = 0
            }
        }
    }

    /**
     * Get current session stats for debugging
     */
    suspend fun getSessionStats(): SessionStats {
        val preferences = context.adFrequencyDataStore.data.first()
        return SessionStats(
            gamesSinceInterstitial = preferences[GAMES_SINCE_INTERSTITIAL] ?: 0,
            interstitialsThisSession = preferences[INTERSTITIALS_THIS_SESSION] ?: 0,
            totalGamesPlayed = preferences[TOTAL_GAMES_PLAYED] ?: 0
        )
    }

    data class SessionStats(
        val gamesSinceInterstitial: Int,
        val interstitialsThisSession: Int,
        val totalGamesPlayed: Int
    )
}
