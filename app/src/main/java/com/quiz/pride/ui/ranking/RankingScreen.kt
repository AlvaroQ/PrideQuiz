package com.quiz.pride.ui.ranking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import coil3.compose.SubcomposeAsyncImage
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import com.quiz.pride.ui.components.TrackScreenTime
import com.quiz.pride.managers.AnalyticsManager
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
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

// ---------------------------------------------------------------------------
// Avatar composable
// ---------------------------------------------------------------------------

@Composable
private fun UserAvatar(
    userImage: String?,
    isTopThree: Boolean,
    borderColor: Color,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)
        .border(
            width = if (isTopThree) 3.dp else 2.dp,
            color = borderColor,
            shape = CircleShape
        )

    when {
        userImage != null && !userImage.startsWith("http") && userImage.length > 100 -> {
            // Base64 — decodificado en IO thread para evitar OOM en composition thread
            val bitmapState = produceState<android.graphics.Bitmap?>(
                initialValue = null,
                key1 = userImage
            ) {
                value = withContext(Dispatchers.IO) {
                    runCatching {
                        val bytes = Base64.decode(userImage, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }.getOrNull()
                }
            }
            val bitmap = bitmapState.value

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
            // URL — use Coil
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

// ---------------------------------------------------------------------------
// Colores de medallas
// ---------------------------------------------------------------------------

private val GoldColor   = Color(0xFFFFD700)
private val GoldGlow    = Color(0x80FFD700)
private val SilverColor = Color(0xFFE8E8E8)
private val SilverGlow  = Color(0x80E8E8E8)
private val BronzeColor = Color(0xFFE8943A)
private val BronzeGlow  = Color(0x99E8943A)

private fun medalEmoji(position: Int) = when (position) {
    1 -> "\uD83E\uDD47" // 🥇
    2 -> "\uD83E\uDD48" // 🥈
    3 -> "\uD83E\uDD49" // 🥉
    else -> ""
}

private fun medalColor(position: Int) = when (position) {
    1 -> GoldColor
    2 -> SilverColor
    3 -> BronzeColor
    else -> NeonPurple
}

private fun medalGlow(position: Int) = when (position) {
    1 -> GoldGlow
    2 -> SilverGlow
    3 -> BronzeGlow
    else -> GlowPurple
}

private fun medalGradient(position: Int) = when (position) {
    1 -> listOf(GoldColor,   GoldColor.copy(alpha = 0.7f))
    2 -> listOf(SilverColor, SilverColor.copy(alpha = 0.7f))
    3 -> listOf(BronzeColor, BronzeColor.copy(alpha = 0.7f))
    else -> listOf(GradientPositionTop, GradientPositionBottom)
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return ""
    return try {
        java.time.Instant.ofEpochMilli(timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            .format(
                java.time.format.DateTimeFormatter.ofPattern(
                    "dd MMM yyyy",
                    Locale.getDefault()
                )
            )
    } catch (e: Exception) {
        ""
    }
}

// ---------------------------------------------------------------------------
// Podium slot reutilizable
// ---------------------------------------------------------------------------

/**
 * Un slot del podio. Acepta los datos minimos para ser reutilizable
 * tanto con [User] como con [XpLeaderboardEntry].
 *
 * @param position    1, 2 o 3
 * @param name        Nombre a mostrar
 * @param valueText   Puntaje ya formateado ("42" o "1500 XP")
 * @param userImage   URL o Base64 de avatar (puede ser null)
 * @param avatarSize  60.dp para posicion 1, 48.dp para 2 y 3
 * @param podiumHeight Alto de la base del podio (64 / 44 / 32)
 */
@Composable
private fun PodiumSlot(
    position: Int,
    name: String,
    valueText: String,
    userImage: String?,
    avatarSize: Dp,
    podiumHeight: Dp,
    modifier: Modifier = Modifier
) {
    val color = medalColor(position)
    val glow  = medalGlow(position)
    val medal = medalEmoji(position)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Emoji de medalla
        Text(
            text = medal,
            fontSize = if (position == 1) 28.sp else 22.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Avatar con glow
        Box(
            modifier = Modifier.drawBehind {
                drawCircle(color = glow, radius = size.minDimension / 1.4f)
            }
        ) {
            UserAvatar(
                userImage = userImage,
                isTopThree = true,
                borderColor = color,
                size = avatarSize
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nombre
        Text(
            text = name.ifEmpty { "Unknown" },
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = color.copy(alpha = 0.6f),
                    offset = Offset(0f, 0f),
                    blurRadius = 6f
                )
            ),
            color = White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Caja de puntaje con tinte del color de medalla
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.18f))
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    color = color.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Base del podio — altura variable segun posicion
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(color.copy(alpha = 0.45f), color.copy(alpha = 0.20f))
                    ),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(color.copy(alpha = 0.7f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = position.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = color.copy(alpha = 0.85f)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// PodiumEntry + UnifiedPodiumSection — top 3 (reutilizable para User y XP)
// ---------------------------------------------------------------------------

/**
 * Datos minimos para renderizar un slot del podio.
 * Permite unificar [PodiumSection] para User y XpLeaderboardEntry.
 */
private data class PodiumEntry(
    val name: String,
    val valueText: String,
    val userImage: String?
)

@Composable
private fun UnifiedPodiumSection(
    entries: List<PodiumEntry>,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (entries.size >= 2) {
                PodiumSlot(
                    position = 2,
                    name = entries[1].name,
                    valueText = entries[1].valueText,
                    userImage = entries[1].userImage,
                    avatarSize = 48.dp,
                    podiumHeight = 44.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            PodiumSlot(
                position = 1,
                name = entries[0].name,
                valueText = entries[0].valueText,
                userImage = entries[0].userImage,
                avatarSize = 60.dp,
                podiumHeight = 64.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (entries.size >= 3) {
                PodiumSlot(
                    position = 3,
                    name = entries[2].name,
                    valueText = entries[2].valueText,
                    userImage = entries[2].userImage,
                    avatarSize = 48.dp,
                    podiumHeight = 32.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Empty state visual
// ---------------------------------------------------------------------------

@Composable
private fun ClassicModeFilterRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf("" to "Legacy", "NORMAL" to "Normal", "ADVANCE" to "Advanced")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (value, label) ->
            val isSelected = selectedFilter == value
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onFilterSelected(value) }
                    .then(
                        if (isSelected) Modifier.background(
                            Brush.horizontalGradient(listOf(NeonPurple, NeonPink)),
                            RoundedCornerShape(20.dp)
                        ) else Modifier.border(
                            1.dp,
                            White.copy(alpha = 0.3f),
                            RoundedCornerShape(20.dp)
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) White else White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun EmptyRankingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = NeonPurple.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.ranking_empty_title),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.ranking_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

// ---------------------------------------------------------------------------
// RankingScreen principal
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    onNavigateBack: () -> Unit,
    viewModel: RankingViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val analyticsManager: AnalyticsManager = koinInject()
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

    // Scroll depth tracking: estado compartido para el maximo indice visto
    val rankingListState = rememberLazyListState()
    val maxVisibleIndex = remember { mutableIntStateOf(0) }

    // Actualizar maxVisibleIndex cuando cambia el scroll
    LaunchedEffect(rankingListState.layoutInfo.visibleItemsInfo) {
        val lastVisible = rankingListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        if (lastVisible > maxVisibleIndex.intValue) {
            maxVisibleIndex.intValue = lastVisible
        }
    }

    // Reportar scroll depth y tiempo en pantalla al salir
    TrackScreenTime(AnalyticsManager.SCREEN_RANKING, analyticsManager)
    DisposableEffect(Unit) {
        onDispose {
            val currentTabItems = when (pagerState.currentPage) {
                0 -> uiState.rankingList.size
                1 -> uiState.timedRankingList.size
                else -> uiState.xpLeaderboardList.size
            }
            analyticsManager.analyticsRankingScrollDepth(
                tab = when (pagerState.currentPage) {
                    0 -> "classic"
                    1 -> "timed"
                    else -> "xp_global"
                },
                maxPositionSeen = maxVisibleIndex.intValue + 1,
                totalItems = currentTabItems
            )
        }
    }

    // Sincronizar pager con seleccion de tab
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
                            // Banner de error cuando algun ranking no cargo
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
                                        TextButton(onClick = { viewModel.onRetryClicked() }) {
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
                                                    fontWeight = if (pagerState.currentPage == index)
                                                        FontWeight.Bold
                                                    else
                                                        FontWeight.Normal
                                                ),
                                                color = if (pagerState.currentPage == index)
                                                    White
                                                else
                                                    White.copy(alpha = 0.6f)
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
                                // Shimmer de carga
                                AnimatedVisibility(
                                    visible = uiState.isLoading,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(8, key = { "shimmer_${page}_$it" }) {
                                            ShimmerRankingItem()
                                        }
                                    }
                                }

                                // Contenido real
                                AnimatedVisibility(
                                    visible = !uiState.isLoading,
                                    enter = fadeIn() + slideInVertically { it / 2 },
                                    exit = fadeOut()
                                ) {
                                    when (page) {
                                        0, 1 -> {
                                            val rawList =
                                                if (page == 0) uiState.rankingList
                                                else uiState.timedRankingList

                                            // Apply gameMode filter only on Classic tab
                                            val rankingList = if (page == 0 && uiState.classicModeFilter.isNotEmpty()) {
                                                rawList.filter { it.gameMode == uiState.classicModeFilter }
                                            } else {
                                                rawList
                                            }

                                            Column(modifier = Modifier.fillMaxSize()) {
                                            // Filter chips only on Classic tab
                                            if (page == 0) {
                                                ClassicModeFilterRow(
                                                    selectedFilter = uiState.classicModeFilter,
                                                    onFilterSelected = { viewModel.onClassicModeFilterSelected(it) }
                                                )
                                            }

                                            if (rankingList.isEmpty()) {
                                                EmptyRankingState(
                                                    modifier = Modifier.fillMaxSize().weight(1f)
                                                )
                                            } else {
                                                val topThree = rankingList.take(3)
                                                val rest     = rankingList.drop(3)

                                                LazyColumn(
                                                    state = rankingListState,
                                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                                    contentPadding = PaddingValues(
                                                        start = 16.dp,
                                                        end = 16.dp,
                                                        top = 8.dp,
                                                        bottom = 16.dp
                                                    ),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    // Podio cuando hay exactamente 3 o mas
                                                    if (topThree.size >= 3) {
                                                        item(key = "podium_${page}") {
                                                            UnifiedPodiumSection(
                                                                entries = topThree.map { user ->
                                                                    PodiumEntry(
                                                                        name = user.name,
                                                                        valueText = user.score.toString(),
                                                                        userImage = user.userImage
                                                                    )
                                                                }
                                                            )
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                        }
                                                    } else {
                                                        // Menos de 3: items normales con animacion
                                                        itemsIndexed(
                                                            items = topThree,
                                                            key = { idx, user ->
                                                                "${page}_top_${idx}_${user.name}"
                                                            }
                                                        ) { idx, user ->
                                                            AnimatedVisibility(
                                                                visible = true,
                                                                enter = fadeIn(
                                                                    tween(300, delayMillis = idx * 50)
                                                                ) + slideInVertically(
                                                                    tween(300, delayMillis = idx * 50)
                                                                ) { it / 3 }
                                                            ) {
                                                                VibrantRankingItem(
                                                                    position = idx + 1,
                                                                    user = user
                                                                )
                                                            }
                                                        }
                                                    }

                                                    // Posiciones 4+
                                                    itemsIndexed(
                                                        items = rest,
                                                        key = { idx, user ->
                                                            "${page}_rest_${idx}_${user.name}"
                                                        }
                                                    ) { idx, user ->
                                                        AnimatedVisibility(
                                                            visible = true,
                                                            enter = fadeIn(
                                                                tween(300, delayMillis = (idx + 3) * 50)
                                                            ) + slideInVertically(
                                                                tween(300, delayMillis = (idx + 3) * 50)
                                                            ) { it / 3 }
                                                        ) {
                                                            VibrantRankingItem(
                                                                position = idx + 4,
                                                                user = user,
                                                                alternateTint = (idx + 4) % 2 == 0
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            } // Column
                                        }

                                        2 -> {
                                            // XP Global Leaderboard
                                            val xpList = uiState.xpLeaderboardList

                                            if (xpList.isEmpty()) {
                                                EmptyRankingState(
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                val topThree = xpList.take(3)
                                                val rest     = xpList.drop(3)

                                                LazyColumn(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentPadding = PaddingValues(
                                                        start = 16.dp,
                                                        end = 16.dp,
                                                        top = 8.dp,
                                                        bottom = 16.dp
                                                    ),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    if (topThree.size >= 3) {
                                                        item(key = "xp_podium") {
                                                            UnifiedPodiumSection(
                                                                entries = topThree.map { entry ->
                                                                    PodiumEntry(
                                                                        name = entry.nickname.ifBlank { "Unknown" },
                                                                        valueText = "${entry.totalXp} XP",
                                                                        userImage = entry.imageBase64
                                                                    )
                                                                }
                                                            )
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                        }
                                                    } else {
                                                        itemsIndexed(
                                                            items = topThree,
                                                            key = { _, entry ->
                                                                "xp_top_${entry.uid}"
                                                            }
                                                        ) { idx, entry ->
                                                            AnimatedVisibility(
                                                                visible = true,
                                                                enter = fadeIn(
                                                                    tween(300, delayMillis = idx * 50)
                                                                ) + slideInVertically(
                                                                    tween(300, delayMillis = idx * 50)
                                                                ) { it / 3 }
                                                            ) {
                                                                XpLeaderboardItem(
                                                                    position = idx + 1,
                                                                    entry = entry
                                                                )
                                                            }
                                                        }
                                                    }

                                                    itemsIndexed(
                                                        items = rest,
                                                        key = { _, entry -> "xp_rest_${entry.uid}" }
                                                    ) { idx, entry ->
                                                        AnimatedVisibility(
                                                            visible = true,
                                                            enter = fadeIn(
                                                                tween(300, delayMillis = (idx + 3) * 50)
                                                            ) + slideInVertically(
                                                                tween(300, delayMillis = (idx + 3) * 50)
                                                            ) { it / 3 }
                                                        ) {
                                                            XpLeaderboardItem(
                                                                position = idx + 4,
                                                                entry = entry,
                                                                alternateTint = (idx + 4) % 2 == 0
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } // cierra Column interna
                } // cierra AnimatedScreenBackground
            } // cierra Box(weight(1f))

            // Banner publicitario al fondo
            if (uiState.showBannerAd) {
                BannerAdView(
                    adUnitId = stringResource(R.string.BANNER_RANKING)
                )
            }
        } // cierra Column exterior
    } // cierra Scaffold
}

// ---------------------------------------------------------------------------
// VibrantRankingItem — posiciones 4+ (o top si hay menos de 3 entradas)
// ---------------------------------------------------------------------------

@Composable
private fun VibrantRankingItem(
    position: Int,
    user: User,
    alternateTint: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isTopThree   = position <= 3
    val mColor       = medalColor(position)
    val mGlow        = medalGlow(position)
    val gradient     = medalGradient(position)

    // Tint alterno muy sutil para posiciones pares (solo 4+)
    val alternateBg = if (!isTopThree && alternateTint)
        NeonPurple.copy(alpha = 0.08f)
    else
        Color.Transparent

    val itemShape = RoundedCornerShape(if (isTopThree) 16.dp else 12.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isTopThree) 10.dp else 2.dp,
                shape = itemShape,
                ambientColor = mGlow,
                spotColor = mGlow
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = itemShape,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            if (isTopThree) mColor.copy(alpha = 0.15f) else alternateBg,
                            Color.Transparent
                        )
                    )
                )
                .then(
                    if (isTopThree) {
                        Modifier.border(
                            width = 2.dp,
                            brush = Brush.linearGradient(gradient),
                            shape = itemShape
                        )
                    } else Modifier
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp), // reducido de 16 a 12
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge de posicion
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .drawBehind {
                                if (isTopThree) {
                                    drawCircle(color = mGlow, radius = size.minDimension / 1.5f)
                                }
                            }
                            .background(
                                brush = Brush.linearGradient(gradient),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = position.toString(),
                            style = MaterialTheme.typography.titleSmall.copy(
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

                    Spacer(modifier = Modifier.width(12.dp))

                    // Avatar con glow
                    Box(
                        modifier = Modifier.drawBehind {
                            if (isTopThree) {
                                drawCircle(
                                    color = mGlow.copy(alpha = 0.5f),
                                    radius = size.minDimension / 1.6f
                                )
                            }
                        }
                    ) {
                        UserAvatar(
                            userImage = user.userImage,
                            isTopThree = isTopThree,
                            borderColor = if (isTopThree) mColor else NeonPurple.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name.ifEmpty { "Unknown" },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = if (isTopThree) Shadow(
                                    color = mColor.copy(alpha = 0.5f),
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

                    // Badge de puntaje — compacto para 4+
                    Box(
                        modifier = Modifier
                            .then(
                                if (isTopThree) Modifier.drawBehind {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                GradientPointsTop.copy(alpha = 0.4f),
                                                Color.Transparent
                                            )
                                        ),
                                        radius = size.minDimension * 0.8f
                                    )
                                } else Modifier
                            )
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(GradientPointsTop, GradientPointsBottom)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(
                                horizontal = if (isTopThree) 16.dp else 12.dp,
                                vertical = if (isTopThree) 10.dp else 7.dp
                            )
                    ) {
                        Text(
                            text = user.score.toString(),
                            // labelMedium en lugar de titleMedium para 4+
                            style = MaterialTheme.typography.labelMedium.copy(
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

// ---------------------------------------------------------------------------
// XpLeaderboardItem — posiciones 4+ (o top si hay menos de 3 entradas)
// ---------------------------------------------------------------------------

@Composable
private fun XpLeaderboardItem(
    position: Int,
    entry: XpLeaderboardEntry,
    alternateTint: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isTopThree = position <= 3
    val mColor     = medalColor(position)
    val mGlow      = medalGlow(position)
    val gradient   = medalGradient(position)

    val alternateBg = if (!isTopThree && alternateTint)
        NeonPurple.copy(alpha = 0.08f)
    else
        Color.Transparent

    val itemShape = RoundedCornerShape(if (isTopThree) 16.dp else 12.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isTopThree) 10.dp else 2.dp,
                shape = itemShape,
                ambientColor = mGlow,
                spotColor = mGlow
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = itemShape,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            if (isTopThree) mColor.copy(alpha = 0.15f) else alternateBg,
                            Color.Transparent
                        )
                    )
                )
                .then(
                    if (isTopThree) {
                        Modifier.border(
                            width = 2.dp,
                            brush = Brush.linearGradient(gradient),
                            shape = itemShape
                        )
                    } else Modifier
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge de posicion
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .drawBehind {
                            if (isTopThree) {
                                drawCircle(color = mGlow, radius = size.minDimension / 1.5f)
                            }
                        }
                        .background(
                            brush = Brush.linearGradient(gradient),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = position.toString(),
                        style = MaterialTheme.typography.titleSmall.copy(
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

                Spacer(modifier = Modifier.width(12.dp))

                // Avatar
                Box(
                    modifier = Modifier.drawBehind {
                        if (isTopThree) {
                            drawCircle(
                                color = mGlow.copy(alpha = 0.5f),
                                radius = size.minDimension / 1.6f
                            )
                        }
                    }
                ) {
                    UserAvatar(
                        userImage = entry.imageBase64,
                        isTopThree = isTopThree,
                        borderColor = if (isTopThree) mColor else NeonPurple.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Info: nombre, nivel y titulo
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.nickname.ifBlank { "Unknown" },
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            shadow = if (isTopThree) Shadow(
                                color = mColor.copy(alpha = 0.5f),
                                offset = Offset(0f, 0f),
                                blurRadius = 4f
                            ) else null
                        ),
                        color = White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                // Badge de XP — compacto para 4+
                Box(
                    modifier = Modifier
                        .then(
                            if (isTopThree) Modifier.drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            NeonPurple.copy(alpha = 0.4f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.minDimension * 0.8f
                                )
                            } else Modifier
                        )
                        .background(
                            brush = Brush.horizontalGradient(listOf(NeonPurple, NeonPink)),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(
                            horizontal = if (isTopThree) 12.dp else 10.dp,
                            vertical = if (isTopThree) 10.dp else 7.dp
                        )
                ) {
                    Text(
                        text = "${entry.totalXp} XP",
                        style = MaterialTheme.typography.labelMedium.copy(
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
