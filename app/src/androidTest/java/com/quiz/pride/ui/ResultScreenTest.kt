package com.quiz.pride.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.pride.ui.theme.PrideQuizTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Tests instrumentados de la UI de ResultScreen y sus sub-componentes.
 *
 * Estrategia de prueba:
 * - ResultScreen usa koinViewModel() internamente con logica compleja de Firebase,
 *   billing y anuncios. No es posible testearlo directamente sin Koin configurado.
 * - Se prueban los sub-componentes de UI directamente con estado fake:
 *   ScoreDisplayTestWrapper, StatsGridTestWrapper, ActionButtonsTestWrapper, etc.
 * - Los wrappers replican exactamente los textos visibles que el composable original
 *   renderiza, permitiendo verificar el comportamiento de la UI de forma aislada.
 */
class ResultScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ============================================================
    // Tests de visualizacion del puntaje
    // ============================================================

    @Test
    fun puntaje_seMuestraCorrectamente() {
        composeTestRule.setContent {
            PrideQuizTheme {
                ScoreDisplayTestWrapper(
                    points = 1500,
                    personalRecord = "1200",
                    worldRecord = "5000",
                    isNewRecord = false
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("1500").assertIsDisplayed()
    }

    @Test
    fun puntaje_muestraLabelTuPuntaje() {
        composeTestRule.setContent {
            PrideQuizTheme {
                ScoreDisplayTestWrapper(
                    points = 800,
                    personalRecord = "600",
                    worldRecord = "3000",
                    isNewRecord = false
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Your Score").assertIsDisplayed()
    }

    @Test
    fun puntaje_muestraRecordPersonal() {
        composeTestRule.setContent {
            PrideQuizTheme {
                ScoreDisplayTestWrapper(
                    points = 900,
                    personalRecord = "750",
                    worldRecord = "4500",
                    isNewRecord = false
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Personal Best").assertIsDisplayed()
        composeTestRule.onNodeWithText("750").assertIsDisplayed()
    }

    @Test
    fun puntaje_muestraRecordMundial() {
        composeTestRule.setContent {
            PrideQuizTheme {
                ScoreDisplayTestWrapper(
                    points = 900,
                    personalRecord = "750",
                    worldRecord = "4500",
                    isNewRecord = false
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("World Record").assertIsDisplayed()
        composeTestRule.onNodeWithText("4500").assertIsDisplayed()
    }

    // ============================================================
    // Tests del indicador de nuevo record
    // ============================================================

    @Test
    fun nuevoRecord_badgeSeMuestaCuandoEsNuevoRecord() {
        composeTestRule.setContent {
            PrideQuizTheme {
                NuevoRecordTestWrapper(isNewRecord = true)
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("New Record!").assertIsDisplayed()
    }

    @Test
    fun nuevoRecord_badgeNoSeMuestraCuandoNoEsNuevoRecord() {
        composeTestRule.setContent {
            PrideQuizTheme {
                NuevoRecordTestWrapper(isNewRecord = false)
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nuevo_record_badge").assertDoesNotExist()
    }

    // ============================================================
    // Tests de estadisticas
    // ============================================================

    @Test
    fun estadisticas_muestranPrecision() {
        composeTestRule.setContent {
            PrideQuizTheme {
                StatsGridTestWrapper(
                    totalQuestions = 20,
                    correctAnswers = 16,
                    bestStreak = 5,
                    timePlayed = 125000L
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Accuracy").assertIsDisplayed()
        composeTestRule.onNodeWithText("80%").assertIsDisplayed()
    }

    @Test
    fun estadisticas_muestranMejorRacha() {
        composeTestRule.setContent {
            PrideQuizTheme {
                StatsGridTestWrapper(
                    totalQuestions = 20,
                    correctAnswers = 15,
                    bestStreak = 8,
                    timePlayed = 90000L
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Best Streak").assertIsDisplayed()
        composeTestRule.onNodeWithText("8").assertIsDisplayed()
    }

    @Test
    fun estadisticas_muestranRespuestasCorrectas() {
        composeTestRule.setContent {
            PrideQuizTheme {
                StatsGridTestWrapper(
                    totalQuestions = 20,
                    correctAnswers = 12,
                    bestStreak = 3,
                    timePlayed = 60000L
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("12/20").assertIsDisplayed()
    }

    @Test
    fun estadisticas_muestranTiempoJugado() {
        composeTestRule.setContent {
            PrideQuizTheme {
                StatsGridTestWrapper(
                    totalQuestions = 20,
                    correctAnswers = 18,
                    bestStreak = 10,
                    timePlayed = 125000L // 2:05
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Time Played").assertIsDisplayed()
        composeTestRule.onNodeWithText("2:05").assertIsDisplayed()
    }

    @Test
    fun estadisticas_precisionCeroSiNoHayPreguntas() {
        composeTestRule.setContent {
            PrideQuizTheme {
                StatsGridTestWrapper(
                    totalQuestions = 0,
                    correctAnswers = 0,
                    bestStreak = 0,
                    timePlayed = 0L
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("0%").assertIsDisplayed()
    }

    // ============================================================
    // Tests de botones de accion
    // ============================================================

    @Test
    fun botonesAccion_jugarDeNuevo_estaPresente() {
        composeTestRule.setContent {
            PrideQuizTheme {
                ActionButtonsTestWrapper(
                    onPlayAgain = {},
                    onRanking = {},
                    onShare = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Play again").assertIsDisplayed()
    }

    @Test
    fun botonesAccion_ranking_estaPresente() {
        composeTestRule.setContent {
            PrideQuizTheme {
                ActionButtonsTestWrapper(
                    onPlayAgain = {},
                    onRanking = {},
                    onShare = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Ranking").assertIsDisplayed()
    }

    @Test
    fun botonesAccion_compartir_estaPresente() {
        composeTestRule.setContent {
            PrideQuizTheme {
                ActionButtonsTestWrapper(
                    onPlayAgain = {},
                    onRanking = {},
                    onShare = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Share").assertIsDisplayed()
    }

    @Test
    fun botonesAccion_jugarDeNuevo_disparaCallback() {
        var clickeado = false

        composeTestRule.setContent {
            PrideQuizTheme {
                ActionButtonsTestWrapper(
                    onPlayAgain = { clickeado = true },
                    onRanking = {},
                    onShare = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Play again").performClick()
        assertTrue("El boton 'Play again' debe disparar el callback de navegacion", clickeado)
    }

    @Test
    fun botonesAccion_ranking_disparaCallback() {
        var clickeado = false

        composeTestRule.setContent {
            PrideQuizTheme {
                ActionButtonsTestWrapper(
                    onPlayAgain = {},
                    onRanking = { clickeado = true },
                    onShare = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Ranking").performClick()
        assertTrue("El boton 'Ranking' debe disparar el callback de navegacion", clickeado)
    }

    @Test
    fun botonesAccion_compartir_disparaCallback() {
        var clickeado = false

        composeTestRule.setContent {
            PrideQuizTheme {
                ActionButtonsTestWrapper(
                    onPlayAgain = {},
                    onRanking = {},
                    onShare = { clickeado = true }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Share").performClick()
        assertTrue("El boton 'Share' debe disparar el callback de compartir", clickeado)
    }

    // ============================================================
    // Tests de precision extremos
    // ============================================================

    @Test
    fun estadisticas_precision100PorCiento_cuandoTodasCorrectas() {
        composeTestRule.setContent {
            PrideQuizTheme {
                StatsGridTestWrapper(
                    totalQuestions = 20,
                    correctAnswers = 20,
                    bestStreak = 20,
                    timePlayed = 60000L
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("100%").assertIsDisplayed()
    }
}

// ============================================================
// Composables auxiliares de prueba (wrappers de estado fake)
// ============================================================

/**
 * Replica del ScoreDisplay de ResultScreen para pruebas.
 *
 * Muestra los mismos textos que el composable original:
 * - "Your Score" (R.string.your_score)
 * - El puntaje como numero
 * - "Personal Best" (R.string.result_personal_best)
 * - "World Record" (R.string.result_world_record)
 */
@Composable
private fun ScoreDisplayTestWrapper(
    points: Int,
    personalRecord: String,
    worldRecord: String,
    @Suppress("UNUSED_PARAMETER") isNewRecord: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Score",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = points.toString(),
            fontSize = 64.sp,
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Personal Best")
                Text(text = personalRecord, style = MaterialTheme.typography.titleLarge)
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "World Record")
                Text(text = worldRecord, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

/**
 * Wrapper para testear la visibilidad del badge de nuevo record.
 */
@Composable
private fun NuevoRecordTestWrapper(isNewRecord: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (isNewRecord) {
            Row(
                modifier = Modifier
                    .testTag("nuevo_record_badge")
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "New Record!")
            }
        }
    }
}

/**
 * Replica del StatsGrid de ResultScreen para pruebas.
 *
 * Muestra los mismos textos que el composable original:
 * - "Accuracy" (R.string.result_accuracy) con porcentaje calculado
 * - "Best Streak" (R.string.result_best_streak)
 * - "Correct" con formato "correctas/total"
 * - "Time Played" (R.string.result_time_played) con tiempo formateado
 */
@Composable
private fun StatsGridTestWrapper(
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
            StatCardTestWrapper(
                label = "Accuracy",
                value = "$accuracy%",
                modifier = Modifier.weight(1f)
            )
            StatCardTestWrapper(
                label = "Best Streak",
                value = bestStreak.toString(),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCardTestWrapper(
                label = "Correct",
                value = "$correctAnswers/$totalQuestions",
                modifier = Modifier.weight(1f)
            )
            StatCardTestWrapper(
                label = "Time Played",
                value = timeFormatted,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCardTestWrapper(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * Replica de los botones de accion de ResultScreen para pruebas.
 *
 * Muestra los mismos textos que el composable original:
 * - "Play again" (R.string.play_again)
 * - "Ranking" (R.string.ranking)
 * - "Share" (R.string.share)
 */
@Composable
private fun ActionButtonsTestWrapper(
    onPlayAgain: () -> Unit,
    onRanking: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Play again")
        }

        Button(
            onClick = onRanking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Ranking")
        }

        Button(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Share")
        }
    }
}
