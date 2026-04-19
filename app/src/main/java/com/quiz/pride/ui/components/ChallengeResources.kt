package com.quiz.pride.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.quiz.domain.challenge.DailyChallenge
import com.quiz.pride.R
import com.quiz.pride.managers.ChallengeAppConfig.DescriptionKeys

/**
 * Resuelve el texto localizado de un desafio a partir de su `descriptionKey` y `targetValue`.
 *
 * El dominio (DailyChallenge) almacena solo una clave logica para respetar Clean Architecture
 * (domain no depende de recursos Android). La UI resuelve aqui con plurales y el target real
 * escalado por nivel del jugador, de modo que el texto siempre coincide con el objetivo visible.
 */
@Composable
fun DailyChallenge.resolveDescription(): String = when (descriptionKey) {
    DescriptionKeys.GAMES_PLAYED ->
        pluralStringResource(R.plurals.challenge_desc_games_played, targetValue, targetValue)
    DescriptionKeys.GAMES_PLAYED_WEEKLY ->
        pluralStringResource(R.plurals.challenge_desc_games_played_weekly, targetValue, targetValue)
    DescriptionKeys.TOTAL_CORRECT ->
        pluralStringResource(R.plurals.challenge_desc_total_correct, targetValue, targetValue)
    DescriptionKeys.TOTAL_CORRECT_WEEKLY ->
        pluralStringResource(R.plurals.challenge_desc_total_correct_weekly, targetValue, targetValue)
    DescriptionKeys.SCORE_MINIMUM ->
        pluralStringResource(R.plurals.challenge_desc_score_minimum, targetValue, targetValue)
    DescriptionKeys.CORRECT_STREAK ->
        pluralStringResource(R.plurals.challenge_desc_correct_streak, targetValue, targetValue)
    DescriptionKeys.STREAK_IN_GAME ->
        pluralStringResource(R.plurals.challenge_desc_streak_in_game, targetValue, targetValue)
    DescriptionKeys.CUMULATIVE_SCORE ->
        pluralStringResource(R.plurals.challenge_desc_cumulative_score, targetValue, targetValue)
    DescriptionKeys.PERFECT_GAME_WEEKLY ->
        pluralStringResource(R.plurals.challenge_desc_perfect_game_weekly, targetValue, targetValue)
    DescriptionKeys.PERFECT_GAME -> stringResource(R.string.challenge_desc_perfect_game)
    DescriptionKeys.WIN_GAME -> stringResource(R.string.challenge_desc_win_game)
    DescriptionKeys.PLAY_MODE_ADVANCE -> stringResource(R.string.challenge_desc_play_mode_advance)
    else -> ""
}
