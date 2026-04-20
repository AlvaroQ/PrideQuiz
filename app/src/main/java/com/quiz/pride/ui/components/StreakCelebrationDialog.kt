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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import com.quiz.pride.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.domain.StreakCheckResult
import com.quiz.pride.ui.theme.DarkSurfaceVariant
import com.quiz.pride.ui.theme.GradientPointsBottom
import com.quiz.pride.ui.theme.GradientPointsTop
import com.quiz.pride.ui.theme.NeonBlue
import com.quiz.pride.ui.theme.NeonGreen
import com.quiz.pride.ui.theme.NeonOrange
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.OffWhiteInk
import com.quiz.pride.ui.theme.PrideButtonStyles
import com.quiz.pride.ui.theme.White

/**
 * Dialog de celebracion que se muestra en ResultScreen cuando la racha
 * cambia de estado (continua, se salva, se rompe, o es nueva).
 *
 * Stateless: no se muestra para AlreadyPlayedToday.
 * El contenido varia segun el tipo de StreakCheckResult recibido.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakCelebrationDialog(
    streakCheckResult: StreakCheckResult,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dialog_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(DarkSurfaceVariant)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            NeonPink.copy(alpha = 0.5f),
                            NeonPurple.copy(alpha = 0.4f),
                            NeonBlue.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (streakCheckResult) {
                    is StreakCheckResult.StreakContinued -> {
                        StreakContinuedContent(
                            result = streakCheckResult,
                            glowScale = glowScale
                        )
                    }
                    is StreakCheckResult.StreakSavedByFreeze -> {
                        StreakSavedContent(
                            result = streakCheckResult,
                            glowScale = glowScale
                        )
                    }
                    is StreakCheckResult.StreakBroken -> {
                        StreakBrokenContent(
                            result = streakCheckResult,
                            glowScale = glowScale
                        )
                    }
                    is StreakCheckResult.NewStreak -> {
                        NewStreakContent(glowScale = glowScale)
                    }
                    is StreakCheckResult.AlreadyPlayedToday -> {
                        // No deberia llegar aqui: el composable que llama
                        // debe filtrar AlreadyPlayedToday antes de mostrar el dialog
                        return@Column
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrideButton(
                    text = stringResource(R.string.continue_action),
                    onClick = onDismiss,
                    style = PrideButtonStyles.Start.current(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─────────────────────── VARIANTES DE CONTENIDO ──────────────────────────────

@Composable
private fun StreakContinuedContent(
    result: StreakCheckResult.StreakContinued,
    glowScale: Float
) {
    val state = result.newState
    val reward = result.reward
    val isMilestone = reward.isMilestone

    // Icono principal
    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(glowScale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(NeonOrange.copy(alpha = 0.3f), Color.Transparent)
                )
            )
            .drawBehind {
                drawCircle(
                    color = NeonOrange.copy(alpha = 0.2f),
                    radius = size.minDimension / 1.5f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (isMilestone) {
            Icon(
                painter = painterResource(R.drawable.ic_emoji_events),
                contentDescription = null,
                tint = GradientPointsBottom,
                modifier = Modifier.size(44.dp)
            )
        } else {
            Text(
                text = "\uD83D\uDD25",
                fontSize = 44.sp,
                color = NeonOrange
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Titulo
    val titulo = if (isMilestone) {
        "${reward.milestoneDay} dias seguidos"
    } else {
        "Racha de ${state.currentStreak} dias"
    }

    Text(
        text = if (isMilestone) "Hito alcanzado" else "Racha continua",
        style = MaterialTheme.typography.labelLarge,
        color = if (isMilestone) GradientPointsBottom else NeonOrange,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = titulo,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            shadow = Shadow(
                color = if (isMilestone) GradientPointsTop.copy(alpha = 0.5f) else NeonOrange.copy(alpha = 0.5f),
                offset = Offset(0f, 0f),
                blurRadius = 10f
            )
        ),
        color = if (isMilestone) GradientPointsBottom else NeonOrange,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(12.dp))

    // XP bonus
    if (reward.xpBonus > 0) {
        XpBonusBadge(xpBonus = reward.xpBonus, multiplier = reward.streakMultiplier)
        Spacer(modifier = Modifier.height(12.dp))
    }

    // Freeze tokens ganados
    if (reward.freezeTokens > 0) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "❄", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.streak_freeze_tokens_gained, reward.freezeTokens),
                style = MaterialTheme.typography.bodyMedium,
                color = NeonBlue
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    // Puntos del ciclo
    CycleProgressDotsCompact(
        cycleDay = state.cycleDay,
        completed = true
    )
}

@Composable
private fun StreakSavedContent(
    result: StreakCheckResult.StreakSavedByFreeze,
    glowScale: Float
) {
    val state = result.newState
    val reward = result.reward

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(glowScale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(NeonBlue.copy(alpha = 0.3f), Color.Transparent)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "❄", fontSize = 40.sp)
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.streak_saved_title),
        style = MaterialTheme.typography.labelLarge,
        color = NeonBlue,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = stringResource(R.string.streak_days, state.currentStreak),
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            shadow = Shadow(
                color = NeonBlue.copy(alpha = 0.5f),
                offset = Offset(0f, 0f),
                blurRadius = 10f
            )
        ),
        color = NeonBlue,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.streak_freeze_used),
        style = MaterialTheme.typography.bodySmall,
        color = OffWhiteInk.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
    )

    // Tokens restantes
    if (state.freezeTokens > 0) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.streak_tokens_remaining, state.freezeTokens),
            style = MaterialTheme.typography.labelMedium,
            color = NeonBlue.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }

    if (reward.xpBonus > 0) {
        Spacer(modifier = Modifier.height(12.dp))
        XpBonusBadge(xpBonus = reward.xpBonus, multiplier = reward.streakMultiplier)
    }

    Spacer(modifier = Modifier.height(12.dp))

    CycleProgressDotsCompact(
        cycleDay = state.cycleDay,
        completed = true
    )
}

@Composable
private fun StreakBrokenContent(
    result: StreakCheckResult.StreakBroken,
    glowScale: Float
) {
    val state = result.newState

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(glowScale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(NeonPink.copy(alpha = 0.2f), Color.Transparent)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = NeonPink.copy(alpha = 0.5f),
            modifier = Modifier.size(44.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.streak_lost_title),
        style = MaterialTheme.typography.labelLarge,
        color = NeonPink,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = stringResource(R.string.streak_previous_days, result.previousStreak),
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        color = OffWhiteInk.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NeonGreen.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = NeonGreen.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.streak_new_started),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = NeonGreen,
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.streak_play_daily_tip),
        style = MaterialTheme.typography.bodySmall,
        color = OffWhiteInk.copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun NewStreakContent(glowScale: Float) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(glowScale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(NeonGreen.copy(alpha = 0.3f), Color.Transparent)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "🎉", fontSize = 40.sp)
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.streak_first_title),
        style = MaterialTheme.typography.labelLarge,
        color = NeonGreen,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = stringResource(R.string.streak_day_number, 1),
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            shadow = Shadow(
                color = NeonGreen.copy(alpha = 0.5f),
                offset = Offset(0f, 0f),
                blurRadius = 10f
            )
        ),
        color = NeonGreen,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.streak_play_each_day_tip),
        style = MaterialTheme.typography.bodySmall,
        color = OffWhiteInk.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Descripcion de recompensas del ciclo
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NeonGreen.copy(alpha = 0.10f))
            .border(
                width = 1.dp,
                color = NeonGreen.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.streak_weekly_reward_info),
            style = MaterialTheme.typography.labelSmall,
            color = NeonGreen.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

// ────────────────────────── COMPONENTES COMPARTIDOS ──────────────────────────

@Composable
private fun XpBonusBadge(xpBonus: Int, multiplier: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "xp_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        GradientPointsTop.copy(alpha = 0.15f),
                        GradientPointsBottom.copy(alpha = 0.15f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        GradientPointsTop.copy(alpha = glowAlpha),
                        GradientPointsBottom.copy(alpha = glowAlpha)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = "⭐", fontSize = 18.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.xp_gained, xpBonus),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = GradientPointsBottom
        )
        if (multiplier > 1f) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "x${String.format("%.1f", multiplier)}",
                style = MaterialTheme.typography.bodySmall,
                color = GradientPointsTop
            )
        }
    }
}

@Composable
private fun CycleProgressDotsCompact(
    cycleDay: Int,
    completed: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.streak_cycle_label),
            style = MaterialTheme.typography.labelSmall,
            color = OffWhiteInk.copy(alpha = 0.6f)
        )
        for (day in 1..7) {
            val isFilled = if (completed) day <= cycleDay else day < cycleDay
            val isCurrent = day == cycleDay

            Box(
                modifier = Modifier
                    .size(if (isCurrent) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFilled) NeonOrange
                        else Color.Transparent
                    )
                    .border(
                        width = 1.dp,
                        color = if (isFilled) NeonOrange else White.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }
    }
}
