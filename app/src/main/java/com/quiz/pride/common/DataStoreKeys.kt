package com.quiz.pride.common

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Claves centralizadas de DataStore para el modulo de progresion del jugador.
 * Todos los managers que accedan a progressionDataStore deben referenciar estas
 * claves en lugar de definirlas localmente.
 *
 * DataStores del proyecto:
 * - progressionDataStore ("progression_preferences") -> claves en [ProgressionKeys] y [GameStatsKeys]
 * - adFrequencyDataStore ("ad_frequency_preferences") -> [AdFrequencyKeys]
 * - syncDataStore ("xp_sync_preferences")            -> [XpSyncKeys]
 * - themeDataStore ("pride_settings")                -> [ThemeKeys]
 */
object DataStoreKeys {

    /** Claves para progressionDataStore — perfil, XP y nivel del jugador */
    object ProgressionKeys {
        val USER_NICKNAME = stringPreferencesKey("user_nickname")
        val USER_IMAGE = stringPreferencesKey("user_image")
        val TOTAL_XP = longPreferencesKey("total_xp")
        val CURRENT_LEVEL = intPreferencesKey("current_level")
        val UNLOCKED_ACHIEVEMENTS = stringPreferencesKey("unlocked_achievements")
    }

    /** Claves para progressionDataStore — estadisticas de partidas */
    object GameStatsKeys {
        val TOTAL_GAMES_PLAYED = intPreferencesKey("total_games_played")
        val TOTAL_CORRECT_ANSWERS = intPreferencesKey("total_correct_answers")
        val TOTAL_WRONG_ANSWERS = intPreferencesKey("total_wrong_answers")
        val BEST_STREAK_EVER = intPreferencesKey("best_streak_ever")
        val TOTAL_TIME_PLAYED_MS = longPreferencesKey("total_time_played_ms")
        val PERFECT_GAMES = intPreferencesKey("perfect_games")
        val GAMES_WON = intPreferencesKey("games_won")
        val NORMAL_GAMES = intPreferencesKey("normal_games")
        val ADVANCE_GAMES = intPreferencesKey("advance_games")
        val EXPERT_GAMES = intPreferencesKey("expert_games")
        val TIMED_GAMES = intPreferencesKey("timed_games")
    }

    /** Claves para adFrequencyDataStore — frecuencia y tracking de anuncios */
    object AdFrequencyKeys {
        val GAMES_SINCE_INTERSTITIAL = intPreferencesKey("games_since_interstitial")
        val INTERSTITIALS_THIS_SESSION = intPreferencesKey("interstitials_this_session")
        val SESSION_START_TIME = longPreferencesKey("session_start_time")
        val LAST_INTERSTITIAL_TIME = longPreferencesKey("last_interstitial_time")
        val TOTAL_GAMES_PLAYED = intPreferencesKey("total_games_played")
    }

    /** Claves para syncDataStore — sincronizacion de XP con Firestore */
    object XpSyncKeys {
        val LAST_SYNCED_TIME = longPreferencesKey("last_synced_time")
        val PENDING_SYNC = booleanPreferencesKey("pending_sync")
    }

    /** Claves para themeDataStore ("pride_settings") — preferencias de tema y accesibilidad */
    object ThemeKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast_enabled")
        val LARGE_TEXT = booleanPreferencesKey("large_text_enabled")
    }
}
