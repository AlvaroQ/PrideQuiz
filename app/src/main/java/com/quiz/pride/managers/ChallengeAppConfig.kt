package com.quiz.pride.managers

import com.quiz.domain.challenge.ChallengeType

/**
 * Configuracion especifica de PrideQuiz para el sistema de desafios diarios.
 * Centraliza los templates de desafios por dificultad y los parametros de la app.
 * Separado del manager para facilitar ajuste de balanceo sin tocar la logica.
 */
object ChallengeAppConfig {

    /** Modos de juego disponibles en PrideQuiz */
    val availableGameModes = listOf("NORMAL", "ADVANCE", "TIMED")

    /** Total de preguntas posibles en una partida */
    const val maxQuestionsPerGame = 163

    /**
     * Template de un desafio: tipo, objetivo base y clave logica de descripcion.
     * El objetivo base se escala por nivel del jugador (1.0x nivel 1, hasta 1.8x nivel 50).
     * `descriptionKey` es una clave neutral que UI resuelve a traves de pluralStringResource /
     * stringResource para soportar multi-idioma y reflejar siempre el targetValue escalado real.
     */
    data class ChallengeTemplate(
        val type: ChallengeType,
        val baseTarget: Int,
        val descriptionKey: String,
        val extraParam: String = ""
    )

    /** Claves de descripcion usadas por los templates. Mapeadas a strings en ChallengeResources. */
    object DescriptionKeys {
        const val GAMES_PLAYED = "games_played"
        const val GAMES_PLAYED_WEEKLY = "games_played_weekly"
        const val TOTAL_CORRECT = "total_correct"
        const val TOTAL_CORRECT_WEEKLY = "total_correct_weekly"
        const val SCORE_MINIMUM = "score_minimum"
        const val CORRECT_STREAK = "correct_streak"
        const val STREAK_IN_GAME = "streak_in_game"
        const val CUMULATIVE_SCORE = "cumulative_score"
        const val PERFECT_GAME = "perfect_game"
        const val PERFECT_GAME_WEEKLY = "perfect_game_weekly"
        const val WIN_GAME = "win_game"
        const val PLAY_MODE_ADVANCE = "play_mode_advance"
    }

    val easyTemplates = listOf(
        ChallengeTemplate(ChallengeType.GAMES_PLAYED, 1, DescriptionKeys.GAMES_PLAYED),
        ChallengeTemplate(ChallengeType.TOTAL_CORRECT, 5, DescriptionKeys.TOTAL_CORRECT),
        ChallengeTemplate(ChallengeType.SCORE_MINIMUM, 3, DescriptionKeys.SCORE_MINIMUM),
        ChallengeTemplate(ChallengeType.CORRECT_STREAK, 3, DescriptionKeys.CORRECT_STREAK)
    )

    val mediumTemplates = listOf(
        ChallengeTemplate(ChallengeType.GAMES_PLAYED, 2, DescriptionKeys.GAMES_PLAYED),
        ChallengeTemplate(ChallengeType.TOTAL_CORRECT, 15, DescriptionKeys.TOTAL_CORRECT),
        ChallengeTemplate(ChallengeType.SCORE_MINIMUM, 8, DescriptionKeys.SCORE_MINIMUM),
        ChallengeTemplate(ChallengeType.STREAK_IN_GAME, 5, DescriptionKeys.STREAK_IN_GAME),
        ChallengeTemplate(ChallengeType.CUMULATIVE_SCORE, 12, DescriptionKeys.CUMULATIVE_SCORE),
        ChallengeTemplate(ChallengeType.PLAY_MODE, 1, DescriptionKeys.PLAY_MODE_ADVANCE, "ADVANCE")
    )

    val hardTemplates = listOf(
        ChallengeTemplate(ChallengeType.PERFECT_GAME, 1, DescriptionKeys.PERFECT_GAME),
        ChallengeTemplate(ChallengeType.GAMES_PLAYED, 3, DescriptionKeys.GAMES_PLAYED),
        ChallengeTemplate(ChallengeType.TOTAL_CORRECT, 30, DescriptionKeys.TOTAL_CORRECT),
        ChallengeTemplate(ChallengeType.WIN_GAME, 1, DescriptionKeys.WIN_GAME),
        ChallengeTemplate(ChallengeType.STREAK_IN_GAME, 10, DescriptionKeys.STREAK_IN_GAME),
        ChallengeTemplate(ChallengeType.SCORE_MINIMUM, 15, DescriptionKeys.SCORE_MINIMUM)
    )

    val weeklyTemplates = listOf(
        ChallengeTemplate(ChallengeType.GAMES_PLAYED, 10, DescriptionKeys.GAMES_PLAYED_WEEKLY),
        ChallengeTemplate(ChallengeType.TOTAL_CORRECT, 100, DescriptionKeys.TOTAL_CORRECT_WEEKLY),
        ChallengeTemplate(ChallengeType.PERFECT_GAME, 3, DescriptionKeys.PERFECT_GAME_WEEKLY)
    )
}
