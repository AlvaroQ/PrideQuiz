package com.quiz.domain

/**
 * Logica pura de negocio para el sistema de rachas diarias.
 *
 * Object singleton sin dependencias externas (Kotlin puro, sin Android framework).
 * Todas las decisiones de estado de racha ocurren aqui.
 * StreakManager en app/ solo persiste el resultado en DataStore.
 *
 * Ciclo de 7 dias:
 *   Dia 1 = 10 XP, Dia 2 = 15, Dia 3 = 20, Dia 4 = 30,
 *   Dia 5 = 50, Dia 6 = 75, Dia 7 = 100 XP + 1 freeze token
 *
 * Multiplicador sobre XP de partida:
 *   1-6 dias  = 1.0x
 *   7-13 dias = 1.1x
 *   14-29     = 1.2x
 *   30-59     = 1.3x
 *   60-89     = 1.5x
 *   90+       = 2.0x
 */
object StreakRules {

    const val MAX_FREEZE_TOKENS = 3

    /** XP bonus por dia del ciclo (1 a 7). */
    private val CYCLE_REWARDS = mapOf(
        1 to 10, 2 to 15, 3 to 20, 4 to 30, 5 to 50, 6 to 75, 7 to 100
    )

    /** XP bonus por hito de racha acumulada. */
    private val MILESTONES = mapOf(
        7 to 200, 14 to 500, 30 to 1000, 60 to 2500, 90 to 5000, 180 to 7500, 365 to 10000
    )

    /** Dias de racha en los que se gana un freeze token adicional por hito. */
    private val FREEZE_MILESTONES = setOf(7, 30, 90)

    /**
     * Retorna el multiplicador de XP aplicable segun los dias de racha acumulados.
     * Se multiplica sobre el XP base que el jugador gano en la partida.
     */
    fun streakMultiplier(streakDays: Int): Float = when {
        streakDays < 7 -> 1.0f
        streakDays < 14 -> 1.1f
        streakDays < 30 -> 1.2f
        streakDays < 60 -> 1.3f
        streakDays < 90 -> 1.5f
        else -> 2.0f
    }

    /**
     * Evalua el estado de racha al completar una partida.
     *
     * @param currentState Estado actual almacenado en DataStore
     * @param todayDate Fecha de hoy en formato "yyyy-MM-dd"
     * @param yesterdayDate Fecha de ayer en formato "yyyy-MM-dd"
     * @return StreakCheckResult con el nuevo estado y la recompensa correspondiente
     */
    fun checkStreak(
        currentState: StreakState,
        todayDate: String,
        yesterdayDate: String
    ): StreakCheckResult {
        val lastPlayed = currentState.lastPlayedDate

        // Caso 1: ya jugo hoy — no modificar nada
        if (lastPlayed == todayDate) {
            return StreakCheckResult.AlreadyPlayedToday(currentState)
        }

        // Caso 2: primer juego registrado nunca
        if (lastPlayed.isEmpty()) {
            val newState = StreakState(
                currentStreak = 1,
                bestStreak = 1,
                lastPlayedDate = todayDate,
                freezeTokens = currentState.freezeTokens,
                cycleDay = 1,
                totalDaysPlayed = 1,
                streakStartDate = todayDate
            )
            return StreakCheckResult.NewStreak(
                newState = newState,
                reward = buildReward(newState)
            )
        }

        // Caso 3: jugo ayer — la racha continua normalmente
        if (lastPlayed == yesterdayDate) {
            val newStreak = currentState.currentStreak + 1
            val newCycleDay = (currentState.cycleDay % 7) + 1
            val newBest = maxOf(newStreak, currentState.bestStreak)

            // Freeze ganado: fin de ciclo de 7 dias
            var freezeEarned = if (newCycleDay == 7) 1 else 0
            // Freeze adicional por hito de racha (7, 30, 90 dias)
            if (newStreak in FREEZE_MILESTONES) freezeEarned++

            val newState = currentState.copy(
                currentStreak = newStreak,
                bestStreak = newBest,
                lastPlayedDate = todayDate,
                freezeTokens = minOf(currentState.freezeTokens + freezeEarned, MAX_FREEZE_TOKENS),
                cycleDay = newCycleDay,
                totalDaysPlayed = currentState.totalDaysPlayed + 1
            )
            return StreakCheckResult.StreakContinued(
                newState = newState,
                reward = buildReward(newState)
            )
        }

        // Caso 4: falto ayer — intentar salvar con freeze token
        if (currentState.freezeTokens > 0 && currentState.currentStreak > 0) {
            val newStreak = currentState.currentStreak + 1
            val newCycleDay = (currentState.cycleDay % 7) + 1
            val newBest = maxOf(newStreak, currentState.bestStreak)

            val newState = currentState.copy(
                currentStreak = newStreak,
                bestStreak = newBest,
                lastPlayedDate = todayDate,
                freezeTokens = currentState.freezeTokens - 1, // se consume un token
                cycleDay = newCycleDay,
                totalDaysPlayed = currentState.totalDaysPlayed + 1,
                lastFreezeUsedDate = todayDate
            )
            return StreakCheckResult.StreakSavedByFreeze(
                newState = newState,
                reward = buildReward(newState)
            )
        }

        // Caso 5: racha rota — resetear a 1 (el dia de hoy cuenta como inicio)
        val previousStreak = currentState.currentStreak
        val newState = StreakState(
            currentStreak = 1,
            bestStreak = maxOf(currentState.bestStreak, previousStreak),
            lastPlayedDate = todayDate,
            freezeTokens = currentState.freezeTokens,
            cycleDay = 1,
            totalDaysPlayed = currentState.totalDaysPlayed + 1,
            streakStartDate = todayDate
        )
        return StreakCheckResult.StreakBroken(
            newState = newState,
            reward = buildReward(newState),
            previousStreak = previousStreak
        )
    }

    /**
     * Indica si la racha del jugador esta en riesgo de romperse hoy.
     * Se usa para mostrar notificaciones o avisos en la UI.
     *
     * Condicion: tiene racha activa, jugo ayer pero aun no jugo hoy.
     */
    fun isStreakAtRisk(state: StreakState, todayDate: String, yesterdayDate: String): Boolean {
        return state.currentStreak > 0 &&
                state.lastPlayedDate == yesterdayDate &&
                state.lastPlayedDate != todayDate
    }

    /** Construye la recompensa a partir del estado ya actualizado. */
    private fun buildReward(state: StreakState): StreakReward {
        val cycleXp = CYCLE_REWARDS[state.cycleDay] ?: 10
        val milestoneXp = MILESTONES[state.currentStreak] ?: 0
        val isMilestone = milestoneXp > 0
        val freezeFromCycle = if (state.cycleDay == 7) 1 else 0
        val freezeFromMilestone = if (state.currentStreak in FREEZE_MILESTONES) 1 else 0

        return StreakReward(
            xpBonus = cycleXp + milestoneXp,
            freezeTokens = freezeFromCycle + freezeFromMilestone,
            isMilestone = isMilestone,
            milestoneDay = if (isMilestone) state.currentStreak else 0,
            cycleDay = state.cycleDay,
            streakMultiplier = streakMultiplier(state.currentStreak)
        )
    }
}
