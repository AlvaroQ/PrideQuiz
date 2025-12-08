package com.quiz.pride.ui.ranking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import com.quiz.domain.User
import com.quiz.domain.XpLeaderboardEntry
import com.quiz.pride.R
import com.quiz.pride.ui.components.PrideTopAppBar
import com.quiz.pride.ui.components.ShimmerRankingItem
import com.quiz.pride.ui.theme.DarkSurfaceVariant
import com.quiz.pride.ui.theme.GradientBackgroundEnd
import com.quiz.pride.ui.theme.GradientBackgroundMid
import com.quiz.pride.ui.theme.GradientBackgroundStart
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
import java.text.SimpleDateFormat
import java.util.Date
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

    // Check if it's a Base64 image
    val isBase64 = userImage != null && !userImage.startsWith("http") && userImage.length > 100

    if (isBase64 && userImage != null) {
        // Decode Base64 image
        val bitmap = try {
            val imageBytes = Base64.decode(userImage, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            null
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
    } else if (!userImage.isNullOrEmpty()) {
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
    } else {
        DefaultAvatar(modifier = avatarModifier)
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

private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return ""
    return try {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun RankingScreen(
    onNavigateBack: () -> Unit,
    viewModel: RankingViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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

    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "ranking_bg")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    Scaffold(
        topBar = {
            PrideTopAppBar(
                title = stringResource(R.string.ranking),
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
                    .size(140.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 60.dp, y = -50.dp - floatOffset.dp)
                    .alpha(0.15f)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(NeonPurple, Color.Transparent)
                            ),
                            radius = size.minDimension / 2
                        )
                    }
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Tab Row
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
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
                                            key = { index, user -> "${page}_${user.name}_$index" }
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
                                            key = { index, entry -> "xp_${entry.uid}_$index" }
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
            }
        }
    }
}

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

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(),
        label = "item_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
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
                            text = user.name ?: "Unknown",
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
                            text = (user.score ?: 0).toString(),
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

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(),
        label = "xp_item_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
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
