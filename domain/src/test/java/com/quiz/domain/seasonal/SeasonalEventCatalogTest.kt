package com.quiz.domain.seasonal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.Month

/**
 * Tests del catalogo de eventos seasonal.
 *
 * Es dominio puro — cero dependencias Android, cero DataStore. Con un LocalDate
 * inyectado podemos cubrir todos los bordes del evento Pride Month.
 */
class SeasonalEventCatalogTest {

    @Test
    fun `activeEvent retorna Pride Month cuando la fecha es 1 de junio`() {
        val event = SeasonalEventCatalog.activeEvent(LocalDate.of(2026, Month.JUNE, 1))
        assertNotNull(event)
        assertEquals("pride_month_2026", event!!.id)
        assertEquals(1.5f, event.xpMultiplier, 0f)
    }

    @Test
    fun `activeEvent retorna Pride Month cuando la fecha es 30 de junio`() {
        val event = SeasonalEventCatalog.activeEvent(LocalDate.of(2026, Month.JUNE, 30))
        assertNotNull(event)
        assertEquals("pride_month_2026", event!!.id)
    }

    @Test
    fun `activeEvent retorna Pride Month cuando la fecha esta en medio de junio`() {
        val event = SeasonalEventCatalog.activeEvent(LocalDate.of(2026, Month.JUNE, 15))
        assertNotNull(event)
        assertEquals("pride_month_2026", event!!.id)
    }

    @Test
    fun `activeEvent retorna null el 31 de mayo (un dia antes del evento)`() {
        val event = SeasonalEventCatalog.activeEvent(LocalDate.of(2026, Month.MAY, 31))
        assertNull(event)
    }

    @Test
    fun `activeEvent retorna null el 1 de julio (un dia despues del evento)`() {
        val event = SeasonalEventCatalog.activeEvent(LocalDate.of(2026, Month.JULY, 1))
        assertNull(event)
    }

    @Test
    fun `activeEvent retorna null en enero`() {
        val event = SeasonalEventCatalog.activeEvent(LocalDate.of(2026, Month.JANUARY, 15))
        assertNull(event)
    }

    @Test
    fun `prideMonth del año actual tiene id con el año correcto`() {
        val event = SeasonalEventCatalog.prideMonth(2027)
        assertEquals("pride_month_2027", event.id)
        assertEquals(LocalDate.of(2027, Month.JUNE, 1), event.startDate)
        assertEquals(LocalDate.of(2027, Month.JUNE, 30), event.endDate)
    }

    @Test
    fun `isActiveOn es true para startDate y endDate (inclusivo)`() {
        val event = SeasonalEventCatalog.prideMonth(2026)
        assertTrue(event.isActiveOn(event.startDate))
        assertTrue(event.isActiveOn(event.endDate))
    }

    @Test
    fun `isActiveOn es false fuera del rango`() {
        val event = SeasonalEventCatalog.prideMonth(2026)
        assertFalse(event.isActiveOn(event.startDate.minusDays(1)))
        assertFalse(event.isActiveOn(event.endDate.plusDays(1)))
    }

    @Test
    fun `daysRemaining cuenta inclusivo del dia actual`() {
        val event = SeasonalEventCatalog.prideMonth(2026)
        // 1 de junio: quedan 30 dias (1 de junio + 29 dias hasta el 30)
        assertEquals(30, event.daysRemaining(event.startDate))
        // 30 de junio: queda 1 dia (hoy mismo)
        assertEquals(1, event.daysRemaining(event.endDate))
        // 29 de junio: quedan 2 dias
        assertEquals(2, event.daysRemaining(event.endDate.minusDays(1)))
    }

    @Test
    fun `daysRemaining retorna 0 despues del evento`() {
        val event = SeasonalEventCatalog.prideMonth(2026)
        assertEquals(0, event.daysRemaining(event.endDate.plusDays(1)))
    }
}
