package com.quiz.pride.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.pride.ui.theme.NeonOrange
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.NeonYellow
import com.quiz.pride.ui.theme.PridePurple

/**
 * Llama animada que escala segun la racha actual.
 *
 * Tiers:
 *  - 0     : no renderiza
 *  - 1-2   : llama estatica amarilla
 *  - 3-6   : llama con pulso lento, naranja
 *  - 7-13  : doble llama, pulso medio, rosa intenso
 *  - 14-29 : triple llama, pulso rapido, violeta
 *  - 30+   : arcoiris + llama, pulso mas rapido
 */
@Composable
fun AnimatedStreakFlame(
    currentStreak: Int,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 22.sp
) {
    if (currentStreak < 1) return

    val baseScale = when {
        currentStreak < 3 -> 1f
        currentStreak < 7 -> 1.08f
        currentStreak < 14 -> 1.15f
        else -> 1.22f
    }

    val pulseDuration = when {
        currentStreak < 3 -> 0
        currentStreak < 7 -> 2000
        currentStreak < 14 -> 1500
        else -> 1000
    }

    val pulseScale = if (pulseDuration > 0) {
        val infiniteTransition = rememberInfiniteTransition(label = "flame")
        val animated by infiniteTransition.animateFloat(
            initialValue = baseScale * 0.92f,
            targetValue = baseScale * 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(pulseDuration, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flamePulse"
        )
        animated
    } else {
        baseScale
    }

    val glowColor = when {
        currentStreak < 3 -> NeonYellow
        currentStreak < 7 -> NeonOrange
        currentStreak < 14 -> NeonPink
        currentStreak < 30 -> NeonPurple
        else -> PridePurple
    }

    val flameText = when {
        currentStreak < 7 -> "\uD83D\uDD25"
        currentStreak < 14 -> "\uD83D\uDD25\uD83D\uDD25"
        currentStreak < 30 -> "\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25"
        else -> "\uD83C\uDF08\uD83D\uDD25"
    }

    Box(
        modifier = modifier.drawBehind {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.35f),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.9f
            )
        },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = flameText,
            fontSize = fontSize,
            modifier = Modifier.graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
        )
    }
}

/**
 * Version contenedora con fondo circular - variante "badge" para lugares
 * donde la llama debe destacar en una fila de KPIs.
 */
@Composable
fun AnimatedStreakFlameBadge(
    currentStreak: Int,
    modifier: Modifier = Modifier,
    sizeDp: Int = 48
) {
    val glowColor = when {
        currentStreak < 3 -> NeonYellow
        currentStreak < 7 -> NeonOrange
        currentStreak < 14 -> NeonPink
        currentStreak < 30 -> NeonPurple
        else -> PridePurple
    }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.28f),
                        Color.Transparent
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedStreakFlame(
            currentStreak = currentStreak,
            fontSize = (sizeDp * 0.55).sp
        )
    }
}
