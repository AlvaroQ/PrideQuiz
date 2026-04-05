package com.quiz.pride.managers

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.quiz.pride.BuildConfig

class AnalyticsManager(context: Context) {
    private val firebase = FirebaseAnalytics.getInstance(context.applicationContext)

    @Volatile
    var uid: String = ""

    fun analyticsScreenViewed(screenTitle: String) {
        logEvent(Event("screen_viewed")
            .with("uid", uid)
            .with("screen_title", screenTitle)
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_name", BuildConfig.APPLICATION_ID))
    }

    fun analyticsGameFinished(points: String) {
        logEvent(Event("game_finished")
            .with("uid", uid)
            .with("points", points)
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_name", BuildConfig.APPLICATION_ID))
    }

    fun analyticsClicked(btnDescription: String) {
        logEvent(Event("clicked")
            .with("uid", uid)
            .with("component", btnDescription)
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_name", BuildConfig.APPLICATION_ID))
    }

    fun analyticsAppRecommendedOpen(appName: String) {
        logEvent(Event("app_recommended_open")
            .with("uid", uid)
            .with("app_name", appName)
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_id", BuildConfig.APPLICATION_ID))
    }

    fun analyticsGameModeSelected(gameMode: String) {
        logEvent(Event("game_mode_selected")
            .with("uid", uid)
            .with("game_mode", gameMode)
            .with("app_version", BuildConfig.VERSION_NAME))
    }

    fun analyticsOnboardingStep(step: Int, action: String) {
        logEvent(Event("onboarding_step")
            .with("uid", uid)
            .with("step", step.toString())
            .with("action", action)
            .with("app_version", BuildConfig.VERSION_NAME))
    }

    fun analyticsExtraLife(action: String) {
        logEvent(Event("extra_life")
            .with("uid", uid)
            .with("action", action)
            .with("app_version", BuildConfig.VERSION_NAME))
    }

    fun analyticsGameExit(stage: Int, points: Int) {
        logEvent(Event("game_exit")
            .with("uid", uid)
            .with("stage", stage.toString())
            .with("points", points.toString())
            .with("app_version", BuildConfig.VERSION_NAME))
    }

    fun analyticsScoreSaved(type: String, saved: Boolean) {
        logEvent(Event("score_saved")
            .with("uid", uid)
            .with("type", type)
            .with("saved", saved.toString())
            .with("app_version", BuildConfig.VERSION_NAME))
    }

    fun analyticsPurchaseIntent(product: String) {
        logEvent(Event("purchase_intent")
            .with("uid", uid)
            .with("product", product)
            .with("app_version", BuildConfig.VERSION_NAME))
    }

    fun analyticsPurchaseResult(result: String) {
        logEvent(Event("purchase_result")
            .with("uid", uid)
            .with("result", result)
            .with("app_version", BuildConfig.VERSION_NAME))
    }

    fun analyticsAdEvent(adType: String, action: String) {
        logEvent(Event("ad_event")
            .with("uid", uid)
            .with("ad_type", adType)
            .with("action", action)
            .with("app_version", BuildConfig.VERSION_NAME))
    }

    fun analyticsSettingChanged(setting: String, value: String) {
        logEvent(Event("setting_changed")
            .with("uid", uid)
            .with("setting", setting)
            .with("value", value)
            .with("app_version", BuildConfig.VERSION_NAME))
    }

    fun analyticsRankingTabSelected(tab: String) {
        logEvent(Event("ranking_tab_selected")
            .with("uid", uid)
            .with("tab", tab)
            .with("app_version", BuildConfig.VERSION_NAME))
    }

    fun analyticsLevelUp(oldLevel: Int, newLevel: Int, totalXp: Long) {
        logEvent(Event("level_up")
            .with("uid", uid)
            .with("old_level", oldLevel.toString())
            .with("new_level", newLevel.toString())
            .with("total_xp", totalXp.toString())
            .with("app_version", BuildConfig.VERSION_NAME))
    }

    fun setUserProperties(level: Int, totalGames: Int) {
        firebase.setUserProperty("player_level", level.toString())
        firebase.setUserProperty("total_games", totalGames.toString())
    }

    private fun logEvent(event: Event) {
        firebase.logEvent(event.eventName, event.bundle)
    }

    private class Event(val eventName: String) {
        val bundle = Bundle()
        fun with(key: String, value: String): Event {
            bundle.putString(key, value)
            return this
        }
    }

    companion object {
        // screen
        const val SCREEN_SELECT = "screen_select"
        const val SCREEN_GAME = "screen_game"
        const val SCREEN_RESULT = "screen_result"
        const val SCREEN_RANKING = "screen_ranking"
        const val SCREEN_INFO = "screen_info"
        const val SCREEN_SELECT_GAME = "screen_select_game"
        const val SCREEN_MORE_APPS = "screen_more_apps"
        const val SCREEN_DIALOG_SAVE_SCORE = "screen_dialog_save_score"
        const val SCREEN_SETTINGS = "screen_settings"

        // screen - nuevas pantallas
        const val SCREEN_PROFILE = "screen_profile"
        const val SCREEN_ONBOARDING = "screen_onboarding"
        const val SCREEN_XP_LEADERBOARD = "screen_xp_leaderboard"

        // clicked
        const val BTN_PLAY_AGAIN = "btn_play_again"
        const val BTN_RATE = "btn_rate"
        const val BTN_RANKING = "btn_ranking"
        const val BTN_SHARE = "btn_share"
        const val BTN_REMOVE_ADS = "btn_remove_ads"
    }
}
