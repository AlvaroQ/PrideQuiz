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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.quiz.pride.ui.components.ChallengeCompletionBanner
import com.quiz.pride.ui.components.ConfettiOverlay
import com.quiz.pride.ui.components.SaveScoreDialog
import com.quiz.pride.ui.components.StreakCelebrationDialog
import com.quiz.pride.ui.components.findActivity
import com.quiz.pride.ui.components.rememberInterstitialAdState
import java.util.Locale
import com.quiz.pride.ui.components.rememberRewardedAdState
import androidx.compose.ui.tooling.preview.Preview
import com.quiz.pride.ui.theme.PrideQuizTheme
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.ui.components.TrackScreenTime
import com.quiz.pride.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
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
    val analyticsManager: AnalyticsManager = koinInject()

    TrackScreenTime(AnalyticsManager.SCREEN_RESULT, analyticsManager)

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
    var pendingInterstitial by remember { mutableStateOf(false) }
    // Fix 2: displayedPoints viene del ViewModel (no local state) para que saveScore use el valor correcto
    val displayedPoints = uiState.displayedPoints.takeIf { it > 0 } ?: points
    // hasDoubledPoints: se determina comparando displayedPoints con el valor original
    val hasDoubledPoints = displayedPoints > points
    var isShowingAd by remember { mutableStateOf(false) }
    var showXpGain by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Check if new record
    val isNewRecord = displayedPoints > (uiState.personalRecord.toIntOrNull() ?: 0)
    val recordDifference = displayedPoints - (uiState.personalRecord.toIntOrNull() ?: 0)

    // Escuchar eventos del ViewModel (interstitial, save score result, etc.)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ResultEvent.ShowInterstitialAd -> {
                    val activity = context.findActivity()
                    if (interstitialAdState.isReady && !hasShownInterstitial && activity != null) {
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
                    } else if (!hasShownInterstitial) {
                        pendingInterstitial = true
                    }
                }
                // Fix 7: mostrar snackbar en caso de error al guardar el puntaje
                is ResultEvent.SaveScoreResult -> {
                    if (!event.success) {
                        launch {
                            snackbarHostState.showSnackbar(
                                message = event.message.ifEmpty { "Error al guardar el puntaje" },
                                actionLabel = "Reintentar"
                            )
                        }
                    }
                }
            }
        }
    }

    // Mostrar interstitial pendiente cuando el ad este listo (evita race condition)
    LaunchedEffect(interstitialAdState.isReady, pendingInterstitial) {
        if (pendingInterstitial && interstitialAdState.isReady && !hasShownInterstitial) {
            val activity = context.findActivity()
            if (activity != null) {
                hasShownInterstitial = true
                isShowingAd = true
                pendingInterstitial = false
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

    Box(modifier = Modifier.fillMaxSize()) {
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Espacio para la top row flotante (back + título) + status bar
                Spacer(modifier = Modifier.height(120.dp))

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
                    val xpResult = uiState.xpGainResult
                    ScoreDisplay(
                        points = displayedPoints,
                        isNewRecord = isNewRecord,
                        recordDifference = recordDifference,
                        xpGained = xpResult?.xpGained,
                        newLevel = if (xpResult?.leveledUp == true) xpResult.newLevel else null,
                        streakXpBonus = uiState.streakXpBonus,
                        coinsEarned = uiState.coinsEarned,
                        gemsEarned = uiState.gemsEarned,
                        showRewards = showXpGain
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Grid
                AnimatedVisibility(
                    visible = showStats,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    StatsGrid(
                        personalRecord = uiState.personalRecord,
                        worldRecord = uiState.worldRecord,
                        totalQuestions = totalQuestions,
                        correctAnswers = correctAnswers,
                        bestStreak = bestStreak,
                        timePlayed = timePlayed
                    )
                }

                // Banner de desafios completados durante la partida
                val challengeResult = uiState.challengeCompletionResult
                if (challengeResult != null && challengeResult.completedChallenges.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ChallengeCompletionBanner(
                        completionResult = challengeResult,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                                viewModel.onPointsDoubled()
                            } else {
                                val activity = context.findActivity()
                                if (activity != null && rewardedAdState.isReady) {
                                    isShowingAd = true
                                    rewardedAdState.showAd(
                                        activity = activity,
                                        onRewardEarned = {
                                            // Fix 2: delegar al ViewModel para que el score a guardar sea el correcto
                                            viewModel.onPointsDoubled()
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
                            viewModel.onShareClicked()
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
                Spacer(modifier = Modifier.height(24.dp))
            } // cierra Column (scroll)

            // Top row flotante: back button (solo flecha blanca) + título.
            // Mismo patrón visual que SelectGameScreen/Settings/Profile.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.result_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        shadow = Shadow(
                            color = NeonPink.copy(alpha = 0.6f),
                            offset = Offset(0f, 0f),
                            blurRadius = 12f
                        )
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start
                )
            }
        } // cierra AnimatedScreenBackground

        // Confetti celebratorio — se muestra en partida perfecta, nuevo record
        // o level up. Es un overlay no bloqueante que se desvanece en 2s.
        val isPerfect = totalQuestions > 0 && correctAnswers == totalQuestions
        val shouldCelebrate = showScore && (isPerfect || isNewRecord || uiState.showLevelUpDialog)
        if (shouldCelebrate) {
            ConfettiOverlay(modifier = Modifier.fillMaxSize())
        }

        // Snackbar host como overlay al fondo (antes venía del Scaffold)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    } // cierra Box(fillMaxSize)

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

    // Streak Celebration Dialog — se muestra cuando la racha cambia de estado
    // No se muestra para AlreadyPlayedToday (filtrado en el ViewModel)
    val streakResult = uiState.streakCheckResult
    if (uiState.showStreakDialog && streakResult != null) {
        StreakCelebrationDialog(
            streakCheckResult = streakResult,
            onDismiss = { viewModel.dismissStreakDialog() }
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
            style = MaterialTheme.typography.labelLarge,
            color = Color.Black
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
                    radius = 200f * pulseScale
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = points.toString(),
            color = GradientPointsBottom,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 72.sp,
                lineHeight = 80.sp,
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = GradientPointsTop,
                    offset = Offset(0f, 0f),
                    blurRadius = 20f
                )
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScoreDisplay(
    points: Int,
    isNewRecord: Boolean,
    recordDifference: Int,
    xpGained: Long? = null,
    newLevel: Int? = null,
    streakXpBonus: Int = 0,
    coinsEarned: Int = 0,
    gemsEarned: Int = 0,
    showRewards: Boolean = false
) {
    val hasRewards = showRewards && (
        xpGained != null ||
            streakXpBonus > 0 ||
            coinsEarned > 0 ||
            gemsEarned > 0
        )

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

                // Recompensas inline: XP, bonus de racha, monedas y gemas
                AnimatedVisibility(
                    visible = hasRewards,
                    enter = scaleIn() + fadeIn()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = NeonPink.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (xpGained != null) {
                                RewardChip(
                                    emoji = "\u2728",
                                    label = stringResource(R.string.xp_gained, xpGained.toInt()),
                                    accent = GradientPointsBottom
                                )
                            }
                            if (streakXpBonus > 0) {
                                RewardChip(
                                    emoji = "\uD83D\uDD25",
                                    label = stringResource(R.string.result_streak_xp_bonus, streakXpBonus),
                                    accent = NeonOrange
                                )
                            }
                            if (coinsEarned > 0) {
                                RewardChip(
                                    emoji = "\uD83E\uDE99",
                                    label = "+$coinsEarned",
                                    accent = NeonYellow
                                )
                            }
                            if (gemsEarned > 0) {
                                RewardChip(
                                    emoji = "\uD83D\uDC8E",
                                    label = "+$gemsEarned",
                                    accent = NeonBlue
                                )
                            }
                        }

                        if (newLevel != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.result_inline_level_up, newLevel),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    shadow = Shadow(
                                        color = NeonGreen.copy(alpha = 0.5f),
                                        offset = Offset(0f, 0f),
                                        blurRadius = 8f
                                    )
                                ),
                                color = NeonGreen,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardChip(
    emoji: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.45f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                shadow = Shadow(
                    color = accent.copy(alpha = 0.4f),
                    offset = Offset(0f, 0f),
                    blurRadius = 6f
                )
            ),
            color = accent
        )
    }
}

@Composable
private fun StatsGrid(
    personalRecord: String,
    worldRecord: String,
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
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                icon = rememberVectorPainter(Icons.Default.Star),
                label = stringResource(R.string.result_personal_best),
                value = personalRecord,
                color = NeonPink,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            StatCard(
                icon = painterResource(R.drawable.ic_emoji_events),
                label = stringResource(R.string.result_world_record),
                value = worldRecord,
                color = GradientPointsBottom,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            StatCard(
                icon = painterResource(R.drawable.ic_trending_up),
                label = stringResource(R.string.result_accuracy),
                value = "$accuracy%",
                color = if (accuracy >= 80) NeonGreen else if (accuracy >= 50) NeonYellow else NeonOrange,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                icon = painterResource(R.drawable.ic_local_fire_department),
                label = stringResource(R.string.result_best_streak),
                value = bestStreak.toString(),
                color = when {
                    bestStreak >= 10 -> NeonPurple
                    bestStreak >= 5 -> NeonOrange
                    else -> NeonYellow
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            StatCard(
                icon = rememberVectorPainter(Icons.Default.Star),
                label = stringResource(R.string.result_correct),
                value = "$correctAnswers/$totalQuestions",
                color = NeonGreen,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            StatCard(
                icon = painterResource(R.drawable.ic_timer),
                label = stringResource(R.string.result_time_played),
                value = timeFormatted,
                color = NeonPink,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
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
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .size(24.dp)
                    .drawBehind {
                        drawCircle(
                            color = color.copy(alpha = 0.2f),
                            radius = size.minDimension
                        )
                    }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        shadow = Shadow(
                            color = color.copy(alpha = 0.5f),
                            offset = Offset(0f, 0f),
                            blurRadius = 8f
                        )
                    ),
                    color = White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
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

private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}

// ============================================
// PREVIEWS
// ============================================

@Preview(showBackground = true, name = "ScoreDisplay - Con recompensas")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "ScoreDisplay - Con recompensas Dark")
@Composable
private fun ScoreDisplayPreview() {
    PrideQuizTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ScoreDisplay(
                points = 1250,
                isNewRecord = true,
                recordDifference = 270,
                xpGained = 320,
                newLevel = 5,
                streakXpBonus = 50,
                coinsEarned = 35,
                gemsEarned = 2,
                showRewards = true
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
                isNewRecord = false,
                recordDifference = -530,
                xpGained = 120,
                newLevel = null,
                streakXpBonus = 0,
                coinsEarned = 18,
                gemsEarned = 0,
                showRewards = true
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
                personalRecord = "980",
                worldRecord = "2400",
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

