package com.quiz.pride.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.quiz.domain.cosmetics.CurrencyBalance
import com.quiz.pride.ui.theme.GradientPointsBottom
import com.quiz.pride.ui.theme.GradientPointsTop
import com.quiz.pride.ui.theme.NeonBlue

/**
 * Componente reutilizable que muestra el balance de monedas y gemas del jugador.
 * Stateless: recibe [balance] del ViewModel via state hoisting.
 */
@Composable
fun CurrencyDisplay(
    balance: CurrencyBalance,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        GradientPointsTop.copy(alpha = 0.6f),
                        NeonBlue.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Monedas
        Text(
            text = "\uD83E\uDE99",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = balance.coins.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = GradientPointsBottom
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Gemas
        Text(
            text = "\uD83D\uDC8E",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = balance.gems.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = NeonBlue
        )
    }
}
