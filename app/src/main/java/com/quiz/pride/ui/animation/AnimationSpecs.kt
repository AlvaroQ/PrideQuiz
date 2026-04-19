package com.quiz.pride.ui.animation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.KeyframesSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color

object AnimationSpecs {

    // --- Durations ---
    const val ANSWER_COLOR_DURATION = 300
    const val ANSWER_HOLD_DURATION = 1300
    const val QUESTION_SLIDE_DURATION = 400
    const val WRONG_SHAKE_DURATION = 400
    const val SCORE_COUNT_DURATION = 600
    const val POINTS_POPUP_DURATION = 800
    const val NAV_TRANSITION_DURATION = 350
    const val NAV_FADE_DURATION = 300
    const val LOADING_BOUNCE_DURATION = 600
    const val LOADING_STAGGER_DELAY = 150
    const val CONFETTI_DURATION = 2000

    // --- Easing ---
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    // --- Springs ---
    val CorrectBounceSpec: SpringSpec<Float> = spring(
        dampingRatio = 0.4f,
        stiffness = 400f
    )

    val XpBarSpringSpec: SpringSpec<Float> = spring(
        dampingRatio = 0.7f,
        stiffness = 150f
    )

    // --- Tweens ---
    val AnswerColorSpec: TweenSpec<Color> = tween(
        durationMillis = ANSWER_COLOR_DURATION,
        easing = FastOutSlowInEasing
    )

    val ScoreCountSpec: TweenSpec<Float> = tween(
        durationMillis = SCORE_COUNT_DURATION,
        easing = FastOutSlowInEasing
    )

    // --- Shake Keyframes ---
    fun wrongShakeSpec(): KeyframesSpec<Float> = keyframes {
        durationMillis = WRONG_SHAKE_DURATION
        0f at 0 using LinearEasing
        -8f at 50 using FastOutSlowInEasing
        8f at 100 using FastOutSlowInEasing
        -6f at 175 using FastOutSlowInEasing
        6f at 250 using FastOutSlowInEasing
        -3f at 325 using FastOutSlowInEasing
        0f at WRONG_SHAKE_DURATION using FastOutSlowInEasing
    }

    // --- Points Popup ---
    val PointsPopupOffsetSpec: TweenSpec<Float> = tween(
        durationMillis = POINTS_POPUP_DURATION,
        easing = LinearOutSlowInEasing
    )

    val PointsPopupFadeSpec: TweenSpec<Float> = tween(
        durationMillis = POINTS_POPUP_DURATION,
        delayMillis = 200,
        easing = LinearEasing
    )
}
