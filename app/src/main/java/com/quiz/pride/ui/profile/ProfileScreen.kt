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
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.pride.R
import com.quiz.pride.managers.Achievement
import com.quiz.pride.managers.LevelInfo
import com.quiz.pride.managers.PlayerStatistics
import com.quiz.pride.managers.UserProfile
import com.quiz.pride.ui.components.LoadingIndicator
import com.quiz.pride.ui.components.PrideTopAppBar
import com.quiz.pride.ui.theme.DarkSurfaceVariant
import com.quiz.pride.ui.theme.GradientBackgroundEnd
import com.quiz.pride.ui.theme.GradientBackgroundMid
import com.quiz.pride.ui.theme.GradientBackgroundStart
import com.quiz.pride.ui.theme.GradientPointsBottom
import com.quiz.pride.ui.theme.GradientPointsTop
import com.quiz.pride.ui.theme.NeonBlue
import com.quiz.pride.ui.theme.NeonGreen
import com.quiz.pride.ui.theme.NeonOrange
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.NeonYellow
import com.quiz.pride.ui.theme.White
import org.koin.androidx.compose.koinViewModel
import java.io.ByteArrayOutputStream

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLeaderboard: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Detect theme
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = colorScheme.background.luminance() < 0.5f

    // Background animation
    val infiniteTransition = rememberInfiniteTransition(label = "profile_bg")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    Scaffold(
        topBar = {
            PrideTopAppBar(
                title = stringResource(R.string.profile_title),
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
                    .size(180.dp)
                    .offset(x = (-50).dp, y = 100.dp + floatOffset.dp)
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

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = 200.dp - floatOffset.dp)
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

            if (uiState.isLoading) {
                LoadingIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
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

                    // Statistics Section
                    uiState.statistics?.let { stats ->
                        StatisticsSection(statistics = stats, isDarkTheme = isDarkTheme)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Achievements Section
                    AchievementsSection(
                        allAchievements = uiState.allAchievements,
                        unlockedAchievements = uiState.unlockedAchievements,
                        isDarkTheme = isDarkTheme
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun LevelCard(levelInfo: LevelInfo, isDarkTheme: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "level_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Theme-aware colors
    val cardBackground = if (isDarkTheme) DarkSurfaceVariant else Color.White
    val textColor = if (isDarkTheme) White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) White.copy(alpha = 0.7f) else Color(0xFF6B7280)
    val trackColor = if (isDarkTheme) White.copy(alpha = 0.1f) else Color(0xFFE5E7EB)

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
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = levelInfo.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
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
    val infiniteTransition = rememberInfiniteTransition(label = "rank_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rank_glow_alpha"
    )

    // Theme-aware colors
    val cardBackground = if (isDarkTheme) DarkSurfaceVariant else Color.White
    val titleColor = if (isDarkTheme) White else Color(0xFF1F2937)
    val subtextColor = if (isDarkTheme) White.copy(alpha = 0.6f) else Color(0xFF6B7280)

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
                    imageVector = Icons.Default.Leaderboard,
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
                        fontWeight = FontWeight.Bold,
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
                            fontWeight = FontWeight.Bold,
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
                        color = if (isDarkTheme) White.copy(alpha = 0.4f) else Color(0xFF9CA3AF)
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
private fun StatisticsSection(statistics: PlayerStatistics, isDarkTheme: Boolean) {
    // Theme-aware colors
    val titleColor = if (isDarkTheme) White else Color(0xFF1F2937)
    val subtitleColor = if (isDarkTheme) White.copy(alpha = 0.8f) else Color(0xFF4B5563)

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
                icon = Icons.Default.Star,
                label = stringResource(R.string.games_played),
                value = statistics.totalGamesPlayed.toString(),
                color = NeonPink,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.EmojiEvents,
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
                icon = Icons.Default.LocalFireDepartment,
                label = stringResource(R.string.best_streak),
                value = statistics.bestStreakEver.toString(),
                color = NeonOrange,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.Star,
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
                icon = Icons.Default.Star,
                label = stringResource(R.string.perfect_games),
                value = statistics.perfectGames.toString(),
                color = NeonPurple,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.Timer,
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
                mode = "Expert",
                count = statistics.expertGamesPlayed,
                color = NeonPurple,
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
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // Theme-aware colors
    val cardBackground = if (isDarkTheme) DarkSurfaceVariant else Color.White
    val valueColor = if (isDarkTheme) White else Color(0xFF1F2937)
    val labelColor = if (isDarkTheme) White.copy(alpha = 0.7f) else Color(0xFF6B7280)

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
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
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
    val labelColor = if (isDarkTheme) White.copy(alpha = 0.7f) else Color(0xFF6B7280)

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
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
private fun AchievementsSection(
    allAchievements: List<Achievement>,
    unlockedAchievements: Set<Achievement>,
    isDarkTheme: Boolean
) {
    val titleColor = if (isDarkTheme) White else Color(0xFF1F2937)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.achievements_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
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
            items(allAchievements) { achievement ->
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
    val infiniteTransition = rememberInfiniteTransition(label = "achievement_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isUnlocked) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    // Theme-aware colors
    val cardBackground = if (isDarkTheme) DarkSurfaceVariant else Color.White
    val titleColor = if (isDarkTheme) White else Color(0xFF1F2937)
    val descriptionColor = if (isDarkTheme) White.copy(alpha = 0.7f) else Color(0xFF6B7280)

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
                fontSize = 32.sp,
                modifier = Modifier.alpha(if (isUnlocked) 1f else 0.3f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isUnlocked) titleColor else titleColor.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.labelSmall,
                color = if (isUnlocked) descriptionColor else descriptionColor.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            if (isUnlocked) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "+${achievement.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
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
    val titleColor = if (isDarkTheme) White else Color(0xFF1F2937)
    val nicknameColor = if (isDarkTheme) White else Color(0xFF1F2937)
    val placeholderColor = if (isDarkTheme) White.copy(alpha = 0.5f) else Color(0xFF9CA3AF)
    val borderColor = if (isDarkTheme) White.copy(alpha = 0.3f) else Color(0xFFE5E7EB)

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
                    fontWeight = FontWeight.Bold,
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
                        imageVector = Icons.Default.CameraAlt,
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
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
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
