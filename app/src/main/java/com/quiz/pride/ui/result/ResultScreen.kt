package com.quiz.pride.ui.result

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.pride.R
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.ui.components.PrideButton
import com.quiz.pride.ui.components.PrideTopAppBar
import com.quiz.pride.ui.theme.DarkSurfaceVariant
import com.quiz.pride.ui.theme.GlowPink
import com.quiz.pride.ui.theme.GlowPurple
import com.quiz.pride.ui.theme.GradientBackgroundEnd
import com.quiz.pride.ui.theme.GradientBackgroundMid
import com.quiz.pride.ui.theme.GradientBackgroundStart
import com.quiz.pride.ui.theme.GradientPointsBottom
import com.quiz.pride.ui.theme.GradientPointsTop
import com.quiz.pride.ui.theme.LearnGradientBottom
import com.quiz.pride.ui.theme.LearnGradientTop
import com.quiz.pride.ui.theme.NeonGreen
import com.quiz.pride.ui.theme.NeonOrange
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.NeonYellow
import com.quiz.pride.ui.theme.SettingsGradientBottom
import com.quiz.pride.ui.theme.SettingsGradientTop
import com.quiz.pride.ui.theme.StartGradientBottom
import com.quiz.pride.ui.theme.StartGradientTop
import com.quiz.pride.ui.theme.White
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun ResultScreen(
    points: Int,
    totalQuestions: Int = 0,
    correctAnswers: Int = 0,
    bestStreak: Int = 0,
    timePlayed: Long = 0,
    onNavigateToGame: () -> Unit,
    onNavigateToRanking: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ResultViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Animation states
    var showScore by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }
    var showNewRecord by remember { mutableStateOf(false) }

    // Check if new record
    val isNewRecord = points > (uiState.personalRecord.toIntOrNull() ?: 0)
    val recordDifference = points - (uiState.personalRecord.toIntOrNull() ?: 0)

    LaunchedEffect(Unit) {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_RESULT)
        delay(300)
        showScore = true
        delay(500)
        showStats = true
        if (isNewRecord) {
            delay(300)
            showNewRecord = true
        }
        delay(400)
        showButtons = true
    }

    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "result_bg")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    // Pulse animation for score
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Scaffold(
        topBar = {
            PrideTopAppBar(
                title = stringResource(R.string.result_title),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GradientBackgroundStart,
                            GradientBackgroundMid,
                            GradientBackgroundEnd
                        )
                    )
                )
        ) {
            // Decorative glow orbs
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = (-60).dp, y = 80.dp + floatOffset.dp)
                    .alpha(0.3f)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(NeonPink, Color.Transparent)
                            ),
                            radius = size.minDimension / 2
                        )
                    }
            )

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 50.dp, y = 150.dp - floatOffset.dp)
                    .alpha(0.25f)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(NeonPurple, Color.Transparent)
                            ),
                            radius = size.minDimension / 2
                        )
                    }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // New Record Badge
                AnimatedVisibility(
                    visible = showNewRecord && isNewRecord,
                    enter = scaleIn() + fadeIn()
                ) {
                    NewRecordBadge()
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Score display with neon glow
                AnimatedVisibility(
                    visible = showScore,
                    enter = scaleIn() + fadeIn()
                ) {
                    ScoreDisplay(
                        points = points,
                        pulseScale = pulseScale,
                        personalRecord = uiState.personalRecord,
                        worldRecord = uiState.worldRecord,
                        isNewRecord = isNewRecord,
                        recordDifference = recordDifference
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Grid
                AnimatedVisibility(
                    visible = showStats,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    StatsGrid(
                        totalQuestions = totalQuestions,
                        correctAnswers = correctAnswers,
                        bestStreak = bestStreak,
                        timePlayed = timePlayed
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                AnimatedVisibility(
                    visible = showButtons,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    ActionButtons(
                        points = points,
                        onNavigateToGame = onNavigateToGame,
                        onNavigateToRanking = onNavigateToRanking,
                        onShare = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    context.getString(R.string.share_message, points)
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share)))
                        },
                        onRate = { viewModel.rateApp(context) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NewRecordBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "record_anim")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "record_scale"
    )

    Row(
        modifier = Modifier
            .scale(scale)
            .background(
                Brush.horizontalGradient(
                    listOf(GradientPointsTop, GradientPointsBottom)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.result_new_record),
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun ScoreDisplay(
    points: Int,
    pulseScale: Float,
    personalRecord: String,
    worldRecord: String,
    isNewRecord: Boolean,
    recordDifference: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main score card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            DarkSurfaceVariant,
                            DarkSurfaceVariant.copy(alpha = 0.95f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            NeonPink.copy(alpha = 0.5f),
                            NeonPurple.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.your_score),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        shadow = Shadow(
                            color = NeonPink.copy(alpha = 0.4f),
                            offset = Offset(0f, 0f),
                            blurRadius = 6f
                        )
                    ),
                    color = White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Big score with golden glow
                Box(
                    modifier = Modifier
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        GradientPointsTop.copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                ),
                                radius = 120f * pulseScale
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = points.toString(),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = GradientPointsBottom,
                        style = MaterialTheme.typography.displayLarge.copy(
                            shadow = Shadow(
                                color = GradientPointsTop,
                                offset = Offset(0f, 0f),
                                blurRadius = 16f
                            )
                        )
                    )
                }

                // Record comparison
                if (recordDifference != 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val comparisonText = if (isNewRecord) {
                        stringResource(R.string.result_comparison_better, recordDifference)
                    } else {
                        stringResource(R.string.result_comparison_close, -recordDifference)
                    }
                    val comparisonColor = if (isNewRecord) NeonGreen else NeonOrange

                    Text(
                        text = comparisonText,
                        style = MaterialTheme.typography.bodySmall,
                        color = comparisonColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Records row - balanced layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RecordBadge(
                icon = Icons.Default.Star,
                label = "Personal Best",
                value = personalRecord,
                accentColor = NeonPink,
                modifier = Modifier.weight(1f)
            )
            RecordBadge(
                icon = Icons.Default.EmojiEvents,
                label = "World Record",
                value = worldRecord,
                accentColor = GradientPointsBottom,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatsGrid(
    totalQuestions: Int,
    correctAnswers: Int,
    bestStreak: Int,
    timePlayed: Long
) {
    val accuracy = if (totalQuestions > 0) {
        (correctAnswers.toFloat() / totalQuestions.toFloat() * 100).toInt()
    } else 0

    val timeFormatted = formatTime(timePlayed)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Default.TrendingUp,
                label = stringResource(R.string.result_accuracy),
                value = "$accuracy%",
                color = if (accuracy >= 80) NeonGreen else if (accuracy >= 50) NeonYellow else NeonOrange,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.LocalFireDepartment,
                label = stringResource(R.string.result_best_streak),
                value = bestStreak.toString(),
                color = when {
                    bestStreak >= 10 -> NeonPurple
                    bestStreak >= 5 -> NeonOrange
                    else -> NeonYellow
                },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Default.Star,
                label = "Correct",
                value = "$correctAnswers/$totalQuestions",
                color = NeonGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.Timer,
                label = stringResource(R.string.result_time_played),
                value = timeFormatted,
                color = NeonPink,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .size(28.dp)
                    .drawBehind {
                        drawCircle(
                            color = color.copy(alpha = 0.2f),
                            radius = size.minDimension
                        )
                    }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = color.copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 8f
                    )
                ),
                color = White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActionButtons(
    points: Int,
    onNavigateToGame: () -> Unit,
    onNavigateToRanking: () -> Unit,
    onShare: () -> Unit,
    onRate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PrideButton(
            text = stringResource(R.string.play_again),
            onClick = onNavigateToGame,
            gradientColors = listOf(StartGradientTop, StartGradientBottom),
            glowColor = GlowPink,
            modifier = Modifier.fillMaxWidth()
        )

        PrideButton(
            text = stringResource(R.string.ranking),
            onClick = onNavigateToRanking,
            gradientColors = listOf(SettingsGradientTop, SettingsGradientBottom),
            glowColor = GlowPurple,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrideButton(
                text = stringResource(R.string.share),
                onClick = onShare,
                gradientColors = listOf(LearnGradientTop, LearnGradientBottom),
                glowColor = NeonGreen.copy(alpha = 0.4f),
                modifier = Modifier.weight(1f)
            )

            PrideButton(
                text = stringResource(R.string.rate),
                onClick = onRate,
                gradientColors = listOf(GradientPointsTop, GradientPointsBottom),
                glowColor = GradientPointsTop.copy(alpha = 0.4f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RecordBadge(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon with glow
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .drawBehind {
                        drawCircle(
                            color = accentColor.copy(alpha = 0.2f),
                            radius = size.minDimension * 0.8f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Label
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Value
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = accentColor.copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 8f
                    )
                ),
                color = White,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%d:%02d", minutes, seconds)
}
