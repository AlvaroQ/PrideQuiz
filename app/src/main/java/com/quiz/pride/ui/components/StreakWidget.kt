package com.quiz.pride.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.quiz.pride.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.domain.StreakState
import com.quiz.pride.ui.theme.DarkPurpleBackground
import com.quiz.pride.ui.theme.DarkPurpleSurfaceVariant
import com.quiz.pride.ui.theme.GlowPink
import com.quiz.pride.ui.theme.NeonGreen
import com.quiz.pride.ui.theme.NeonOrange
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.NeonYellow
import com.quiz.pride.ui.theme.White

/**
 * Widget compacto que muestra el estado actual de la racha diaria del jugador.
 *
 * Se usa en SelectScreen (con `onDismiss` para swipe-to-dismiss diario) y en
 * ProfileScreen (sin `onDismiss` → siempre visible). Stateless: recibe datos del
 * ViewModel y no maneja estado interno.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakWidget(
    streakState: StreakState,
    isAtRisk: Boolean,
    hasPlayedToday: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    if (onDismiss == null) {
        StreakWidgetContent(streakState, isAtRisk, hasPlayedToday, modifier)
        return
    }

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.5f }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            onDismiss()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.fillMaxWidth(),
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            StreakDismissBackground(direction = dismissState.dismissDirection)
        }
    ) {
        StreakWidgetContent(streakState, isAtRisk, hasPlayedToday, Modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StreakDismissBackground(direction: SwipeToDismissBoxValue) {
    val alignment = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(NeonPurple.copy(alpha = 0.12f))
            .padding(horizontal = 24.dp),
        contentAlignment = alignment
    ) {
        Text(
            text = "\uD83D\uDC41\u200D\uD83D\uDDE8 " + stringResource(R.string.daily_reward_hide_until_tomorrow),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = White.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun StreakWidgetContent(
    streakState: StreakState,
    isAtRisk: Boolean,
    hasPlayedToday: Boolean,
    modifier: Modifier = Modifier
) {
    // Animacion de pulso para el borde cuando la racha esta en riesgo
    val riskBorderAlpha by if (isAtRisk) {
        val transition = rememberInfiniteTransition(label = "streak_risk")
        transition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "risk_pulse"
        )
    } else {
        // Sin animacion cuando no hay riesgo
        val transition = rememberInfiniteTransition(label = "streak_idle")
        transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "idle_border"
        )
    }

    val borderColor = when {
        hasPlayedToday -> NeonGreen
        isAtRisk -> NeonOrange.copy(alpha = riskBorderAlpha)
        else -> NeonPurple.copy(alpha = 0.3f)
    }

    val glowColor = when {
        hasPlayedToday -> NeonGreen.copy(alpha = 0.3f)
        isAtRisk -> NeonOrange.copy(alpha = 0.4f)
        else -> GlowPink
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isAtRisk) 16.dp else 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        DarkPurpleSurfaceVariant.copy(alpha = 0.88f),
                        DarkPurpleBackground.copy(alpha = 0.92f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .border(
                width = if (isAtRisk) 2.dp else 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor,
                        White.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Fila superior: icono de fuego + dias + estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Icono de fuego + contador de dias
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        NeonOrange.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .drawBehind {
                                drawCircle(
                                    color = NeonOrange.copy(alpha = 0.15f),
                                    radius = size.minDimension / 1.5f
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "\uD83D\uDD25",
                            fontSize = 26.sp,
                            color = if (streakState.currentStreak > 0) NeonOrange else White.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.streak_days, streakState.currentStreak),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = Shadow(
                                    color = NeonOrange.copy(alpha = 0.6f),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 8f
                                )
                            ),
                            color = if (streakState.currentStreak > 0) NeonOrange else White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = stringResource(R.string.streak_current),
                            style = MaterialTheme.typography.labelSmall,
                            color = White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Estado: ya jugo hoy / en riesgo / freeze tokens
                Column(horizontalAlignment = Alignment.End) {
                    when {
                        hasPlayedToday -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.streak_played_today),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonGreen
                                )
                            }
                        }
                        isAtRisk -> {
                            Text(
                                text = stringResource(R.string.streak_at_risk),
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        else -> {
                            Text(
                                text = stringResource(R.string.streak_play_today),
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonPink
                            )
                        }
                    }

                    // Freeze tokens si tiene
                    if (streakState.freezeTokens > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(minOf(streakState.freezeTokens, 3)) {
                                Text(
                                    text = "❄",
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 1.dp)
                                )
                            }
                            if (streakState.freezeTokens > 3) {
                                Text(
                                    text = "+${streakState.freezeTokens - 3}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ciclo de 7 dias — puntos de progreso
            CycleProgressDots(
                cycleDay = streakState.cycleDay,
                hasPlayedToday = hasPlayedToday
            )

            // Mejor racha si es > 0
            if (streakState.bestStreak > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.streak_best_days, streakState.bestStreak),
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonYellow,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

/**
 * Fila de 7 puntos que visualiza el progreso del ciclo semanal.
 * Los dias completados se muestran rellenos, el dia actual resaltado,
 * y los dias restantes como contorno.
 */
@Composable
private fun CycleProgressDots(
    cycleDay: Int,
    hasPlayedToday: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (day in 1..7) {
            val isCompleted = day < cycleDay || (day == cycleDay && hasPlayedToday)
            val isCurrent = day == cycleDay && !hasPlayedToday
            val dotColor = when {
                isCompleted -> NeonOrange
                isCurrent -> NeonPink
                else -> White.copy(alpha = 0.35f)
            }

            Box(
                modifier = Modifier
                    .size(if (isCurrent) 14.dp else 10.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted || isCurrent) dotColor
                        else Color.Transparent
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isCompleted) dotColor else if (isCurrent) NeonPink else White.copy(alpha = 0.55f),
                        shape = CircleShape
                    )
                    .then(
                        if (isCurrent) Modifier.drawBehind {
                            drawCircle(
                                color = NeonPink.copy(alpha = 0.3f),
                                radius = size.minDimension
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.8f))
                    )
                }
            }
        }
    }
}
