package com.quiz.pride.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.quiz.pride.managers.AnalyticsManager

/**
 * Composable sin UI que mide cuanto tiempo permanece el usuario en una pantalla.
 * Al salir de la composicion, reporta la duracion via [AnalyticsManager.analyticsScreenTimeSpent].
 *
 * Uso:
 * ```kotlin
 * TrackScreenTime(AnalyticsManager.SCREEN_INFO, analyticsManager)
 * ```
 */
@Composable
fun TrackScreenTime(screenName: String, analyticsManager: AnalyticsManager) {
    val startTime = remember { System.currentTimeMillis() }
    DisposableEffect(screenName) {
        onDispose {
            analyticsManager.analyticsScreenTimeSpent(screenName, System.currentTimeMillis() - startTime)
        }
    }
}
