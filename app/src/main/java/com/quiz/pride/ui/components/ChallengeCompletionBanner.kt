package com.quiz.pride.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quiz.pride.R
import com.quiz.domain.challenge.ChallengeDifficulty
import com.quiz.domain.challenge.ChallengeCompletionResult
import com.quiz.domain.challenge.ChallengeReward
import com.quiz.domain.challenge.ChallengeType
import com.quiz.domain.challenge.DailyChallenge
import com.quiz.pride.ui.theme.DarkSurfaceVariant
import com.quiz.pride.ui.theme.GradientPointsBottom
import com.quiz.pride.ui.theme.NeonGreen
import com.quiz.pride.ui.theme.NeonOrange
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.NeonYellow
import com.quiz.pride.ui.theme.ResponseFail
import com.quiz.pride.ui.theme.PrideQuizTheme
import com.quiz.pride.ui.theme.SettingsGradientBottom
import com.quiz.pride.ui.theme.SettingsGradientTop
import com.quiz.pride.ui.theme.White

/**
 * Banner inline que se muestra en ResultScreen cuando el jugador completo
 * uno o mas desafios durante la partida.
 *
 * NO es un dialog: es contenido inline que se integra en el flujo de la pantalla.
 * Aparece con animacion de slide desde abajo + fade.
 */
@Composable
fun ChallengeCompletionBanner(
    completionResult: ChallengeCompletionResult,
    modifier: Modifier = Modifier
) {
    // Animacion de entrada retrasada para que no compita con la animacion del score
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(400)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = tween(400)
        ) + fadeIn(animationSpec = tween(400))
    ) {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            // Header del banner
            BannerHeader()

            Spacer(modifier = Modifier.height(8.dp))

            // Filas de desafios completados
            completionResult.completedChallenges.forEach { challenge ->
                CompletedChallengeItem(challenge = challenge)
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Bonus por completar todos los diarios
            if (completionResult.allDailyJustCompleted) {
                Spacer(modifier = Modifier.height(4.dp))
                AllDailyBonusItem()
            }
        }
    }
}

@Composable
private fun BannerHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(NeonGreen.copy(alpha = 0.15f))
                .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = stringResource(R.string.challenges_completed),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = NeonGreen
            )
        }
    }
}

@Composable
private fun CompletedChallengeItem(challenge: DailyChallenge) {
    val difficultyColor = when (challenge.difficulty) {
        ChallengeDifficulty.EASY -> NeonGreen
        ChallengeDifficulty.MEDIUM -> com.quiz.pride.ui.theme.NeonOrange
        ChallengeDifficulty.HARD -> ResponseFail
        ChallengeDifficulty.WEEKLY -> NeonPurple
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        DarkSurfaceVariant.copy(alpha = 0.8f),
                        DarkSurfaceVariant.copy(alpha = 0.6f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        difficultyColor.copy(alpha = 0.4f),
                        difficultyColor.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Icono de check
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(NeonGreen.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.size(12.dp)
            )
        }

        // Descripcion
        Text(
            text = challenge.resolveDescription(),
            style = MaterialTheme.typography.bodySmall,
            color = White.copy(alpha = 0.9f),
            modifier = Modifier.weight(1f),
            maxLines = 1
        )

        // Recompensa
        Column(horizontalAlignment = Alignment.End) {
            if (challenge.reward.xp > 0) {
                Text(
                    text = stringResource(R.string.xp_gained, challenge.reward.xp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        shadow = Shadow(
                            color = GradientPointsBottom.copy(alpha = 0.5f),
                            offset = Offset(0f, 0f),
                            blurRadius = 4f
                        )
                    ),
                    color = GradientPointsBottom
                )
            }
            if (challenge.reward.coins > 0) {
                Text(
                    text = "+${challenge.reward.coins}🪙",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonYellow
                )
            }
        }
    }
}

@Composable
private fun AllDailyBonusItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        SettingsGradientTop.copy(alpha = 0.15f),
                        SettingsGradientBottom.copy(alpha = 0.12f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        SettingsGradientTop.copy(alpha = 0.5f),
                        SettingsGradientBottom.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "🎉", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(R.string.challenges_completion_bonus),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = NeonPurple
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.xp_gained, 75),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = GradientPointsBottom
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "+15🪙",
                style = MaterialTheme.typography.labelSmall,
                color = NeonYellow
            )
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "ChallengeCompletionBanner - Con bonus - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "ChallengeCompletionBanner - Con bonus - Dark")
@Composable
private fun ChallengeCompletionBannerWithBonusPreview() {
    PrideQuizTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ChallengeCompletionBanner(
                completionResult = ChallengeCompletionResult(
                    completedChallenges = listOf(
                        DailyChallenge(
                            id = "easy1",
                            type = ChallengeType.GAMES_PLAYED,
                            difficulty = ChallengeDifficulty.EASY,
                            descriptionKey = "games_played",
                            targetValue = 2,
                            currentProgress = 2,
                            isCompleted = true,
                            reward = ChallengeReward(30, 5)
                        ),
                        DailyChallenge(
                            id = "med1",
                            type = ChallengeType.TOTAL_CORRECT,
                            difficulty = ChallengeDifficulty.MEDIUM,
                            descriptionKey = "total_correct",
                            targetValue = 10,
                            currentProgress = 10,
                            isCompleted = true,
                            reward = ChallengeReward(60, 10)
                        )
                    ),
                    allDailyJustCompleted = true,
                    totalXpEarned = 165,
                    totalCoinsEarned = 30
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "ChallengeCompletionBanner - Solo un desafio")
@Composable
private fun ChallengeCompletionBannerSinglePreview() {
    PrideQuizTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ChallengeCompletionBanner(
                completionResult = ChallengeCompletionResult(
                    completedChallenges = listOf(
                        DailyChallenge(
                            id = "hard1",
                            type = ChallengeType.CORRECT_STREAK,
                            difficulty = ChallengeDifficulty.HARD,
                            descriptionKey = "correct_streak",
                            targetValue = 8,
                            currentProgress = 8,
                            isCompleted = true,
                            reward = ChallengeReward(100, 20)
                        )
                    ),
                    allDailyJustCompleted = false,
                    totalXpEarned = 100,
                    totalCoinsEarned = 20
                )
            )
        }
    }
}
