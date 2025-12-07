package com.quiz.pride.ui.select

import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AnalyticsManager

class SelectGameViewModel : ComposeViewModel() {

    init {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT_GAME)
    }
}