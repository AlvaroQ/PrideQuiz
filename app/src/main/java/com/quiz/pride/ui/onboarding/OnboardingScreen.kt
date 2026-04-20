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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quiz.pride.R
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.ui.components.AnimatedScreenBackground
import com.quiz.pride.ui.components.PrideButton
import com.quiz.pride.ui.theme.ButtonGradient
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.OffBlackInk
import com.quiz.pride.ui.theme.Pink900
import com.quiz.pride.ui.theme.PrideButtonStyles
import com.quiz.pride.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: Painter,
    val style: ButtonGradient
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    analyticsManager: AnalyticsManager = koinInject()
) {
    // Onboarding siempre usa fondo pastel y botones vibrantes con texto blanco,
    // independientemente del tema del sistema (el usuario aun no lo ha elegido).
    val pages = listOf(
        OnboardingPage(
            title = stringResource(R.string.onboarding_welcome_title),
            description = stringResource(R.string.onboarding_welcome_desc),
            icon = rememberVectorPainter(Icons.Default.Favorite),
            style = PrideButtonStyles.Start.dark
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_learn_title),
            description = stringResource(R.string.onboarding_learn_desc),
            icon = painterResource(id = R.drawable.ic_school),
            style = PrideButtonStyles.Learn.dark
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_play_title),
            description = stringResource(R.string.onboarding_play_desc),
            icon = rememberVectorPainter(Icons.Default.PlayArrow),
            style = PrideButtonStyles.Normal.dark
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_modes_title),
            description = stringResource(R.string.onboarding_modes_desc),
            icon = rememberVectorPainter(Icons.Default.Star),
            style = PrideButtonStyles.Advance.dark
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        analyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_ONBOARDING)
        analyticsManager.analyticsOnboardingStep(0, "viewed")
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage > 0) {
            analyticsManager.analyticsOnboardingStep(pagerState.currentPage, "viewed")
        }
    }

    AnimatedScreenBackground(
        orbColor1 = NeonPink,
        orbColor2 = NeonPurple,
        forceLightPalette = true
    ) {
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
                TextButton(onClick = {
                    analyticsManager.analyticsOnboardingStep(pagerState.currentPage, "skipped")
                    onFinish()
                }) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        style = MaterialTheme.typography.labelLarge,
                        color = OffBlackInk.copy(alpha = 0.85f)
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
                                    Brush.horizontalGradient(pages[index].style.colors)
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(
                                            Pink900.copy(alpha = 0.25f),
                                            Pink900.copy(alpha = 0.25f)
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
                    onClick = {
                        analyticsManager.analyticsOnboardingStep(pagerState.currentPage, "completed")
                        onFinish()
                    },
                    style = PrideButtonStyles.Start.current(),
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
                    horizontalArrangement = Arrangement.End
                ) {
                    PrideButton(
                        text = stringResource(R.string.onboarding_next),
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        style = pages[pagerState.currentPage].style
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
                                page.style.glow,
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension
                    )
                }
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(page.style.colors)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = page.icon,
                contentDescription = null,
                tint = page.style.contentColor,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = Shadow(
                    color = White.copy(alpha = 0.6f),
                    offset = Offset(0f, 1f),
                    blurRadius = 3f
                )
            ),
            color = OffBlackInk,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = OffBlackInk.copy(alpha = 0.78f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
