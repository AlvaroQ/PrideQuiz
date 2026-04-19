package com.quiz.domain

/**
 * Resultado del chequeo de racha diaria al completar una partida.
 *
 * Sealed class que cubre todos los escenarios posibles:
 * - Ya jugo hoy (sin cambios)
 * - Racha continua (jugo ayer o es el primer dia)
 * - Racha salvada por freeze token (falto ayer, tenia token disponible)
 * - Racha rota (falto ayer, sin tokens de freeze)
 * - Nueva racha (primer juego de la historia o despues de ausencia larga)
 */
sealed class StreakCheckResult {

    /** Ya jugo hoy, no se generan cambios ni recompensas adicionales. */
    data class AlreadyPlayedToday(val state: StreakState) : StreakCheckResult()

    /** La racha continua normalmente (jugo ayer o es el primer dia del historial). */
    data class StreakContinued(
        val newState: StreakState,
        val reward: StreakReward
    ) : StreakCheckResult()

    /** La racha fue salvada consumiendo un freeze token (falto el dia anterior). */
    data class StreakSavedByFreeze(
        val newState: StreakState,
        val reward: StreakReward
    ) : StreakCheckResult()

    /** La racha se rompio: falto ayer y no habia tokens de freeze disponibles. */
    data class StreakBroken(
        val newState: StreakState,
        val reward: StreakReward,
        val previousStreak: Int
    ) : StreakCheckResult()

    /** Racha nueva: primer juego registrado o retorno despues de una ausencia prolongada. */
    data class NewStreak(
        val newState: StreakState,
        val reward: StreakReward
    ) : StreakCheckResult()
}
