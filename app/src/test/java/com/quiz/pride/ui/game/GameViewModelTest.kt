package com.quiz.pride.ui.game

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import arrow.core.Either
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.Name
import com.quiz.domain.Pride
import com.quiz.pride.MainDispatcherRule
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.ThemeManager
import com.quiz.pride.utils.Constants
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetPrideById
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: GameViewModel

    private val getPrideById: GetPrideById = mockk()
    private val getPaymentDone: GetPaymentDone = mockk()
    private val analyticsManager: AnalyticsManager = mockk(relaxed = true)
    private val themeManager: ThemeManager = mockk()
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()

    private fun buildPride(id: Int = 0): Pride = Pride(
        name = Name(ES = "Orgullo $id", EN = "Pride $id"),
        description = Name(ES = "Descripcion $id", EN = "Description $id"),
        flag = "flag_$id"
    )

    @Before
    fun setup() {
        every { getPaymentDone.invoke() } returns false
        every { themeManager.isSoundEnabled } returns flowOf(true)

        // Por defecto, cualquier ID devuelve una Pride valida
        coEvery { getPrideById.invoke(any()) } answers {
            val id = firstArg<Int>()
            Either.Right(buildPride(id))
        }

        viewModel = GameViewModel(
            getPrideById = getPrideById,
            getPaymentDone = getPaymentDone,
            analyticsManager = analyticsManager,
            themeManager = themeManager,
            savedStateHandle = savedStateHandle
        )
    }

    // =========================================================
    // generateNewStage
    // =========================================================

    @Test
    fun `generateNewStage actualiza el estado con una pregunta no nula`() = runTest {
        viewModel.generateNewStage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.question)
        assertEquals(4, state.options.size)
    }

    @Test
    fun `generateNewStage coloca la respuesta correcta en el indice declarado`() = runTest {
        viewModel.generateNewStage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val question = state.question
        val correctIndex = state.correctOptionIndex

        assertTrue("El indice correcto debe estar en rango [0,3]", correctIndex in 0..3)
        assertEquals(question, state.options[correctIndex])
    }

    @Test
    fun `generateNewStage cuando falla getPrideById deja isLoading en false`() = runTest {
        coEvery { getPrideById.invoke(any()) } returns Either.Left(RepositoryException.NoConnectionException)

        viewModel.generateNewStage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
    }

    // =========================================================
    // onAnswerSelected — respuesta correcta
    // =========================================================

    @Test
    fun `onAnswerSelected con respuesta correcta incrementa puntos en 1`() = runTest {
        viewModel.generateNewStage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val correctIndex = state.correctOptionIndex
        val puntosAntes = state.points

        viewModel.onAnswerSelected(correctIndex, "En llamas", "Imparable", "Legendario", "Combo x%d")
        advanceUntilIdle()

        assertEquals(puntosAntes + 1, viewModel.uiState.value.points)
    }

    @Test
    fun `onAnswerSelected con respuesta correcta incrementa correctAnswers en 1`() = runTest {
        viewModel.generateNewStage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val correctIndex = state.correctOptionIndex

        viewModel.onAnswerSelected(correctIndex, "En llamas", "Imparable", "Legendario", "Combo x%d")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.correctAnswers)
    }

    @Test
    fun `onAnswerSelected con respuesta correcta emite evento PlaySuccessSound`() = runTest {
        viewModel.generateNewStage()
        advanceUntilIdle()

        val correctIndex = viewModel.uiState.value.correctOptionIndex

        viewModel.events.test {
            viewModel.onAnswerSelected(correctIndex, "En llamas", "Imparable", "Legendario", "Combo x%d")

            val event = awaitItem()
            assertTrue(event is GameEvent.PlaySuccessSound)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onAnswerSelected con respuesta correcta actualiza streak`() = runTest {
        viewModel.generateNewStage()
        advanceUntilIdle()

        val correctIndex = viewModel.uiState.value.correctOptionIndex

        viewModel.onAnswerSelected(correctIndex, "En llamas", "Imparable", "Legendario", "Combo x%d")

        val stateInmediato = viewModel.uiState.value
        assertEquals(1, stateInmediato.currentStreak)
        assertEquals(1, stateInmediato.bestStreak)
    }

    // =========================================================
    // onAnswerSelected — respuesta incorrecta
    // =========================================================

    @Test
    fun `onAnswerSelected con respuesta incorrecta resta una vida`() = runTest {
        viewModel.generateNewStage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val incorrectIndex = (state.correctOptionIndex + 1) % 4
        val vidasAntes = state.lives

        viewModel.onAnswerSelected(incorrectIndex, "En llamas", "Imparable", "Legendario", "Combo x%d")

        val stateActualizado = viewModel.uiState.value
        assertEquals(vidasAntes - 1, stateActualizado.lives)
    }

    @Test
    fun `onAnswerSelected con respuesta incorrecta resetea el streak`() = runTest {
        viewModel.generateNewStage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val incorrectIndex = (state.correctOptionIndex + 1) % 4

        viewModel.onAnswerSelected(incorrectIndex, "En llamas", "Imparable", "Legendario", "Combo x%d")

        assertEquals(0, viewModel.uiState.value.currentStreak)
    }

    @Test
    fun `onAnswerSelected con respuesta incorrecta emite evento PlayFailSound`() = runTest {
        viewModel.generateNewStage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val incorrectIndex = (state.correctOptionIndex + 1) % 4

        viewModel.events.test {
            viewModel.onAnswerSelected(incorrectIndex, "En llamas", "Imparable", "Legendario", "Combo x%d")

            val event = awaitItem()
            assertTrue(event is GameEvent.PlayFailSound)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // =========================================================
    // Game Over — vidas agotadas
    // =========================================================

    @Test
    fun `game over ocurre cuando se agotan las vidas en modo normal`() = runTest {
        viewModel.initGame(Constants.GameType.NORMAL)
        advanceUntilIdle()

        // Responder 3 veces incorrectas para agotar 3 vidas
        // Avanzamos el tiempo virtual para que el delay(1000) dentro del ViewModel se ejecute
        repeat(3) {
            val state = viewModel.uiState.value
            val incorrectIndex = (state.correctOptionIndex + 1) % 4
            viewModel.onAnswerSelected(incorrectIndex, "", "", "", "")
            advanceTimeBy(1100L) // Superar el delay(1000)
            advanceUntilIdle()
        }

        // Despues de 3 respuestas incorrectas debe haber emitido NavigateToResult
        // Verificamos el estado final: 0 vidas y isLoading = false
        val finalState = viewModel.uiState.value
        assertTrue(
            "Debe tener 0 vidas o haber navegado. Lives: ${finalState.lives}",
            finalState.lives <= 0 || finalState.showExtraLifeDialog
        )
    }

    // =========================================================
    // generateRandomWithExclusion (probado indirectamente)
    // =========================================================

    @Test
    fun `generateNewStage no repite la misma pregunta en llamadas consecutivas`() = runTest {
        val questionsIds = mutableSetOf<Pride?>()

        repeat(5) {
            viewModel.generateNewStage()
            advanceUntilIdle()
            questionsIds.add(viewModel.uiState.value.question)
        }

        // Con 5 llamadas en un banco de 110+ prides, NO debe repetir ninguna
        assertEquals(5, questionsIds.size)
    }

    // =========================================================
    // Modo timed — vidas infinitas
    // =========================================================

    @Test
    fun `en modo timed las vidas no disminuyen con respuesta incorrecta`() = runTest {
        viewModel.initGame(Constants.GameType.TIMED)
        advanceUntilIdle()

        val livesAntes = viewModel.uiState.value.lives
        assertTrue("Las vidas en modo timed deben ser Int.MAX_VALUE", livesAntes == Int.MAX_VALUE)

        val state = viewModel.uiState.value
        val incorrectIndex = (state.correctOptionIndex + 1) % 4
        viewModel.onAnswerSelected(incorrectIndex, "", "", "", "")

        // Las vidas no deben reducirse en modo timed
        assertEquals(Int.MAX_VALUE, viewModel.uiState.value.lives)
    }

    // =========================================================
    // Ignorar segunda seleccion de respuesta
    // =========================================================

    @Test
    fun `onAnswerSelected ignora segunda llamada si ya hay respuesta seleccionada`() = runTest {
        viewModel.generateNewStage()
        advanceUntilIdle()

        val correctIndex = viewModel.uiState.value.correctOptionIndex

        // Primera seleccion
        viewModel.onAnswerSelected(correctIndex, "", "", "", "")
        val puntosTrasPrimera = viewModel.uiState.value.points

        // Segunda seleccion — debe ser ignorada (selectedAnswer != null)
        viewModel.onAnswerSelected(correctIndex, "", "", "", "")
        val puntosTrasTercera = viewModel.uiState.value.points

        assertEquals(puntosTrasPrimera, puntosTrasTercera)
    }

    // =========================================================
    // Dialogo de salida
    // =========================================================

    @Test
    fun `showExitDialog activa el flag en el estado`() {
        viewModel.showExitDialog()
        assertTrue(viewModel.uiState.value.showExitDialog)
    }

    @Test
    fun `dismissExitDialog desactiva el flag en el estado`() {
        viewModel.showExitDialog()
        viewModel.dismissExitDialog()
        assertFalse(viewModel.uiState.value.showExitDialog)
    }

    // =========================================================
    // initGame
    // =========================================================

    @Test
    fun `initGame NORMAL configura 3 vidas y no es modo timed`() = runTest {
        viewModel.initGame(Constants.GameType.NORMAL)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.lives)
        assertFalse(state.isTimedMode)
    }

    @Test
    fun `initGame TIMED configura vidas infinitas y activa modo timed`() = runTest {
        viewModel.initGame(Constants.GameType.TIMED)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(Int.MAX_VALUE, state.lives)
        assertTrue(state.isTimedMode)
    }
}
