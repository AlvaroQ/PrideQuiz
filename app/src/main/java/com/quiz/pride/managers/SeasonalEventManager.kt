package com.quiz.pride.managers

import com.quiz.domain.seasonal.SeasonalEvent
import com.quiz.domain.seasonal.SeasonalEventCatalog
import java.time.LocalDate

/**
 * Wrapper del catalogo seasonal con inyeccion de reloj para testear.
 *
 * No persiste nada: la activacion de eventos depende solo de la fecha del
 * dispositivo. Consumirse desde ViewModels para decidir si mostrar banner,
 * aplicar multiplicador XP adicional, etc.
 *
 * El multiplicador del evento es *aditivo con el de racha*:
 *   xpFinal = xpBase * streakMultiplier * eventMultiplier
 * (hasta 2.0x streak + 1.5x pride = 3.0x total en junio con racha de 90+ dias).
 */
class SeasonalEventManager(
    private val clock: () -> LocalDate = { LocalDate.now() }
) {
    /** Evento activo hoy, o null si no hay ninguno. */
    fun activeEvent(): SeasonalEvent? = SeasonalEventCatalog.activeEvent(clock())

    /** Multiplicador XP del evento activo, 1.0 si no hay evento. */
    fun xpMultiplier(): Float = activeEvent()?.xpMultiplier ?: 1.0f

    /** Dias restantes del evento activo, 0 si no hay. */
    fun daysRemaining(): Int = activeEvent()?.daysRemaining(clock()) ?: 0

    /** true si hoy es el mes del Orgullo (junio). */
    fun isPrideMonth(): Boolean = activeEvent()?.id?.startsWith("pride_month_") == true
}
