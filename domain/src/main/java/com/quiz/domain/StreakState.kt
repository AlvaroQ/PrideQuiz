package com.quiz.domain

/**
 * Estado completo de la racha diaria del jugador.
 *
 * Las fechas se almacenan como strings "yyyy-MM-dd" para evitar dependencias
 * de java.time en el modulo domain (que debe ser Kotlin puro).
 */
data class StreakState(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastPlayedDate: String = "",       // "yyyy-MM-dd"
    val freezeTokens: Int = 0,
    val cycleDay: Int = 1,                 // 1-7, se resetea al completar el ciclo
    val totalDaysPlayed: Int = 0,
    val streakStartDate: String = "",      // fecha en que comenzo la racha actual
    val lastFreezeUsedDate: String = ""    // previene uso doble de freeze en el mismo dia
)
