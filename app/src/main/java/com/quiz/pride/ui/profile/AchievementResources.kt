package com.quiz.pride.ui.profile

import com.quiz.domain.Achievement
import com.quiz.pride.R

/**
 * Provee los IDs de recursos de strings para cada Achievement.
 * Esta capa de mapeo vive en app/ para mantener el modulo domain libre de dependencias Android.
 */
val Achievement.titleRes: Int
    get() = when (this) {
        Achievement.FIRST_GAME -> R.string.achievement_first_game_title
        Achievement.TEN_GAMES -> R.string.achievement_ten_games_title
        Achievement.FIFTY_GAMES -> R.string.achievement_fifty_games_title
        Achievement.HUNDRED_GAMES -> R.string.achievement_hundred_games_title
        Achievement.FIRST_PERFECT -> R.string.achievement_first_perfect_title
        Achievement.FIVE_PERFECT -> R.string.achievement_five_perfect_title
        Achievement.STREAK_5 -> R.string.achievement_streak_5_title
        Achievement.STREAK_10 -> R.string.achievement_streak_10_title
        Achievement.STREAK_15 -> R.string.achievement_streak_15_title
        Achievement.STREAK_20 -> R.string.achievement_streak_20_title
        Achievement.LEVEL_10 -> R.string.achievement_level_10_title
        Achievement.LEVEL_25 -> R.string.achievement_level_25_title
        Achievement.LEVEL_50 -> R.string.achievement_level_50_title
        Achievement.SPEED_DEMON -> R.string.achievement_speed_demon_title
        Achievement.DEDICATED -> R.string.achievement_dedicated_title
        Achievement.ACCURACY_80 -> R.string.achievement_accuracy_80_title
        Achievement.ACCURACY_90 -> R.string.achievement_accuracy_90_title
        Achievement.STREAK_DAILY_7 -> R.string.achievement_streak_daily_7_title
        Achievement.STREAK_DAILY_14 -> R.string.achievement_streak_daily_14_title
        Achievement.STREAK_DAILY_30 -> R.string.achievement_streak_daily_30_title
        Achievement.STREAK_DAILY_60 -> R.string.achievement_streak_daily_60_title
        Achievement.STREAK_DAILY_90 -> R.string.achievement_streak_daily_90_title
        Achievement.STREAK_DAILY_365 -> R.string.achievement_streak_daily_365_title
    }

val Achievement.descriptionRes: Int
    get() = when (this) {
        Achievement.FIRST_GAME -> R.string.achievement_first_game_desc
        Achievement.TEN_GAMES -> R.string.achievement_ten_games_desc
        Achievement.FIFTY_GAMES -> R.string.achievement_fifty_games_desc
        Achievement.HUNDRED_GAMES -> R.string.achievement_hundred_games_desc
        Achievement.FIRST_PERFECT -> R.string.achievement_first_perfect_desc
        Achievement.FIVE_PERFECT -> R.string.achievement_five_perfect_desc
        Achievement.STREAK_5 -> R.string.achievement_streak_5_desc
        Achievement.STREAK_10 -> R.string.achievement_streak_10_desc
        Achievement.STREAK_15 -> R.string.achievement_streak_15_desc
        Achievement.STREAK_20 -> R.string.achievement_streak_20_desc
        Achievement.LEVEL_10 -> R.string.achievement_level_10_desc
        Achievement.LEVEL_25 -> R.string.achievement_level_25_desc
        Achievement.LEVEL_50 -> R.string.achievement_level_50_desc
        Achievement.SPEED_DEMON -> R.string.achievement_speed_demon_desc
        Achievement.DEDICATED -> R.string.achievement_dedicated_desc
        Achievement.ACCURACY_80 -> R.string.achievement_accuracy_80_desc
        Achievement.ACCURACY_90 -> R.string.achievement_accuracy_90_desc
        Achievement.STREAK_DAILY_7 -> R.string.achievement_streak_daily_7_desc
        Achievement.STREAK_DAILY_14 -> R.string.achievement_streak_daily_14_desc
        Achievement.STREAK_DAILY_30 -> R.string.achievement_streak_daily_30_desc
        Achievement.STREAK_DAILY_60 -> R.string.achievement_streak_daily_60_desc
        Achievement.STREAK_DAILY_90 -> R.string.achievement_streak_daily_90_desc
        Achievement.STREAK_DAILY_365 -> R.string.achievement_streak_daily_365_desc
    }
