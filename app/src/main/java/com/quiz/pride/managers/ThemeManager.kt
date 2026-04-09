package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.quiz.pride.common.DataStoreKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension to create DataStore singleton
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pride_settings")

/**
 * Gestor de preferencias de usuario persistidas via DataStore.
 *
 * Aunque el nombre indica "Theme", este manager actua como "Settings Manager" general
 * y centraliza todas las preferencias de la app:
 * - Tema visual: dark mode, dynamic colors (Material You)
 * - Accesibilidad: alto contraste, texto grande
 * - Audio: sonido habilitado
 * - Navegacion: estado del onboarding (usado por MainActivity para elegir destino inicial)
 *
 * Refactor pendiente: si el scope crece, considerar separar en PreferencesManager
 * con sub-namespaces (theme, accessibility, navigation).
 */
class ThemeManager(private val context: Context) {

    companion object {
        private val DARK_MODE_KEY = DataStoreKeys.ThemeKeys.DARK_MODE
        private val SOUND_ENABLED_KEY = DataStoreKeys.ThemeKeys.SOUND_ENABLED
        private val DYNAMIC_COLORS_KEY = DataStoreKeys.ThemeKeys.DYNAMIC_COLORS
        private val HIGH_CONTRAST_KEY = DataStoreKeys.ThemeKeys.HIGH_CONTRAST
        private val LARGE_TEXT_KEY = DataStoreKeys.ThemeKeys.LARGE_TEXT
    }

    // Dark Mode
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    // Sound
    val isSoundEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SOUND_ENABLED_KEY] ?: true
        }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED_KEY] = enabled
        }
    }

    // Dynamic Colors (Material You)
    val isDynamicColorsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DYNAMIC_COLORS_KEY] ?: true
        }

    suspend fun setDynamicColorsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLORS_KEY] = enabled
        }
    }

    // Accessibility - High Contrast
    val isHighContrastEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HIGH_CONTRAST_KEY] ?: false
        }

    suspend fun setHighContrastEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HIGH_CONTRAST_KEY] = enabled
        }
    }

    // Accessibility - Large Text
    val isLargeTextEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[LARGE_TEXT_KEY] ?: false
        }

    suspend fun setLargeTextEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LARGE_TEXT_KEY] = enabled
        }
    }
}
