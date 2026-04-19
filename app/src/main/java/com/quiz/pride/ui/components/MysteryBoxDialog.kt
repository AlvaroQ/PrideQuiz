package com.quiz.pride.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.quiz.domain.reward.MysteryReward
import com.quiz.domain.reward.MysteryRewardType
import com.quiz.pride.R
import com.quiz.pride.ui.theme.DarkSurfaceVariant
import com.quiz.pride.ui.theme.GradientPointsBottom
import com.quiz.pride.ui.theme.GradientPointsTop
import com.quiz.pride.ui.theme.NeonBlue
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.White
import kotlinx.coroutines.delay

/**
 * Dialog de caja misteriosa. Oculta con "?" durante 1200ms antes de revelar.
 *
 * Coherencia visual con PrideQuiz:
 * - Fondo DarkSurfaceVariant con borde arco-iris (pink/violet/blue).
 * - Tipografia Material 3 (no fuentes custom).
 * - Emoji centrado estilo celebracion, sin assets adicionales.
 */
@Composable
fun MysteryBoxDialog(
    reward: MysteryReward,
    onDismiss: () -> Unit
) {
    var revealed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1200L)
        revealed = true
    }

    Dialog(onDismissRequest = if (revealed) onDismiss else { {} }) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = DarkSurfaceVariant,
            shadowElevation = 12.dp,
            modifier = Modifier.border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        NeonPink.copy(alpha = 0.55f),
                        NeonPurple.copy(alpha = 0.5f),
                        NeonBlue.copy(alpha = 0.45f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.mystery_box_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = GradientPointsBottom,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.mystery_box_subtitle),
                    style = MaterialTheme.typography.labelMedium,
                    color = White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedContent(
                    targetState = revealed,
                    transitionSpec = {
                        (scaleIn(tween(400)) + fadeIn(tween(400))) togetherWith
                                fadeOut(tween(200))
                    },
                    label = "mystery_reveal"
                ) { isRevealed ->
                    if (!isRevealed) {
                        Text(
                            text = "?",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = GradientPointsBottom,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when (reward.type) {
                                    MysteryRewardType.XP_BONUS -> "\u26A1"
                                    MysteryRewardType.COINS_BONUS -> "\uD83E\uDE99"
                                    MysteryRewardType.FREEZE_TOKEN -> "\u2744\uFE0F"
                                },
                                fontSize = 56.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (reward.xpAmount > 0) {
                                RewardRow(
                                    icon = "\u26A1",
                                    text = stringResource(R.string.xp_gained, reward.xpAmount),
                                    color = NeonPurple
                                )
                            }
                            if (reward.coinsAmount > 0) {
                                RewardRow(
                                    icon = "\uD83E\uDE99",
                                    text = stringResource(R.string.mystery_box_coins, reward.coinsAmount),
                                    color = GradientPointsTop
                                )
                            }
                            if (reward.freezeTokens > 0) {
                                RewardRow(
                                    icon = "\u2744\uFE0F",
                                    text = stringResource(R.string.mystery_box_freeze_token),
                                    color = NeonBlue
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (revealed) {
                    Surface(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(NeonPink, NeonPurple)
                                )
                            )
                    ) {
                        Text(
                            text = stringResource(R.string.mystery_box_claim),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.mystery_box_opening),
                        style = MaterialTheme.typography.labelSmall,
                        color = White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardRow(icon: String, text: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 18.sp)
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}
