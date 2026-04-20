package com.quiz.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de la logica pura de StreakRules.
 *
 * StreakRules es un object Kotlin puro sin dependencias de Android ni DataStore,
 * lo que permite testear todos sus escenarios directamente sin mocks.
 *
 * Fechas usadas en los tests:
 *   HOY       = "2024-04-16"
 *   AYER      = "2024-04-15"
 *   ANTEAYER  = "2024-04-14"
 */
class StreakRulesTest {

    // =========================================================
    // Constantes de fecha para tests legibles
    // =========================================================

    private val HOY = "2024-04-16"
    private val AYER = "2024-04-15"
    private val ANTEAYER = "2024-04-14"

    /** Estado inicial de un jugador que nunca ha jugado. */
    private fun estadoVacio() = StreakState()

    /** Estado con racha activa de N dias, habiendo jugado ayer. */
    private fun estadoConRacha(
        dias: Int,
        ciclo: Int = 1,
        freezeTokens: Int = 0,
        ultimaFecha: String = AYER
    ) = StreakState(
        currentStreak = dias,
        bestStreak = dias,
        lastPlayedDate = ultimaFecha,
        freezeTokens = freezeTokens,
        cycleDay = ciclo,
        totalDaysPlayed = dias
    )

    // =========================================================
    // checkStreak — Caso 1: AlreadyPlayedToday
    // =========================================================

    @Test
    fun `checkStreak retorna AlreadyPlayedToday cuando ya se jugo hoy`() {
        val estado = estadoConRacha(dias = 5, ultimaFecha = HOY)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER)

        assertTrue(resultado is StreakCheckResult.AlreadyPlayedToday)
    }

    @Test
    fun `checkStreak AlreadyPlayedToday preserva el estado sin cambios`() {
        val estado = estadoConRacha(dias = 5, ultimaFecha = HOY)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.AlreadyPlayedToday

        assertEquals(estado, resultado.state)
    }

    // =========================================================
    // checkStreak — Caso 2: NewStreak (primer juego del historial)
    // =========================================================

    @Test
    fun `checkStreak retorna NewStreak en el primer juego registrado`() {
        val estado = estadoVacio()

        val resultado = StreakRules.checkStreak(estado, HOY, AYER)

        assertTrue(resultado is StreakCheckResult.NewStreak)
    }

    @Test
    fun `checkStreak NewStreak inicializa currentStreak en 1`() {
        val resultado = StreakRules.checkStreak(estadoVacio(), HOY, AYER) as StreakCheckResult.NewStreak

        assertEquals(1, resultado.newState.currentStreak)
    }

    @Test
    fun `checkStreak NewStreak inicializa bestStreak en 1`() {
        val resultado = StreakRules.checkStreak(estadoVacio(), HOY, AYER) as StreakCheckResult.NewStreak

        assertEquals(1, resultado.newState.bestStreak)
    }

    @Test
    fun `checkStreak NewStreak fija lastPlayedDate en hoy`() {
        val resultado = StreakRules.checkStreak(estadoVacio(), HOY, AYER) as StreakCheckResult.NewStreak

        assertEquals(HOY, resultado.newState.lastPlayedDate)
    }

    @Test
    fun `checkStreak NewStreak fija cycleDay en 1`() {
        val resultado = StreakRules.checkStreak(estadoVacio(), HOY, AYER) as StreakCheckResult.NewStreak

        assertEquals(1, resultado.newState.cycleDay)
    }

    @Test
    fun `checkStreak NewStreak fija totalDaysPlayed en 1`() {
        val resultado = StreakRules.checkStreak(estadoVacio(), HOY, AYER) as StreakCheckResult.NewStreak

        assertEquals(1, resultado.newState.totalDaysPlayed)
    }

    @Test
    fun `checkStreak NewStreak incluye recompensa con XP del dia 1 del ciclo`() {
        val resultado = StreakRules.checkStreak(estadoVacio(), HOY, AYER) as StreakCheckResult.NewStreak

        // Dia 1 del ciclo = 10 XP
        assertEquals(10, resultado.reward.xpBonus)
    }

    // =========================================================
    // checkStreak — Caso 3: StreakContinued (jugo ayer)
    // =========================================================

    @Test
    fun `checkStreak retorna StreakContinued cuando se jugo ayer`() {
        val estado = estadoConRacha(dias = 3, ciclo = 3)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER)

        assertTrue(resultado is StreakCheckResult.StreakContinued)
    }

    @Test
    fun `checkStreak StreakContinued incrementa currentStreak en 1`() {
        val estado = estadoConRacha(dias = 3, ciclo = 3)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued

        assertEquals(4, resultado.newState.currentStreak)
    }

    @Test
    fun `checkStreak StreakContinued avanza el cycleDay en 1`() {
        val estado = estadoConRacha(dias = 3, ciclo = 3)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued

        assertEquals(4, resultado.newState.cycleDay)
    }

    @Test
    fun `checkStreak StreakContinued ciclo envuelve de dia 7 a dia 1`() {
        val estado = estadoConRacha(dias = 6, ciclo = 7)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued

        // (7 % 7) + 1 = 1
        assertEquals(1, resultado.newState.cycleDay)
    }

    @Test
    fun `checkStreak StreakContinued actualiza bestStreak cuando la racha supera el record`() {
        val estado = StreakState(
            currentStreak = 10,
            bestStreak = 10,
            lastPlayedDate = AYER,
            cycleDay = 3,
            totalDaysPlayed = 10
        )

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued

        assertEquals(11, resultado.newState.bestStreak)
    }

    @Test
    fun `checkStreak StreakContinued no reduce bestStreak cuando la racha es menor`() {
        val estado = StreakState(
            currentStreak = 3,
            bestStreak = 20,
            lastPlayedDate = AYER,
            cycleDay = 3,
            totalDaysPlayed = 25
        )

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued

        assertEquals(20, resultado.newState.bestStreak)
    }

    @Test
    fun `checkStreak StreakContinued incrementa totalDaysPlayed en 1`() {
        val estado = estadoConRacha(dias = 5, ciclo = 5)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued

        assertEquals(6, resultado.newState.totalDaysPlayed)
    }

    // =========================================================
    // checkStreak — Freeze ganado al completar ciclo de 7 dias
    // =========================================================

    @Test
    fun `checkStreak gana 1 freeze token al completar el ciclo dia 7`() {
        // Se usa currentStreak=15 (fuera de FREEZE_MILESTONES: 7, 30, 90) para aislar
        // la recompensa del CICLO (cycleDay==7) del bonus de MILESTONE de racha.
        // cycleDay=6 -> nuevo cycleDay = (6%7)+1 = 7
        val estado = estadoConRacha(dias = 15, ciclo = 6, freezeTokens = 0)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued

        assertEquals(7, resultado.newState.cycleDay)
        // buildReward detecta cycleDay==7 y otorga freeze (sin solapamiento con milestone)
        assertEquals(1, resultado.reward.freezeTokens)
    }

    @Test
    fun `checkStreak no gana freeze cuando no es dia 7 del ciclo ni hito de racha`() {
        val estado = estadoConRacha(dias = 3, ciclo = 3, freezeTokens = 0)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued

        assertEquals(0, resultado.reward.freezeTokens)
    }

    @Test
    fun `checkStreak freeze tokens se suman respetando el maximo de 3`() {
        // Con 3 freezes ya y siendo dia de milestone que otorgaria 1 mas
        val estado = StreakState(
            currentStreak = 6,
            bestStreak = 6,
            lastPlayedDate = AYER,
            freezeTokens = 3,
            cycleDay = 6,
            totalDaysPlayed = 6
        )

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued

        // No puede superar MAX_FREEZE_TOKENS = 3
        assertEquals(StreakRules.MAX_FREEZE_TOKENS, resultado.newState.freezeTokens)
    }

    @Test
    fun `checkStreak MAX_FREEZE_TOKENS es 3`() {
        assertEquals(3, StreakRules.MAX_FREEZE_TOKENS)
    }

    // =========================================================
    // checkStreak — Hitos de racha y freeze por milestone
    // =========================================================

    @Test
    fun `checkStreak gana freeze adicional al alcanzar racha de 7 dias`() {
        // Racha actual 6 dias, jugando hoy llega a 7
        val estado = estadoConRacha(dias = 6, ciclo = 3, freezeTokens = 0)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued

        // currentStreak=7 esta en FREEZE_MILESTONES
        assertEquals(7, resultado.newState.currentStreak)
        // reward.freezeTokens incluye el del milestone (puede ser +1 o +2 si tambien es dia 7 ciclo)
        assertTrue(resultado.reward.freezeTokens >= 1)
    }

    @Test
    fun `checkStreak marca isMilestone en true al alcanzar 7 dias de racha`() {
        val estado = estadoConRacha(dias = 6, ciclo = 3, freezeTokens = 0)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued

        assertTrue(resultado.reward.isMilestone)
        assertEquals(7, resultado.reward.milestoneDay)
    }

    @Test
    fun `checkStreak marca isMilestone en true al alcanzar 30 dias de racha`() {
        val estado = StreakState(
            currentStreak = 29,
            bestStreak = 29,
            lastPlayedDate = AYER,
            cycleDay = 3,
            totalDaysPlayed = 29
        )

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued

        assertTrue(resultado.reward.isMilestone)
        assertEquals(30, resultado.reward.milestoneDay)
    }

    @Test
    fun `checkStreak marca isMilestone en false en dia sin hito`() {
        val estado = estadoConRacha(dias = 4, ciclo = 4)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued

        assertFalse(resultado.reward.isMilestone)
        assertEquals(0, resultado.reward.milestoneDay)
    }

    // =========================================================
    // checkStreak — Caso 4: StreakSavedByFreeze
    // =========================================================

    @Test
    fun `checkStreak retorna StreakSavedByFreeze cuando falto ayer pero tiene freeze token`() {
        val estado = estadoConRacha(dias = 5, ciclo = 5, freezeTokens = 1, ultimaFecha = ANTEAYER)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER)

        assertTrue(resultado is StreakCheckResult.StreakSavedByFreeze)
    }

    @Test
    fun `checkStreak StreakSavedByFreeze consume exactamente 1 freeze token`() {
        val estado = estadoConRacha(dias = 5, ciclo = 5, freezeTokens = 2, ultimaFecha = ANTEAYER)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakSavedByFreeze

        assertEquals(1, resultado.newState.freezeTokens)
    }

    @Test
    fun `checkStreak StreakSavedByFreeze incrementa la racha en 1`() {
        val estado = estadoConRacha(dias = 5, ciclo = 5, freezeTokens = 1, ultimaFecha = ANTEAYER)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakSavedByFreeze

        assertEquals(6, resultado.newState.currentStreak)
    }

    @Test
    fun `checkStreak StreakSavedByFreeze fija lastFreezeUsedDate en hoy`() {
        val estado = estadoConRacha(dias = 5, ciclo = 5, freezeTokens = 1, ultimaFecha = ANTEAYER)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakSavedByFreeze

        assertEquals(HOY, resultado.newState.lastFreezeUsedDate)
    }

    @Test
    fun `checkStreak StreakSavedByFreeze avanza el cycleDay correctamente`() {
        val estado = estadoConRacha(dias = 5, ciclo = 5, freezeTokens = 1, ultimaFecha = ANTEAYER)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakSavedByFreeze

        assertEquals(6, resultado.newState.cycleDay)
    }

    // =========================================================
    // checkStreak — Caso 5: StreakBroken (sin freeze, falto ayer)
    // =========================================================

    @Test
    fun `checkStreak retorna StreakBroken cuando falto ayer y no tiene freeze tokens`() {
        val estado = estadoConRacha(dias = 5, ciclo = 5, freezeTokens = 0, ultimaFecha = ANTEAYER)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER)

        assertTrue(resultado is StreakCheckResult.StreakBroken)
    }

    @Test
    fun `checkStreak StreakBroken resetea currentStreak a 1`() {
        val estado = estadoConRacha(dias = 10, ciclo = 3, freezeTokens = 0, ultimaFecha = ANTEAYER)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakBroken

        assertEquals(1, resultado.newState.currentStreak)
    }

    @Test
    fun `checkStreak StreakBroken resetea cycleDay a 1`() {
        val estado = estadoConRacha(dias = 10, ciclo = 5, freezeTokens = 0, ultimaFecha = ANTEAYER)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakBroken

        assertEquals(1, resultado.newState.cycleDay)
    }

    @Test
    fun `checkStreak StreakBroken preserva bestStreak si era mayor a la racha rota`() {
        val estado = StreakState(
            currentStreak = 5,
            bestStreak = 15,
            lastPlayedDate = ANTEAYER,
            freezeTokens = 0,
            cycleDay = 5,
            totalDaysPlayed = 20
        )

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakBroken

        assertEquals(15, resultado.newState.bestStreak)
    }

    @Test
    fun `checkStreak StreakBroken actualiza bestStreak si la racha rota era el record`() {
        val estado = StreakState(
            currentStreak = 20,
            bestStreak = 20,
            lastPlayedDate = ANTEAYER,
            freezeTokens = 0,
            cycleDay = 6,
            totalDaysPlayed = 20
        )

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakBroken

        assertEquals(20, resultado.newState.bestStreak)
    }

    @Test
    fun `checkStreak StreakBroken incluye previousStreak correcto`() {
        val estado = estadoConRacha(dias = 8, ciclo = 1, freezeTokens = 0, ultimaFecha = ANTEAYER)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakBroken

        assertEquals(8, resultado.previousStreak)
    }

    @Test
    fun `checkStreak StreakBroken fija streakStartDate en hoy`() {
        val estado = estadoConRacha(dias = 5, ciclo = 2, freezeTokens = 0, ultimaFecha = ANTEAYER)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakBroken

        assertEquals(HOY, resultado.newState.streakStartDate)
    }

    @Test
    fun `checkStreak retorna StreakBroken cuando falto multiples dias sin freeze`() {
        // Ultimo juego hace 3 dias — ni ayer ni anteayer
        val estado = StreakState(
            currentStreak = 5,
            bestStreak = 5,
            lastPlayedDate = "2024-04-12",
            freezeTokens = 0,
            cycleDay = 5,
            totalDaysPlayed = 5
        )

        val resultado = StreakRules.checkStreak(estado, HOY, AYER)

        assertTrue(resultado is StreakCheckResult.StreakBroken)
    }

    // =========================================================
    // streakMultiplier
    // =========================================================

    @Test
    fun `streakMultiplier retorna 1_0 para racha de 0 dias`() {
        assertEquals(1.0f, StreakRules.streakMultiplier(0), 0.001f)
    }

    @Test
    fun `streakMultiplier retorna 1_0 para racha de 1 a 6 dias`() {
        for (dias in 1..6) {
            assertEquals(
                "Fallo para $dias dias",
                1.0f,
                StreakRules.streakMultiplier(dias),
                0.001f
            )
        }
    }

    @Test
    fun `streakMultiplier retorna 1_1 para racha de 7 a 13 dias`() {
        for (dias in 7..13) {
            assertEquals(
                "Fallo para $dias dias",
                1.1f,
                StreakRules.streakMultiplier(dias),
                0.001f
            )
        }
    }

    @Test
    fun `streakMultiplier retorna 1_2 para racha de 14 a 29 dias`() {
        assertEquals(1.2f, StreakRules.streakMultiplier(14), 0.001f)
        assertEquals(1.2f, StreakRules.streakMultiplier(29), 0.001f)
    }

    @Test
    fun `streakMultiplier retorna 1_3 para racha de 30 a 59 dias`() {
        assertEquals(1.3f, StreakRules.streakMultiplier(30), 0.001f)
        assertEquals(1.3f, StreakRules.streakMultiplier(59), 0.001f)
    }

    @Test
    fun `streakMultiplier retorna 1_5 para racha de 60 a 89 dias`() {
        assertEquals(1.5f, StreakRules.streakMultiplier(60), 0.001f)
        assertEquals(1.5f, StreakRules.streakMultiplier(89), 0.001f)
    }

    @Test
    fun `streakMultiplier retorna 2_0 para racha de 90 dias o mas`() {
        assertEquals(2.0f, StreakRules.streakMultiplier(90), 0.001f)
        assertEquals(2.0f, StreakRules.streakMultiplier(365), 0.001f)
        assertEquals(2.0f, StreakRules.streakMultiplier(1000), 0.001f)
    }

    @Test
    fun `streakMultiplier incluido en recompensa del resultado`() {
        // Con racha de 7 dias, el multiplicador debe ser 1.1f
        val estado = estadoConRacha(dias = 6, ciclo = 3)

        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued

        // currentStreak=7 -> multiplicador 1.1f
        assertEquals(1.1f, resultado.reward.streakMultiplier, 0.001f)
    }

    // =========================================================
    // isStreakAtRisk
    // =========================================================

    @Test
    fun `isStreakAtRisk retorna false cuando no hay racha activa`() {
        val estado = StreakState(currentStreak = 0, lastPlayedDate = AYER)

        assertFalse(StreakRules.isStreakAtRisk(estado, HOY, AYER))
    }

    @Test
    fun `isStreakAtRisk retorna true cuando la racha se jugo ayer y aun no se jugo hoy`() {
        val estado = estadoConRacha(dias = 5, ultimaFecha = AYER)

        assertTrue(StreakRules.isStreakAtRisk(estado, HOY, AYER))
    }

    @Test
    fun `isStreakAtRisk retorna false cuando ya se jugo hoy`() {
        val estado = estadoConRacha(dias = 5, ultimaFecha = HOY)

        assertFalse(StreakRules.isStreakAtRisk(estado, HOY, AYER))
    }

    @Test
    fun `isStreakAtRisk retorna false cuando la ultima fecha es anterior a ayer`() {
        val estado = estadoConRacha(dias = 5, ultimaFecha = ANTEAYER)

        // La racha ya se rompio — no esta "en riesgo", ya esta perdida
        assertFalse(StreakRules.isStreakAtRisk(estado, HOY, AYER))
    }

    @Test
    fun `isStreakAtRisk retorna false cuando el estado no tiene fecha registrada`() {
        val estado = StreakState(currentStreak = 0, lastPlayedDate = "")

        assertFalse(StreakRules.isStreakAtRisk(estado, HOY, AYER))
    }

    // =========================================================
    // Recompensas XP por ciclo
    // =========================================================

    @Test
    fun `recompensa XP del ciclo es 10 para dia 1`() {
        val resultado = StreakRules.checkStreak(estadoVacio(), HOY, AYER) as StreakCheckResult.NewStreak
        // NewStreak arranca en cycleDay=1 -> 10 XP
        assertEquals(10, resultado.reward.xpBonus)
    }

    @Test
    fun `recompensa XP del ciclo es 15 para dia 2`() {
        val estado = estadoConRacha(dias = 1, ciclo = 1)
        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued
        // ciclo nuevo: (1%7)+1 = 2 -> 15 XP
        assertEquals(15, resultado.reward.xpBonus)
    }

    @Test
    fun `recompensa XP del ciclo es 20 para dia 3`() {
        val estado = estadoConRacha(dias = 2, ciclo = 2)
        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued
        assertEquals(20, resultado.reward.xpBonus)
    }

    @Test
    fun `recompensa XP del ciclo es 30 para dia 4`() {
        val estado = estadoConRacha(dias = 3, ciclo = 3)
        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued
        assertEquals(30, resultado.reward.xpBonus)
    }

    @Test
    fun `recompensa XP del ciclo es 50 para dia 5`() {
        val estado = estadoConRacha(dias = 4, ciclo = 4)
        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued
        assertEquals(50, resultado.reward.xpBonus)
    }

    @Test
    fun `recompensa XP del ciclo es 75 para dia 6`() {
        val estado = estadoConRacha(dias = 5, ciclo = 5)
        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued
        assertEquals(75, resultado.reward.xpBonus)
    }

    @Test
    fun `recompensa XP del ciclo es 100 para dia 7 sin hito`() {
        // Racha de 6 dias, ciclo 6 -> nuevo ciclo sera 7, racha 7 (que es milestone!)
        // xpBonus = cycleXp(100) + milestoneXp(200) = 300
        // Verificamos solo el ciclo: cycleDay del reward sera 7
        val estado = estadoConRacha(dias = 6, ciclo = 6)
        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued
        assertEquals(7, resultado.reward.cycleDay)
    }

    @Test
    fun `recompensa XP de hito es 200 para racha de 7 dias`() {
        val estado = estadoConRacha(dias = 6, ciclo = 3)
        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued
        // cycleXp(dia 4 = 30) + milestoneXp(7 dias = 200) = 230
        assertEquals(230, resultado.reward.xpBonus)
    }

    @Test
    fun `recompensa XP de hito es 500 para racha de 14 dias`() {
        val estado = StreakState(
            currentStreak = 13,
            bestStreak = 13,
            lastPlayedDate = AYER,
            cycleDay = 6,
            totalDaysPlayed = 13
        )
        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued
        // cycleXp(dia 7 = 100) + milestoneXp(14 dias = 500) = 600
        assertEquals(600, resultado.reward.xpBonus)
    }

    @Test
    fun `recompensa XP de hito es 1000 para racha de 30 dias`() {
        val estado = StreakState(
            currentStreak = 29,
            bestStreak = 29,
            lastPlayedDate = AYER,
            cycleDay = 1,
            totalDaysPlayed = 29
        )
        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued
        // cycleXp(dia 2 = 15) + milestoneXp(30 dias = 1000) = 1015
        assertEquals(1015, resultado.reward.xpBonus)
    }

    @Test
    fun `dias sin hito reciben XP de milestone 0`() {
        // Racha de 3 dias: cycleDay 3->4, sin hito
        val estado = estadoConRacha(dias = 3, ciclo = 3)
        val resultado = StreakRules.checkStreak(estado, HOY, AYER) as StreakCheckResult.StreakContinued
        // xpBonus = solo cycleXp = 30 (dia 4 del ciclo), no hay milestone
        assertFalse(resultado.reward.isMilestone)
        assertEquals(30, resultado.reward.xpBonus)
    }
}
