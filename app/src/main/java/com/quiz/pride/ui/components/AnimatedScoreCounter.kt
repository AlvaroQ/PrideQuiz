package com.quiz.pride.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quiz.pride.R
import com.quiz.pride.ui.animation.AnimationSpecs
import com.quiz.pride.ui.theme.GradientPointsBottom
import com.quiz.pride.ui.theme.GradientPointsTop
import com.quiz.pride.ui.theme.NeonPink
import kotlinx.coroutines.launch

@Composable
fun AnimatedScoreCounter(
    targetScore: Int,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    var displayScore by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetScore) {
        animate(
            initialValue = displayScore.toFloat(),
            targetValue = targetScore.toFloat(),
            animationSpec = AnimationSpecs.ScoreCountSpec
        ) { value, _ ->
            displayScore = value.toInt()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = "$displayScore",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum",
                shadow = Shadow(
                    color = GradientPointsBottom.copy(alpha = 0.6f),
                    offset = Offset(0f, 0f),
                    blurRadius = 18f
                )
            ),
            color = GradientPointsBottom
        )
    }
}

@Composable
fun PointsPopup(
    points: Int,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(visible) {
        launch {
            offsetY.animateTo(
                -40f,
                AnimationSpecs.PointsPopupOffsetSpec
            )
        }
        launch {
            alpha.animateTo(
                0f,
                AnimationSpecs.PointsPopupFadeSpec
            )
        }
    }

    Text(
        text = stringResource(R.string.xp_gained, points),
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            shadow = Shadow(
                color = NeonPink.copy(alpha = 0.8f),
                offset = Offset(0f, 0f),
                blurRadius = 12f
            )
        ),
        color = GradientPointsTop,
        modifier = modifier
            .offset(y = offsetY.value.dp)
            .alpha(alpha.value)
    )
}
