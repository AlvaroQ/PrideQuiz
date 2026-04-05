package com.quiz.pride.ui.ranking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import com.quiz.domain.User
import com.quiz.domain.XpLeaderboardEntry
import com.quiz.pride.R
import com.quiz.pride.ui.components.AnimatedScreenBackground
import com.quiz.pride.ui.components.BannerAdView
import com.quiz.pride.ui.components.PrideTopAppBar
import com.quiz.pride.ui.components.ShimmerRankingItem
import com.quiz.pride.ui.theme.DarkSurfaceVariant
import com.quiz.pride.ui.theme.GradientPointsBottom
import com.quiz.pride.ui.theme.GlowPurple
import com.quiz.pride.ui.theme.GradientPointsTop
import com.quiz.pride.ui.theme.GradientPositionBottom
import com.quiz.pride.ui.theme.GradientPositionTop
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
private fun UserAvatar(
    userImage: String?,
    isTopThree: Boolean,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    val avatarModifier = modifier
        .size(56.dp)
        .clip(CircleShape)
        .border(
            width = 2.dp,
            color = borderColor,
            shape = CircleShape
        )

    when {
        userImage != null && !userImage.startsWith("http") && userImage.length > 100 -> {
            // Base64 image - cacheado para evitar decode repetido en cada recomposicion
            val bitmap = remember(userImage) {
                runCatching {
                    val bytes = Base64.decode(userImage, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }.getOrNull()
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = avatarModifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                DefaultAvatar(modifier = avatarModifier)
            }
        }
        !userImage.isNullOrEmpty() -> {
            // URL image - use Coil
            SubcomposeAsyncImage(
                model = userImage,
                contentDescription = null,
                modifier = avatarModifier,
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = NeonPurple,
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = {
                    DefaultAvatar(modifier = Modifier.fillMaxSize())
                }
            )
        }
        else -> {
            DefaultAvatar(modifier = avatarModifier)
        }
    }
}

@Composable
private fun DefaultAvatar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(DarkSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = White.copy(alpha = 0.6f)
        )
    }
}

// Vibrant medal colors with glow
private val GoldColor = Color(0xFFFFD700)
private val GoldGlow = Color(0x80FFD700)
private val SilverColor = Color(0xFFE8E8E8)
private val SilverGlow = Color(0x80E8E8E8)
private val BronzeColor = Color(0xFFCD7F32)
private val BronzeGlow = Color(0x80CD7F32)

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return ""
    return try {
        java.time.Instant.ofEpochMilli(timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun RankingScreen(
    onNavigateBack: () -> Unit,
    viewModel: RankingViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedTabIndex,
        pageCount = { 3 }
    )
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf(
        stringResource(R.string.ranking_tab_classic),
        stringResource(R.string.ranking_tab_timed),
        stringResource(R.string.ranking_tab_xp_global)
    )

    // Sync pager with tab selection
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onTabSelected(pagerState.currentPage)
    }

    Scaffold(
        topBar = {
            PrideTopAppBar(
                title = stringResource(R.string.ranking),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.weight(1f)) {
            AnimatedScreenBackground(
                orbColor1 = NeonPink,
                orbColor2 = NeonPurple
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Banner de error sutil cuando algún ranking no cargo
                    if (uiState.hasError) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.error_ranking_partial),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.refreshRanking() }) {
                                    Text(
                                        text = stringResource(R.string.error_retry),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }

                    // Tab Row
                    SecondaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = White,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                            height = 3.dp,
                            color = NeonPurple
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (pagerState.currentPage == index) White else White.copy(alpha = 0.6f)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    // Shimmer loading state
                    AnimatedVisibility(
                        visible = uiState.isLoading,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(8) {
                                ShimmerRankingItem()
                            }
                        }
                    }

                    // Actual content
                    AnimatedVisibility(
                        visible = !uiState.isLoading,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut()
                    ) {
                        when (page) {
                            0, 1 -> {
                                val rankingList = if (page == 0) uiState.rankingList else uiState.timedRankingList
                                if (rankingList.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.ranking_empty),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = White.copy(alpha = 0.6f)
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        itemsIndexed(
                                            items = rankingList,
                                            // Key estable sin index ni score: el nombre identifica
                                            // al usuario de forma unica. El prefijo de tab
                                            // evita colision entre pestanas Classic y Timed.
                                            key = { _, user -> "${page}_${user.name}" }
                                        ) { index, user ->
                                            VibrantRankingItem(
                                                position = index + 1,
                                                user = user
                                            )
                                        }
                                    }
                                }
                            }
                            2 -> {
                                // XP Global Leaderboard
                                if (uiState.xpLeaderboardList.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.ranking_empty),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = White.copy(alpha = 0.6f)
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        itemsIndexed(
                                            items = uiState.xpLeaderboardList,
                                            // Key estable: uid del usuario sin incluir index
                                            key = { _, entry -> "xp_${entry.uid}" }
                                        ) { index, entry ->
                                            XpLeaderboardItem(
                                                position = index + 1,
                                                entry = entry
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } // cierra Column interna del AnimatedScreenBackground
        } // cierra AnimatedScreenBackground
        } // cierra Box(weight(1f))

            // Banner publicitario al fondo (solo cuando el usuario no pago)
            if (uiState.showBannerAd) {
                BannerAdView(
                    adUnitId = stringResource(R.string.BANNER_RANKING)
                )
            }
        } // cierra Column exterior
    } // cierra Scaffold
} // cierra RankingScreen

@Composable
private fun VibrantRankingItem(
    position: Int,
    user: User,
    modifier: Modifier = Modifier
) {
    val isTopThree = position <= 3
    val positionInfo = when (position) {
        1 -> Triple(GoldColor, GoldGlow, listOf(GoldColor, GoldColor.copy(alpha = 0.7f)))
        2 -> Triple(SilverColor, SilverGlow, listOf(SilverColor, SilverColor.copy(alpha = 0.7f)))
        3 -> Triple(BronzeColor, BronzeGlow, listOf(BronzeColor, BronzeColor.copy(alpha = 0.7f)))
        else -> Triple(GradientPositionTop, GlowPurple, listOf(GradientPositionTop, GradientPositionBottom))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isTopThree) 16.dp else 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = positionInfo.second,
                spotColor = positionInfo.second
            )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkSurfaceVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                if (isTopThree) positionInfo.first.copy(alpha = 0.15f) else Color.Transparent,
                                Color.Transparent
                            )
                        )
                    )
                    .then(
                        if (isTopThree) {
                            Modifier.border(
                                width = 2.dp,
                                brush = Brush.linearGradient(positionInfo.third),
                                shape = RoundedCornerShape(20.dp)
                            )
                        } else Modifier
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Position badge with glow
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .drawBehind {
                                if (isTopThree) {
                                    drawCircle(
                                        color = positionInfo.second,
                                        radius = size.minDimension / 1.5f
                                    )
                                }
                            }
                            .background(
                                brush = Brush.linearGradient(positionInfo.third),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = position.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            ),
                            color = if (position == 2) Color.DarkGray else White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // User avatar with glow
                    Box(
                        modifier = Modifier
                            .drawBehind {
                                if (isTopThree) {
                                    drawCircle(
                                        color = positionInfo.second.copy(alpha = 0.5f),
                                        radius = size.minDimension / 1.6f
                                    )
                                }
                            }
                    ) {
                        UserAvatar(
                            userImage = user.userImage,
                            isTopThree = isTopThree,
                            borderColor = if (isTopThree) positionInfo.first else NeonPurple.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // User info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = user.name.ifEmpty { "Unknown" },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = if (isTopThree) Shadow(
                                    color = positionInfo.first.copy(alpha = 0.5f),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 4f
                                ) else null
                            ),
                            color = White
                        )
                        Text(
                            text = formatTimestamp(user.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = White.copy(alpha = 0.6f)
                        )
                    }

                    // Score badge with glow
                    Box(
                        modifier = Modifier
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            GradientPointsTop.copy(alpha = 0.4f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = 60f
                                )
                            }
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(GradientPointsTop, GradientPointsBottom)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = user.score.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            ),
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun XpLeaderboardItem(
    position: Int,
    entry: XpLeaderboardEntry,
    modifier: Modifier = Modifier
) {
    val isTopThree = position <= 3
    val positionInfo = when (position) {
        1 -> Triple(GoldColor, GoldGlow, listOf(GoldColor, GoldColor.copy(alpha = 0.7f)))
        2 -> Triple(SilverColor, SilverGlow, listOf(SilverColor, SilverColor.copy(alpha = 0.7f)))
        3 -> Triple(BronzeColor, BronzeGlow, listOf(BronzeColor, BronzeColor.copy(alpha = 0.7f)))
        else -> Triple(GradientPositionTop, GlowPurple, listOf(GradientPositionTop, GradientPositionBottom))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isTopThree) 16.dp else 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = positionInfo.second,
                spotColor = positionInfo.second
            )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkSurfaceVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                if (isTopThree) positionInfo.first.copy(alpha = 0.15f) else Color.Transparent,
                                Color.Transparent
                            )
                        )
                    )
                    .then(
                        if (isTopThree) {
                            Modifier.border(
                                width = 2.dp,
                                brush = Brush.linearGradient(positionInfo.third),
                                shape = RoundedCornerShape(20.dp)
                            )
                        } else Modifier
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Position badge with glow
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .drawBehind {
                                if (isTopThree) {
                                    drawCircle(
                                        color = positionInfo.second,
                                        radius = size.minDimension / 1.5f
                                    )
                                }
                            }
                            .background(
                                brush = Brush.linearGradient(positionInfo.third),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = position.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            ),
                            color = if (position == 2) Color.DarkGray else White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // User avatar with glow
                    Box(
                        modifier = Modifier
                            .drawBehind {
                                if (isTopThree) {
                                    drawCircle(
                                        color = positionInfo.second.copy(alpha = 0.5f),
                                        radius = size.minDimension / 1.6f
                                    )
                                }
                            }
                    ) {
                        UserAvatar(
                            userImage = entry.imageBase64,
                            isTopThree = isTopThree,
                            borderColor = if (isTopThree) positionInfo.first else NeonPurple.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // User info with level and title
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = entry.nickname.ifBlank { "Unknown" },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = if (isTopThree) Shadow(
                                    color = positionInfo.first.copy(alpha = 0.5f),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 4f
                                ) else null
                            ),
                            color = White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lv.${entry.level}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = NeonPurple
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = entry.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // XP badge with glow
                    Box(
                        modifier = Modifier
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            NeonPurple.copy(alpha = 0.4f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = 60f
                                )
                            }
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(NeonPurple, NeonPink)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "${entry.totalXp} XP",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            ),
                            color = White
                        )
                    }
                }
            }
        }
    }
}
