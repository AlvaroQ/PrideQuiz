package com.quiz.pride.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.pride.ui.theme.GlowPink
import com.quiz.pride.ui.theme.GlowPurple
import com.quiz.pride.ui.theme.GradientBackgroundEnd
import com.quiz.pride.ui.theme.GradientBackgroundMid
import com.quiz.pride.ui.theme.GradientBackgroundMidWarm
import com.quiz.pride.ui.theme.GradientBackgroundStart
import com.quiz.pride.ui.theme.GradientPointsBottom
import com.quiz.pride.ui.theme.GradientPointsTop
import com.quiz.pride.ui.theme.GradientPositionBottom
import com.quiz.pride.ui.theme.GradientPositionTop
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.PrideRed
import com.quiz.pride.ui.theme.PrideQuizTheme
import com.quiz.pride.ui.theme.RainbowColors
import com.quiz.pride.ui.theme.Shimmer
import com.quiz.pride.ui.theme.StartGradientBottom
import com.quiz.pride.ui.theme.StartGradientTop
import com.quiz.pride.ui.theme.White

/**
 * Loading indicator with Pride rainbow animation
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
        )
    }
}

/**
 * Vibrant gradient card with glow effect
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(GradientBackgroundStart, GradientBackgroundMid),
    glowColor: Color = GlowPink,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 400f
        ),
        label = "card_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = glowColor,
                spotColor = glowColor
            )
    ) {
        Card(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = gradientColors,
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    ),
                content = content
            )
        }
    }
}

/**
 * Rainbow gradient background
 */
@Composable
fun RainbowGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to GradientBackgroundStart,
                        0.35f to GradientBackgroundMid,
                        0.65f to GradientBackgroundMidWarm,
                        0.85f to GradientBackgroundEnd,
                        1.0f to GradientBackgroundEnd
                    )
                )
            )
    ) {
        content()
    }
}

/**
 * Animated rainbow divider
 */
@Composable
fun RainbowDivider(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(
                Brush.horizontalGradient(
                    colors = RainbowColors,
                    startX = offset * 500f,
                    endX = offset * 500f + 1000f
                )
            )
    )
}

/**
 * Top App Bar with gradient background
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrideTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = White,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                color = contentColor
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = contentColor
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        ),
        modifier = modifier
    )
}

/**
 * Game Top Bar with vibrant gradient and stats
 */
@Composable
fun GameTopBar(
    points: Int,
    lives: Int,
    maxLives: Int = 2,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        GradientPositionTop,
                        GradientPositionBottom
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = White
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Points with glow effect
        PointsDisplay(points = points, modifier = Modifier.weight(1f))

        // Lives indicator
        LifeIndicator(
            currentLives = lives,
            maxLives = maxLives
        )
    }
}

/**
 * Points display with golden glow
 */
@Composable
fun PointsDisplay(
    points: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$points pts",
        style = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            shadow = Shadow(
                color = GradientPointsTop,
                offset = Offset(0f, 0f),
                blurRadius = 8f
            )
        ),
        color = GradientPointsBottom,
        modifier = modifier
    )
}

/**
 * Animated life indicator with hearts
 */
@Composable
fun LifeIndicator(
    currentLives: Int,
    maxLives: Int = 2,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(maxLives) { index ->
            val isAlive = index < currentLives

            val scale by animateFloatAsState(
                targetValue = if (isAlive) 1f else 0.8f,
                animationSpec = spring(
                    dampingRatio = 0.5f,
                    stiffness = 300f
                ),
                label = "heart_scale"
            )

            Icon(
                imageVector = if (isAlive) {
                    Icons.Default.Favorite
                } else {
                    Icons.Default.FavoriteBorder
                },
                contentDescription = null,
                tint = if (isAlive) PrideRed else White.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(28.dp)
                    .scale(scale)
                    .drawBehind {
                        if (isAlive) {
                            drawCircle(
                                color = PrideRed.copy(alpha = 0.15f),
                                radius = size.minDimension * 0.5f
                            )
                        }
                    }
            )
        }
    }
}

/**
 * Section header with Pride accent
 */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * Empty state with Pride styling
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Pride button with vibrant gradient and glow
 */
@Composable
fun PrideButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(NeonPink, GradientBackgroundStart),
    glowColor: Color = GlowPurple,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 400f
        ),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (enabled) 12.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (enabled) glowColor else Color.Transparent,
                spotColor = if (enabled) glowColor else Color.Transparent
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) {
                    Brush.horizontalGradient(gradientColors)
                } else {
                    Brush.horizontalGradient(
                        listOf(Color.Gray, Color.DarkGray)
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 32.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 16.sp,
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

/**
 * Shimmer effect modifier for loading states
 */
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    return this.drawBehind {
        val width = size.width
        val startX = shimmerOffset * width

        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Shimmer,
                    Color.Transparent
                ),
                startX = startX,
                endX = startX + width * 0.5f
            )
        )
    }
}

/**
 * Glass card with blur effect appearance
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = spring(),
        label = "glass_scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            White.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                ),
            content = content
        )
    }
}

/**
 * Animated gradient background with floating glow orbs.
 * Replaces the repeated pattern across 7 screens.
 */
@Composable
fun AnimatedScreenBackground(
    orbColor1: Color = NeonPink,
    orbColor2: Color = NeonPurple,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to GradientBackgroundStart,
                        0.35f to GradientBackgroundMid,
                        0.65f to GradientBackgroundMidWarm,
                        0.85f to GradientBackgroundEnd,
                        1.0f to GradientBackgroundEnd
                    )
                )
            )
    ) {
        // Decorative glow orb 1
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp, y = 100.dp + floatOffset.dp)
                .alpha(0.3f)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(orbColor1, Color.Transparent)
                        ),
                        radius = size.minDimension / 2
                    )
                }
        )

        // Decorative glow orb 2
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = 200.dp - floatOffset.dp)
                .alpha(0.25f)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(orbColor2, Color.Transparent)
                        ),
                        radius = size.minDimension / 2
                    )
                }
        )

        content()
    }
}

// ============================================
// PREVIEWS
// ============================================

@Preview(showBackground = true, name = "RainbowDivider - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "RainbowDivider - Dark")
@Composable
private fun RainbowDividerPreview() {
    PrideQuizTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            RainbowDivider()
        }
    }
}

@Preview(showBackground = true, name = "LoadingIndicator - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "LoadingIndicator - Dark")
@Composable
private fun LoadingIndicatorPreview() {
    PrideQuizTheme {
        Box(modifier = Modifier.size(200.dp)) {
            LoadingIndicator()
        }
    }
}

@Preview(showBackground = true, name = "PointsDisplay - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "PointsDisplay - Dark")
@Composable
private fun PointsDisplayPreview() {
    PrideQuizTheme {
        Box(
            modifier = Modifier
                .background(GradientPositionTop)
                .padding(16.dp)
        ) {
            PointsDisplay(points = 1250)
        }
    }
}

@Preview(showBackground = true, name = "LifeIndicator - Full - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "LifeIndicator - Full - Dark")
@Composable
private fun LifeIndicatorFullPreview() {
    PrideQuizTheme {
        Box(
            modifier = Modifier
                .background(GradientPositionTop)
                .padding(16.dp)
        ) {
            LifeIndicator(currentLives = 2, maxLives = 2)
        }
    }
}

@Preview(showBackground = true, name = "LifeIndicator - Partial")
@Composable
private fun LifeIndicatorPartialPreview() {
    PrideQuizTheme {
        Box(
            modifier = Modifier
                .background(GradientPositionTop)
                .padding(16.dp)
        ) {
            LifeIndicator(currentLives = 1, maxLives = 2)
        }
    }
}

@Preview(showBackground = true, name = "GameTopBar - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "GameTopBar - Dark")
@Composable
private fun GameTopBarPreview() {
    PrideQuizTheme {
        GameTopBar(
            points = 750,
            lives = 1,
            maxLives = 2,
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "PrideTopAppBar - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "PrideTopAppBar - Dark")
@Composable
private fun PrideTopAppBarPreview() {
    PrideQuizTheme {
        PrideTopAppBar(
            title = "Configuracion",
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "PrideButton - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "PrideButton - Dark")
@Composable
private fun PrideButtonPreview() {
    PrideQuizTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrideButton(
                text = "Jugar",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
            PrideButton(
                text = "Deshabilitado",
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, name = "GlassCard - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "GlassCard - Dark")
@Composable
private fun GlassCardPreview() {
    PrideQuizTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            GlassCard(onClick = {}) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pride Quiz",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Juego de preguntas sobre comunidad LGBTQ+",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "SectionHeader - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "SectionHeader - Dark")
@Composable
private fun SectionHeaderPreview() {
    PrideQuizTheme {
        SectionHeader(text = "Modo Normal")
    }
}

@Preview(showBackground = true, name = "EmptyState - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "EmptyState - Dark")
@Composable
private fun EmptyStatePreview() {
    PrideQuizTheme {
        Box(modifier = Modifier.size(300.dp)) {
            EmptyState(message = "No hay datos disponibles")
        }
    }
}

@Preview(showBackground = true, name = "GradientCard - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "GradientCard - Dark")
@Composable
private fun GradientCardPreview() {
    PrideQuizTheme {
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp)
        ) {
            GradientCard(
                gradientColors = listOf(StartGradientTop, StartGradientBottom),
                glowColor = GlowPink,
                onClick = {}
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Jugar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = false, name = "AnimatedScreenBackground")
@Composable
private fun AnimatedScreenBackgroundPreview() {
    PrideQuizTheme {
        AnimatedScreenBackground(
            orbColor1 = NeonPink,
            orbColor2 = NeonPurple
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pride Quiz",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        }
    }
}
