package com.quiz.pride.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.pride.R
import com.quiz.pride.ui.components.PrideButton
import com.quiz.pride.ui.theme.AdvanceGradientBottom
import com.quiz.pride.ui.theme.AdvanceGradientTop
import com.quiz.pride.ui.theme.ExpertGradientBottom
import com.quiz.pride.ui.theme.ExpertGradientTop
import com.quiz.pride.ui.theme.GlowPink
import com.quiz.pride.ui.theme.GlowPurple
import com.quiz.pride.ui.theme.GradientBackgroundEnd
import com.quiz.pride.ui.theme.GradientBackgroundMid
import com.quiz.pride.ui.theme.GradientBackgroundStart
import com.quiz.pride.ui.theme.LearnGradientBottom
import com.quiz.pride.ui.theme.LearnGradientTop
import com.quiz.pride.ui.theme.NeonGreen
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.NormalGradientBottom
import com.quiz.pride.ui.theme.NormalGradientTop
import com.quiz.pride.ui.theme.StartGradientBottom
import com.quiz.pride.ui.theme.StartGradientTop
import com.quiz.pride.ui.theme.White
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val glowColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = stringResource(R.string.onboarding_welcome_title),
            description = stringResource(R.string.onboarding_welcome_desc),
            icon = Icons.Default.Favorite,
            gradientColors = listOf(StartGradientTop, StartGradientBottom),
            glowColor = GlowPink
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_learn_title),
            description = stringResource(R.string.onboarding_learn_desc),
            icon = Icons.Default.School,
            gradientColors = listOf(LearnGradientTop, LearnGradientBottom),
            glowColor = GlowPurple
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_play_title),
            description = stringResource(R.string.onboarding_play_desc),
            icon = Icons.Default.PlayArrow,
            gradientColors = listOf(NormalGradientTop, NormalGradientBottom),
            glowColor = NeonGreen.copy(alpha = 0.4f)
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_modes_title),
            description = stringResource(R.string.onboarding_modes_desc),
            icon = Icons.Default.Star,
            gradientColors = listOf(AdvanceGradientTop, AdvanceGradientBottom),
            glowColor = Color(0x40FBBF24)
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "onboarding_bg")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                .size(200.dp)
                .offset(x = (-70).dp, y = 100.dp + floatOffset.dp)
                .alpha(0.25f)
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
                .size(160.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = 200.dp - floatOffset.dp)
                .alpha(0.2f)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonPurple, Color.Transparent)
                        ),
                        radius = size.minDimension / 2
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onFinish) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        color = White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(page = pages[page])
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Page indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateFloatAsState(
                        targetValue = if (isSelected) 32f else 12f,
                        animationSpec = tween(300),
                        label = "indicator_width"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(12.dp)
                            .width(width.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(pages[index].gradientColors)
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(
                                            White.copy(alpha = 0.3f),
                                            White.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation buttons
            val isLastPage = pagerState.currentPage == pages.size - 1

            AnimatedVisibility(
                visible = isLastPage,
                enter = fadeIn() + slideInHorizontally { it },
                exit = fadeOut() + slideOutHorizontally { it }
            ) {
                PrideButton(
                    text = stringResource(R.string.onboarding_get_started),
                    onClick = onFinish,
                    gradientColors = listOf(StartGradientTop, StartGradientBottom),
                    glowColor = GlowPink,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = !isLastPage,
                enter = fadeIn() + slideInHorizontally { -it },
                exit = fadeOut() + slideOutHorizontally { -it }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onFinish
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_skip),
                            color = White.copy(alpha = 0.7f)
                        )
                    }

                    PrideButton(
                        text = stringResource(R.string.onboarding_next),
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        gradientColors = pages[pagerState.currentPage].gradientColors,
                        glowColor = pages[pagerState.currentPage].glowColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage
) {
    val infiniteTransition = rememberInfiniteTransition(label = "icon_anim")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with glow
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(iconScale)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                page.glowColor,
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension
                    )
                }
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(page.gradientColors)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.3f),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            ),
            color = White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
