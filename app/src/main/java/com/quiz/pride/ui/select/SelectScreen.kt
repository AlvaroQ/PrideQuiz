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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quiz.pride.R
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.ui.components.GradientCard
import com.quiz.pride.ui.theme.GradientBottom
import com.quiz.pride.ui.theme.GradientTop
import com.quiz.pride.ui.theme.LearnGradientBottom
import com.quiz.pride.ui.theme.LearnGradientTop
import com.quiz.pride.ui.theme.SettingsGradientBottom
import com.quiz.pride.ui.theme.SettingsGradientTop
import com.quiz.pride.ui.theme.StartGradientBottom
import com.quiz.pride.ui.theme.StartGradientTop
import com.quiz.pride.ui.theme.White

@Composable
fun SelectScreen(
    onNavigateToSelectGame: () -> Unit,
    onNavigateToInfo: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // Analytics
    LaunchedEffect(Unit) {
        AnalyticsManager.analyticsScreenViewed(AnalyticsManager.SCREEN_SELECT)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                .height(280.dp)
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

            // Title Image
            Image(
                painter = painterResource(R.drawable.title),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Start Game Card
            MenuCard(
                title = stringResource(R.string.start),
                description = stringResource(R.string.start_new_game),
                imageRes = R.drawable.image_play,
                gradientColors = listOf(StartGradientTop, StartGradientBottom),
                onClick = onNavigateToSelectGame
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Learn Card
            MenuCard(
                title = stringResource(R.string.learn),
                description = stringResource(R.string.learn_more),
                imageRes = R.drawable.image_learn,
                gradientColors = listOf(LearnGradientTop, LearnGradientBottom),
                onClick = onNavigateToInfo
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Card
            MenuCard(
                title = stringResource(R.string.settings),
                description = stringResource(R.string.settings_description),
                imageRes = R.drawable.image_settings,
                gradientColors = listOf(SettingsGradientTop, SettingsGradientBottom),
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun MenuCard(
    title: String,
    description: String,
    imageRes: Int,
    gradientColors: List<androidx.compose.ui.graphics.Color>,
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
                    color = White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Start
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
