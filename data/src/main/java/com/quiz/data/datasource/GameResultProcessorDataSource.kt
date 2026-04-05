package com.quiz.data.datasource

import com.quiz.domain.Achievement
import com.quiz.domain.GameResult
import com.quiz.domain.XpGainResult

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
     * Registra las estadisticas del juego y calcula el XP ganado.
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
}
