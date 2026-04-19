package com.quiz.pride.ui.game

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import arrow.core.Either
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.Name
import com.quiz.domain.Pride
import com.quiz.pride.MainDispatcherRule
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.DailyChallengeManager
import com.quiz.pride.managers.ThemeManager
import com.quiz.pride.support.createGameViewModel
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
    private val dailyChallengeManager: DailyChallengeManager = mockk(relaxed = true)
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

        viewModel = createGameViewModel(
            getPrideById = getPrideById,
            getPaymentDone = getPaymentDone,
            analyticsManager = analyticsManager,
            themeManager = themeManager,
            savedStateHandle = savedStateHandle,
            dailyChallengeManager = dailyChallengeManager,
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

        // Despues de 3 respuestas incorrectas, con extraLivesUsed=0 < maxExtraLives=2
        // y getPaymentDone=false, el VM debe mostrar el dialogo de vida extra
        val finalState = viewModel.uiState.value
        assertTrue(
            "Con vidas agotadas y extra lives disponibles debe mostrar el dialogo. Lives: ${finalState.lives}, showExtraLifeDialog: ${finalState.showExtraLifeDialog}",
            finalState.showExtraLifeDialog
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

    // =========================================================
    // retryCurrentStage
    // =========================================================

    @Test
    fun `retryCurrentStage limpia hasError y genera nueva pregunta`() = runTest {
        // Force an error
        coEvery { getPrideById.invoke(any()) } returns Either.Left(RepositoryException.DataNotFoundException)
        viewModel.initGame(Constants.GameType.NORMAL)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasError)

        // Fix datasource and retry
        coEvery { getPrideById.invoke(any()) } answers { Either.Right(buildPride(firstArg())) }
        viewModel.retryCurrentStage()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasError)
        assertNotNull(viewModel.uiState.value.question)
    }

    // =========================================================
    // onExtraLifeAccepted / onExtraLifeDeclined
    // =========================================================

    @Test
    fun `onExtraLifeAccepted restaura 1 vida e incrementa extraLivesUsed`() = runTest {
        viewModel.initGame(Constants.GameType.NORMAL)
        advanceUntilIdle()

        viewModel.onExtraLifeAccepted()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.lives)
        assertEquals(1, state.extraLivesUsed)
        assertFalse(state.showExtraLifeDialog)
    }

    @Test
    fun `onExtraLifeDeclined cierra dialog y emite NavigateToResult`() = runTest {
        viewModel.initGame(Constants.GameType.NORMAL)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onExtraLifeDeclined()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is GameEvent.NavigateToResult)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // =========================================================
    // SavedStateHandle restoration
    // =========================================================

    @Test
    fun `init restaura estado desde SavedStateHandle tras process death`() = runTest {
        val handle = SavedStateHandle(mapOf(
            "points" to 15,
            "lives" to 2,
            "stage" to 8,
            "correctAnswers" to 12,
            "bestStreak" to 6
        ))

        val vm = createGameViewModel(
            getPrideById = getPrideById,
            getPaymentDone = getPaymentDone,
            analyticsManager = analyticsManager,
            themeManager = themeManager,
            savedStateHandle = handle,
            dailyChallengeManager = dailyChallengeManager,
        )

        assertEquals(15, vm.uiState.value.points)
        assertEquals(2, vm.uiState.value.lives)
        assertEquals(8, vm.uiState.value.stage)
        assertEquals(12, vm.uiState.value.correctAnswers)
        assertEquals(6, vm.uiState.value.bestStreak)
    }
}
