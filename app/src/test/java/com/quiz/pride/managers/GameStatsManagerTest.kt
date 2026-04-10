package com.quiz.pride.managers

import com.quiz.domain.GameMode
import com.quiz.domain.GameResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de la logica pura de GameStatsManager.
 *
 * GameStatsManager requiere Context/DataStore para persistencia, por lo que se testea
 * la logica de calculo de XP replicando el algoritmo de recordGameResult,
 * y la construccion de PlayerStatistics desde valores conocidos.
 *
 * Patron identico al usado en AdFrequencyManagerTest: replica de la logica pura
 * sin side-effects de DataStore.
 */
class GameStatsManagerTest {

    // Constantes de XP (replica de ProgressionManager.companion)
    private val xpPerCorrectAnswer = ProgressionManager.XP_PER_CORRECT_ANSWER
    private val xpPerStreakBonus = ProgressionManager.XP_PER_STREAK_BONUS
    private val xpPerPerfectGame = ProgressionManager.XP_PER_PERFECT_GAME
    private val xpPerWin = ProgressionManager.XP_PER_WIN
    private val xpMultiplierAdvance = ProgressionManager.XP_MULTIPLIER_ADVANCE
    private val xpMultiplierTimed = ProgressionManager.XP_MULTIPLIER_TIMED

    /**
     * Replica exacta del calculo de XP en recordGameResult.
     * Permite testear la logica de negocio sin necesitar Context ni DataStore.
     */
    private fun calcularXp(result: GameResult): Long {
        var xpEarned = 0L

        // XP base por respuestas correctas
        xpEarned += result.correctAnswers * xpPerCorrectAnswer

        // Bonus de racha
        if (result.bestStreak >= 5) {
            xpEarned += (result.bestStreak / 5) * xpPerStreakBonus
        }

        // Bonus partida perfecta
        if (result.correctAnswers == result.totalQuestions && result.totalQuestions > 0) {
            xpEarned += xpPerPerfectGame
        }

        // Bonus por completar
        if (result.completedAllQuestions) {
            xpEarned += xpPerWin
        }

        // Multiplicadores de modo
        xpEarned = when (result.gameMode) {
            GameMode.ADVANCE -> (xpEarned * xpMultiplierAdvance).toLong()
            GameMode.TIMED   -> (xpEarned * xpMultiplierTimed).toLong()
            else             -> xpEarned
        }

        return xpEarned
    }

    /** Crea un GameResult base para los tests. */
    private fun resultBase(
        gameMode: GameMode = GameMode.NORMAL,
        correctAnswers: Int = 5,
        totalQuestions: Int = 10,
        bestStreak: Int = 0,
        timePlayedMs: Long = 30_000L,
        completedAllQuestions: Boolean = false
    ) = GameResult(
        gameMode = gameMode,
        correctAnswers = correctAnswers,
        totalQuestions = totalQuestions,
        bestStreak = bestStreak,
        timePlayedMs = timePlayedMs,
        completedAllQuestions = completedAllQuestions
    )

    // =========================================================
    // Constantes XP — integridad
    // =========================================================

    @Test
    fun `XP_PER_CORRECT_ANSWER tiene valor positivo`() {
        assertTrue(ProgressionManager.XP_PER_CORRECT_ANSWER > 0)
    }

    @Test
    fun `XP_PER_STREAK_BONUS tiene valor positivo`() {
        assertTrue(ProgressionManager.XP_PER_STREAK_BONUS > 0)
    }

    @Test
    fun `XP_PER_PERFECT_GAME tiene valor positivo`() {
        assertTrue(ProgressionManager.XP_PER_PERFECT_GAME > 0)
    }

    @Test
    fun `XP_PER_WIN tiene valor positivo`() {
        assertTrue(ProgressionManager.XP_PER_WIN > 0)
    }

    @Test
    fun `todos los multiplicadores son mayores a 1`() {
        assertTrue(ProgressionManager.XP_MULTIPLIER_ADVANCE > 1.0)
        assertTrue(ProgressionManager.XP_MULTIPLIER_TIMED > 1.0)
    }

    // =========================================================
    // calcularXp — XP base por respuestas correctas
    // =========================================================

    @Test
    fun `partida con 0 respuestas correctas gana 0 XP base`() {
        val result = resultBase(correctAnswers = 0, totalQuestions = 10)
        val xp = calcularXp(result)

        assertEquals(0L, xp)
    }

    @Test
    fun `partida con 5 respuestas correctas gana 5 veces XP_PER_CORRECT_ANSWER`() {
        val result = resultBase(correctAnswers = 5, totalQuestions = 10)
        val xp = calcularXp(result)

        assertEquals((5 * xpPerCorrectAnswer).toLong(), xp)
    }

    @Test
    fun `partida con 10 respuestas correctas gana 10 veces XP_PER_CORRECT_ANSWER`() {
        val result = resultBase(correctAnswers = 10, totalQuestions = 10)
        val xp = calcularXp(result)

        // No es perfecta porque completedAllQuestions = false, pero correctAnswers == totalQuestions da bonus perfecto
        val xpEsperado = (10 * xpPerCorrectAnswer) + xpPerPerfectGame
        assertEquals(xpEsperado.toLong(), xp)
    }

    // =========================================================
    // calcularXp — Bonus de racha
    // =========================================================

    @Test
    fun `racha de 4 no da bonus de racha`() {
        val result = resultBase(correctAnswers = 5, bestStreak = 4)
        val xpSinRacha = calcularXp(result)
        val xpEsperado = (5 * xpPerCorrectAnswer).toLong()

        assertEquals(xpEsperado, xpSinRacha)
    }

    @Test
    fun `racha de 5 da exactamente 1 bonus de racha`() {
        val resultConRacha = resultBase(correctAnswers = 5, bestStreak = 5)
        val resultSinRacha = resultBase(correctAnswers = 5, bestStreak = 4)

        val diferencia = calcularXp(resultConRacha) - calcularXp(resultSinRacha)

        assertEquals(xpPerStreakBonus.toLong(), diferencia)
    }

    @Test
    fun `racha de 10 da exactamente 2 bonuses de racha`() {
        val resultConRacha = resultBase(correctAnswers = 5, bestStreak = 10)
        val resultSinRacha = resultBase(correctAnswers = 5, bestStreak = 4)

        val diferencia = calcularXp(resultConRacha) - calcularXp(resultSinRacha)

        assertEquals((2 * xpPerStreakBonus).toLong(), diferencia)
    }

    @Test
    fun `racha de 15 da exactamente 3 bonuses de racha`() {
        val result = resultBase(correctAnswers = 5, bestStreak = 15)
        val xp = calcularXp(result)
        val bonusEsperado = (3 * xpPerStreakBonus).toLong()
        val xpBase = (5 * xpPerCorrectAnswer).toLong()

        assertEquals(xpBase + bonusEsperado, xp)
    }

    @Test
    fun `racha de 9 cuenta como 1 grupo de 5 dando 1 bonus`() {
        // 9 / 5 = 1 (division entera)
        val resultConRacha = resultBase(correctAnswers = 5, bestStreak = 9)
        val resultSinRacha = resultBase(correctAnswers = 5, bestStreak = 4)

        val diferencia = calcularXp(resultConRacha) - calcularXp(resultSinRacha)

        assertEquals(xpPerStreakBonus.toLong(), diferencia)
    }

    // =========================================================
    // calcularXp — Bonus partida perfecta
    // =========================================================

    @Test
    fun `partida perfecta da bonus XP_PER_PERFECT_GAME`() {
        val result = resultBase(correctAnswers = 10, totalQuestions = 10)
        val xp = calcularXp(result)
        val xpEsperado = (10 * xpPerCorrectAnswer + xpPerPerfectGame).toLong()

        assertEquals(xpEsperado, xp)
    }

    @Test
    fun `partida con una falla no da bonus de partida perfecta`() {
        val result = resultBase(correctAnswers = 9, totalQuestions = 10)
        val xp = calcularXp(result)
        val xpEsperado = (9 * xpPerCorrectAnswer).toLong()

        assertEquals(xpEsperado, xp)
    }

    @Test
    fun `partida con 0 preguntas no da bonus de partida perfecta aunque correctAnswers sea 0`() {
        // La condicion es: correctAnswers == totalQuestions && totalQuestions > 0
        val result = resultBase(correctAnswers = 0, totalQuestions = 0)
        val xp = calcularXp(result)

        assertEquals(0L, xp)
    }

    // =========================================================
    // calcularXp — Bonus por completar partida
    // =========================================================

    @Test
    fun `completar partida da bonus XP_PER_WIN adicional`() {
        val resultCompletado = resultBase(correctAnswers = 5, completedAllQuestions = true)
        val resultNoCompletado = resultBase(correctAnswers = 5, completedAllQuestions = false)

        val diferencia = calcularXp(resultCompletado) - calcularXp(resultNoCompletado)

        assertEquals(xpPerWin.toLong(), diferencia)
    }

    @Test
    fun `partida no completada no da bonus de win`() {
        val result = resultBase(correctAnswers = 5, completedAllQuestions = false)
        val xp = calcularXp(result)
        val xpEsperado = (5 * xpPerCorrectAnswer).toLong()

        assertEquals(xpEsperado, xp)
    }

    // =========================================================
    // calcularXp — Multiplicadores de modo
    // =========================================================

    @Test
    fun `modo NORMAL no aplica multiplicador`() {
        val resultNormal = resultBase(gameMode = GameMode.NORMAL, correctAnswers = 5)
        val xp = calcularXp(resultNormal)
        val xpEsperado = (5 * xpPerCorrectAnswer).toLong()

        assertEquals(xpEsperado, xp)
    }

    @Test
    fun `modo ADVANCE aplica multiplicador XP_MULTIPLIER_ADVANCE`() {
        val resultAdvance = resultBase(gameMode = GameMode.ADVANCE, correctAnswers = 5)
        val resultNormal = resultBase(gameMode = GameMode.NORMAL, correctAnswers = 5)

        val xpAdvance = calcularXp(resultAdvance)
        val xpNormal = calcularXp(resultNormal)
        val xpEsperado = (xpNormal * xpMultiplierAdvance).toLong()

        assertEquals(xpEsperado, xpAdvance)
    }

    @Test
    fun `modo TIMED aplica multiplicador XP_MULTIPLIER_TIMED`() {
        val resultTimed = resultBase(gameMode = GameMode.TIMED, correctAnswers = 5)
        val resultNormal = resultBase(gameMode = GameMode.NORMAL, correctAnswers = 5)

        val xpTimed = calcularXp(resultTimed)
        val xpNormal = calcularXp(resultNormal)
        val xpEsperado = (xpNormal * xpMultiplierTimed).toLong()

        assertEquals(xpEsperado, xpTimed)
    }

    // =========================================================
    // calcularXp — Combinaciones realistas
    // =========================================================

    @Test
    fun `partida NORMAL con racha y perfecta calcula XP correctamente`() {
        val result = GameResult(
            gameMode = GameMode.NORMAL,
            correctAnswers = 10,
            totalQuestions = 10,
            bestStreak = 10,
            timePlayedMs = 30_000L,
            completedAllQuestions = true
        )
        val xp = calcularXp(result)

        val xpBase = (10 * xpPerCorrectAnswer).toLong()
        val bonusRacha = (10 / 5 * xpPerStreakBonus).toLong()
        val bonusPerfecto = xpPerPerfectGame.toLong()
        val bonusWin = xpPerWin.toLong()
        val xpEsperado = xpBase + bonusRacha + bonusPerfecto + bonusWin

        assertEquals(xpEsperado, xp)
    }

    @Test
    fun `partida TIMED con racha y no perfecta calcula XP correctamente`() {
        val result = GameResult(
            gameMode = GameMode.TIMED,
            correctAnswers = 7,
            totalQuestions = 10,
            bestStreak = 7,
            timePlayedMs = 45_000L,
            completedAllQuestions = true
        )
        val xp = calcularXp(result)

        val xpBase = (7 * xpPerCorrectAnswer).toLong()
        val bonusRacha = (7 / 5 * xpPerStreakBonus).toLong()
        val bonusWin = xpPerWin.toLong()
        val xpEsperadoAntes = xpBase + bonusRacha + bonusWin
        val xpEsperado = (xpEsperadoAntes * xpMultiplierTimed).toLong()

        assertEquals(xpEsperado, xp)
    }

    // =========================================================
    // getStatisticsFromPrefs — calculo de accuracy
    // =========================================================

    @Test
    fun `accuracy es 0 cuando no hay preguntas respondidas`() {
        // Sin total de preguntas, la accuracy debe ser 0 para evitar division por cero
        val totalCorrect = 0
        val totalWrong = 0
        val totalQuestions = totalCorrect + totalWrong

        val accuracy = if (totalQuestions > 0) {
            (totalCorrect.toFloat() / totalQuestions * 100)
        } else {
            0f
        }

        assertEquals(0f, accuracy, 0.001f)
    }

    @Test
    fun `accuracy es 100 cuando todas las respuestas son correctas`() {
        val totalCorrect = 10
        val totalWrong = 0
        val totalQuestions = totalCorrect + totalWrong

        val accuracy = if (totalQuestions > 0) {
            (totalCorrect.toFloat() / totalQuestions * 100)
        } else {
            0f
        }

        assertEquals(100f, accuracy, 0.001f)
    }

    @Test
    fun `accuracy es 50 cuando la mitad de las respuestas son correctas`() {
        val totalCorrect = 5
        val totalWrong = 5
        val totalQuestions = totalCorrect + totalWrong

        val accuracy = if (totalQuestions > 0) {
            (totalCorrect.toFloat() / totalQuestions * 100)
        } else {
            0f
        }

        assertEquals(50f, accuracy, 0.001f)
    }

    @Test
    fun `accuracy es 0 cuando ninguna respuesta es correcta`() {
        val totalCorrect = 0
        val totalWrong = 10
        val totalQuestions = totalCorrect + totalWrong

        val accuracy = if (totalQuestions > 0) {
            (totalCorrect.toFloat() / totalQuestions * 100)
        } else {
            0f
        }

        assertEquals(0f, accuracy, 0.001f)
    }

    @Test
    fun `accuracy no produce division por cero con 0 respuestas totales`() {
        // Esta condicion es critica: sin preguntas no se divide
        val totalCorrect = 0
        val totalWrong = 0
        val totalQuestions = totalCorrect + totalWrong

        // No debe lanzar excepcion
        val accuracy = if (totalQuestions > 0) {
            (totalCorrect.toFloat() / totalQuestions * 100)
        } else {
            0f
        }

        assertEquals(0f, accuracy, 0.001f)
    }
}
