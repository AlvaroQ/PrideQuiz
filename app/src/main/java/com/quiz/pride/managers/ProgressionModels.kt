package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Extension de DataStore para el almacenamiento de progresion del jugador.
 * Los modelos de dominio (UserProfile, LevelInfo, XpGainResult, GameResult,
 * GameMode, PlayerStatistics, Achievement) viven en el modulo domain.
 */
internal val Context.progressionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "progression_preferences"
)
