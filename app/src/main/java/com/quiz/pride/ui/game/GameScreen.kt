package com.quiz.pride.ui.game

import android.media.MediaPlayer
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import com.quiz.domain.Pride
import com.quiz.pride.R
import com.quiz.pride.managers.ThemeManager
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun GameScreen(
    gameType: Constants.GameType,
    onNavigateToResult: (Int, Int, Int, Int, Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val themeManager: ThemeManager = koinInject()
    val soundEnabled by themeManager.isSoundEnabled.collectAsState(initial = true)
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    // Game state
    var points by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(if (gameType == Constants.GameType.TIMED) Int.MAX_VALUE else 3) }
    var stage by remember { mutableIntStateOf(1) }
    var extraLivesUsed by remember { mutableIntStateOf(0) }
    val maxExtraLives = 2
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var showExtraLifeDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var isShowingRewardedAd by remember { mutableStateOf(false) }

    // Timed mode state - 3 minutes global timer
    var timeRemaining by remember { mutableIntStateOf(Constants.TIMED_MODE_TOTAL_SECONDS) }

    // Rewarded Ad state
    val rewardedAdState = rememberRewardedAdState()

    // Stats for result screen
    var correctAnswers by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var bestStreak by remember { mutableIntStateOf(0) }
    var showStreakEffect by remember { mutableStateOf(false) }
    var streakMessage by remember { mutableStateOf("") }
    val startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Handle back button press
    BackHandler {
        showExitDialog = true
    }

    // Global timer for TIMED mode - 3 minutes countdown
    LaunchedEffect(gameType) {
        if (gameType == Constants.GameType.TIMED) {
            while (timeRemaining > 0) {
                delay(1000)
                timeRemaining--
            }
            // Time's up - end game
            if (timeRemaining <= 0) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.playFailSound()
                val timePlayed = System.currentTimeMillis() - startTime
                onNavigateToResult(points, stage, correctAnswers, bestStreak, timePlayed)
            }
        }
    }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is GameEvent.NavigateToResult -> {
                    val timePlayed = System.currentTimeMillis() - startTime
                    onNavigateToResult(points, stage, correctAnswers, bestStreak, timePlayed)
                }
                is GameEvent.ShowExtraLifeDialog -> showExtraLifeDialog = true
                is GameEvent.PlaySuccessSound -> {
                    if (soundEnabled) {
                        try {
                            MediaPlayer.create(context, R.raw.success)?.apply {
                                start()
                                setOnCompletionListener { release() }
                            }
                        } catch (_: Exception) {}
                    }
                }
                is GameEvent.PlayFailSound -> {
                    if (soundEnabled) {
                        try {
                            MediaPlayer.create(context, R.raw.fail)?.apply {
                                start()
                                setOnCompletionListener { release() }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    // Detect theme using MaterialTheme colors
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = colorScheme.background.luminance() < 0.5f

    // Theme-aware background gradient
    val gameBackgroundGradient = if (isDarkTheme) {
        Brush.verticalGradient(listOf(GradientGameTop, GradientGameBottom))
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFE8F5E9), // Soft mint
                Color(0xFFF3E5F5), // Soft lavender
                Color(0xFFFCE4EC), // Soft pink
                Color(0xFFE3F2FD)  // Soft blue
            )
        )
    }

    // Floating animation for background
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

    Scaffold(
        topBar = {
            EnhancedGameTopBar(
                points = points,
                lives = lives,
                stage = stage,
                totalStages = Constants.TOTAL_PRIDES,
                currentStreak = currentStreak,
                isTimedMode = gameType == Constants.GameType.TIMED,
                timeRemaining = timeRemaining,
                onBackClick = { showExitDialog = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(gameBackgroundGradient)
        ) {
            // Decorative glow orbs
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

            // Streak effect overlay
            AnimatedVisibility(
                visible = showStreakEffect,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                StreakEffectOverlay(
                    streak = currentStreak,
                    message = streakMessage
                )
            }

            if (uiState.isLoading) {
                LoadingIndicator()
            } else {
                GameContent(
                    gameType = gameType,
                    question = uiState.question,
                    options = uiState.options,
                    correctOptionIndex = uiState.correctOptionIndex,
                    selectedAnswer = selectedAnswer,
                    isDarkTheme = isDarkTheme,
                    onAnswerSelected = { index ->
                        if (selectedAnswer == null) {
                            selectedAnswer = index
                            val isCorrect = index == uiState.correctOptionIndex

                            if (isCorrect) {
                                // Haptic feedback for correct answer
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.playSuccessSound()
                                points++
                                correctAnswers++
                                currentStreak++

                                // Update best streak
                                if (currentStreak > bestStreak) {
                                    bestStreak = currentStreak
                                }

                                // Show streak effects at milestones
                                when {
                                    currentStreak == 5 -> {
                                        streakMessage = context.getString(R.string.streak_on_fire)
                                        showStreakEffect = true
                                    }
                                    currentStreak == 10 -> {
                                        streakMessage = context.getString(R.string.streak_unstoppable)
                                        showStreakEffect = true
                                    }
                                    currentStreak == 15 -> {
                                        streakMessage = context.getString(R.string.streak_legendary)
                                        showStreakEffect = true
                                    }
                                    currentStreak >= 3 && currentStreak % 3 == 0 -> {
                                        streakMessage = context.getString(R.string.streak_combo, currentStreak)
                                        showStreakEffect = true
                                    }
                                }
                            } else {
                                // Haptic feedback for wrong answer
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.playFailSound()
                                // In timed mode, wrong answers don't reduce lives (time is the challenge)
                                if (gameType != Constants.GameType.TIMED) {
                                    lives--
                                }
                                currentStreak = 0
                            }

                            // Delay before next question
                            coroutineScope.launch {
                                delay(1000)
                                selectedAnswer = null
                                showStreakEffect = false

                                when {
                                    gameType != Constants.GameType.TIMED && lives < 1 && extraLivesUsed < maxExtraLives && stage < Constants.TOTAL_PRIDES -> {
                                        viewModel.navigateToExtraLifeDialog()
                                    }
                                    stage >= Constants.TOTAL_PRIDES || (gameType != Constants.GameType.TIMED && lives < 1) -> {
                                        viewModel.navigateToResult(points.toString())
                                    }
                                    else -> {
                                        stage++
                                        viewModel.generateNewStage()
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    // Exit Confirmation Dialog
    if (showExitDialog) {
        ExitConfirmationDialog(
            onStay = { showExitDialog = false },
            onLeave = {
                showExitDialog = false
                onNavigateBack()
            }
        )
    }

    // Extra Life Dialog
    if (showExtraLifeDialog) {
        ExtraLifeDialog(
            extraLivesRemaining = maxExtraLives - extraLivesUsed,
            isAdReady = rewardedAdState.isReady,
            isAdLoading = rewardedAdState.isLoading,
            onAccept = {
                showExtraLifeDialog = false
                isShowingRewardedAd = true

                val activity = context.findActivity()
                if (activity != null && rewardedAdState.isReady) {
                    rewardedAdState.showAd(
                        activity = activity,
                        onRewardEarned = {
                            // User watched the ad, grant extra life
                            lives = 1
                            extraLivesUsed++
                            viewModel.generateNewStage()
                        },
                        onAdDismissed = {
                            isShowingRewardedAd = false
                        },
                        onAdFailed = { _ ->
                            isShowingRewardedAd = false
                            // If ad fails, grant life anyway as fallback
                            lives = 1
                            extraLivesUsed++
                            viewModel.generateNewStage()
                        }
                    )
                } else {
                    // Ad not ready, grant life as fallback
                    isShowingRewardedAd = false
                    lives = 1
                    extraLivesUsed++
                    viewModel.generateNewStage()
                }
            },
            onDecline = {
                showExtraLifeDialog = false
                val timePlayed = System.currentTimeMillis() - startTime
                onNavigateToResult(points, stage, correctAnswers, bestStreak, timePlayed)
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

    val infiniteTransition = rememberInfiniteTransition(label = "timer")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (timeRemaining <= 30) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timer_scale"
    )

    val color = when {
        timeRemaining <= 30 -> PrideRed
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
            imageVector = Icons.Default.Timer,
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
    val infiniteTransition = rememberInfiniteTransition(label = "streak")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streak_scale"
    )

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
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(20.dp)
                .scale(scale)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$streak",
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 14.sp
        )
    }
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
            val isAlive = index < currentLives

            val scale by animateFloatAsState(
                targetValue = if (isAlive) 1f else 0.7f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "heart_scale"
            )

            val infiniteTransition = rememberInfiniteTransition(label = "heart_beat_$index")
            val heartBeat by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isAlive) 1.15f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 100),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "heart_beat"
            )

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
                imageVector = Icons.Default.LocalFireDepartment,
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
                            model = question?.flag,
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
                            model = question?.flag,
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

@Composable
private fun ExtraLifeDialog(
    extraLivesRemaining: Int,
    isAdReady: Boolean,
    isAdLoading: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // Pulsing animation for the heart icon
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

    AlertDialog(
        onDismissRequest = { /* Cannot dismiss */ },
        containerColor = colorScheme.surface,
        titleContentColor = colorScheme.onSurface,
        textContentColor = colorScheme.onSurfaceVariant,
        icon = {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = PrideRed,
                modifier = Modifier
                    .size(48.dp)
                    .scale(heartScale)
            )
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
