package com.quiz.pride.ui.select

import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.AnalyticsManager

class SelectGameViewModel(
    private val analyticsManager: AnalyticsManager
) : ComposeViewModel() {

    init {
        analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT_GAME)
    }
}
