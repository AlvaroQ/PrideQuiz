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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.pride.R
import com.quiz.pride.ui.theme.NeonOrange
import com.quiz.pride.ui.theme.NeonYellow
import com.quiz.pride.ui.theme.White

/**
 * Banner de advertencia que se muestra en la parte superior de SelectScreen
 * cuando la racha del jugador esta en riesgo de romperse.
 *
 * Stateless: recibe los datos directamente del ViewModel.
 * El banner no es descartable por diseno — reaparece en cada visita
 * mientras la racha siga en riesgo. La urgencia es parte del mensaje.
 */
@Composable
fun StreakAtRiskBanner(
    currentStreak: Int,
    freezeTokens: Int,
    modifier: Modifier = Modifier
) {
    // Animacion de pulso en el borde para transmitir urgencia
    val infiniteTransition = rememberInfiniteTransition(label = "banner_pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_alpha"
    )

    val message = if (freezeTokens > 0) {
        "Tenes $freezeTokens ❄ para protegerla"
    } else {
        "Juga hoy para no perderla"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        NeonOrange.copy(alpha = 0.20f),
                        NeonYellow.copy(alpha = 0.15f),
                        NeonOrange.copy(alpha = 0.20f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        NeonOrange.copy(alpha = borderAlpha),
                        NeonYellow.copy(alpha = borderAlpha * 0.8f),
                        NeonOrange.copy(alpha = borderAlpha)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icono de advertencia
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = NeonOrange,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Icono de fuego + texto principal
            Text(
                text = "\uD83D\uDD25",
                fontSize = 18.sp,
                color = NeonOrange
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Texto principal
            androidx.compose.foundation.layout.Column {
                Text(
                    text = stringResource(R.string.streak_at_risk_message, currentStreak),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = NeonOrange
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.labelSmall,
                    color = White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
