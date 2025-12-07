package com.quiz.pride.ui.components

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.pride.ui.theme.GlowPink
import com.quiz.pride.ui.theme.GlowPurple
import com.quiz.pride.ui.theme.GradientBackgroundEnd
import com.quiz.pride.ui.theme.GradientBackgroundMid
import com.quiz.pride.ui.theme.GradientBackgroundStart
import com.quiz.pride.ui.theme.GradientPointsBottom
import com.quiz.pride.ui.theme.GradientPointsTop
import com.quiz.pride.ui.theme.GradientPositionBottom
import com.quiz.pride.ui.theme.GradientPositionTop
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.PrideRed
import com.quiz.pride.ui.theme.RainbowColors
import com.quiz.pride.ui.theme.Shimmer
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
                    colors = listOf(
                        GradientBackgroundStart,
                        GradientBackgroundMid,
                        GradientBackgroundEnd
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
                                color = PrideRed.copy(alpha = 0.3f),
                                radius = size.minDimension * 0.8f
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
