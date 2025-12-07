package com.quiz.pride.ui.select

import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quiz.pride.R
import com.quiz.pride.ui.components.GradientCard
import com.quiz.pride.ui.components.PrideTopAppBar
import com.quiz.pride.ui.theme.GradientBottom
import com.quiz.pride.ui.theme.GradientTop
import com.quiz.pride.ui.theme.White
import com.quiz.pride.utils.Constants

// Difficulty gradient colors
private val NormalGradientTop = Color(0xFF81C784)
private val NormalGradientBottom = Color(0xFF388E3C)

private val AdvanceGradientTop = Color(0xFFFFB74D)
private val AdvanceGradientBottom = Color(0xFFF57C00)

private val ExpertGradientTop = Color(0xFFE57373)
private val ExpertGradientBottom = Color(0xFFD32F2F)

@Composable
fun SelectGameScreen(
    onNavigateToGame: (Constants.GameType) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            PrideTopAppBar(
                title = stringResource(R.string.select_difficulty),
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
                        listOf(GradientTop, GradientBottom)
                    )
                )
        ) {
            // Background image
            Image(
                painter = painterResource(R.drawable.protest),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .align(Alignment.BottomCenter)
                    .alpha(0.75f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Title
                Text(
                    text = stringResource(R.string.choose_level),
                    style = MaterialTheme.typography.headlineMedium,
                    color = White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Normal difficulty
                DifficultyCard(
                    title = stringResource(R.string.normal),
                    description = stringResource(R.string.normal_description),
                    imageRes = R.drawable.normal,
                    gradientColors = listOf(NormalGradientTop, NormalGradientBottom),
                    onClick = { onNavigateToGame(Constants.GameType.NORMAL) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Advance difficulty
                DifficultyCard(
                    title = stringResource(R.string.advance),
                    description = stringResource(R.string.advance_description),
                    imageRes = R.drawable.advance,
                    gradientColors = listOf(AdvanceGradientTop, AdvanceGradientBottom),
                    onClick = { onNavigateToGame(Constants.GameType.ADVANCE) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Expert difficulty
                DifficultyCard(
                    title = stringResource(R.string.expert),
                    description = stringResource(R.string.expert_description),
                    imageRes = R.drawable.expert,
                    gradientColors = listOf(ExpertGradientTop, ExpertGradientBottom),
                    onClick = { onNavigateToGame(Constants.GameType.EXPERT) }
                )
            }
        }
    }
}

@Composable
private fun DifficultyCard(
    title: String,
    description: String,
    imageRes: Int,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    GradientCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        gradientColors = gradientColors,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White.copy(alpha = 0.9f)
                )
            }

            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
