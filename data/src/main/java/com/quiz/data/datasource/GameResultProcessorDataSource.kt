package com.quiz.data.datasource

import com.quiz.domain.Achievement
import com.quiz.domain.GameResult
import com.quiz.domain.StreakCheckResult
import com.quiz.domain.XpGainResult
import com.quiz.domain.challenge.ChallengeCompletionResult

/**
 * Contrato para procesar el resultado de una partida.
 *
 * Esta interfaz permite que la capa usecases/ orqueste el procesamiento
 * sin depender de los managers concretos que viven en app/.
 * La implementacion concreta (GameResultProcessorImpl) se registra en Koin
 * desde el modulo app/ siguiendo la Dependency Rule.
 */
interface GameResultProcessorDataSource {

    /**
     * Registra las estadisticas del juego, calcula el XP ganado y procesa
     * la racha diaria (aplicando multiplicador y acreditando XP de ciclo).
     * Tambien procesa el evento GameCompleted en el sistema de desafios diarios.
     *
     * @param result Datos del resultado de la partida
     * @return XpGainResult con el XP ganado, nivel nuevo y si hubo level-up
     */
    suspend fun recordGameAndComputeXp(result: GameResult): XpGainResult

    /**
     * Verifica y desbloquea logros basados en las estadisticas acumuladas.
     * Debe llamarse DESPUES de recordGameAndComputeXp para que las stats
     * ya esten actualizadas al momento de la evaluacion.
     *
     * @return Lista de achievements recien desbloqueados (vacia si ninguno)
     */
    suspend fun checkAndUnlockAchievements(): List<Achievement>

    /**
     * Dispara la sincronizacion del XP del usuario con el leaderboard de Firestore.
     * Es fire-and-forget: no bloquea el flujo principal.
     */
    suspend fun triggerXpSync()

    /**
     * Retorna el resultado de racha del ultimo procesamiento.
     * Disponible despues de llamar a [recordGameAndComputeXp].
     */
    fun getLastStreakResult(): StreakCheckResult?

    /**
     * Retorna el XP total de racha acreditado en el ultimo procesamiento
     * (suma de XP por multiplicador + XP bonus del ciclo diario).
     * Disponible despues de llamar a [recordGameAndComputeXp].
     */
    fun getLastStreakXpBonus(): Int

    /**
     * Retorna el resultado de desafios diarios del ultimo procesamiento.
     * Incluye desafios recien completados y XP/coins ganados.
     * Disponible despues de llamar a [recordGameAndComputeXp].
     */
    fun getLastChallengeResult(): ChallengeCompletionResult?

    /**
     * Retorna el total de coins ganados en el ultimo procesamiento.
     * Incluye coins base por partida + coins de desafios + coins de ciclo de racha.
     * Disponible despues de llamar a [recordGameAndComputeXp].
     */
    fun getLastCoinsEarned(): Int

    /**
     * Retorna el total de gems ganadas en el ultimo procesamiento.
     * Las gems se otorgan en hitos de racha significativos (30, 90, 365 dias).
     * Disponible despues de llamar a [recordGameAndComputeXp].
     */
    fun getLastGemsEarned(): Int
}
