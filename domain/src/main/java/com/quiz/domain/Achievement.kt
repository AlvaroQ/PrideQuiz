package com.quiz.domain

/**
 * Logros desbloqueables del juego.
 *
 * Los textos de titulo y descripcion son responsabilidad de la capa de presentacion.
 * Ver AchievementResources en el modulo app para la resolucion de strings.
 */
enum class Achievement(
    val id: String,
    val xpReward: Int,
    val icon: String
) {
    // Hitos de partidas
    FIRST_GAME("first_game", 50, "\uD83C\uDFAE"),
    TEN_GAMES("ten_games", 100, "\uD83C\uDFAF"),
    FIFTY_GAMES("fifty_games", 250, "\uD83C\uDFC5"),
    HUNDRED_GAMES("hundred_games", 500, "\uD83C\uDF96\uFE0F"),

    // Partidas perfectas
    FIRST_PERFECT("first_perfect", 100, "\u2728"),
    FIVE_PERFECT("five_perfect", 300, "\uD83D\uDC8E"),

    // Rachas
    STREAK_5("streak_5", 50, "\uD83D\uDD25"),
    STREAK_10("streak_10", 150, "\u26A1"),
    STREAK_15("streak_15", 300, "\uD83C\uDF1F"),
    STREAK_20("streak_20", 500, "\uD83D\uDC51"),

    // Niveles
    LEVEL_10("level_10", 200, "\u2B50"),
    LEVEL_25("level_25", 500, "\uD83C\uDF08"),
    LEVEL_50("level_50", 1000, "\uD83C\uDFC6"),

    // Especiales
    SPEED_DEMON("speed_demon", 300, "\u23F1\uFE0F"),
    DEDICATED("dedicated", 200, "\u23F0"),
    ACCURACY_80("accuracy_80", 250, "\uD83C\uDFAF"),
    ACCURACY_90("accuracy_90", 500, "\uD83D\uDCAF")
}
