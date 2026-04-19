package com.quiz.pride.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quiz.domain.Achievement
import com.quiz.domain.LevelInfo
import com.quiz.domain.PlayerStatistics
import com.quiz.domain.StreakState
import com.quiz.domain.UserProfile
import com.quiz.domain.challenge.ChallengeStats
import com.quiz.domain.reward.DailyReward
import com.quiz.domain.reward.RewardTier
import com.quiz.pride.R
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.ui.components.AnimatedScreenBackground
import com.quiz.pride.ui.components.DailyChallengesCard
import com.quiz.pride.ui.components.LoadingIndicator
import com.quiz.pride.ui.components.StreakWidget
import com.quiz.pride.ui.components.TrackScreenTime
import com.quiz.pride.ui.theme.DarkSurfaceVariant
import com.quiz.pride.ui.theme.GradientPointsBottom
import com.quiz.pride.ui.theme.GradientPointsTop
import com.quiz.pride.ui.theme.NeonBlue
import com.quiz.pride.ui.theme.NeonGreen
import com.quiz.pride.ui.theme.NeonOrange
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.NeonYellow
import com.quiz.pride.ui.theme.RankGold
import com.quiz.pride.ui.theme.ResponseCorrect
import com.quiz.pride.ui.theme.White
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.io.ByteArrayOutputStream

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLeaderboard: () -> Unit = {},
    onNavigateToShop: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val analyticsManager: AnalyticsManager = koinInject()

    TrackScreenTime(AnalyticsManager.SCREEN_PROFILE, analyticsManager)

    // Detect theme
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = colorScheme.background.luminance() < 0.5f

    AnimatedScreenBackground(
        orbColor1 = NeonPurple,
        orbColor2 = NeonPink
    ) {
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Espacio para la top row flotante (back + título) + status bar
                Spacer(modifier = Modifier.height(120.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                // User Profile Card
                UserProfileCard(
                    userProfile = uiState.userProfile,
                    isDarkTheme = isDarkTheme,
                    onSaveNickname = { viewModel.saveNickname(it) },
                    onSaveImage = { viewModel.saveUserImage(it) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Level Card
                uiState.levelInfo?.let { levelInfo ->
                    LevelCard(levelInfo = levelInfo, isDarkTheme = isDarkTheme)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Global Rank Card
                GlobalRankCard(
                    globalRank = uiState.globalRank,
                    isLoading = uiState.isLoadingRank,
                    isDarkTheme = isDarkTheme,
                    onViewLeaderboard = onNavigateToLeaderboard
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Streak Section
                StreakSection(
                    streakState = uiState.streakState,
                    isAtRisk = uiState.isStreakAtRisk,
                    hasPlayedToday = uiState.hasPlayedToday,
                    isDarkTheme = isDarkTheme
                )

                // Estado de la recompensa del dia (siempre visible en perfil)
                if (uiState.dailyReward != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DailyRewardStatusBadge(
                        reward = uiState.dailyReward!!,
                        isDarkTheme = isDarkTheme
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Statistics Section
                uiState.statistics?.let { stats ->
                    StatisticsSection(statistics = stats, isDarkTheme = isDarkTheme)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Desafios activos del dia
                if (!uiState.isLoadingChallenges && uiState.challengeState.challenges.isNotEmpty()) {
                    DailyChallengesCard(challengeState = uiState.challengeState)
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Seccion de Desafios Diarios
                ChallengesStatsSection(
                    challengeStats = uiState.challengeStats,
                    isDarkTheme = isDarkTheme
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Achievements Section
                AchievementsSection(
                    allAchievements = uiState.allAchievements,
                    unlockedAchievements = uiState.unlockedAchievements,
                    isDarkTheme = isDarkTheme
                )

                Spacer(modifier = Modifier.height(24.dp))
                } // cierra Column interior (scrolleable)
            } // cierra Column exterior
        }

        // Top row flotante: back button (solo flecha blanca) + título en la misma row.
        // Scrim gradient vertical (negro→transparente) para que el contenido scrolleable
        // se disuelva al pasar bajo el top bar en lugar de verse nítido detrás.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    shadow = Shadow(
                        color = NeonPurple.copy(alpha = 0.6f),
                        offset = Offset(0f, 0f),
                        blurRadius = 12f
                    )
                ),
                color = White,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun LevelCard(levelInfo: LevelInfo, isDarkTheme: Boolean) {
    // Solo se anima en dark theme: en light theme el glow no se dibuja
    val glowAlpha = if (isDarkTheme) {
        val infiniteTransition = rememberInfiniteTransition(label = "level_glow")
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
        ).value
    } else {
        0f
    }

    // Theme-aware colors
    val cardBackground = if (isDarkTheme) DarkSurfaceVariant else Color.White
    val textColor = MaterialTheme.colorScheme.onSurface
    val subtextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val trackColor = MaterialTheme.colorScheme.outlineVariant

    // Animated progress
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val progressAnimation by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )

    LaunchedEffect(levelInfo.progressPercent) {
        animatedProgress = levelInfo.progressPercent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (isDarkTheme) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GradientPointsTop.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        ),
                        radius = size.maxDimension * 0.6f,
                        center = Offset(size.width * 0.3f, size.height * 0.5f)
                    )
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Level badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(GradientPointsTop, GradientPointsBottom)
                        )
                    )
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            listOf(White.copy(alpha = 0.5f), White.copy(alpha = 0.2f))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = levelInfo.level.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = levelInfo.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    shadow = if (isDarkTheme) Shadow(
                        color = GradientPointsTop.copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 8f
                    ) else null
                ),
                color = GradientPointsBottom
            )

            Spacer(modifier = Modifier.height(16.dp))

            // XP Progress bar
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${levelInfo.xpInCurrentLevel} XP",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor
                    )
                    Text(
                        text = "${levelInfo.xpNeededForNextLevel} XP",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { progressAnimation },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = GradientPointsBottom,
                    trackColor = trackColor,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.total_xp, levelInfo.totalXp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun GlobalRankCard(
    globalRank: Int?,
    isLoading: Boolean,
    isDarkTheme: Boolean,
    onViewLeaderboard: () -> Unit
) {
    // Solo se anima en dark theme: en light theme el glow no se dibuja
    val glowAlpha = if (isDarkTheme) {
        val infiniteTransition = rememberInfiniteTransition(label = "rank_glow")
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rank_glow_alpha"
        ).value
    } else {
        0f
    }

    // Theme-aware colors
    val cardBackground = if (isDarkTheme) DarkSurfaceVariant else Color.White
    val titleColor = MaterialTheme.colorScheme.onSurface
    val subtextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewLeaderboard() }
            .drawBehind {
                if (isDarkTheme) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NeonPurple.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        ),
                        radius = size.maxDimension * 0.5f,
                        center = Offset(size.width * 0.7f, size.height * 0.5f)
                    )
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(NeonPurple, NeonPink)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_leaderboard),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.global_rank),
                    style = MaterialTheme.typography.titleMedium.copy(
                        shadow = if (isDarkTheme) Shadow(
                            color = NeonPurple.copy(alpha = 0.5f),
                            offset = Offset(0f, 0f),
                            blurRadius = 6f
                        ) else null
                    ),
                    color = titleColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (isLoading) {
                    Text(
                        text = "...",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor
                    )
                } else if (globalRank != null) {
                    Text(
                        text = stringResource(R.string.global_rank_value, globalRank),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            shadow = if (isDarkTheme) Shadow(
                                color = NeonPink.copy(alpha = 0.5f),
                                offset = Offset(0f, 0f),
                                blurRadius = 8f
                            ) else null
                        ),
                        color = NeonPink
                    )
                } else {
                    Text(
                        text = stringResource(R.string.not_ranked),
                        style = MaterialTheme.typography.bodyMedium,
                        color = subtextColor
                    )
                    Text(
                        text = stringResource(R.string.play_to_rank),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // Arrow indicator
            Text(
                text = "→",
                style = MaterialTheme.typography.titleLarge,
                color = NeonPurple
            )
        }
    }
}

@Composable
private fun StreakSection(
    streakState: StreakState,
    isAtRisk: Boolean,
    hasPlayedToday: Boolean,
    isDarkTheme: Boolean
) {
    val titleColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.profile_daily_streak),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                shadow = if (isDarkTheme) Shadow(
                    color = NeonOrange.copy(alpha = 0.5f),
                    offset = Offset(0f, 0f),
                    blurRadius = 6f
                ) else null
            ),
            color = titleColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Reutilizamos el widget de SelectScreen — misma UI, mismos datos
        StreakWidget(
            streakState = streakState,
            isAtRisk = isAtRisk,
            hasPlayedToday = hasPlayedToday
        )

        // Stats rapidas de racha en una fila
        if (streakState.totalDaysPlayed > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StreakStatChip(
                    label = "Total dias",
                    value = streakState.totalDaysPlayed.toString(),
                    color = NeonBlue,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f)
                )
                StreakStatChip(
                    label = "Tokens ❄",
                    value = streakState.freezeTokens.toString(),
                    color = NeonBlue,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f)
                )
                StreakStatChip(
                    label = "Dia del ciclo",
                    value = "${streakState.cycleDay}/7",
                    color = NeonPurple,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StreakStatChip(
    label: String,
    value: String,
    color: Color,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.25f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatisticsSection(statistics: PlayerStatistics, isDarkTheme: Boolean) {
    // Theme-aware colors
    val titleColor = MaterialTheme.colorScheme.onSurface
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.statistics_title),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                shadow = if (isDarkTheme) Shadow(
                    color = NeonPurple.copy(alpha = 0.5f),
                    offset = Offset(0f, 0f),
                    blurRadius = 6f
                ) else null
            ),
            color = titleColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Stats grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = rememberVectorPainter(Icons.Default.Star),
                label = stringResource(R.string.games_played),
                value = statistics.totalGamesPlayed.toString(),
                color = NeonPink,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = painterResource(id = R.drawable.ic_emoji_events),
                label = stringResource(R.string.games_won),
                value = statistics.gamesWon.toString(),
                color = GradientPointsBottom,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = painterResource(id = R.drawable.ic_local_fire_department),
                label = stringResource(R.string.best_streak),
                value = statistics.bestStreakEver.toString(),
                color = NeonOrange,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = rememberVectorPainter(Icons.Default.Star),
                label = stringResource(R.string.accuracy_label),
                value = "${statistics.accuracy.toInt()}%",
                color = NeonGreen,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = rememberVectorPainter(Icons.Default.Star),
                label = stringResource(R.string.perfect_games),
                value = statistics.perfectGames.toString(),
                color = NeonPurple,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = painterResource(R.drawable.ic_timer),
                label = stringResource(R.string.time_played),
                value = statistics.totalTimePlayed,
                color = NeonBlue,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
        }

        // Mode breakdown
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.games_by_mode),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = subtitleColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModeChip(
                mode = "Normal",
                count = statistics.normalGamesPlayed,
                color = NeonGreen,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
            ModeChip(
                mode = "Advance",
                count = statistics.advanceGamesPlayed,
                color = NeonBlue,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
            ModeChip(
                mode = "Timed",
                count = statistics.timedGamesPlayed,
                color = NeonOrange,
                isDarkTheme = isDarkTheme,
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
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // Theme-aware colors
    val cardBackground = if (isDarkTheme) DarkSurfaceVariant else Color.White
    val valueColor = MaterialTheme.colorScheme.onSurface
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    shadow = if (isDarkTheme) Shadow(
                        color = color.copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 6f
                    ) else null
                ),
                color = valueColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = labelColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModeChip(
    mode: String,
    count: Int,
    color: Color,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
            Text(
                text = mode,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor
            )
        }
    }
}

@Composable
private fun ChallengesStatsSection(
    challengeStats: ChallengeStats,
    isDarkTheme: Boolean
) {
    val titleColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.profile_challenges),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                shadow = if (isDarkTheme) Shadow(
                    color = NeonPurple.copy(alpha = 0.5f),
                    offset = Offset(0f, 0f),
                    blurRadius = 6f
                ) else null
            ),
            color = titleColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = rememberVectorPainter(Icons.Default.Star),
                label = "Total completados",
                value = challengeStats.totalCompleted.toString(),
                color = NeonPurple,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = painterResource(id = R.drawable.ic_emoji_events),
                label = "Dias completos",
                value = challengeStats.totalAllDailyCompleteDays.toString(),
                color = NeonGreen,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = painterResource(id = R.drawable.ic_local_fire_department),
                label = "Racha actual",
                value = "${challengeStats.currentAllDailyStreak}d",
                color = NeonOrange,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = painterResource(id = R.drawable.ic_local_fire_department),
                label = "Mejor racha",
                value = "${challengeStats.bestAllDailyStreak}d",
                color = GradientPointsBottom,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AchievementsSection(
    allAchievements: List<Achievement>,
    unlockedAchievements: Set<Achievement>,
    isDarkTheme: Boolean
) {
    val titleColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.achievements_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    shadow = if (isDarkTheme) Shadow(
                        color = NeonYellow.copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 6f
                    ) else null
                ),
                color = titleColor
            )

            Text(
                text = "${unlockedAchievements.size}/${allAchievements.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = NeonYellow
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Achievement cards in a scrollable row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = allAchievements,
                key = { it.ordinal },
                contentType = { "achievement" }
            ) { achievement ->
                AchievementCard(
                    achievement = achievement,
                    isUnlocked = achievement in unlockedAchievements,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    isUnlocked: Boolean,
    isDarkTheme: Boolean
) {
    // Solo se anima si el logro esta desbloqueado — evita InfiniteTransition activa para todos
    val glowScale = if (isUnlocked) {
        val infiniteTransition = rememberInfiniteTransition(label = "achievement_glow")
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_scale"
        ).value
    } else {
        1f
    }

    // Theme-aware colors
    val cardBackground = if (isDarkTheme) DarkSurfaceVariant else Color.White
    val titleColor = MaterialTheme.colorScheme.onSurface
    val descriptionColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .width(140.dp)
            .scale(glowScale)
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked)
                cardBackground
            else
                cardBackground.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Text(
                text = achievement.icon,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.alpha(if (isUnlocked) 1f else 0.3f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = stringResource(achievement.titleRes),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isUnlocked) titleColor else titleColor.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = stringResource(achievement.descriptionRes),
                style = MaterialTheme.typography.labelSmall,
                color = if (isUnlocked) descriptionColor else descriptionColor.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            if (isUnlocked) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.xp_gained, achievement.xpReward),
                    style = MaterialTheme.typography.labelSmall,
                    color = GradientPointsBottom
                )
            }
        }
    }
}

@Composable
private fun UserProfileCard(
    userProfile: UserProfile,
    isDarkTheme: Boolean,
    onSaveNickname: (String) -> Unit,
    onSaveImage: (String) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var isEditingNickname by remember { mutableStateOf(false) }
    var nicknameText by remember(userProfile.nickname) { mutableStateOf(userProfile.nickname) }
    var currentImageBase64 by remember(userProfile.imageBase64) { mutableStateOf(userProfile.imageBase64) }

    // Theme-aware colors
    val cardBackground = if (isDarkTheme) DarkSurfaceVariant else Color.White
    val titleColor = MaterialTheme.colorScheme.onSurface
    val nicknameColor = MaterialTheme.colorScheme.onSurface
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                // Resize and compress the bitmap
                val resizedBitmap = resizeBitmap(bitmap, 200)
                val base64 = bitmapToBase64(resizedBitmap)

                currentImageBase64 = base64
                onSaveImage(base64)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.my_profile),
                style = MaterialTheme.typography.titleMedium.copy(
                    shadow = if (isDarkTheme) Shadow(
                        color = NeonPurple.copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 6f
                    ) else null
                ),
                color = titleColor,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Avatar with edit button
            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(NeonPurple.copy(alpha = 0.3f), NeonPink.copy(alpha = 0.3f))
                            )
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(listOf(NeonPurple, NeonPink)),
                            shape = CircleShape
                        )
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (currentImageBase64.isNotEmpty()) {
                        val bitmap = try {
                            val imageBytes = Base64.decode(currentImageBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        } catch (e: Exception) {
                            null
                        }

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            DefaultProfileIcon()
                        }
                    } else {
                        DefaultProfileIcon()
                    }
                }

                // Camera button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(NeonPurple)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera_alt),
                        contentDescription = stringResource(R.string.change_photo),
                        tint = White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nickname section
            if (isEditingNickname) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nicknameText,
                        onValueChange = { nicknameText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.enter_nickname),
                                color = placeholderColor
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = nicknameColor,
                            unfocusedTextColor = nicknameColor,
                            cursorColor = NeonPurple
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onSaveNickname(nicknameText)
                                isEditingNickname = false
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            onSaveNickname(nicknameText)
                            isEditingNickname = false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.save),
                            tint = NeonGreen
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = userProfile.nickname.ifEmpty { stringResource(R.string.tap_to_set_nickname) },
                        style = MaterialTheme.typography.titleLarge,
                        color = if (userProfile.nickname.isEmpty()) placeholderColor else nicknameColor,
                        modifier = Modifier.clickable { isEditingNickname = true }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { isEditingNickname = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_nickname),
                            tint = NeonPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DefaultProfileIcon() {
    Icon(
        imageVector = Icons.Default.Person,
        contentDescription = null,
        modifier = Modifier.size(50.dp),
        tint = White.copy(alpha = 0.6f)
    )
}

private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val ratio = width.toFloat() / height.toFloat()

    val newWidth: Int
    val newHeight: Int

    if (width > height) {
        newWidth = maxSize
        newHeight = (maxSize / ratio).toInt()
    } else {
        newHeight = maxSize
        newWidth = (maxSize * ratio).toInt()
    }

    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

private fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

// ---------------------------------------------------------------------------
// Daily Reward Status Badge — indicador compacto del estado de la recompensa
// del dia. Siempre visible en perfil (ignora swipe-to-dismiss de SelectScreen).
// ---------------------------------------------------------------------------

@Composable
private fun DailyRewardStatusBadge(reward: DailyReward, isDarkTheme: Boolean) {
    val accent = when (reward.tier) {
        RewardTier.COMMON -> NeonBlue
        RewardTier.UNCOMMON -> NeonPurple
        RewardTier.RARE -> RankGold
    }
    val claimed = reward.isClaimed
    val statusColor = if (claimed) ResponseCorrect else accent
    val statusText = stringResource(
        if (claimed) R.string.daily_reward_status_claimed
        else R.string.daily_reward_status_pending
    )
    val icon = if (claimed) "\u2705" else "\uD83C\uDF81"

    // Fondo opaco consistente con el resto de cards del perfil.
    // El color de estado se aplica solo al borde, icono y chips para mantener identidad.
    val cardBackground = if (isDarkTheme) DarkSurfaceVariant else Color.White
    val textColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            if (reward.xpAmount > 0) {
                RewardAmountChip(label = "+${reward.xpAmount} XP", color = statusColor)
            }
            if (reward.coinsAmount > 0) {
                RewardAmountChip(label = "+${reward.coinsAmount} \uD83D\uDCB0", color = statusColor)
            }
            if (reward.gemsAmount > 0) {
                RewardAmountChip(label = "+${reward.gemsAmount} \uD83D\uDC8E", color = statusColor)
            }
        }
    }
}

@Composable
private fun RewardAmountChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}
