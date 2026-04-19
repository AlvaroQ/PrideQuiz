package com.quiz.pride.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.quiz.pride.ui.animation.AnimationSpecs
import com.quiz.pride.ui.theme.RainbowProgressFill
import kotlin.math.sin
import kotlin.random.Random

private val PrideConfettiColors: List<Color> = RainbowProgressFill

private data class ConfettiParticle(
    val x: Float,
    val startY: Float,
    val velocityX: Float,
    val speed: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val color: Color,
    val width: Float,
    val height: Float
)

@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier,
    particleCount: Int = 55,
    durationMillis: Int = AnimationSpecs.CONFETTI_DURATION
) {
    val particles = remember {
        List(particleCount) {
            ConfettiParticle(
                x = Random.nextFloat(),
                startY = Random.nextFloat() * -0.3f,
                velocityX = Random.nextFloat() * 0.1f - 0.05f,
                speed = Random.nextFloat() * 0.4f + 0.6f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 720f - 360f,
                color = PrideConfettiColors[Random.nextInt(PrideConfettiColors.size)],
                width = Random.nextFloat() * 8f + 4f,
                height = Random.nextFloat() * 12f + 6f
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis, easing = LinearEasing)
        )
    }

    val alpha = (1f - progress.value).coerceIn(0f, 1f)

    if (alpha > 0.01f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = progress.value

            particles.forEach { p ->
                val currentX = (p.x + p.velocityX * t) * w +
                        sin(t * 6f + p.rotation) * 20f
                val currentY = (p.startY + p.speed * t * 1.5f) * h
                val currentRotation = p.rotation + p.rotationSpeed * t

                if (currentY in -50f..h + 50f) {
                    rotate(
                        degrees = currentRotation,
                        pivot = Offset(currentX, currentY)
                    ) {
                        drawRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(
                                currentX - p.width / 2f,
                                currentY - p.height / 2f
                            ),
                            size = Size(p.width, p.height)
                        )
                    }
                }
            }
        }
    }
}
