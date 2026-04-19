package com.quiz.pride.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.domain.reward.DailyReward
import com.quiz.domain.reward.RewardTier
import com.quiz.pride.R
import com.quiz.pride.ui.theme.DarkSurfaceVariant
import com.quiz.pride.ui.theme.NeonBlue
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.RankGold
import com.quiz.pride.ui.theme.White

// Colores por tier — alineados con la paleta Pride
private val TierCommonColor = NeonBlue                 // Sky
private val TierUncommonColor = NeonPurple             // Violet
private val TierRareColor = RankGold                   // Oro

private fun tierColor(tier: RewardTier): Color = when (tier) {
    RewardTier.COMMON -> TierCommonColor
    RewardTier.UNCOMMON -> TierUncommonColor
    RewardTier.RARE -> TierRareColor
}

/**
 * Card de recompensa diaria para SelectScreen.
 *
 * Estados:
 * - reward == null: placeholder con shimmer
 * - !isClaimed: CTA "Reclamar" con shimmer en el borde segun el tier
 * - isClaimed: muestra lo ganado con tono apagado del tier. Arrastrable en
 *   ambas direcciones (start→end y end→start) para ocultar hasta el dia
 *   siguiente: al confirmar el swipe (umbral 50%) dispara `onDismiss`.
 *
 * Visual coherente con el tema oscuro atmosferico "warm midnight" de PrideQuiz:
 * usa DarkSurfaceVariant de fondo, bordes luminosos del tier y shimmer en el CTA.
 */
@Composable
fun DailyRewardCard(
    reward: DailyReward?,
    isClaiming: Boolean = false,
    onClaim: () -> Unit,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (reward == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(76.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurfaceVariant)
        )
        return
    }

    AnimatedContent(
        targetState = reward.isClaimed,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
        label = "daily_reward_state",
        // modifier externo va aqui para que el align/padding del caller
        // posicione el AnimatedContent (de lo contrario el hijo queda en la
        // esquina top-start del BoxScope padre).
        modifier = modifier
    ) { isClaimed ->
        if (!isClaimed) {
            UnclaimedRewardCard(
                reward = reward,
                enabled = !isClaiming,
                onClaim = onClaim
            )
        } else {
            DismissibleClaimedRewardCard(
                reward = reward,
                onDismiss = onDismiss
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissibleClaimedRewardCard(
    reward: DailyReward,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        // Umbral del 50% del ancho para confirmar el dismiss.
        positionalThreshold = { totalDistance -> totalDistance * 0.5f }
    )

    // Cuando el swipe cruza el umbral y se asienta fuera de Settled, persistir.
    // SwipeToDismissBox hace el fade-out + slide automatico al confirmarse.
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
            DismissBackground(direction = dismissState.dismissDirection, tier = reward.tier)
        }
    ) {
        ClaimedRewardCard(reward = reward)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissBackground(
    direction: SwipeToDismissBoxValue,
    tier: RewardTier
) {
    val accent = tierColor(tier)
    val alignment = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(accent.copy(alpha = 0.12f))
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
private fun UnclaimedRewardCard(
    reward: DailyReward,
    enabled: Boolean,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reward_shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reward_shimmer_alpha"
    )

    val accentColor = tierColor(reward.tier)

    Surface(
        onClick = onClaim,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = DarkSurfaceVariant,
        shadowElevation = 8.dp,
        border = BorderStroke(2.dp, accentColor.copy(alpha = shimmerAlpha))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(accentColor, accentColor.copy(alpha = 0.65f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "\uD83C\uDF81", fontSize = 22.sp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.daily_reward_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = White
                )
                Text(
                    text = stringResource(R.string.daily_reward_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = White.copy(alpha = 0.7f)
                )
            }

            val shimmerTranslate by rememberInfiniteTransition(label = "btn_shimmer")
                .animateFloat(
                    initialValue = -200f,
                    targetValue = 600f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "btn_shimmer_x"
                )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.8f),
                                White.copy(alpha = 0.35f),
                                accentColor.copy(alpha = 0.8f),
                                accentColor
                            ),
                            start = Offset(shimmerTranslate, 0f),
                            end = Offset(shimmerTranslate + 200f, 60f)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.daily_reward_claim),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = White
                )
            }
        }
    }
}

@Composable
private fun ClaimedRewardCard(
    reward: DailyReward,
    modifier: Modifier = Modifier
) {
    val accentColor = tierColor(reward.tier)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = DarkSurfaceVariant,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "\u2705", fontSize = 22.sp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.daily_reward_claimed_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = White
                )
            }

            Text(
                text = stringResource(R.string.daily_reward_come_back),
                style = MaterialTheme.typography.labelSmall,
                color = White.copy(alpha = 0.6f)
            )
        }
    }
}
