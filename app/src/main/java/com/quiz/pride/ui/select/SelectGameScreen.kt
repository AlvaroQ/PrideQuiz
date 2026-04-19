package com.quiz.pride.ui.select

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quiz.pride.R
import com.quiz.pride.ui.components.AnimatedScreenBackground
import com.quiz.pride.ui.theme.AdvanceGradientBottom
import com.quiz.pride.ui.theme.AdvanceGradientTop
import com.quiz.pride.ui.theme.NeonBlue
import com.quiz.pride.ui.theme.NeonGreen
import com.quiz.pride.ui.theme.NeonOrange
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.NormalGradientBottom
import com.quiz.pride.ui.theme.NormalGradientTop
import com.quiz.pride.ui.theme.TimedGradientBottom
import com.quiz.pride.ui.theme.TimedGradientTop
import com.quiz.pride.ui.theme.White
import com.quiz.pride.utils.Constants
import org.koin.androidx.compose.koinViewModel

@Composable
fun SelectGameScreen(
    onNavigateToGame: (Constants.GameType) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SelectGameViewModel = koinViewModel()
) {
    AnimatedScreenBackground(
        orbColor1 = NeonPink,
        orbColor2 = NeonPurple
    ) {
        // Background image — same as SelectScreen for seamless transition
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Espacio para la top row flotante (back + título) + status bar
            Spacer(modifier = Modifier.height(120.dp))

            // Aire entre la top row y la primera card
            Spacer(modifier = Modifier.height(64.dp))

            // Normal difficulty - Green
            VibrantDifficultyCard(
                title = stringResource(R.string.normal),
                description = stringResource(R.string.normal_description),
                imageRes = R.drawable.normal,
                gradientColors = listOf(NormalGradientTop, NormalGradientBottom),
                glowColor = NeonGreen.copy(alpha = 0.5f),
                onClick = { onNavigateToGame(Constants.GameType.NORMAL) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Advance difficulty - Yellow/Amber
            VibrantDifficultyCard(
                title = stringResource(R.string.advance),
                description = stringResource(R.string.advance_description),
                imageRes = R.drawable.advance,
                gradientColors = listOf(AdvanceGradientTop, AdvanceGradientBottom),
                glowColor = NeonOrange.copy(alpha = 0.5f),
                onClick = { onNavigateToGame(Constants.GameType.ADVANCE) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Timed mode - Cyan
            VibrantDifficultyCard(
                title = stringResource(R.string.timed_mode),
                description = stringResource(R.string.timed_mode_description),
                imageRes = R.drawable.normal, // TODO: Add timed mode icon
                gradientColors = listOf(TimedGradientTop, TimedGradientBottom),
                glowColor = NeonBlue.copy(alpha = 0.5f),
                onClick = { onNavigateToGame(Constants.GameType.TIMED) }
            )
        }

        // Top row flotante: back button (solo flecha blanca) + título en la misma row.
        // Misma técnica que SelectScreen: windowInsetsPadding(statusBars) sobre el fondo animado.
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                text = stringResource(R.string.choose_level),
                style = MaterialTheme.typography.headlineMedium.copy(
                    shadow = Shadow(
                        color = NeonPink.copy(alpha = 0.6f),
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
private fun VibrantDifficultyCard(
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
                // Shine overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    White.copy(alpha = 0.25f),
                                    Color.Transparent,
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(350f, 350f)
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
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            ),
                            color = White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = White.copy(alpha = 0.9f)
                        )
                    }

                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(85.dp)
                            .drawBehind {
                                drawCircle(
                                    color = White.copy(alpha = 0.2f),
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
