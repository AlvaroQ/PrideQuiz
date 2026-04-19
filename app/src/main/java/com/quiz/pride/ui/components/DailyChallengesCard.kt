@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.quiz.pride.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quiz.pride.R
import com.quiz.domain.challenge.ChallengeDifficulty
import com.quiz.domain.challenge.ChallengeReward
import com.quiz.domain.challenge.ChallengeType
import com.quiz.domain.challenge.DailyChallenge
import com.quiz.domain.challenge.DailyChallengeState
import com.quiz.pride.ui.theme.DarkSurfaceVariant
import com.quiz.pride.ui.theme.GlowPurple
import com.quiz.pride.ui.theme.GradientPointsBottom
import com.quiz.pride.ui.theme.NeonGreen
import com.quiz.pride.ui.theme.NeonOrange
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.NeonYellow
import com.quiz.pride.ui.theme.ResponseFail
import com.quiz.pride.ui.theme.PrideQuizTheme
import com.quiz.pride.ui.theme.PrideButtonStyles
import com.quiz.pride.ui.theme.White

/**
 * Tarjeta de desafios diarios para SelectScreen.
 *
 * Muestra los 3 desafios diarios y el desafio semanal opcional.
 * Es completamente stateless: recibe DailyChallengeState del ViewModel.
 */
@Composable
fun DailyChallengesCard(
    challengeState: DailyChallengeState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = GlowPurple,
                spotColor = GlowPurple
            )
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceVariant)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(NeonPurple.copy(alpha = 0.4f), NeonPink.copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header
            ChallengesHeader(
                date = challengeState.date,
                allCompleted = challengeState.allDailyCompleted
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Desafios diarios
            if (challengeState.challenges.isEmpty()) {
                Text(
                    text = stringResource(R.string.daily_challenges_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = White.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                challengeState.challenges.forEachIndexed { index, challenge ->
                    ChallengeRow(challenge = challenge)
                    if (index < challengeState.challenges.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            // Banner todos completados
            if (challengeState.allDailyCompleted) {
                Spacer(modifier = Modifier.height(12.dp))
                AllCompletedBanner()
            }

            // Desafio semanal
            challengeState.weeklyChallenge?.let { weekly ->
                Spacer(modifier = Modifier.height(12.dp))
                WeeklyChallengeSection(challenge = weekly)
            }
        }
    }
}

@Composable
private fun ChallengesHeader(date: String, allCompleted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.daily_challenges_title),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = NeonPurple.copy(alpha = 0.5f),
                    offset = Offset(0f, 0f),
                    blurRadius = 6f
                )
            ),
            color = White
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (allCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = if (date.isNotEmpty()) formatDisplayDate(date) else "",
                style = MaterialTheme.typography.labelSmall,
                color = if (allCompleted) NeonGreen else White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ChallengeRow(challenge: DailyChallenge) {
    val difficultyColor = difficultyColor(challenge.difficulty)
    val progressFraction = if (challenge.targetValue > 0) {
        (challenge.currentProgress.toFloat() / challenge.targetValue.toFloat()).coerceIn(0f, 1f)
    } else 0f

    var animatedProgress by remember(challenge.id) { mutableFloatStateOf(0f) }
    val progressAnimation by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "challenge_progress_${challenge.id}"
    )

    LaunchedEffect(progressFraction) {
        animatedProgress = progressFraction
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Badge de dificultad
            DifficultyBadge(difficulty = challenge.difficulty, color = difficultyColor)

            // Descripcion del desafio
            Text(
                text = challenge.resolveDescription(),
                style = MaterialTheme.typography.bodySmall,
                color = if (challenge.isCompleted) White.copy(alpha = 0.6f) else White.copy(alpha = 0.9f),
                modifier = Modifier.weight(1f),
                maxLines = 2
            )

            // Estado: check o recompensa
            if (challenge.isCompleted) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(NeonGreen.copy(alpha = 0.2f))
                        .border(1.dp, NeonGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.cd_completed),
                        tint = NeonGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else {
                RewardLabel(reward = challenge.reward)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Barra de progreso
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinearProgressIndicator(
                progress = { progressAnimation },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (challenge.isCompleted) NeonGreen else difficultyColor,
                trackColor = White.copy(alpha = 0.08f),
                strokeCap = StrokeCap.Round
            )

            Text(
                text = "${challenge.currentProgress}/${challenge.targetValue}",
                style = MaterialTheme.typography.labelSmall,
                color = White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: ChallengeDifficulty, color: Color) {
    val label = when (difficulty) {
        ChallengeDifficulty.EASY -> "F"
        ChallengeDifficulty.MEDIUM -> "M"
        ChallengeDifficulty.HARD -> "D"
        ChallengeDifficulty.WEEKLY -> "S"
    }

    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
private fun RewardLabel(reward: ChallengeReward) {
    Column(horizontalAlignment = Alignment.End) {
        if (reward.xp > 0) {
            Text(
                text = "+${reward.xp}XP",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = GradientPointsBottom
            )
        }
        if (reward.coins > 0) {
            Text(
                text = "+${reward.coins}🪙",
                style = MaterialTheme.typography.labelSmall,
                color = NeonYellow
            )
        }
    }
}

@Composable
private fun AllCompletedBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(NeonGreen.copy(alpha = 0.15f), NeonGreen.copy(alpha = 0.08f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(NeonGreen.copy(alpha = 0.5f), NeonGreen.copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.daily_challenges_all_completed),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = NeonGreen
            )
            Text(
                text = "+75XP +15🪙",
                style = MaterialTheme.typography.labelSmall,
                color = NeonGreen.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun WeeklyChallengeSection(challenge: DailyChallenge) {
    val settings = PrideButtonStyles.Settings.current()
    Column(modifier = Modifier.fillMaxWidth()) {
        // Separador con etiqueta semanal
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(NeonPurple.copy(alpha = 0.2f))
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(settings.top.copy(alpha = 0.3f), settings.bottom.copy(alpha = 0.3f))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(settings.top.copy(alpha = 0.6f), settings.bottom.copy(alpha = 0.6f))
                        ),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.daily_challenges_weekly_label),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = NeonPurple
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(NeonPurple.copy(alpha = 0.2f))
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Fila del desafio semanal
        ChallengeRow(challenge = challenge)
    }
}

// ─── Utilidades ─────────────────────────────────────────────────────────────

private fun difficultyColor(difficulty: ChallengeDifficulty): Color = when (difficulty) {
    ChallengeDifficulty.EASY -> NeonGreen
    ChallengeDifficulty.MEDIUM -> NeonOrange
    ChallengeDifficulty.HARD -> ResponseFail
    ChallengeDifficulty.WEEKLY -> NeonPurple
}

private fun formatDisplayDate(dateStr: String): String {
    // Entrada: "yyyy-MM-dd" → Salida: "16 abr"
    return try {
        val parts = dateStr.split("-")
        if (parts.size < 3) return dateStr
        val day = parts[2].trimStart('0').ifEmpty { "0" }
        val month = when (parts[1]) {
            "01" -> "ene"; "02" -> "feb"; "03" -> "mar"; "04" -> "abr"
            "05" -> "may"; "06" -> "jun"; "07" -> "jul"; "08" -> "ago"
            "09" -> "sep"; "10" -> "oct"; "11" -> "nov"; "12" -> "dic"
            else -> parts[1]
        }
        "$day $month"
    } catch (_: Exception) {
        dateStr
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "DailyChallengesCard - En progreso - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "DailyChallengesCard - En progreso - Dark")
@Composable
private fun DailyChallengesCardInProgressPreview() {
    PrideQuizTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DailyChallengesCard(
                challengeState = DailyChallengeState(
                    date = "2026-04-16",
                    challenges = listOf(
                        DailyChallenge(
                            id = "easy1",
                            type = ChallengeType.GAMES_PLAYED,
                            difficulty = ChallengeDifficulty.EASY,
                            descriptionKey = "games_played",
                            targetValue = 2,
                            currentProgress = 1,
                            isCompleted = false,
                            reward = ChallengeReward(30, 5)
                        ),
                        DailyChallenge(
                            id = "med1",
                            type = ChallengeType.TOTAL_CORRECT,
                            difficulty = ChallengeDifficulty.MEDIUM,
                            descriptionKey = "total_correct",
                            targetValue = 10,
                            currentProgress = 7,
                            isCompleted = false,
                            reward = ChallengeReward(60, 10)
                        ),
                        DailyChallenge(
                            id = "hard1",
                            type = ChallengeType.CORRECT_STREAK,
                            difficulty = ChallengeDifficulty.HARD,
                            descriptionKey = "correct_streak",
                            targetValue = 8,
                            currentProgress = 5,
                            isCompleted = false,
                            reward = ChallengeReward(100, 20)
                        )
                    ),
                    weeklyChallenge = DailyChallenge(
                        id = "weekly1",
                        type = ChallengeType.CUMULATIVE_SCORE,
                        difficulty = ChallengeDifficulty.WEEKLY,
                        descriptionKey = "cumulative_score",
                        targetValue = 5000,
                        currentProgress = 1850,
                        isCompleted = false,
                        reward = ChallengeReward(300, 50)
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "DailyChallengesCard - Todos completados")
@Composable
private fun DailyChallengesCardAllCompletePreview() {
    PrideQuizTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DailyChallengesCard(
                challengeState = DailyChallengeState(
                    date = "2026-04-16",
                    challenges = listOf(
                        DailyChallenge(
                            id = "e1",
                            type = ChallengeType.GAMES_PLAYED,
                            difficulty = ChallengeDifficulty.EASY,
                            descriptionKey = "games_played",
                            targetValue = 2,
                            currentProgress = 2,
                            isCompleted = true,
                            reward = ChallengeReward(30, 5)
                        ),
                        DailyChallenge(
                            id = "m1",
                            type = ChallengeType.TOTAL_CORRECT,
                            difficulty = ChallengeDifficulty.MEDIUM,
                            descriptionKey = "total_correct",
                            targetValue = 10,
                            currentProgress = 10,
                            isCompleted = true,
                            reward = ChallengeReward(60, 10)
                        ),
                        DailyChallenge(
                            id = "h1",
                            type = ChallengeType.CORRECT_STREAK,
                            difficulty = ChallengeDifficulty.HARD,
                            descriptionKey = "correct_streak",
                            targetValue = 8,
                            currentProgress = 8,
                            isCompleted = true,
                            reward = ChallengeReward(100, 20)
                        )
                    ),
                    allDailyCompleted = true
                )
            )
        }
    }
}
