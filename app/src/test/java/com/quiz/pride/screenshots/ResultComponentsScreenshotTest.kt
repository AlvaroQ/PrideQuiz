package com.quiz.pride.screenshots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.quiz.pride.ui.theme.PrideQuizTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Tests de screenshot Roborazzi para los componentes de la pantalla de resultado.
 *
 * Cubre: ScoreDisplay (puntaje alto, bajo, nuevo record), StatsGrid
 * (distintas precisiones) y ActionButtons (Play again, Ranking, Share).
 *
 * Cada componente se testea en tema claro y tema oscuro.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel5, application = com.quiz.pride.TestApplication::class)
class ResultComponentsScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ============================================================
    // ScoreDisplay — puntaje alto
    // ============================================================

    @Test
    fun scoreDisplay_puntajeAlto_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ScoreDisplayWrapper(
                        points = 1850,
                        personalRecord = "1200",
                        worldRecord = "5000",
                        isNewRecord = false
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun scoreDisplay_puntajeAlto_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ScoreDisplayWrapper(
                        points = 1850,
                        personalRecord = "1200",
                        worldRecord = "5000",
                        isNewRecord = false
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    // ============================================================
    // ScoreDisplay — puntaje bajo
    // ============================================================

    @Test
    fun scoreDisplay_puntajeBajo_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ScoreDisplayWrapper(
                        points = 150,
                        personalRecord = "600",
                        worldRecord = "5000",
                        isNewRecord = false
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun scoreDisplay_puntajeBajo_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ScoreDisplayWrapper(
                        points = 150,
                        personalRecord = "600",
                        worldRecord = "5000",
                        isNewRecord = false
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    // ============================================================
    // ScoreDisplay — nuevo record
    // ============================================================

    @Test
    fun scoreDisplay_nuevoRecord_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ScoreDisplayWrapper(
                        points = 5250,
                        personalRecord = "5250",
                        worldRecord = "5250",
                        isNewRecord = true
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun scoreDisplay_nuevoRecord_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ScoreDisplayWrapper(
                        points = 5250,
                        personalRecord = "5250",
                        worldRecord = "5250",
                        isNewRecord = true
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    // ============================================================
    // StatsGrid — precision alta (100%)
    // ============================================================

    @Test
    fun statsGrid_precisionPerfecta_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    StatsGridWrapper(
                        totalQuestions = 20,
                        correctAnswers = 20,
                        bestStreak = 20,
                        timePlayed = 75000L // 1:15
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun statsGrid_precisionPerfecta_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    StatsGridWrapper(
                        totalQuestions = 20,
                        correctAnswers = 20,
                        bestStreak = 20,
                        timePlayed = 75000L
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    // ============================================================
    // StatsGrid — precision media (75%)
    // ============================================================

    @Test
    fun statsGrid_precisionMedia_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    StatsGridWrapper(
                        totalQuestions = 20,
                        correctAnswers = 15,
                        bestStreak = 6,
                        timePlayed = 125000L // 2:05
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun statsGrid_precisionMedia_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    StatsGridWrapper(
                        totalQuestions = 20,
                        correctAnswers = 15,
                        bestStreak = 6,
                        timePlayed = 125000L
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    // ============================================================
    // StatsGrid — precision baja (0%)
    // ============================================================

    @Test
    fun statsGrid_precisionCero_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    StatsGridWrapper(
                        totalQuestions = 20,
                        correctAnswers = 0,
                        bestStreak = 0,
                        timePlayed = 60000L
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun statsGrid_precisionCero_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    StatsGridWrapper(
                        totalQuestions = 20,
                        correctAnswers = 0,
                        bestStreak = 0,
                        timePlayed = 60000L
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    // ============================================================
    // ActionButtons
    // ============================================================

    @Test
    fun actionButtons_temaClaro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ActionButtonsWrapper(
                        onPlayAgain = {},
                        onRanking = {},
                        onShare = {}
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun actionButtons_temaOscuro() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ActionButtonsWrapper(
                        onPlayAgain = {},
                        onRanking = {},
                        onShare = {}
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
 * Replica del ScoreDisplay de ResultScreen.
 *
 * Muestra "Your Score", el valor numerico grande, badge de nuevo record
 * (cuando aplica), "Personal Best" y "World Record" con sus valores.
 */
@Composable
private fun ScoreDisplayWrapper(
    points: Int,
    personalRecord: String,
    worldRecord: String,
    isNewRecord: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Score",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = points.toString(),
            fontSize = 72.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        // Badge de nuevo record
        if (isNewRecord) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.tertiary
            ) {
                Text(
                    text = "New Record!",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Record personal
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Personal Best",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = personalRecord,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            // Record mundial
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "World Record",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = worldRecord,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Replica del StatsGrid de ResultScreen.
 *
 * Muestra 4 tarjetas de estadisticas: Accuracy, Best Streak, Correct, Time Played.
 */
@Composable
private fun StatsGridWrapper(
    totalQuestions: Int,
    correctAnswers: Int,
    bestStreak: Int,
    timePlayed: Long
) {
    val accuracy = if (totalQuestions > 0) {
        (correctAnswers.toFloat() / totalQuestions.toFloat() * 100).toInt()
    } else {
        0
    }

    val seconds = (timePlayed / 1000) % 60
    val minutes = (timePlayed / (1000 * 60)) % 60
    val timeFormatted = String.format("%d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCardWrapper(
                label = "Accuracy",
                value = "$accuracy%",
                modifier = Modifier.weight(1f)
            )
            StatCardWrapper(
                label = "Best Streak",
                value = bestStreak.toString(),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCardWrapper(
                label = "Correct",
                value = "$correctAnswers/$totalQuestions",
                modifier = Modifier.weight(1f)
            )
            StatCardWrapper(
                label = "Time Played",
                value = timeFormatted,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCardWrapper(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Replica de los botones de accion de ResultScreen.
 *
 * Muestra: "Play again" (button primario), "Ranking" (outlined button),
 * "Share" (outlined button).
 */
@Composable
private fun ActionButtonsWrapper(
    onPlayAgain: () -> Unit,
    onRanking: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Play again",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        OutlinedButton(
            onClick = onRanking,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Ranking",
                style = MaterialTheme.typography.labelLarge
            )
        }

        OutlinedButton(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Share",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
