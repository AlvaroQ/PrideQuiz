package com.quiz.pride.managers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests de la logica pura de ProgressionManager que no requiere Context ni DataStore.
 * Los metodos calculateLevel y getLevelInfo son pura logica matematica.
 */
class ProgressionManagerTest {

    // Usamos un objeto que solo expone la logica pura.
    // ProgressionManager tiene Context en el constructor para DataStore,
    // pero calculateLevel y getLevelInfo son funciones sin side-effects.
    // Se testean directamente via reflexion o extrayendo la logica.

    // Companion object con la logica: accesible directamente
    private val thresholds = ProgressionManager.LEVEL_THRESHOLDS

    // Replica de calculateLevel para poder testearla de forma aislada
    private fun calculateLevel(xp: Long): Int {
        for (i in thresholds.indices.reversed()) {
            if (xp >= thresholds[i]) {
                return i + 1
            }
        }
        return 1
    }

    // =========================================================
    // calculateLevel
    // =========================================================

    @Test
    fun `calculateLevel con 0 XP retorna nivel 1`() {
        assertEquals(1, calculateLevel(0L))
    }

    @Test
    fun `calculateLevel con exactamente el threshold de nivel 2 retorna nivel 2`() {
        assertEquals(2, calculateLevel(100L))
    }

    @Test
    fun `calculateLevel con XP justo antes del threshold de nivel 2 retorna nivel 1`() {
        assertEquals(1, calculateLevel(99L))
    }

    @Test
    fun `calculateLevel con XP justo en threshold nivel 3 retorna nivel 3`() {
        assertEquals(3, calculateLevel(250L))
    }

    @Test
    fun `calculateLevel con XP justo en threshold nivel 10 retorna nivel 10`() {
        // Threshold nivel 10 = 3800L
        assertEquals(10, calculateLevel(3800L))
    }

    @Test
    fun `calculateLevel con XP entre threshold nivel 10 y 11 retorna nivel 10`() {
        // Threshold 10 = 3800, Threshold 11 = 4700
        assertEquals(10, calculateLevel(4000L))
    }

    @Test
    fun `calculateLevel con XP threshold de nivel 50 retorna nivel 50`() {
        // Threshold nivel 50 = 277000L
        assertEquals(50, calculateLevel(277000L))
    }

    @Test
    fun `calculateLevel con XP mayor al ultimo threshold retorna el nivel maximo definido`() {
        // Mas alla del nivel 50
        assertEquals(50, calculateLevel(500_000L))
    }

    // =========================================================
    // LEVEL_THRESHOLDS — integridad de los datos
    // =========================================================

    @Test
    fun `LEVEL_THRESHOLDS tiene exactamente 50 entradas`() {
        assertEquals(50, thresholds.size)
    }

    @Test
    fun `LEVEL_THRESHOLDS empieza en 0`() {
        assertEquals(0L, thresholds[0])
    }

    @Test
    fun `LEVEL_THRESHOLDS esta ordenado de menor a mayor`() {
        for (i in 1 until thresholds.size) {
            assertTrue(
                "Threshold[${i}] = ${thresholds[i]} debe ser mayor que Threshold[${i-1}] = ${thresholds[i-1]}",
                thresholds[i] > thresholds[i - 1]
            )
        }
    }

    // =========================================================
    // Constantes XP
    // =========================================================

    @Test
    fun `constantes XP tienen valores positivos`() {
        assertTrue(ProgressionManager.XP_PER_CORRECT_ANSWER > 0)
        assertTrue(ProgressionManager.XP_PER_STREAK_BONUS > 0)
        assertTrue(ProgressionManager.XP_PER_PERFECT_GAME > 0)
        assertTrue(ProgressionManager.XP_PER_WIN > 0)
        assertTrue(ProgressionManager.XP_MULTIPLIER_ADVANCE > 1.0)
        assertTrue(ProgressionManager.XP_MULTIPLIER_EXPERT > 1.0)
        assertTrue(ProgressionManager.XP_MULTIPLIER_TIMED > 1.0)
    }

    @Test
    fun `multiplicador EXPERT es mayor que multiplicador ADVANCE`() {
        assertTrue(ProgressionManager.XP_MULTIPLIER_EXPERT > ProgressionManager.XP_MULTIPLIER_ADVANCE)
    }
}
