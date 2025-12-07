package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension to create DataStore singleton
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pride_settings")

class ThemeManager(private val context: Context) {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
        private val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
        private val DYNAMIC_COLORS_KEY = booleanPreferencesKey("dynamic_colors_enabled")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val HIGH_CONTRAST_KEY = booleanPreferencesKey("high_contrast_enabled")
        private val LARGE_TEXT_KEY = booleanPreferencesKey("large_text_enabled")
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

    // Onboarding
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed
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
