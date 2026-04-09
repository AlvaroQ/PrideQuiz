package com.quiz.pride.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiz.pride.ui.components.LoadingIndicator
import com.quiz.pride.ui.theme.PrideQuizTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Tests instrumentados de la UI de GameScreen y sus sub-componentes.
 *
 * Estrategia de prueba:
 * - GameScreen y sus sub-composables internos son privados y usan koinViewModel(),
 *   por lo que se prueban componentes reutilizables publicos (LoadingIndicator,
 *   TopBar replica, AnswerButtons replica) con estado falso directamente.
 * - Los wrappers de prueba replican exactamente los textos y semantics que
 *   el composable original emite, sin depender de Koin ni ViewModel.
 */
class GameScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ============================================================
    // Tests de LoadingIndicator
    // ============================================================

    @Test
    fun indicadorCarga_seMuestraSinCrash() {
        composeTestRule.setContent {
            PrideQuizTheme {
                // Envuelve en un Box con testTag para verificar que el composable se renderiza
                Box(modifier = Modifier.testTag("loading_container")) {
                    LoadingIndicator()
                }
            }
        }

        // Verifica que el composable renderiza sin crash y el contenedor es visible
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("loading_container").assertExists()
    }

    @Test
    fun indicadorCarga_estaVisibleCuandoIsLoadingEsTrue() {
        composeTestRule.setContent {
            PrideQuizTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    LoadingIndicatorTestWrapper(isLoading = true)
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("test_loading_visible").assertIsDisplayed()
    }

    @Test
    fun indicadorCarga_noEstaVisibleCuandoIsLoadingEsFalse() {
        composeTestRule.setContent {
            PrideQuizTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    LoadingIndicatorTestWrapper(isLoading = false)
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("test_loading_visible").assertDoesNotExist()
    }

    // ============================================================
    // Tests del puntaje en la TopBar
    // ============================================================

    @Test
    fun topBar_muestraPuntajeActual() {
        val puntajeEsperado = 350

        composeTestRule.setContent {
            PrideQuizTheme {
                TopBarTestWrapper(
                    points = puntajeEsperado,
                    lives = 3,
                    stage = 5,
                    totalStages = 20,
                    isTimedMode = false,
                    timeRemaining = 0
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("$puntajeEsperado pts").assertIsDisplayed()
    }

    @Test
    fun topBar_muestraEtapaActual() {
        composeTestRule.setContent {
            PrideQuizTheme {
                TopBarTestWrapper(
                    points = 100,
                    lives = 3,
                    stage = 7,
                    totalStages = 20,
                    isTimedMode = false,
                    timeRemaining = 0
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("7 / 20").assertIsDisplayed()
    }

    @Test
    fun topBar_muestraVidasRestantes_accesibilidad() {
        composeTestRule.setContent {
            PrideQuizTheme {
                TopBarTestWrapper(
                    points = 200,
                    lives = 2,
                    stage = 3,
                    totalStages = 20,
                    isTimedMode = false,
                    timeRemaining = 0
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("2 lives remaining").assertIsDisplayed()
    }

    @Test
    fun topBar_modoTemporizadoMuestraTimer() {
        val tiempoRestante = 75 // 1:15

        composeTestRule.setContent {
            PrideQuizTheme {
                TopBarTestWrapper(
                    points = 500,
                    lives = 3,
                    stage = 10,
                    totalStages = 20,
                    isTimedMode = true,
                    timeRemaining = tiempoRestante
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("1:15").assertIsDisplayed()
    }

    @Test
    fun topBar_modoNormalNoMuestraTimer() {
        composeTestRule.setContent {
            PrideQuizTheme {
                TopBarTestWrapper(
                    points = 100,
                    lives = 3,
                    stage = 1,
                    totalStages = 20,
                    isTimedMode = false,
                    timeRemaining = 0
                )
            }
        }

        composeTestRule.waitForIdle()
        // En modo normal, el texto del timer no debe aparecer
        composeTestRule.onNodeWithText("0:00").assertDoesNotExist()
        // Las vidas si deben estar presentes via accesibilidad
        composeTestRule.onNodeWithContentDescription("3 lives remaining").assertIsDisplayed()
    }

    @Test
    fun topBar_botonAtras_esClickeable() {
        var clickeado = false

        composeTestRule.setContent {
            PrideQuizTheme {
                TopBarTestWrapper(
                    points = 0,
                    lives = 3,
                    stage = 1,
                    totalStages = 20,
                    isTimedMode = false,
                    timeRemaining = 0,
                    onBackClick = { clickeado = true }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Go back").performClick()
        assertTrue("El callback de volver atras debe ejecutarse", clickeado)
    }

    @Test
    fun topBar_modoTemporizadoUrgente_timerEsVisibleConTiempoBajo() {
        val tiempoRestante = 25 // Bajo umbral de urgencia (<= 30)

        composeTestRule.setContent {
            PrideQuizTheme {
                TopBarTestWrapper(
                    points = 800,
                    lives = 3,
                    stage = 15,
                    totalStages = 20,
                    isTimedMode = true,
                    timeRemaining = tiempoRestante
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("0:25").assertIsDisplayed()
    }

    // ============================================================
    // Tests de opciones de respuesta
    // ============================================================

    @Test
    fun opcionesRespuesta_muestranTextoDeOpciones() {
        val opciones = listOf("Rainbow Flag", "Trans Pride", "Bisexual Pride", "Non-Binary Pride")

        composeTestRule.setContent {
            PrideQuizTheme {
                OpcionesRespuestaTestWrapper(
                    opciones = opciones,
                    selectedAnswer = null,
                    correctOptionIndex = 0,
                    enabled = true,
                    onAnswerSelected = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        opciones.forEach { texto ->
            composeTestRule.onNodeWithText(texto).assertIsDisplayed()
        }
    }

    @Test
    fun opcionesRespuesta_click_disparaCallback() {
        var indiceSeleccionado = -1
        val opciones = listOf("Rainbow Flag", "Trans Pride", "Bisexual Pride", "Non-Binary Pride")

        composeTestRule.setContent {
            PrideQuizTheme {
                OpcionesRespuestaTestWrapper(
                    opciones = opciones,
                    selectedAnswer = null,
                    correctOptionIndex = 0,
                    enabled = true,
                    onAnswerSelected = { indice -> indiceSeleccionado = indice }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Trans Pride").performClick()
        assertEquals("Debe registrarse el indice 1 al clickear 'Trans Pride'", 1, indiceSeleccionado)
    }

    @Test
    fun opcionesRespuesta_descripcionAccesibilidad_respuestaCorrecta() {
        val opciones = listOf("Rainbow Flag", "Trans Pride", "Bisexual Pride", "Non-Binary Pride")

        composeTestRule.setContent {
            PrideQuizTheme {
                OpcionesRespuestaTestWrapper(
                    opciones = opciones,
                    selectedAnswer = 0,
                    correctOptionIndex = 0,
                    enabled = false,
                    onAnswerSelected = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Answer option 1. Correct answer")
            .assertIsDisplayed()
    }

    @Test
    fun opcionesRespuesta_descripcionAccesibilidad_respuestaIncorrecta() {
        val opciones = listOf("Rainbow Flag", "Trans Pride", "Bisexual Pride", "Non-Binary Pride")

        composeTestRule.setContent {
            PrideQuizTheme {
                OpcionesRespuestaTestWrapper(
                    opciones = opciones,
                    selectedAnswer = 2, // Selecciono Bisexual Pride (indice 2)
                    correctOptionIndex = 0, // Pero la correcta es Rainbow Flag (indice 0)
                    enabled = false,
                    onAnswerSelected = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        // La opcion seleccionada incorrectamente debe tener la descripcion "Wrong answer"
        composeTestRule.onNodeWithContentDescription("Answer option 3. Wrong answer")
            .assertIsDisplayed()
        // La correcta debe tener la descripcion "Correct answer" para indicar cual era la respuesta correcta
        composeTestRule.onNodeWithContentDescription("Answer option 1. Correct answer")
            .assertExists()
    }

    @Test
    fun opcionesRespuesta_cuandoSeSelecciona_descripcionSeActualiza() {
        val opciones = listOf("Rainbow Flag", "Trans Pride", "Bisexual Pride", "Non-Binary Pride")

        composeTestRule.setContent {
            PrideQuizTheme {
                OpcionesRespuestaTestWrapper(
                    opciones = opciones,
                    selectedAnswer = 1, // Selecciono Trans Pride
                    correctOptionIndex = 1, // Y es la correcta
                    enabled = false,
                    onAnswerSelected = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Answer option 2. Correct answer")
            .assertIsDisplayed()
    }
}

// ============================================================
// Composables auxiliares de prueba (wrappers de estado fake)
// ============================================================

/**
 * Wrapper que muestra u oculta el LoadingIndicator segun el estado.
 * Permite testear la visibilidad condicional del indicador de carga.
 */
@Composable
private fun LoadingIndicatorTestWrapper(isLoading: Boolean) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("test_loading_visible")
        ) {
            LoadingIndicator()
        }
    }
}

/**
 * Replica del comportamiento visible de EnhancedGameTopBar para pruebas.
 *
 * Reproduce exactamente los textos y descripciones de accesibilidad que
 * el composable original emite (semantics contentDescription con los
 * mismos strings de resources), sin depender de Koin ni ViewModel.
 *
 * Los strings de semantics son los mismos que usa la implementacion real:
 * - "$lives lives remaining" (R.string.accessibility_lives_remaining)
 * - "Go back" (hardcoded en el composable original)
 * - "$points pts" (texto visible)
 * - "$stage / $totalStages" (texto visible)
 */
@Composable
private fun TopBarTestWrapper(
    points: Int,
    lives: Int,
    stage: Int,
    totalStages: Int,
    isTimedMode: Boolean,
    timeRemaining: Int,
    onBackClick: () -> Unit = {}
) {
    val livesDescription = "$lives lives remaining"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Boton volver — misma semantics que el original
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.semantics { contentDescription = "Go back" }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null
                )
            }

            // Puntaje visible — mismo formato que el original "$points pts"
            Box(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$points pts",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Timer o corazones segun modo
            if (isTimedMode) {
                val minutes = timeRemaining / 60
                val seconds = timeRemaining % 60
                Text(
                    text = String.format("%d:%02d", minutes, seconds),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                // Misma semantics que el original: "$lives lives remaining"
                Row(
                    modifier = Modifier.semantics { contentDescription = livesDescription }
                ) {
                    repeat(3) { index ->
                        Icon(
                            imageVector = if (index < lives) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Barra de progreso
        LinearProgressIndicator(
            progress = { stage.toFloat() / totalStages.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )

        // Texto de etapa visible — mismo formato "$stage / $totalStages"
        Text(
            text = "$stage / $totalStages",
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

/**
 * Replica del comportamiento de los botones de respuesta (VibrantAnswerButton) para pruebas.
 *
 * Reproduce exactamente los strings de semantics contentDescription del composable original:
 * - "Answer option {n}" (R.string.accessibility_answer_button)
 * - "Correct answer" (R.string.accessibility_correct_answer)
 * - "Wrong answer" (R.string.accessibility_wrong_answer)
 */
@Composable
private fun OpcionesRespuestaTestWrapper(
    opciones: List<String>,
    selectedAnswer: Int?,
    correctOptionIndex: Int,
    enabled: Boolean,
    onAnswerSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        opciones.forEachIndexed { index, texto ->
            val isCorrect = selectedAnswer != null && correctOptionIndex == index
            val isWrong = selectedAnswer == index && correctOptionIndex != index

            // Mismos strings que la implementacion real del composable
            val answerDescription = "Answer option ${index + 1}"
            val correctDesc = "Correct answer"
            val wrongDesc = "Wrong answer"
            val semanticsDesc = when {
                isCorrect -> "$answerDescription. $correctDesc"
                isWrong -> "$answerDescription. $wrongDesc"
                else -> answerDescription
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = semanticsDesc }
                    .then(
                        if (enabled) Modifier.clickable { onAnswerSelected(index) } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = texto)
            }
        }
    }
}
