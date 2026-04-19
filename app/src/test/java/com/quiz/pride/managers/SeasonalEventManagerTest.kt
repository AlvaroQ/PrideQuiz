package com.quiz.pride.managers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.Month

/**
 * Tests de SeasonalEventManager.
 *
 * El manager es un wrapper delgado de SeasonalEventCatalog con un clock inyectable,
 * lo que lo hace testeable sin DataStore ni Context.
 */
class SeasonalEventManagerTest {

    @Test
    fun `isPrideMonth es true en junio`() {
        val manager = SeasonalEventManager(clock = { LocalDate.of(2026, Month.JUNE, 15) })
        assertTrue(manager.isPrideMonth())
    }

    @Test
    fun `isPrideMonth es false fuera de junio`() {
        val manager = SeasonalEventManager(clock = { LocalDate.of(2026, Month.MAY, 31) })
        assertFalse(manager.isPrideMonth())
    }

    @Test
    fun `xpMultiplier es 1_5 en junio`() {
        val manager = SeasonalEventManager(clock = { LocalDate.of(2026, Month.JUNE, 1) })
        assertEquals(1.5f, manager.xpMultiplier(), 0.0001f)
    }

    @Test
    fun `xpMultiplier es 1_0 fuera de junio`() {
        val manager = SeasonalEventManager(clock = { LocalDate.of(2026, Month.DECEMBER, 25) })
        assertEquals(1.0f, manager.xpMultiplier(), 0.0001f)
    }

    @Test
    fun `daysRemaining el 1 de junio son 30 dias`() {
        val manager = SeasonalEventManager(clock = { LocalDate.of(2026, Month.JUNE, 1) })
        assertEquals(30, manager.daysRemaining())
    }

    @Test
    fun `daysRemaining es 0 fuera del evento`() {
        val manager = SeasonalEventManager(clock = { LocalDate.of(2026, Month.JULY, 1) })
        assertEquals(0, manager.daysRemaining())
    }

    @Test
    fun `activeEvent retorna el Pride Month del año del clock`() {
        val manager = SeasonalEventManager(clock = { LocalDate.of(2027, Month.JUNE, 10) })
        val event = manager.activeEvent()
        assertEquals("pride_month_2027", event?.id)
    }
}
