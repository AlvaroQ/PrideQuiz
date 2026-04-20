package com.quiz.pride.ui.game

import android.app.Activity
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.activity.compose.BackHandler
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import android.content.res.Configuration
import com.quiz.domain.Name
import com.quiz.domain.Pride
import com.quiz.pride.R
import com.quiz.pride.ui.components.BannerAdView
import com.quiz.pride.ui.components.LoadingIndicator
import com.quiz.pride.ui.components.RewardedAdState
import com.quiz.pride.ui.components.findActivity
import com.quiz.pride.ui.components.rememberRewardedAdState
import com.quiz.pride.ui.theme.AuroraWashBlue
import com.quiz.pride.ui.theme.AuroraWashPink
import com.quiz.pride.ui.theme.AuroraWashViolet
import com.quiz.pride.ui.theme.GlowBlue
import com.quiz.pride.ui.theme.GlowPink
import com.quiz.pride.ui.theme.GradientGameBottom
import com.quiz.pride.ui.theme.GradientGameTop
import com.quiz.pride.ui.theme.GradientPointsBottom
import com.quiz.pride.ui.theme.GradientPositionTop
import com.quiz.pride.ui.theme.IridescentColors
import com.quiz.pride.ui.theme.NeonBlue
import com.quiz.pride.ui.theme.NeonGreen
import com.quiz.pride.ui.theme.NeonGreenDim
import com.quiz.pride.ui.theme.NeonOrange
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPinkDim
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.NeonYellow
import com.quiz.pride.ui.theme.PrideRed
import com.quiz.pride.ui.theme.RainbowProgressFill
import com.quiz.pride.ui.theme.ResponseCorrect
import com.quiz.pride.ui.theme.ResponseFail
import com.quiz.pride.ui.theme.SpectrumSurface
import com.quiz.pride.ui.theme.SpectrumSurfaceElevated
import com.quiz.pride.ui.theme.SpectrumSurfaceLightElevated
import com.quiz.pride.ui.theme.OffBlackInk
import com.quiz.pride.ui.theme.OffWhiteInk
import com.quiz.pride.ui.theme.White
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.ui.components.TrackScreenTime
import com.quiz.pride.utils.Constants
import androidx.compose.ui.tooling.preview.Preview
import com.quiz.pride.ui.theme.PrideQuizTheme
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel

@Composable
fun GameScreen(
    gameType: Constants.GameType,
    onNavigateToResult: (Int, Int, Int, Int, Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val analyticsManager: AnalyticsManager = koinInject()

    TrackScreenTime(AnalyticsManager.SCREEN_GAME, analyticsManager)
    val soundEnabled by viewModel.isSoundEnabled.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    // Rewarded Ad state
    val rewardedAdState = rememberRewardedAdState()

    // Streak strings (resolved from composable context for ViewModel)
    val streakOnFire = stringResource(R.string.streak_on_fire)
    val streakUnstoppable = stringResource(R.string.streak_unstoppable)
    val streakLegendary = stringResource(R.string.streak_legendary)
    val streakCombo = context.getString(R.string.streak_combo)

    // Initialize game once
    LaunchedEffect(gameType) {
        viewModel.initGame(gameType)
    }

    // Handle back button press
    BackHandler {
        viewModel.showExitDialog()
    }

    // SoundPool compartido para sonidos cortos — evita multiples MediaPlayer activos
    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    // Carga de sonidos (asincrona, listos antes de la primera respuesta del usuario)
    var successSoundReady by remember { mutableStateOf(false) }
    var failSoundReady by remember { mutableStateOf(false) }

    val successSoundId = remember { soundPool.load(context, R.raw.success, 1) }
    val failSoundId = remember { soundPool.load(context, R.raw.fail, 1) }

    DisposableEffect(soundPool) {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                when (sampleId) {
                    successSoundId -> successSoundReady = true
                    failSoundId -> failSoundReady = true
                }
            }
        }
        onDispose {
            soundPool.release()
        }
    }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GameEvent.NavigateToResult -> {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToResult(event.points, event.stage, event.correctAnswers, event.bestStreak, event.timePlayed)
                }
                is GameEvent.PlaySuccessSound -> {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (soundEnabled && successSoundReady) {
                        try {
                            soundPool.play(successSoundId, 1f, 1f, 1, 0, 1f)
                        } catch (_: Exception) {}
                    }
                }
                is GameEvent.PlayFailSound -> {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (soundEnabled && failSoundReady) {
                        try {
                            soundPool.play(failSoundId, 1f, 1f, 1, 0, 1f)
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    // Detect theme using MaterialTheme colors
    val colorScheme = MaterialTheme.colorScheme
    // remember evita recalcular luminance() en cada recomposicion
    val isDarkTheme = remember(colorScheme.background) {
        colorScheme.background.luminance() < 0.5f
    }

    // Force light status bar icons on dark backgrounds so they're visible
    val activity = context as? Activity
    LaunchedEffect(isDarkTheme) {
        activity?.window?.let { window ->
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        topBar = {
            EnhancedGameTopBar(
                points = uiState.points,
                lives = uiState.lives,
                stage = uiState.stage,
                totalStages = Constants.TOTAL_PRIDES,
                currentStreak = uiState.currentStreak,
                isTimedMode = uiState.isTimedMode,
                timeRemaining = uiState.timeRemaining,
                onBackClick = { viewModel.showExitDialog() }
            )
        },
        bottomBar = {
            if (uiState.showBannerAd) {
                BannerAdView(
                    adUnitId = stringResource(R.string.BANNER_GAME)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Streak effect overlay
            AnimatedVisibility(
                visible = uiState.showStreakEffect,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                StreakEffectOverlay(
                    streak = uiState.currentStreak,
                    message = uiState.streakMessage
                )
            }

            when {
                uiState.hasError -> GameErrorState(
                    message = stringResource(R.string.error_loading_question),
                    onRetry = { viewModel.retryCurrentStage() }
                )
                else -> {
                    // Crossfade suave entre fases: evita el parpadeo del LoadingIndicator
                    // cuando los datos de la siguiente pregunta ya estan en memoria.
                    // Solo muestra LoadingIndicator en la primera carga (question == null).
                    AnimatedContent(
                        targetState = uiState.question,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith
                                fadeOut(animationSpec = tween(180))
                        },
                        contentKey = { it?.name?.EN ?: "loading" },
                        label = "question_transition"
                    ) { question ->
                        if (question == null) {
                            LoadingIndicator()
                        } else {
                            // Snapshot de options/correctIndex ligado a ESTA pregunta:
                            // evita que el slot en fade-out muestre datos de la siguiente.
                            val options = remember(question) { uiState.options }
                            val correctOptionIndex = remember(question) { uiState.correctOptionIndex }
                            GameContent(
                                gameType = gameType,
                                question = question,
                                options = options,
                                correctOptionIndex = correctOptionIndex,
                                selectedAnswer = uiState.selectedAnswer,
                                isDarkTheme = isDarkTheme,
                                onAnswerSelected = { index ->
                                    viewModel.onAnswerSelected(index, streakOnFire, streakUnstoppable, streakLegendary, streakCombo)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Exit Confirmation Dialog
    if (uiState.showExitDialog) {
        ExitConfirmationDialog(
            onStay = { viewModel.dismissExitDialog() },
            onLeave = {
                viewModel.dismissExitDialog()
                onNavigateBack()
            }
        )
    }

    // Extra Life Dialog
    if (uiState.showExtraLifeDialog) {
        ExtraLifeDialog(
            extraLivesRemaining = uiState.maxExtraLives - uiState.extraLivesUsed,
            isAdReady = rewardedAdState.isReady,
            isAdLoading = rewardedAdState.isLoading,
            onAccept = {
                val activity = context.findActivity()
                if (activity != null && rewardedAdState.isReady) {
                    rewardedAdState.showAd(
                        activity = activity,
                        onRewardEarned = { viewModel.onExtraLifeAccepted() },
                        onAdDismissed = { },
                        onAdFailed = { _ -> viewModel.onExtraLifeAccepted() }
                    )
                } else {
                    viewModel.onExtraLifeAccepted()
                }
            },
            onDecline = { viewModel.onExtraLifeDeclined() }
        )
    }
}

@Composable
private fun EnhancedGameTopBar(
    points: Int,
    lives: Int,
    stage: Int,
    totalStages: Int,
    currentStreak: Int,
    isTimedMode: Boolean = false,
    timeRemaining: Int = 0,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = stage.toFloat() / totalStages.toFloat()
    val progressDescription = stringResource(R.string.accessibility_progress, stage, totalStages)
    val livesDescription = stringResource(R.string.accessibility_lives_remaining, lives)
    val scoreDescription = stringResource(R.string.accessibility_current_score, points)
    val backContentDescription = stringResource(R.string.cd_back)
    val pointsText = stringResource(R.string.points_suffix, points)

    val colorScheme = MaterialTheme.colorScheme
    val isDark = remember(colorScheme.background) { colorScheme.background.luminance() < 0.5f }
    val onTopBar = colorScheme.onSurface

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Status bar area — unificado con el resto de pantallas usando surfaceVariant.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.surfaceVariant)
                .windowInsetsPadding(WindowInsets.statusBars)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.surfaceVariant)
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.semantics { contentDescription = backContentDescription }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = onTopBar
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Points — clean, no glow, editorial
            Text(
                text = pointsText,
                color = GradientPointsBottom,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = scoreDescription }
            )

            // Streak indicator
            if (currentStreak >= 3) {
                StreakIndicator(streak = currentStreak)
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Timer for timed mode OR Lives indicator for normal modes
            if (isTimedMode) {
                TimerIndicator(timeRemaining = timeRemaining)
            } else {
                AnimatedLivesIndicator(
                    currentLives = lives,
                    maxLives = 3,
                    modifier = Modifier.semantics { contentDescription = livesDescription }
                )
            }
        }

        // Rainbow progress capsule — the ONE place where full pride spectrum appears
        RainbowProgressCapsule(
            progress = progress,
            label = "$stage / $totalStages",
            isDark = isDark,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 10.dp)
                .semantics { contentDescription = progressDescription }
        )
        }
    }
}

/**
 * Capsule progress bar con fill rainbow pride y shimmer sweep.
 * El label de progreso se muestra centrado SOBRE la capsule.
 */
@Composable
private fun RainbowProgressCapsule(
    progress: Float,
    label: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "progress_fill"
    )

    // Shimmer sweep across the fill
    val infiniteTransition = rememberInfiniteTransition(label = "progress_shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_sweep"
    )

    val capsuleHeight = 22.dp
    val trackColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)

    Box(
        modifier = modifier
            .height(capsuleHeight)
            .clip(RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        // Track background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(trackColor, RoundedCornerShape(11.dp))
        )

        // Rainbow fill with shimmer
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(11.dp))
                .drawBehind {
                    // Rainbow gradient fill
                    drawRect(
                        brush = Brush.horizontalGradient(RainbowProgressFill)
                    )
                    // Shimmer sweep overlay
                    val shimmerWidth = size.width * 0.3f
                    val shimmerX = size.width * shimmerOffset
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            startX = shimmerX - shimmerWidth,
                            endX = shimmerX + shimmerWidth
                        )
                    )
                }
        )

        // Label centered on the full capsule
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.5.sp
            ),
            color = labelColor,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun TimerIndicator(
    timeRemaining: Int,
    modifier: Modifier = Modifier
) {
    val totalTime = Constants.TIMED_MODE_TOTAL_SECONDS
    val progress = timeRemaining.toFloat() / totalTime.toFloat()

    // Precalcular isUrgent para usar como key del remember, evitando reinicios innecesarios
    val isUrgent = timeRemaining <= 30

    // Solo se anima cuando es urgente; si no, la escala es fija en 1f
    val scale = if (isUrgent) {
        val transition = rememberInfiniteTransition(label = "timer_urgent")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "timer_scale"
        ).value
    } else {
        1f
    }

    val color = when {
        isUrgent -> PrideRed
        timeRemaining <= 60 -> NeonOrange
        else -> NeonBlue
    }

    // Format time as MM:SS
    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timeText = String.format("%d:%02d", minutes, seconds)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_timer),
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(20.dp)
                .scale(scale)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelLarge,
                color = color,
                modifier = Modifier.scale(scale)
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.3f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun StreakIndicator(
    streak: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        streak >= 15 -> NeonPurple
        streak >= 10 -> NeonOrange
        streak >= 5 -> NeonYellow
        else -> NeonGreen
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Icono animado en composable hijo aislado: solo el hijo recompone cada frame
        StreakFireIcon(color = color, animate = streak >= 3)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$streak",
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

/**
 * Icono de fuego aislado con su propia InfiniteTransition.
 * Solo se anima cuando el streak es relevante (>= 3).
 * Al estar en un composable hijo, la recomposicion por animacion
 * queda confinada a este nodo y no afecta al StreakIndicator padre.
 */
@Composable
private fun StreakFireIcon(color: Color, animate: Boolean) {
    val scale: Float = if (animate) {
        val infiniteTransition = rememberInfiniteTransition(label = "streak_fire")
        val animScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "streak_scale"
        )
        animScale
    } else {
        1f
    }
    Icon(
        painter = painterResource(id = R.drawable.ic_local_fire_department),
        contentDescription = null,
        tint = color,
        modifier = Modifier
            .size(20.dp)
            .scale(scale)
    )
}

/**
 * Icono de corazon aislado con su propia InfiniteTransition.
 * Al extraerlo, cada corazon es un composable independiente y la animacion
 * infinita no fuerza la recomposicion del Row completo.
 */
@Composable
private fun HeartIcon(index: Int, isAlive: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (isAlive) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "heart_scale"
    )

    // Solo crear InfiniteTransition si el corazon esta vivo; si esta muerto no hay animacion
    val heartBeat: Float = if (isAlive) {
        val infiniteTransition = rememberInfiniteTransition(label = "heart_beat_$index")
        val beat by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = index * 100),
                repeatMode = RepeatMode.Reverse
            ),
            label = "heart_beat"
        )
        beat
    } else {
        1f
    }

    Icon(
        imageVector = if (isAlive) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
        contentDescription = null,
        tint = if (isAlive) PrideRed else White.copy(alpha = 0.3f),
        modifier = Modifier
            .size(32.dp)
            .scale(scale * heartBeat)
            .drawBehind {
                if (isAlive) {
                    drawCircle(
                        color = PrideRed.copy(alpha = 0.2f),
                        radius = size.minDimension * 0.55f
                    )
                }
            }
    )
}

@Composable
private fun AnimatedLivesIndicator(
    currentLives: Int,
    maxLives: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(maxLives) { index ->
            HeartIcon(index = index, isAlive = index < currentLives)
        }
    }
}

@Composable
private fun StreakEffectOverlay(
    streak: Int,
    message: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak_effect")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "effect_scale"
    )

    val color = when {
        streak >= 15 -> NeonPurple
        streak >= 10 -> NeonOrange
        streak >= 5 -> NeonYellow
        else -> NeonGreen
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale)
                .background(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_local_fire_department),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = color,
                style = MaterialTheme.typography.headlineSmall.copy(
                    shadow = Shadow(
                        color = color,
                        offset = Offset(0f, 0f),
                        blurRadius = 16f
                    )
                )
            )
        }
    }
}

@Composable
private fun GameContent(
    gameType: Constants.GameType,
    question: Pride?,
    options: List<Pride>,
    correctOptionIndex: Int,
    selectedAnswer: Int?,
    isDarkTheme: Boolean,
    onAnswerSelected: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val questionTextColor = colorScheme.onSurface

    // Iridescent border alpha — softer in light mode
    val iridescentBorderColors = remember(isDarkTheme) {
        IridescentColors.map { it.copy(alpha = if (isDarkTheme) 0.5f else 0.35f) }
    }

    // Question box surface — warm glass with spotlight
    val questionBoxBg = remember(isDarkTheme) {
        if (isDarkTheme) {
            Brush.radialGradient(
                colors = listOf(SpectrumSurfaceElevated, SpectrumSurface),
                radius = 600f
            )
        } else {
            Brush.radialGradient(
                colors = listOf(Color.White, SpectrumSurfaceLightElevated),
                radius = 600f
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Question area (35%) — "the stage"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .drawBehind {
                    // Chromatic shadow — iridescent glow below the card
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                AuroraWashPink.copy(alpha = 0.4f),
                                AuroraWashViolet.copy(alpha = 0.3f),
                                AuroraWashBlue.copy(alpha = 0.3f)
                            )
                        ),
                        topLeft = androidx.compose.ui.geometry.Offset(8.dp.toPx(), 6.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(size.width - 16.dp.toPx(), size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
                    )
                }
                .clip(RoundedCornerShape(24.dp))
                .background(questionBoxBg)
                .border(
                    width = 1.5.dp,
                    brush = Brush.sweepGradient(iridescentBorderColors),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            when (gameType) {
                Constants.GameType.NORMAL -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            GlowPink.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.minDimension / 1.3f
                                )
                            }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(question?.flag)
                                .crossfade(200)
                                .size(Size(600, 400))
                                .build(),
                            contentDescription = stringResource(R.string.game_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Constants.GameType.ADVANCE -> {
                    Text(
                        text = question?.description?.EN ?: "",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            lineHeight = 26.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = questionTextColor
                    )
                }
                Constants.GameType.TIMED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            GlowBlue.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.minDimension / 1.3f
                                )
                            }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(question?.flag)
                                .crossfade(200)
                                .size(Size(600, 400))
                                .build(),
                            contentDescription = stringResource(R.string.game_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Answer options — 4 full-width stacked buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f)
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) { index ->
                VibrantAnswerButton(
                    option = options.getOrNull(index),
                    gameType = gameType,
                    isSelected = selectedAnswer == index,
                    isCorrect = selectedAnswer != null && correctOptionIndex == index,
                    isWrong = selectedAnswer == index && correctOptionIndex != index,
                    enabled = selectedAnswer == null,
                    buttonIndex = index,
                    isDarkTheme = isDarkTheme,
                    onClick = { onAnswerSelected(index) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

/**
 * Spectrum Answer Button — editorial chromatic design.
 *
 * Each button owns a color from the pride spectrum. The rainbow isn't decoration —
 * it's the DIVERSITY of the four buttons together forming the spectrum.
 *
 * Design layers (bottom to top):
 * 1. Chromatic drop shadow (button's accent color, offset down)
 * 2. Warm glass surface with subtle accent tint
 * 3. Iridescent border (thin, refined — intensifies on interaction)
 * 4. Glass highlight (top → transparent → subtle shadow)
 * 5. Left accent strip with option label (A/B/C/D)
 * 6. Content (text or flag image)
 * 7. Feedback icon overlay (check/cross on correct/wrong)
 */
@Composable
private fun VibrantAnswerButton(
    option: Pride?,
    gameType: Constants.GameType,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    enabled: Boolean,
    buttonIndex: Int,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val answerDescription = stringResource(R.string.accessibility_answer_button, buttonIndex + 1)
    val correctDescription = stringResource(R.string.accessibility_correct_answer)
    val wrongDescription = stringResource(R.string.accessibility_wrong_answer)

    val semanticsDescription = when {
        isCorrect -> "$answerDescription. $correctDescription"
        isWrong -> "$answerDescription. $wrongDescription"
        else -> answerDescription
    }

    // --- Neutral accent — se aclara u oscurece con el tema (como los textos) ---
    // Reemplaza la identidad cromática por botón (rosa/azul/violeta/verde)
    // por un tono neutro que sigue a onSurface del tema activo.
    val accentColor = if (isDarkTheme) {
        colorScheme.onSurface.copy(alpha = 0.85f)
    } else {
        colorScheme.onSurface.copy(alpha = 0.7f)
    }

    val optionLabel = when (buttonIndex) {
        0 -> "A"; 1 -> "B"; 2 -> "C"; 3 -> "D"; else -> "A"
    }

    // --- Animations ---
    val scale by animateFloatAsState(
        targetValue = when {
            isCorrect -> 1.02f
            isWrong -> 0.97f
            isSelected -> 0.96f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "answer_scale"
    )

    val borderAlpha by animateFloatAsState(
        targetValue = when {
            isCorrect || isWrong -> 1f
            isSelected -> 0.9f
            else -> 0.35f
        },
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "border_alpha"
    )

    val borderWidth by animateFloatAsState(
        targetValue = when {
            isCorrect || isWrong -> 2.5f
            isSelected -> 2f
            else -> 1f
        },
        animationSpec = tween(300),
        label = "border_width"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = when {
            isCorrect -> 0.5f
            isWrong -> 0.35f
            isSelected -> 0.3f
            else -> 0.1f
        },
        animationSpec = tween(400),
        label = "glow_alpha"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (!enabled && !isCorrect && !isWrong) 0.5f else 1f,
        animationSpec = tween(300),
        label = "content_alpha"
    )

    // --- Border color logic ---
    val borderColor by animateColorAsState(
        targetValue = when {
            isCorrect -> ResponseCorrect
            isWrong -> ResponseFail
            isSelected -> accentColor
            else -> accentColor.copy(alpha = borderAlpha)
        },
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "answer_border"
    )

    // --- Surface background ---
    val surfaceBg = when {
        isCorrect -> if (isDarkTheme) ResponseCorrect.copy(alpha = 0.2f) else ResponseCorrect.copy(alpha = 0.15f)
        isWrong -> if (isDarkTheme) ResponseFail.copy(alpha = 0.15f) else ResponseFail.copy(alpha = 0.1f)
        isSelected -> accentColor.copy(alpha = if (isDarkTheme) 0.2f else 0.15f)
        else -> Color.Transparent
    }
    val animatedSurfaceBg by animateColorAsState(
        targetValue = surfaceBg,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "surface_bg"
    )

    // --- Text color ---
    val textColor = when {
        isCorrect -> if (isDarkTheme) White else Color(0xFF065F46)
        isWrong -> if (isDarkTheme) White else Color(0xFF991B1B)
        isDarkTheme -> White
        else -> colorScheme.onSurface
    }

    val buttonShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .clip(buttonShape)
            .drawBehind {
                // Sombra neutra — negro sutil, no cromática
                val shadowColor = when {
                    isCorrect -> ResponseCorrect.copy(alpha = glowAlpha)
                    isWrong -> ResponseFail.copy(alpha = glowAlpha)
                    else -> Color.Black.copy(alpha = glowAlpha * 0.6f)
                }
                drawRoundRect(
                    color = shadowColor,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, 3.dp.toPx()),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
                )
            }
            .semantics { contentDescription = semanticsDescription }
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
    ) {
        // Layer 1: Superficie neutra (white en light, SpectrumSurface en dark)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = if (isDarkTheme) SpectrumSurface else Color.White,
                    shape = buttonShape
                )
                .background(animatedSurfaceBg, buttonShape)
                .border(
                    width = borderWidth.dp,
                    color = borderColor,
                    shape = buttonShape
                )
        ) {
            // Layer 2: Glass highlight — subtle top shine
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDarkTheme) 0.07f else 0.4f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.03f)
                            )
                        ),
                        shape = buttonShape
                    )
            )

            // Layer 3: Left accent strip with option label
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(32.dp)
                    .align(Alignment.CenterStart)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = if (isDarkTheme) 0.25f else 0.15f),
                                accentColor.copy(alpha = if (isDarkTheme) 0.12f else 0.08f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = optionLabel,
                    color = if (isDarkTheme) accentColor else accentColor.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }

            // Layer 4: Content area (offset right to account for accent strip)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 36.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                    .alpha(contentAlpha),
                contentAlignment = Alignment.Center
            ) {
                when (gameType) {
                    Constants.GameType.NORMAL, Constants.GameType.TIMED -> {
                        Text(
                            text = option?.name?.EN ?: "",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.3.sp
                            ),
                            textAlign = TextAlign.Center,
                            color = textColor
                        )
                    }
                    Constants.GameType.ADVANCE -> {
                        AsyncImage(
                            model = option?.flag,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(if (isWrong && !isCorrect) 0.6f else 1f),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // Layer 5: Feedback icon (check/cross) — uses icon + color, not just color
            AnimatedVisibility(
                visible = isCorrect || isWrong,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(200)),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .size(26.dp)
                        .background(
                            color = if (isCorrect) ResponseCorrect else ResponseFail,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCorrect) "✓" else "✗",
                        color = OffWhiteInk,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ExitConfirmationDialog(
    onStay: () -> Unit,
    onLeave: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = colorScheme.background.luminance() < 0.5f
    // Botones con tono neon en dark y variante dim (mas saturada/oscura) en light,
    // para mantener identidad pride pero ganar contraste sobre surfaces claros.
    val leaveColor = if (isDarkTheme) NeonPink else NeonPinkDim
    val stayColor = if (isDarkTheme) NeonGreen else NeonGreenDim
    val titleShadowColor = if (isDarkTheme) NeonPink.copy(alpha = 0.5f)
                           else NeonPinkDim.copy(alpha = 0.25f)

    AlertDialog(
        onDismissRequest = onStay,
        containerColor = colorScheme.surface,
        titleContentColor = colorScheme.onSurface,
        textContentColor = colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = stringResource(R.string.exit_game_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    shadow = Shadow(
                        color = titleShadowColor,
                        offset = Offset(0f, 0f),
                        blurRadius = 8f
                    )
                )
            )
        },
        text = {
            Text(
                text = stringResource(R.string.exit_game_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onStay) {
                Text(
                    text = stringResource(R.string.exit_game_stay),
                    style = MaterialTheme.typography.labelLarge,
                    color = stayColor
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onLeave) {
                Text(
                    text = stringResource(R.string.exit_game_leave),
                    style = MaterialTheme.typography.labelLarge,
                    color = leaveColor
                )
            }
        }
    )
}

/**
 * Corazon pulsante aislado para el dialog de vida extra.
 * La InfiniteTransition vive aqui y solo recompone este icono,
 * no el contenido del AlertDialog completo.
 */
@Composable
private fun PulsingHeartIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "heart_pulse")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_scale"
    )
    Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = null,
        tint = PrideRed,
        modifier = Modifier
            .size(48.dp)
            .scale(heartScale)
    )
}

@Composable
private fun ExtraLifeDialog(
    extraLivesRemaining: Int,
    isAdReady: Boolean,
    isAdLoading: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = { /* Cannot dismiss */ },
        containerColor = colorScheme.surface,
        titleContentColor = colorScheme.onSurface,
        textContentColor = colorScheme.onSurfaceVariant,
        icon = {
            // Icono animado en composable hijo — no recompone el resto del dialog
            PulsingHeartIcon()
        },
        title = {
            Text(
                text = stringResource(R.string.dialog_extra_life_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = NeonPink.copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 8f
                    )
                )
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.dialog_extra_life_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Show remaining extra lives
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = NeonPurple.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    repeat(extraLivesRemaining) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = PrideRed,
                            modifier = Modifier.size(20.dp)
                        )
                        if (it < extraLivesRemaining - 1) {
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.extra_lives_remaining, extraLivesRemaining),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                // Loading indicator for ad
                if (isAdLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = NeonBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.loading_ad),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onAccept,
                enabled = isAdReady || !isAdLoading
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isAdReady || !isAdLoading) NeonGreen else colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.watch_ad),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isAdReady || !isAdLoading) NeonGreen else colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(
                    text = stringResource(R.string.dialog_extra_life_btn_no),
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonPink
                )
            }
        }
    )
}

// ============================================
// PREVIEWS
// ============================================

private val fakePride = Pride(
    name = Name(ES = "Bandera Arcoiris", EN = "Rainbow Flag"),
    description = Name(
        ES = "Simbolo internacional del movimiento LGBTQ+",
        EN = "International symbol of the LGBTQ+ movement"
    ),
    flag = ""
)

private val fakePrideOptions = listOf(
    Pride(name = Name(EN = "Rainbow Flag")),
    Pride(name = Name(EN = "Bisexual Pride")),
    Pride(name = Name(EN = "Trans Pride")),
    Pride(name = Name(EN = "Non-Binary Pride"))
)

@Preview(showBackground = true, name = "GameContent - Normal - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "GameContent - Normal - Dark")
@Composable
private fun GameContentNormalPreview() {
    PrideQuizTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(GradientGameTop, GradientGameBottom))
                )
        ) {
            GameContent(
                gameType = Constants.GameType.NORMAL,
                question = fakePride,
                options = fakePrideOptions,
                correctOptionIndex = 0,
                selectedAnswer = null,
                isDarkTheme = true,
                onAnswerSelected = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "GameContent - Respuesta Correcta")
@Composable
private fun GameContentCorrectAnswerPreview() {
    PrideQuizTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(GradientGameTop, GradientGameBottom))
                )
        ) {
            GameContent(
                gameType = Constants.GameType.NORMAL,
                question = fakePride,
                options = fakePrideOptions,
                correctOptionIndex = 0,
                selectedAnswer = 0,
                isDarkTheme = true,
                onAnswerSelected = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "GameContent - Respuesta Incorrecta")
@Composable
private fun GameContentWrongAnswerPreview() {
    PrideQuizTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(GradientGameTop, GradientGameBottom))
                )
        ) {
            GameContent(
                gameType = Constants.GameType.ADVANCE,
                question = fakePride,
                options = fakePrideOptions,
                correctOptionIndex = 0,
                selectedAnswer = 2,
                isDarkTheme = true,
                onAnswerSelected = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "EnhancedGameTopBar - Normal - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "EnhancedGameTopBar - Normal - Dark")
@Composable
private fun EnhancedGameTopBarNormalPreview() {
    PrideQuizTheme {
        EnhancedGameTopBar(
            points = 850,
            lives = 2,
            stage = 7,
            totalStages = 20,
            currentStreak = 5,
            isTimedMode = false,
            timeRemaining = 0,
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "EnhancedGameTopBar - Timed Mode")
@Composable
private fun EnhancedGameTopBarTimedPreview() {
    PrideQuizTheme {
        EnhancedGameTopBar(
            points = 1200,
            lives = 3,
            stage = 12,
            totalStages = 20,
            currentStreak = 0,
            isTimedMode = true,
            timeRemaining = 45,
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "StreakEffectOverlay")
@Composable
private fun StreakEffectOverlayPreview() {
    PrideQuizTheme {
        Box(modifier = Modifier.size(300.dp)) {
            StreakEffectOverlay(
                streak = 7,
                message = "On Fire!"
            )
        }
    }
}

@Preview(showBackground = true, name = "AnimatedLivesIndicator - Full")
@Composable
private fun AnimatedLivesIndicatorPreview() {
    PrideQuizTheme {
        Box(
            modifier = Modifier
                .background(GradientPositionTop)
                .padding(16.dp)
        ) {
            AnimatedLivesIndicator(currentLives = 2, maxLives = 3)
        }
    }
}

/**
 * Estado de error del juego con boton de reintentar.
 * Se muestra cuando falla la carga de una pregunta.
 */
@Composable
fun GameErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = OffWhiteInk,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.error_retry))
            }
        }
    }
}
