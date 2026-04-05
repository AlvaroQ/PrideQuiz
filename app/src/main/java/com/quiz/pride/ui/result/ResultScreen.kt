package com.quiz.pride.ui.result

import android.content.Intent
import android.content.res.Configuration
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.pride.R
import com.quiz.pride.ui.components.AnimatedScreenBackground
import com.quiz.pride.ui.components.PrideButton
import com.quiz.pride.ui.components.PrideTopAppBar
import com.quiz.pride.ui.theme.DarkSurfaceVariant
import com.quiz.pride.ui.theme.GlowPink
import com.quiz.pride.ui.theme.GlowPurple
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
import com.quiz.pride.ui.theme.NeonBlue
import com.quiz.pride.ui.components.SaveScoreDialog
import com.quiz.pride.ui.components.findActivity
import com.quiz.pride.ui.components.rememberInterstitialAdState
import com.quiz.pride.ui.components.rememberRewardedAdState
import androidx.compose.ui.tooling.preview.Preview
import com.quiz.pride.ui.theme.PrideQuizTheme
import com.quiz.pride.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ResultScreen(
    points: Int,
    totalQuestions: Int = 0,
    correctAnswers: Int = 0,
    bestStreak: Int = 0,
    timePlayed: Long = 0,
    gameType: Constants.GameType = Constants.GameType.NORMAL,
    onNavigateToGame: () -> Unit,
    onNavigateToRanking: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ResultViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Animation states
    var showScore by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }
    var showNewRecord by remember { mutableStateOf(false) }

    // Ad states
    val interstitialAdState = rememberInterstitialAdState()
    val rewardedAdState = rememberRewardedAdState(
        adUnitId = context.getString(R.string.BONIFICADO_GAME_OVER)
    )
    var hasShownInterstitial by remember { mutableStateOf(false) }
    var displayedPoints by remember { mutableIntStateOf(points) }
    var hasDoubledPoints by remember { mutableStateOf(false) }
    var isShowingAd by remember { mutableStateOf(false) }
    var showXpGain by remember { mutableStateOf(false) }

    // Check if new record
    val isNewRecord = displayedPoints > (uiState.personalRecord.toIntOrNull() ?: 0)
    val recordDifference = displayedPoints - (uiState.personalRecord.toIntOrNull() ?: 0)

    // Escuchar eventos del ViewModel (interstitial, etc.)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ResultEvent.ShowInterstitialAd -> {
                    if (interstitialAdState.isReady && !hasShownInterstitial) {
                        val activity = context.findActivity()
                        if (activity != null) {
                            hasShownInterstitial = true
                            isShowingAd = true
                            interstitialAdState.showAd(
                                activity = activity,
                                onAdDismissed = {
                                    isShowingAd = false
                                    viewModel.onInterstitialShown()
                                },
                                onAdFailed = {
                                    isShowingAd = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Inicializar pantalla: delega toda la logica de negocio al ViewModel (idempotente)
    LaunchedEffect(Unit) {
        viewModel.onScreenInitialized(
            gameType = gameType,
            points = points,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            bestStreak = bestStreak,
            timePlayed = timePlayed
        )

        // Animaciones de entrada progresiva (responsabilidad exclusiva del composable)
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
        delay(300)
        showXpGain = true
    }

    Scaffold(
        topBar = {
            PrideTopAppBar(
                title = stringResource(R.string.result_title),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AnimatedScreenBackground(
                orbColor1 = NeonPink,
                orbColor2 = NeonPurple
            ) {
                // Orbs decorativos en composable hijo — la InfiniteTransition vive ahi
                // y solo ese composable se recompone cada frame (no toda la pantalla)
                ResultBackgroundOrbs()

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
                        points = displayedPoints,
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

                // XP Gained indicator
                AnimatedVisibility(
                    visible = showXpGain && uiState.xpGainResult != null,
                    enter = scaleIn() + fadeIn()
                ) {
                    uiState.xpGainResult?.let { xpResult ->
                        Spacer(modifier = Modifier.height(16.dp))
                        XpGainedBadge(
                            xpGained = xpResult.xpGained,
                            newLevel = if (xpResult.leveledUp) xpResult.newLevel else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                AnimatedVisibility(
                    visible = showButtons,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    ActionButtons(
                        points = displayedPoints,
                        hasDoubledPoints = hasDoubledPoints,
                        hasPaid = uiState.hasPaid,
                        isRewardedAdReady = rewardedAdState.isReady,
                        isRewardedAdLoading = rewardedAdState.isLoading,
                        onDoublePoints = {
                            if (uiState.hasPaid) {
                                // Pagadores duplican gratis, sin ver video
                                displayedPoints = displayedPoints * 2
                                hasDoubledPoints = true
                            } else {
                                val activity = context.findActivity()
                                if (activity != null && rewardedAdState.isReady) {
                                    isShowingAd = true
                                    rewardedAdState.showAd(
                                        activity = activity,
                                        onRewardEarned = {
                                            displayedPoints = displayedPoints * 2
                                            hasDoubledPoints = true
                                        },
                                        onAdDismissed = {
                                            isShowingAd = false
                                        },
                                        onAdFailed = {
                                            isShowingAd = false
                                        }
                                    )
                                }
                            }
                        },
                        onNavigateToGame = {
                            viewModel.onPlayAgainClicked()
                            onNavigateToGame()
                        },
                        onNavigateToRanking = {
                            viewModel.onRankingClicked()
                            onNavigateToRanking()
                        },
                        onShare = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    context.getString(R.string.share_message, displayedPoints)
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share)))
                        },
                        onRate = {
                            viewModel.onRateClicked()
                            com.quiz.pride.utils.rateApp(context)
                        }
                    )
                }
            }
            } // AnimatedScreenBackground
        }
    }

    // World Record Save Dialog
    if (uiState.showWorldRecordDialog) {
        SaveScoreDialog(
            score = uiState.worldRecordPoints,
            initialNickname = uiState.userProfile.nickname,
            initialImageBase64 = uiState.userProfile.imageBase64,
            isSaving = uiState.isSavingWorldRecord,
            onSave = { nickname, imageBase64 ->
                viewModel.saveScore(nickname, imageBase64)
            },
            onDismiss = {
                viewModel.onWorldRecordDialogDismissed()
            }
        )
    }

    // Timed Ranking Save Dialog
    if (uiState.showTimedRankingDialog) {
        SaveScoreDialog(
            score = uiState.timedScore,
            initialNickname = uiState.userProfile.nickname,
            initialImageBase64 = uiState.userProfile.imageBase64,
            isSaving = uiState.isSavingTimedScore,
            onSave = { nickname, imageBase64 ->
                viewModel.saveTimedScore(nickname, imageBase64)
            },
            onDismiss = {
                viewModel.dismissTimedRankingDialog()
            }
        )
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
            painter = painterResource(R.drawable.ic_emoji_events),
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

/**
 * Orbs decorativos con animacion de pulso para ResultScreen.
 * La InfiniteTransition vive aqui adentro para que SOLO este composable
 * se recomponga cada frame — el resto de la pantalla queda estable.
 */
@Composable
private fun ResultBackgroundOrbs() {
    val infiniteTransition = rememberInfiniteTransition(label = "result_bg")
    val glowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "result_glow_offset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-70).dp, y = 60.dp + glowOffset.dp)
                .alpha(0.15f)
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
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = 120.dp - glowOffset.dp)
                .alpha(0.12f)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonPurple, Color.Transparent)
                        ),
                        radius = size.minDimension / 2
                    )
                }
        )
    }
}

/**
 * Glow circular pulsante alrededor del puntaje.
 * La InfiniteTransition vive aqui adentro — solo este composable se recompone
 * cada frame. El texto del puntaje, los records y el resto de ScoreDisplay
 * permanecen estables.
 */
@Composable
private fun PulsingScoreGlow(points: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "score_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
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
}

@Composable
private fun ScoreDisplay(
    points: Int,
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

                // Glow pulsante aislado en composable hijo — no recompone el resto del card
                PulsingScoreGlow(points = points)

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
                icon = rememberVectorPainter(Icons.Default.Star),
                label = stringResource(R.string.result_personal_best),
                value = personalRecord,
                accentColor = NeonPink,
                modifier = Modifier.weight(1f)
            )
            RecordBadge(
                icon = painterResource(R.drawable.ic_emoji_events),
                label = stringResource(R.string.result_world_record),
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
                icon = painterResource(R.drawable.ic_trending_up),
                label = stringResource(R.string.result_accuracy),
                value = "$accuracy%",
                color = if (accuracy >= 80) NeonGreen else if (accuracy >= 50) NeonYellow else NeonOrange,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = painterResource(R.drawable.ic_local_fire_department),
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
                icon = rememberVectorPainter(Icons.Default.Star),
                label = "Correct",
                value = "$correctAnswers/$totalQuestions",
                color = NeonGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = painterResource(R.drawable.ic_timer),
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
    icon: Painter,
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
                painter = icon,
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
    hasDoubledPoints: Boolean,
    hasPaid: Boolean,
    isRewardedAdReady: Boolean,
    isRewardedAdLoading: Boolean,
    onDoublePoints: () -> Unit,
    onNavigateToGame: () -> Unit,
    onNavigateToRanking: () -> Unit,
    onShare: () -> Unit,
    onRate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Double Points Button (only show if not already doubled)
        if (!hasDoubledPoints && points > 0) {
            DoublePointsButton(
                hasPaid = hasPaid,
                isAdReady = isRewardedAdReady,
                isAdLoading = isRewardedAdLoading,
                onClick = onDoublePoints
            )
        }

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
private fun DoublePointsButton(
    hasPaid: Boolean,
    isAdReady: Boolean,
    isAdLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "double_points")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        NeonBlue.copy(alpha = 0.15f),
                        NeonPurple.copy(alpha = 0.15f)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        NeonBlue.copy(alpha = glowAlpha),
                        NeonPurple.copy(alpha = glowAlpha)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = hasPaid || isAdReady || !isAdLoading) { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (!hasPaid && isAdLoading) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = NeonBlue
                )
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = GradientPointsBottom,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.double_points_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(
                            color = NeonBlue.copy(alpha = 0.5f),
                            offset = Offset(0f, 0f),
                            blurRadius = 8f
                        )
                    ),
                    color = White
                )
                Text(
                    text = stringResource(
                        if (hasPaid) R.string.double_points_subtitle_paid
                        else R.string.double_points_subtitle
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = GradientPointsBottom,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun RecordBadge(
    icon: Painter,
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
                    painter = icon,
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

// ============================================
// PREVIEWS
// ============================================

@Preview(showBackground = true, name = "ScoreDisplay - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "ScoreDisplay - Dark")
@Composable
private fun ScoreDisplayPreview() {
    PrideQuizTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ScoreDisplay(
                points = 1250,
                personalRecord = "980",
                worldRecord = "2400",
                isNewRecord = true,
                recordDifference = 270
            )
        }
    }
}

@Preview(showBackground = true, name = "ScoreDisplay - Sin Record")
@Composable
private fun ScoreDisplayNoRecordPreview() {
    PrideQuizTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ScoreDisplay(
                points = 450,
                personalRecord = "980",
                worldRecord = "2400",
                isNewRecord = false,
                recordDifference = -530
            )
        }
    }
}

@Preview(showBackground = true, name = "StatsGrid - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "StatsGrid - Dark")
@Composable
private fun StatsGridPreview() {
    PrideQuizTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            StatsGrid(
                totalQuestions = 20,
                correctAnswers = 16,
                bestStreak = 8,
                timePlayed = 185000L
            )
        }
    }
}

@Preview(showBackground = true, name = "StatCard - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "StatCard - Dark")
@Composable
private fun StatCardPreview() {
    PrideQuizTheme {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(140.dp)
        ) {
            StatCard(
                icon = rememberVectorPainter(Icons.Default.Star),
                label = "Precision",
                value = "80%",
                color = NeonGreen
            )
        }
    }
}

@Preview(showBackground = true, name = "XpGainedBadge - Con Level Up - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "XpGainedBadge - Con Level Up - Dark")
@Composable
private fun XpGainedBadgePreview() {
    PrideQuizTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            XpGainedBadge(
                xpGained = 320,
                newLevel = 5
            )
        }
    }
}

@Preview(showBackground = true, name = "XpGainedBadge - Sin Level Up")
@Composable
private fun XpGainedBadgeNoLevelPreview() {
    PrideQuizTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            XpGainedBadge(
                xpGained = 120,
                newLevel = null
            )
        }
    }
}

@Preview(showBackground = true, name = "RecordBadge - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "RecordBadge - Dark")
@Composable
private fun RecordBadgePreview() {
    PrideQuizTheme {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RecordBadge(
                icon = rememberVectorPainter(Icons.Default.Star),
                label = "Record Personal",
                value = "980",
                accentColor = NeonPink,
                modifier = Modifier.weight(1f)
            )
            RecordBadge(
                icon = painterResource(R.drawable.ic_emoji_events),
                label = "Record Mundial",
                value = "2400",
                accentColor = GradientPointsBottom,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun XpGainedBadge(
    xpGained: Long,
    newLevel: Int? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "xp_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        GradientPointsTop.copy(alpha = 0.15f),
                        GradientPointsBottom.copy(alpha = 0.15f)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        GradientPointsTop.copy(alpha = glowAlpha),
                        GradientPointsBottom.copy(alpha = glowAlpha)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = GradientPointsBottom,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "+$xpGained XP",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = GradientPointsTop.copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 8f
                    )
                ),
                color = GradientPointsBottom
            )

            if (newLevel != null) {
                Text(
                    text = "Level Up! Level $newLevel",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = NeonGreen
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = GradientPointsBottom,
            modifier = Modifier.size(28.dp)
        )
    }
}
