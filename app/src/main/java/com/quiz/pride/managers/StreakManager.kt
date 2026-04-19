package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.quiz.domain.StreakCheckResult
import com.quiz.domain.StreakRules
import com.quiz.domain.StreakState
import com.quiz.pride.common.DataStoreKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Manager que gestiona la persistencia del estado de la racha diaria.
 *
 * Delega toda la logica de negocio a [StreakRules] (dominio puro).
 * Este manager solo es responsable de:
 * - Leer el estado desde progressionDataStore
 * - Escribir el nuevo estado en progressionDataStore
 * - Calcular las fechas de hoy y ayer para proveerlas a StreakRules
 *
 * El Mutex garantiza que no haya condiciones de carrera si se llama
 * onGameCompleted() concurrentemente (ej: doble tap rapido en resultado).
 *
 * Registrado como Koin single en managerModule.
 */
class StreakManager(private val context: Context) {

    private val mutex = Mutex()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Debe llamarse al finalizar cada partida completada.
     * Evalua el estado de racha y persiste el nuevo estado si hubo cambios.
     *
     * @return StreakCheckResult describiendo que ocurrio (continua, rota, salvada, etc.)
     */
    suspend fun onGameCompleted(): StreakCheckResult = mutex.withLock {
        val state = readState()
        val today = todayDate()
        val yesterday = yesterdayDate()

        val result = StreakRules.checkStreak(state, today, yesterday)

        // Solo persistir si el estado cambio (AlreadyPlayedToday no cambia nada)
        val newState = when (result) {
            is StreakCheckResult.AlreadyPlayedToday -> null
            is StreakCheckResult.StreakContinued -> result.newState
            is StreakCheckResult.StreakSavedByFreeze -> result.newState
            is StreakCheckResult.StreakBroken -> result.newState
            is StreakCheckResult.NewStreak -> result.newState
        }

        if (newState != null) {
            writeState(newState)
        }

        result
    }

    /** Retorna el estado actual de la racha sin modificarlo. */
    suspend fun getStreakState(): StreakState = readState()

    /**
     * Indica si la racha del jugador esta en riesgo de romperse hoy.
     * Util para mostrar avisos o notificaciones en la UI.
     */
    suspend fun isStreakAtRisk(): Boolean {
        val state = readState()
        return StreakRules.isStreakAtRisk(state, todayDate(), yesterdayDate())
    }

    /** Verifica si el jugador ya completo al menos una partida hoy. */
    suspend fun hasPlayedToday(): Boolean {
        return readState().lastPlayedDate == todayDate()
    }

    /**
     * Retorna el multiplicador de XP correspondiente a la racha actual.
     * Se debe aplicar sobre el XP base de la partida antes de persistirlo.
     */
    suspend fun getStreakMultiplier(): Float {
        return StreakRules.streakMultiplier(readState().currentStreak)
    }

    // ==================== PERSISTENCIA ====================

    private suspend fun readState(): StreakState {
        val prefs = context.progressionDataStore.data.first()
        return StreakState(
            currentStreak = prefs[DataStoreKeys.StreakKeys.CURRENT_STREAK] ?: 0,
            bestStreak = prefs[DataStoreKeys.StreakKeys.BEST_STREAK] ?: 0,
            lastPlayedDate = prefs[DataStoreKeys.StreakKeys.LAST_PLAYED_DATE] ?: "",
            freezeTokens = prefs[DataStoreKeys.StreakKeys.FREEZE_TOKENS] ?: 0,
            cycleDay = prefs[DataStoreKeys.StreakKeys.CYCLE_DAY] ?: 1,
            totalDaysPlayed = prefs[DataStoreKeys.StreakKeys.TOTAL_DAYS_PLAYED] ?: 0,
            streakStartDate = prefs[DataStoreKeys.StreakKeys.STREAK_START_DATE] ?: "",
            lastFreezeUsedDate = prefs[DataStoreKeys.StreakKeys.LAST_FREEZE_USED_DATE] ?: ""
        )
    }

    private suspend fun writeState(state: StreakState) {
        context.progressionDataStore.edit { prefs ->
            prefs[DataStoreKeys.StreakKeys.CURRENT_STREAK] = state.currentStreak
            prefs[DataStoreKeys.StreakKeys.BEST_STREAK] = state.bestStreak
            prefs[DataStoreKeys.StreakKeys.LAST_PLAYED_DATE] = state.lastPlayedDate
            prefs[DataStoreKeys.StreakKeys.FREEZE_TOKENS] = state.freezeTokens
            prefs[DataStoreKeys.StreakKeys.CYCLE_DAY] = state.cycleDay
            prefs[DataStoreKeys.StreakKeys.TOTAL_DAYS_PLAYED] = state.totalDaysPlayed
            prefs[DataStoreKeys.StreakKeys.STREAK_START_DATE] = state.streakStartDate
            prefs[DataStoreKeys.StreakKeys.LAST_FREEZE_USED_DATE] = state.lastFreezeUsedDate
        }
    }

    // ==================== DISMISS WIDGET ====================

    /**
     * Flow reactivo del estado "widget oculto hoy". Reset automatico al cambiar de fecha.
     * Solo aplica al widget de SelectScreen; en Profile el widget es siempre visible.
     */
    fun observeIsWidgetDismissedToday(): Flow<Boolean> =
        context.progressionDataStore.data.map { prefs ->
            prefs[DataStoreKeys.StreakKeys.WIDGET_DISMISSED_DATE] == todayDate()
        }

    /** Persiste la fecha de hoy como "widget oculto" — se re-expone automaticamente manana. */
    suspend fun dismissWidgetToday() {
        val today = todayDate()
        context.progressionDataStore.edit { prefs ->
            prefs[DataStoreKeys.StreakKeys.WIDGET_DISMISSED_DATE] = today
        }
    }

    // ==================== FECHAS ====================

    private fun todayDate(): String = dateFormat.format(Calendar.getInstance().time)

    private fun yesterdayDate(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(cal.time)
    }
}
