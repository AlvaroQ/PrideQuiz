package com.quiz.pride.ui.select

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.pride.R
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.ui.theme.GlowBlue
import com.quiz.pride.ui.theme.GlowPink
import com.quiz.pride.ui.theme.GlowPurple
import com.quiz.pride.ui.theme.GradientBackgroundEnd
import com.quiz.pride.ui.theme.GradientBackgroundMid
import com.quiz.pride.ui.theme.GradientBackgroundStart
import com.quiz.pride.ui.theme.LearnGradientBottom
import com.quiz.pride.ui.theme.LearnGradientTop
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.SettingsGradientBottom
import com.quiz.pride.ui.theme.SettingsGradientTop
import com.quiz.pride.ui.theme.StartGradientBottom
import com.quiz.pride.ui.theme.StartGradientTop
import com.quiz.pride.ui.theme.White
import com.quiz.pride.ui.theme.GradientPointsTop
import com.quiz.pride.ui.theme.GradientPointsBottom

@Composable
fun SelectScreen(
    onNavigateToSelectGame: () -> Unit,
    onNavigateToInfo: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    // Analytics
    LaunchedEffect(Unit) {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT)
    }

    // Floating animation for background elements
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    Box(
        modifier = Modifier
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
        // Decorative glow orbs in background
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp, y = 100.dp + floatOffset.dp)
                .alpha(0.3f)
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
                .offset(x = 50.dp, y = 200.dp - floatOffset.dp)
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

        // Background image with overlay
        Image(
            painter = painterResource(R.drawable.protest),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .alpha(0.6f),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay on background image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GradientBackgroundEnd,
                            Color.Transparent
                        )
                    )
                )
        )

        // Profile button in top right
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = onNavigateToProfile,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(GradientPointsTop, GradientPointsBottom)
                        ),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.profile_title),
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Title with glow effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        // Glow behind title
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NeonPink.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            ),
                            radius = 200f,
                            center = Offset(size.width / 2, size.height / 2)
                        )
                    }
            ) {
                Image(
                    painter = painterResource(R.drawable.title),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Start Game Card - Coral/Orange gradient
            VibrantMenuCard(
                title = stringResource(R.string.start),
                description = stringResource(R.string.start_new_game),
                imageRes = R.drawable.image_play,
                gradientColors = listOf(StartGradientTop, StartGradientBottom),
                glowColor = GlowPink,
                onClick = onNavigateToSelectGame
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Learn Card - Cyan/Blue gradient
            VibrantMenuCard(
                title = stringResource(R.string.learn),
                description = stringResource(R.string.learn_more),
                imageRes = R.drawable.image_learn,
                gradientColors = listOf(LearnGradientTop, LearnGradientBottom),
                glowColor = GlowBlue,
                onClick = onNavigateToInfo
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Settings Card - Purple/Pink gradient
            VibrantMenuCard(
                title = stringResource(R.string.settings),
                description = stringResource(R.string.settings_description),
                imageRes = R.drawable.image_settings,
                gradientColors = listOf(SettingsGradientTop, SettingsGradientBottom),
                glowColor = GlowPurple,
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun VibrantMenuCard(
    title: String,
    description: String,
    imageRes: Int,
    gradientColors: List<Color>,
    glowColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 400f
        ),
        label = "card_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .scale(scale)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = glowColor,
                spotColor = glowColor
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = gradientColors,
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
            ) {
                // Shine overlay effect
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.Transparent,
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(300f, 300f)
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Start,
                            lineHeight = 18.sp
                        )
                    }

                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(85.dp)
                            .drawBehind {
                                // Glow behind icon
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.2f),
                                    radius = size.minDimension / 1.8f
                                )
                            },
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
