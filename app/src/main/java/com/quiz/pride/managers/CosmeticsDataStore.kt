package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Extension de DataStore para el almacenamiento de cosmeticos y moneda virtual.
 * Separado del progressionDataStore para evitar contaminacion de claves.
 *
 * Accedido por CurrencyManager y UnlockablesManager (ambos en el mismo paquete).
 */
internal val Context.cosmeticsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "cosmetics_preferences"
)
