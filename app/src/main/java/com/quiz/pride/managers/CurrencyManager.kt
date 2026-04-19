package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.quiz.domain.cosmetics.CurrencyBalance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Gestiona el balance de moneda virtual del jugador (coins y gems).
 *
 * Utiliza el cosmeticsDataStore ("cosmetics_preferences") para persistir
 * el saldo de forma independiente al resto de la progresion del jugador.
 *
 * El Mutex garantiza atomicidad en operaciones de earn/spend concurrentes.
 */
class CurrencyManager(private val context: Context) {

    private val dataStore get() = context.cosmeticsDataStore
    private val mutex = Mutex()

    companion object {
        val KEY_COINS = intPreferencesKey("currency_coins")
        val KEY_GEMS = intPreferencesKey("currency_gems")
    }

    /** Retorna el balance actual de moneda del jugador. */
    suspend fun getBalance(): CurrencyBalance {
        val prefs = dataStore.data.first()
        return CurrencyBalance(
            coins = prefs[KEY_COINS] ?: 0,
            gems = prefs[KEY_GEMS] ?: 0
        )
    }

    /** Flow reactivo del balance: ideal para colectar desde ViewModels. */
    fun observeBalance(): Flow<CurrencyBalance> = dataStore.data.map { prefs ->
        CurrencyBalance(
            coins = prefs[KEY_COINS] ?: 0,
            gems = prefs[KEY_GEMS] ?: 0
        )
    }

    /**
     * Suma [amount] coins al balance.
     * @param source Identificador de la fuente (ej: "game_completion", "streak_milestone") para trazabilidad.
     * @return Balance actualizado despues de la operacion.
     */
    suspend fun earnCoins(amount: Int, source: String = ""): CurrencyBalance = mutex.withLock {
        dataStore.edit { prefs ->
            prefs[KEY_COINS] = (prefs[KEY_COINS] ?: 0) + amount
        }
        getBalance()
    }

    /**
     * Suma [amount] gems al balance.
     * @param source Identificador de la fuente para trazabilidad.
     * @return Balance actualizado despues de la operacion.
     */
    suspend fun earnGems(amount: Int, source: String = ""): CurrencyBalance = mutex.withLock {
        dataStore.edit { prefs ->
            prefs[KEY_GEMS] = (prefs[KEY_GEMS] ?: 0) + amount
        }
        getBalance()
    }

    /**
     * Descuenta [amount] coins si el balance es suficiente.
     * @return true si el gasto fue exitoso, false si no hay saldo suficiente.
     */
    suspend fun spendCoins(amount: Int): Boolean = mutex.withLock {
        val current = dataStore.data.first()[KEY_COINS] ?: 0
        if (current < amount) return@withLock false
        dataStore.edit { prefs ->
            prefs[KEY_COINS] = current - amount
        }
        true
    }

    /**
     * Descuenta [amount] gems si el balance es suficiente.
     * @return true si el gasto fue exitoso, false si no hay saldo suficiente.
     */
    suspend fun spendGems(amount: Int): Boolean = mutex.withLock {
        val current = dataStore.data.first()[KEY_GEMS] ?: 0
        if (current < amount) return@withLock false
        dataStore.edit { prefs ->
            prefs[KEY_GEMS] = current - amount
        }
        true
    }
}
