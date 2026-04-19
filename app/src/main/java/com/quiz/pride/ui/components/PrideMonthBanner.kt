package com.quiz.pride.ui.components

import androidx.compose.animation.core.EaseInOutSine
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.pride.R
import com.quiz.pride.ui.theme.PrideBlue
import com.quiz.pride.ui.theme.PrideGreen
import com.quiz.pride.ui.theme.PrideOrange
import com.quiz.pride.ui.theme.PridePurple
import com.quiz.pride.ui.theme.PrideRed
import com.quiz.pride.ui.theme.PrideYellow
import com.quiz.pride.ui.theme.White

/**
 * Banner para SelectScreen activo durante el mes del Orgullo (junio).
 *
 * Visual:
 * - Gradient horizontal completo arcoiris Pride (6 colores oficiales).
 * - Borde animado que se desliza con tension-release EaseInOutSine.
 * - Emojis 🌈 flanqueando el titulo.
 * - Copy temporal con dias restantes y multiplicador XP.
 *
 * Solo se renderiza si [isActive] es true. El ViewModel es responsable
 * de consultar SeasonalEventManager.isPrideMonth() y pasarlo.
 */
@Composable
fun PrideMonthBanner(
    isActive: Boolean,
    xpMultiplier: Float,
    daysRemaining: Int,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "pride_banner_shimmer")
    val translate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pride_banner_translate"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        PrideRed,
                        PrideOrange,
                        PrideYellow,
                        PrideGreen,
                        PrideBlue,
                        PridePurple
                    ),
                    start = Offset(translate * 100f, 0f),
                    end = Offset(1200f + translate * 100f, 200f)
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        White.copy(alpha = 0.55f),
                        White.copy(alpha = 0.15f),
                        White.copy(alpha = 0.55f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "\uD83C\uDF08", fontSize = 26.sp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.seasonal_pride_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        shadow = Shadow(
                            color = White.copy(alpha = 0.45f),
                            offset = Offset(0f, 0f),
                            blurRadius = 8f
                        )
                    ),
                    color = White
                )
                Text(
                    text = stringResource(
                        R.string.seasonal_pride_subtitle,
                        String.format("%.1f", xpMultiplier)
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = White.copy(alpha = 0.92f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "x${String.format("%.1f", xpMultiplier)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        shadow = Shadow(
                            color = White.copy(alpha = 0.7f),
                            offset = Offset(0f, 0f),
                            blurRadius = 10f
                        )
                    ),
                    color = White
                )
                if (daysRemaining > 0) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.seasonal_pride_days_remaining,
                            daysRemaining,
                            daysRemaining
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
