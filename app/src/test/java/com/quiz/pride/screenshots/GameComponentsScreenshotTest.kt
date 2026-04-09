package com.quiz.pride.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.quiz.pride.ui.components.GameTopBar
import com.quiz.pride.ui.components.LifeIndicator
import com.quiz.pride.ui.components.LoadingIndicator
import com.quiz.pride.ui.components.PointsDisplay
import com.quiz.pride.ui.theme.GradientBackgroundEnd
import com.quiz.pride.ui.theme.GradientBackgroundMid
import com.quiz.pride.ui.theme.GradientBackgroundStart
import com.quiz.pride.ui.theme.GradientPositionBottom
import com.quiz.pride.ui.theme.GradientPositionTop
import com.quiz.pride.ui.theme.PrideQuizTheme
import com.quiz.pride.ui.theme.ResponseCorrect
import com.quiz.pride.ui.theme.ResponseFail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Tests de screenshot Roborazzi para los componentes de la pantalla de juego.
 *
 * Cubre: LoadingIndicator, GameTopBar, PointsDisplay, LifeIndicator y
 * botones de respuesta en los estados disponibles (sin seleccion, correcto, incorrecto).
 *
 * Cada componente se testea al menos en tema claro y oscuro.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel5, application = com.quiz.pride.TestApplication::class)
class GameComponentsScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ============================================================
    // LoadingIndicator
    // ============================================================

    @Test
    fun loadingIndicator_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                Surface(
                    modifier = Modifier.size(200.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoadingIndicator()
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun loadingIndicator_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                Surface(
                    modifier = Modifier.size(200.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoadingIndicator()
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    // ============================================================
    // GameTopBar
    // ============================================================

    @Test
    fun gameTopBar_puntajeAlto_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                GameTopBarWrapper(
                    points = 1250,
                    lives = 2,
                    maxLives = 2
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun gameTopBar_puntajeAlto_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                GameTopBarWrapper(
                    points = 1250,
                    lives = 2,
                    maxLives = 2
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun gameTopBar_unaVida_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                GameTopBarWrapper(
                    points = 450,
                    lives = 1,
                    maxLives = 2
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun gameTopBar_sinVidas_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                GameTopBarWrapper(
                    points = 200,
                    lives = 0,
                    maxLives = 2
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun gameTopBar_modoTemporizadoNormal_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                TopBarTemporizadorWrapper(
                    points = 800,
                    lives = 3,
                    stage = 10,
                    totalStages = 20,
                    timeRemaining = 90 // 1:30
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun gameTopBar_modoTemporizadoUrgente_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                TopBarTemporizadorWrapper(
                    points = 600,
                    lives = 2,
                    stage = 15,
                    totalStages = 20,
                    timeRemaining = 20 // Bajo umbral de urgencia
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    // ============================================================
    // PointsDisplay
    // ============================================================

    @Test
    fun pointsDisplay_puntajeBajo_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(GradientPositionTop, GradientPositionBottom)
                            )
                        )
                        .padding(16.dp)
                ) {
                    PointsDisplay(points = 50)
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun pointsDisplay_puntajeAlto_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(GradientPositionTop, GradientPositionBottom)
                            )
                        )
                        .padding(16.dp)
                ) {
                    PointsDisplay(points = 9999)
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    // ============================================================
    // LifeIndicator
    // ============================================================

    @Test
    fun lifeIndicator_vidasCompletas_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                Box(
                    modifier = Modifier
                        .background(GradientBackgroundStart)
                        .padding(16.dp)
                ) {
                    LifeIndicator(currentLives = 2, maxLives = 2)
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun lifeIndicator_vidaUnica_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                Box(
                    modifier = Modifier
                        .background(GradientBackgroundMid)
                        .padding(16.dp)
                ) {
                    LifeIndicator(currentLives = 1, maxLives = 2)
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun lifeIndicator_sinVidas_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                Box(
                    modifier = Modifier
                        .background(GradientBackgroundEnd)
                        .padding(16.dp)
                ) {
                    LifeIndicator(currentLives = 0, maxLives = 2)
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    // ============================================================
    // Botones de respuesta
    // ============================================================

    @Test
    fun botonesRespuesta_sinSeleccion_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    BotonesRespuestaWrapper(
                        opciones = listOf(
                            "Rainbow Flag",
                            "Trans Pride",
                            "Bisexual Pride",
                            "Non-Binary Pride"
                        ),
                        selectedAnswer = null,
                        correctOptionIndex = 0
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun botonesRespuesta_sinSeleccion_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    BotonesRespuestaWrapper(
                        opciones = listOf(
                            "Rainbow Flag",
                            "Trans Pride",
                            "Bisexual Pride",
                            "Non-Binary Pride"
                        ),
                        selectedAnswer = null,
                        correctOptionIndex = 0
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun botonesRespuesta_respuestaCorrecta_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    BotonesRespuestaWrapper(
                        opciones = listOf(
                            "Rainbow Flag",
                            "Trans Pride",
                            "Bisexual Pride",
                            "Non-Binary Pride"
                        ),
                        selectedAnswer = 0, // Seleccion correcta
                        correctOptionIndex = 0
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun botonesRespuesta_respuestaCorrecta_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    BotonesRespuestaWrapper(
                        opciones = listOf(
                            "Rainbow Flag",
                            "Trans Pride",
                            "Bisexual Pride",
                            "Non-Binary Pride"
                        ),
                        selectedAnswer = 0,
                        correctOptionIndex = 0
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun botonesRespuesta_respuestaIncorrecta_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    BotonesRespuestaWrapper(
                        opciones = listOf(
                            "Rainbow Flag",
                            "Trans Pride",
                            "Bisexual Pride",
                            "Non-Binary Pride"
                        ),
                        selectedAnswer = 2, // Seleccion incorrecta
                        correctOptionIndex = 0  // La correcta era la primera
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun botonesRespuesta_respuestaIncorrecta_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    BotonesRespuestaWrapper(
                        opciones = listOf(
                            "Rainbow Flag",
                            "Trans Pride",
                            "Bisexual Pride",
                            "Non-Binary Pride"
                        ),
                        selectedAnswer = 2,
                        correctOptionIndex = 0
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}

// ============================================================
// Composables auxiliares de prueba (privados a este archivo)
// ============================================================

/**
 * Wrapper que replica el GameTopBar de PrideComponents con fondo de gradiente
 * para que el screenshot sea representativo del contexto real de juego.
 */
@Composable
private fun GameTopBarWrapper(
    points: Int,
    lives: Int,
    maxLives: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        GameTopBar(
            points = points,
            lives = lives,
            maxLives = maxLives,
            onBackClick = {}
        )
    }
}

/**
 * Wrapper que simula la TopBar con temporizador visible (modo Time Attack).
 * Replica el comportamiento de EnhancedGameTopBar con estado de temporizador.
 */
@Composable
private fun TopBarTemporizadorWrapper(
    points: Int,
    lives: Int,
    stage: Int,
    totalStages: Int,
    timeRemaining: Int
) {
    val isUrgent = timeRemaining <= 30
    val timerColor = if (isUrgent) Color(0xFFEF4444) else Color(0xFFFFD700)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(GradientPositionTop, GradientPositionBottom)
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Puntaje
            PointsDisplay(
                points = points,
                modifier = Modifier.weight(1f)
            )

            // Timer
            val minutes = timeRemaining / 60
            val seconds = timeRemaining % 60
            Text(
                text = String.format("%d:%02d", minutes, seconds),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = timerColor
            )
        }

        LinearProgressIndicator(
            progress = { stage.toFloat() / totalStages.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "$stage / $totalStages",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, bottom = 4.dp),
            textAlign = TextAlign.End
        )
    }
}

/**
 * Wrapper que replica los botones de respuesta del GameScreen.
 *
 * Muestra 4 opciones con estados visuales:
 * - Sin seleccion: todos con color neutro (surface)
 * - Seleccion correcta: boton seleccionado en verde
 * - Seleccion incorrecta: boton seleccionado en rojo + boton correcto en verde
 */
@Composable
private fun BotonesRespuestaWrapper(
    opciones: List<String>,
    selectedAnswer: Int?,
    correctOptionIndex: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        opciones.forEachIndexed { index, texto ->
            val isSelected = selectedAnswer == index
            val isCorrect = selectedAnswer != null && correctOptionIndex == index
            val isWrong = isSelected && correctOptionIndex != index

            val backgroundColor = when {
                isCorrect -> ResponseCorrect.copy(alpha = 0.85f)
                isWrong -> ResponseFail.copy(alpha = 0.85f)
                else -> MaterialTheme.colorScheme.surface
            }

            val textColor = when {
                isCorrect || isWrong -> Color.White
                else -> MaterialTheme.colorScheme.onSurface
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .clickable(enabled = selectedAnswer == null) {}
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = texto,
                    color = textColor,
                    fontWeight = if (isSelected || isCorrect) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
