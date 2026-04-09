package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.quiz.pride.common.DataStoreKeys
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
        private val GAMES_SINCE_INTERSTITIAL = DataStoreKeys.AdFrequencyKeys.GAMES_SINCE_INTERSTITIAL
        private val INTERSTITIALS_THIS_SESSION = DataStoreKeys.AdFrequencyKeys.INTERSTITIALS_THIS_SESSION
        private val SESSION_START_TIME = DataStoreKeys.AdFrequencyKeys.SESSION_START_TIME
        private val LAST_INTERSTITIAL_TIME = DataStoreKeys.AdFrequencyKeys.LAST_INTERSTITIAL_TIME
        private val TOTAL_GAMES_PLAYED = DataStoreKeys.AdFrequencyKeys.TOTAL_GAMES_PLAYED

        // Configuration constants
        const val GAMES_BEFORE_FIRST_INTERSTITIAL = 2  // Show after 2nd game
        const val GAMES_BETWEEN_INTERSTITIALS = 3      // Then every 3 games
        const val MAX_INTERSTITIALS_PER_SESSION = 4    // Max 4 per session
        const val COOLDOWN_MINUTES = 2                  // Min 2 min between ads
        const val SESSION_TIMEOUT_MINUTES = 30          // New session after 30 min

        /**
         * Logica pura de decision de mostrar interstitial, sin side-effects.
         * Extraida del companion para facilitar tests unitarios sin Context/DataStore.
         */
        internal fun evaluateShouldShowInterstitial(
            gamesSinceLast: Int,
            interstitialsThisSession: Int,
            lastInterstitialTimeMs: Long,
            totalGamesPlayed: Int,
            currentTimeMs: Long = System.currentTimeMillis()
        ): Boolean {
            if (interstitialsThisSession >= MAX_INTERSTITIALS_PER_SESSION) {
                return false
            }

            val timeSinceLastAd = currentTimeMs - lastInterstitialTimeMs
            val cooldownMs = COOLDOWN_MINUTES * 60 * 1000L
            if (timeSinceLastAd < cooldownMs && lastInterstitialTimeMs > 0) {
                return false
            }

            if (totalGamesPlayed < GAMES_BEFORE_FIRST_INTERSTITIAL) {
                return false
            }

            return gamesSinceLast >= GAMES_BETWEEN_INTERSTITIALS
        }
    }

    /**
     * Check if we should show an interstitial ad
     */
    suspend fun shouldShowInterstitial(): Boolean {
        // Lectura unica del DataStore — evita doble lectura con checkAndResetSession
        val preferences = context.adFrequencyDataStore.data.first()

        val gamesSinceLast = preferences[GAMES_SINCE_INTERSTITIAL] ?: 0
        val interstitialsThisSession = preferences[INTERSTITIALS_THIS_SESSION] ?: 0
        val lastInterstitialTime = preferences[LAST_INTERSTITIAL_TIME] ?: 0L
        val totalGamesPlayed = preferences[TOTAL_GAMES_PLAYED] ?: 0

        // Check session reset pasando las preferencias ya leidas
        checkAndResetSession(preferences)

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
     * Check and reset session if needed.
     * Recibe las preferencias ya leidas para evitar una segunda lectura del DataStore.
     */
    private suspend fun checkAndResetSession(preferences: Preferences) {
        val sessionStart = preferences[SESSION_START_TIME] ?: 0L
        val now = System.currentTimeMillis()
        val sessionTimeoutMs = SESSION_TIMEOUT_MINUTES * 60 * 1000L

        if (now - sessionStart > sessionTimeoutMs || sessionStart == 0L) {
            // Solo escribe si se necesita resetear la sesion
            context.adFrequencyDataStore.edit { prefs ->
                prefs[SESSION_START_TIME] = now
                prefs[INTERSTITIALS_THIS_SESSION] = 0
            }
        }
    }

}
