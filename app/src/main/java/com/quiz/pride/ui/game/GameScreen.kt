package com.quiz.pride.ui.game

import android.media.AudioAttributes
import android.media.SoundPool
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.offset
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.content.res.Configuration
import com.quiz.domain.Name
import com.quiz.domain.Pride
import com.quiz.pride.R
import com.quiz.pride.ui.components.LoadingIndicator
import com.quiz.pride.ui.components.RewardedAdState
import com.quiz.pride.ui.components.findActivity
import com.quiz.pride.ui.components.rememberRewardedAdState
import com.quiz.pride.ui.theme.DarkSurfaceVariant
import com.quiz.pride.ui.theme.GlassWhite
import com.quiz.pride.ui.theme.GlowBlue
import com.quiz.pride.ui.theme.GlowPink
import com.quiz.pride.ui.theme.GlowPurple
import com.quiz.pride.ui.theme.GradientGameBottom
import com.quiz.pride.ui.theme.GradientGameTop
import com.quiz.pride.ui.theme.GradientPointsBottom
import com.quiz.pride.ui.theme.GradientPointsTop
import com.quiz.pride.ui.theme.GradientPositionBottom
import com.quiz.pride.ui.theme.GradientPositionTop
import com.quiz.pride.ui.theme.NeonBlue
import com.quiz.pride.ui.theme.NeonGreen
import com.quiz.pride.ui.theme.NeonOrange
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.NeonYellow
import com.quiz.pride.ui.theme.PrideRed
import com.quiz.pride.ui.theme.ResponseCorrect
import com.quiz.pride.ui.theme.ResponseFail
import com.quiz.pride.ui.theme.White
import com.quiz.pride.utils.Constants
import androidx.compose.ui.tooling.preview.Preview
import com.quiz.pride.ui.theme.PrideQuizTheme
import kotlinx.coroutines.flow.collectLatest
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

    remember(soundPool) {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                when (sampleId) {
                    successSoundId -> successSoundReady = true
                    failSoundId -> failSoundReady = true
                }
            }
        }
        null
    }

    DisposableEffect(Unit) {
        onDispose { soundPool.release() }
    }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
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

    // Theme-aware background gradient — remember evita crear un nuevo Brush en cada recomposicion
    val gameBackgroundGradient = remember(isDarkTheme) {
        if (isDarkTheme) {
            Brush.verticalGradient(listOf(GradientGameTop, GradientGameBottom))
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0xFFE8F5E9),
                    Color(0xFFF3E5F5),
                    Color(0xFFFCE4EC),
                    Color(0xFFE3F2FD)
                )
            )
        }
    }

    Scaffold(
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(gameBackgroundGradient)
        ) {
            // Orbs decorativos en composable hijo — la InfiniteTransition vive ahi
            // y solo ese composable se recompone cada frame (no toda la pantalla)
            GameBackgroundOrbs()

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
                uiState.isLoading -> LoadingIndicator()
                uiState.hasError -> GameErrorState(
                    message = stringResource(R.string.error_loading_question),
                    onRetry = { viewModel.retryCurrentStage() }
                )
                else -> GameContent(
                    gameType = gameType,
                    question = uiState.question,
                    options = uiState.options,
                    correctOptionIndex = uiState.correctOptionIndex,
                    selectedAnswer = uiState.selectedAnswer,
                    isDarkTheme = isDarkTheme,
                    onAnswerSelected = { index ->
                        viewModel.onAnswerSelected(index, streakOnFire, streakUnstoppable, streakLegendary, streakCombo)
                    }
                )
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

/**
 * Orbs decorativos con animacion flotante.
 * La InfiniteTransition vive aqui adentro para que SOLO este composable
 * se recomponga cada frame — el resto de la pantalla queda estable.
 */
@Composable
private fun GameBackgroundOrbs() {
    val infiniteTransition = rememberInfiniteTransition(label = "game_bg")
    val glowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_offset"
    )

    // Box contenedor necesario para que .align() tenga un BoxScope valido
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-60).dp, y = 50.dp + glowOffset.dp)
                .alpha(0.2f)
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
                .offset(x = 60.dp, y = 100.dp - glowOffset.dp)
                .alpha(0.15f)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonBlue, Color.Transparent)
                        ),
                        radius = size.minDimension / 2
                    )
                }
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(GradientPositionTop, GradientPositionBottom)
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.semantics { contentDescription = "Go back" }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Points with glow effect
            Box(
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = scoreDescription }
            ) {
                Text(
                    text = "$points pts",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GradientPointsBottom,
                    style = MaterialTheme.typography.titleMedium.copy(
                        shadow = Shadow(
                            color = GradientPointsTop,
                            offset = Offset(0f, 0f),
                            blurRadius = 8f
                        )
                    )
                )
            }

            // Streak indicator
            if (currentStreak >= 3) {
                StreakIndicator(streak = currentStreak)
                Spacer(modifier = Modifier.width(12.dp))
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

        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(3.dp))
                .semantics { contentDescription = progressDescription },
            color = NeonGreen,
            trackColor = White.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )

        // Stage indicator
        Text(
            text = "$stage / $totalStages",
            style = MaterialTheme.typography.bodySmall,
            color = White.copy(alpha = 0.7f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            textAlign = TextAlign.End
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
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 16.sp,
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
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 14.sp
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
                        color = PrideRed.copy(alpha = 0.3f),
                        radius = size.minDimension * 0.9f
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
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
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

    // Theme-aware question box colors
    val questionBoxBackground = if (isDarkTheme) {
        Brush.linearGradient(
            colors = listOf(
                DarkSurfaceVariant,
                DarkSurfaceVariant.copy(alpha = 0.8f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White,
                Color.White.copy(alpha = 0.9f)
            )
        )
    }

    val questionTextColor = colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Question area (35%)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f)
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(questionBoxBackground)
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            NeonPurple.copy(alpha = if (isDarkTheme) 0.5f else 0.4f),
                            NeonPink.copy(alpha = if (isDarkTheme) 0.3f else 0.25f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (gameType) {
                Constants.GameType.NORMAL -> {
                    // Show flag image with glow
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            GlowPink.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.minDimension / 1.5f
                                )
                            }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(question?.flag)
                                .crossfade(200)
                                .size(600, 400)
                                .build(),
                            contentDescription = stringResource(R.string.game_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Constants.GameType.ADVANCE -> {
                    // Show description with styling
                    Text(
                        text = question?.description?.EN ?: "",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            shadow = if (isDarkTheme) Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(1f, 1f),
                                blurRadius = 3f
                            ) else null
                        ),
                        textAlign = TextAlign.Center,
                        color = questionTextColor
                    )
                }
                Constants.GameType.EXPERT -> {
                    // Show name only with neon effect
                    Text(
                        text = stringResource(R.string.expert_question),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            shadow = Shadow(
                                color = NeonPink.copy(alpha = if (isDarkTheme) 0.8f else 0.5f),
                                offset = Offset(0f, 0f),
                                blurRadius = 10f
                            )
                        ),
                        textAlign = TextAlign.Center,
                        color = questionTextColor
                    )
                }
                Constants.GameType.TIMED -> {
                    // Timed mode shows flag image like NORMAL mode
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            GlowBlue.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.minDimension / 1.5f
                                )
                            }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(question?.flag)
                                .crossfade(200)
                                .size(600, 400)
                                .build(),
                            contentDescription = stringResource(R.string.game_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Answer options area (65%)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // First row of options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VibrantAnswerButton(
                        option = options.getOrNull(0),
                        gameType = gameType,
                        isSelected = selectedAnswer == 0,
                        isCorrect = selectedAnswer != null && correctOptionIndex == 0,
                        isWrong = selectedAnswer == 0 && correctOptionIndex != 0,
                        enabled = selectedAnswer == null,
                        buttonIndex = 0,
                        isDarkTheme = isDarkTheme,
                        onClick = { onAnswerSelected(0) },
                        modifier = Modifier.weight(1f)
                    )
                    VibrantAnswerButton(
                        option = options.getOrNull(1),
                        gameType = gameType,
                        isSelected = selectedAnswer == 1,
                        isCorrect = selectedAnswer != null && correctOptionIndex == 1,
                        isWrong = selectedAnswer == 1 && correctOptionIndex != 1,
                        enabled = selectedAnswer == null,
                        buttonIndex = 1,
                        isDarkTheme = isDarkTheme,
                        onClick = { onAnswerSelected(1) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Second row of options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VibrantAnswerButton(
                        option = options.getOrNull(2),
                        gameType = gameType,
                        isSelected = selectedAnswer == 2,
                        isCorrect = selectedAnswer != null && correctOptionIndex == 2,
                        isWrong = selectedAnswer == 2 && correctOptionIndex != 2,
                        enabled = selectedAnswer == null,
                        buttonIndex = 2,
                        isDarkTheme = isDarkTheme,
                        onClick = { onAnswerSelected(2) },
                        modifier = Modifier.weight(1f)
                    )
                    VibrantAnswerButton(
                        option = options.getOrNull(3),
                        gameType = gameType,
                        isSelected = selectedAnswer == 3,
                        isCorrect = selectedAnswer != null && correctOptionIndex == 3,
                        isWrong = selectedAnswer == 3 && correctOptionIndex != 3,
                        enabled = selectedAnswer == null,
                        buttonIndex = 3,
                        isDarkTheme = isDarkTheme,
                        onClick = { onAnswerSelected(3) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

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

    // Smooth scale animation
    val scale by animateFloatAsState(
        targetValue = when {
            isCorrect -> 1.02f
            isWrong -> 0.96f
            isSelected -> 0.95f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "answer_scale"
    )

    // Different neon colors for each button
    val buttonAccentColor = when (buttonIndex) {
        0 -> NeonPink
        1 -> NeonBlue
        2 -> NeonPurple
        3 -> NeonGreen
        else -> NeonPink
    }

    val buttonGlowColor = buttonAccentColor.copy(alpha = 0.4f)

    // Theme-aware default background
    val defaultBackground = if (isDarkTheme) {
        DarkSurfaceVariant.copy(alpha = 0.95f)
    } else {
        Color.White.copy(alpha = 0.95f)
    }

    // Smooth background color transition
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isCorrect -> ResponseCorrect.copy(alpha = 0.85f)
            isWrong -> ResponseFail.copy(alpha = 0.85f)
            isSelected -> buttonAccentColor.copy(alpha = 0.3f)
            else -> defaultBackground
        },
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "answer_bg"
    )

    // Theme-aware text color
    val textColor = if (isDarkTheme || isCorrect || isWrong || isSelected) {
        White
    } else {
        colorScheme.onSurface
    }

    // Smooth border color transition
    val borderColor by animateColorAsState(
        targetValue = when {
            isCorrect -> NeonGreen
            isWrong -> ResponseFail
            isSelected -> buttonAccentColor
            else -> buttonAccentColor.copy(alpha = 0.4f)
        },
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "answer_border"
    )

    // Border width animation
    val borderWidth by animateFloatAsState(
        targetValue = when {
            isCorrect || isWrong -> 3f
            isSelected -> 2.5f
            else -> 1.5f
        },
        animationSpec = tween(300),
        label = "border_width"
    )

    // Glow intensity animation
    val glowAlpha by animateFloatAsState(
        targetValue = when {
            isCorrect -> 0.8f
            isWrong -> 0.6f
            isSelected -> 0.5f
            else -> 0.2f
        },
        animationSpec = tween(400),
        label = "glow_alpha"
    )

    // Overlay alpha for feedback
    val overlayAlpha by animateFloatAsState(
        targetValue = when {
            isCorrect -> 0.15f
            isWrong -> 0.1f
            else -> 0f
        },
        animationSpec = tween(500),
        label = "overlay_alpha"
    )

    // Text/content alpha
    val contentAlpha by animateFloatAsState(
        targetValue = if (!enabled && !isCorrect && !isWrong) 0.6f else 1f,
        animationSpec = tween(300),
        label = "content_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .drawBehind {
                // Outer glow effect
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            when {
                                isCorrect -> NeonGreen.copy(alpha = glowAlpha)
                                isWrong -> ResponseFail.copy(alpha = glowAlpha)
                                else -> buttonAccentColor.copy(alpha = glowAlpha * 0.5f)
                            },
                            Color.Transparent
                        ),
                        radius = size.maxDimension * 0.8f
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
                )
            }
            .semantics { contentDescription = semanticsDescription }
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Remove default ripple
            )
    ) {
        // Main card background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = borderWidth.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            borderColor,
                            borderColor.copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            // Gradient overlay for depth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            )

            // Success/Error overlay with smooth gradient
            if (isCorrect || isWrong) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (isCorrect) NeonGreen.copy(alpha = overlayAlpha)
                                    else ResponseFail.copy(alpha = overlayAlpha),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                )
            }

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .alpha(contentAlpha),
                contentAlignment = Alignment.Center
            ) {
                when (gameType) {
                    Constants.GameType.NORMAL, Constants.GameType.TIMED -> {
                        // Show text name for NORMAL and TIMED modes
                        Text(
                            text = option?.name?.EN ?: "",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = if (isDarkTheme || isCorrect || isWrong) Shadow(
                                    color = when {
                                        isCorrect -> NeonGreen.copy(alpha = 0.8f)
                                        isWrong -> ResponseFail.copy(alpha = 0.5f)
                                        else -> buttonAccentColor.copy(alpha = 0.5f)
                                    },
                                    offset = Offset(0f, 0f),
                                    blurRadius = if (isCorrect || isWrong) 12f else 6f
                                ) else null
                            ),
                            textAlign = TextAlign.Center,
                            color = textColor
                        )
                    }
                    Constants.GameType.ADVANCE, Constants.GameType.EXPERT -> {
                        // Show flag image for ADVANCE and EXPERT modes
                        AsyncImage(
                            model = option?.flag,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(if (isWrong && !isCorrect) 0.7f else 1f),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // Animated check/cross icon overlay for feedback
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
                        .padding(8.dp)
                        .size(28.dp)
                        .background(
                            color = if (isCorrect) NeonGreen else ResponseFail,
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = White.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCorrect) "✓" else "✗",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
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
    AlertDialog(
        onDismissRequest = onStay,
        containerColor = colorScheme.surface,
        titleContentColor = colorScheme.onSurface,
        textContentColor = colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = stringResource(R.string.exit_game_title),
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
            Text(
                text = stringResource(R.string.exit_game_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onStay) {
                Text(
                    text = stringResource(R.string.exit_game_stay),
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onLeave) {
                Text(
                    text = stringResource(R.string.exit_game_leave),
                    color = NeonPink,
                    fontWeight = FontWeight.Bold
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
                        color = if (isAdReady || !isAdLoading) NeonGreen else colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(
                    text = stringResource(R.string.dialog_extra_life_btn_no),
                    color = NeonPink,
                    fontWeight = FontWeight.Bold
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
                color = White,
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
