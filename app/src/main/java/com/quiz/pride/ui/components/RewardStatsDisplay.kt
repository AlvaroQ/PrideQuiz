package com.quiz.pride.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.domain.reward.DailyReward
import com.quiz.pride.ui.theme.DarkSurfaceVariant
import com.quiz.pride.ui.theme.White

/**
 * Componente independiente que muestra XP, coins y gems de una recompensa.
 * Se posiciona arriba y a la derecha del DailyRewardCard.
 *
 * Muestra:
 * - XP (siempre)
 * - Coins (siempre)
 * - Gems (solo si > 0)
 */
@Composable
fun RewardStatsDisplay(
    reward: DailyReward?,
    modifier: Modifier = Modifier
) {
    if (reward == null) return

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // XP
        RewardStatItem(
            icon = "⭐",
            value = reward.xpAmount.toString(),
            label = "XP"
        )

        // Coins
        RewardStatItem(
            icon = "🪙",
            value = reward.coinsAmount.toString(),
            label = "Coins"
        )

        // Gems (solo si hay)
        if (reward.gemsAmount > 0) {
            RewardStatItem(
                icon = "💎",
                value = reward.gemsAmount.toString(),
                label = "Gems"
            )
        }
    }
}

@Composable
private fun RewardStatItem(
    icon: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 16.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = White,
            fontSize = 13.sp
        )
    }
}


