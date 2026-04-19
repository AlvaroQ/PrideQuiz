package com.quiz.domain.seasonal

import java.time.LocalDate
import java.time.Month

/**
 * Representa un evento temporal / seasonal.
 *
 * Modelo de dominio puro: sin dependencias Android. Puede testearse con
 * cualquier LocalDate inyectada (util para clock fake en tests).
 */
data class SeasonalEvent(
    val id: String,
    val titleKey: String,
    val descriptionKey: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val xpMultiplier: Float,
    val accentHex: String
) {
    fun isActiveOn(date: LocalDate): Boolean =
        !date.isBefore(startDate) && !date.isAfter(endDate)

    fun daysRemaining(from: LocalDate): Int =
        if (from.isAfter(endDate)) 0
        else java.time.temporal.ChronoUnit.DAYS.between(from, endDate).toInt() + 1
}

/**
 * Catalogo de eventos seasonal conocidos.
 *
 * - PRIDE_MONTH: junio completo. Multiplicador XP 1.5x, banner arcoiris.
 */
object SeasonalEventCatalog {

    fun prideMonth(year: Int): SeasonalEvent = SeasonalEvent(
        id = "pride_month_$year",
        titleKey = "seasonal_pride_title",
        descriptionKey = "seasonal_pride_description",
        startDate = LocalDate.of(year, Month.JUNE, 1),
        endDate = LocalDate.of(year, Month.JUNE, 30),
        xpMultiplier = 1.5f,
        accentHex = "#EC4899"
    )

    /** Retorna el evento activo hoy, si hay alguno. Null si no hay ninguno. */
    fun activeEvent(today: LocalDate = LocalDate.now()): SeasonalEvent? {
        val pride = prideMonth(today.year)
        return if (pride.isActiveOn(today)) pride else null
    }
}
