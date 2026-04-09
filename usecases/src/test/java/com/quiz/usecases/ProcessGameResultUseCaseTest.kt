package com.quiz.usecases

import com.quiz.data.datasource.GameResultProcessorDataSource
import com.quiz.domain.Achievement
import com.quiz.domain.GameMode
import com.quiz.domain.GameResult
import com.quiz.domain.XpGainResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProcessGameResultUseCaseTest {

    private lateinit var useCase: ProcessGameResultUseCase
    private val processor: GameResultProcessorDataSource = mockk()

    private val sampleResult = GameResult(
        gameMode = GameMode.NORMAL,
        correctAnswers = 8,
        totalQuestions = 10,
        bestStreak = 5,
        timePlayedMs = 60_000L,
        completedAllQuestions = false
    )

    private val sampleXpGain = XpGainResult(
        xpGained = 100L,
        totalXp = 500L,
        oldLevel = 3,
        newLevel = 3,
        leveledUp = false
    )

    @Before
    fun setup() {
        useCase = ProcessGameResultUseCase(processor)
    }

    // =========================================================
    // Orchestration order — critical business rule
    // =========================================================

    @Test
    fun `invoke ejecuta las 3 operaciones en orden correcto`() = runTest {
        coEvery { processor.recordGameAndComputeXp(any()) } returns sampleXpGain
        coEvery { processor.checkAndUnlockAchievements() } returns emptyList()
        coEvery { processor.triggerXpSync() } returns Unit

        useCase(sampleResult)

        // Order matters: stats first, then achievements, then sync
        coVerifyOrder {
            processor.recordGameAndComputeXp(sampleResult)
            processor.checkAndUnlockAchievements()
            processor.triggerXpSync()
        }
    }

    // =========================================================
    // Return value — positive cases
    // =========================================================

    @Test
    fun `invoke retorna xpGainResult del processor`() = runTest {
        coEvery { processor.recordGameAndComputeXp(any()) } returns sampleXpGain
        coEvery { processor.checkAndUnlockAchievements() } returns emptyList()
        coEvery { processor.triggerXpSync() } returns Unit

        val result = useCase(sampleResult)

        assertEquals(100L, result.xpGainResult.xpGained)
        assertEquals(500L, result.xpGainResult.totalXp)
        assertEquals(3, result.xpGainResult.newLevel)
        assertFalse(result.xpGainResult.leveledUp)
    }

    @Test
    fun `invoke retorna achievements desbloqueados`() = runTest {
        val achievements = listOf(Achievement.FIRST_GAME, Achievement.STREAK_5)
        coEvery { processor.recordGameAndComputeXp(any()) } returns sampleXpGain
        coEvery { processor.checkAndUnlockAchievements() } returns achievements
        coEvery { processor.triggerXpSync() } returns Unit

        val result = useCase(sampleResult)

        assertEquals(2, result.newAchievements.size)
        assertTrue(result.newAchievements.contains(Achievement.FIRST_GAME))
        assertTrue(result.newAchievements.contains(Achievement.STREAK_5))
    }

    @Test
    fun `invoke con level up retorna leveledUp true`() = runTest {
        val xpWithLevelUp = sampleXpGain.copy(newLevel = 4, leveledUp = true)
        coEvery { processor.recordGameAndComputeXp(any()) } returns xpWithLevelUp
        coEvery { processor.checkAndUnlockAchievements() } returns emptyList()
        coEvery { processor.triggerXpSync() } returns Unit

        val result = useCase(sampleResult)

        assertTrue(result.xpGainResult.leveledUp)
        assertEquals(4, result.xpGainResult.newLevel)
    }

    // =========================================================
    // Edge cases
    // =========================================================

    @Test
    fun `invoke sin achievements retorna lista vacia`() = runTest {
        coEvery { processor.recordGameAndComputeXp(any()) } returns sampleXpGain
        coEvery { processor.checkAndUnlockAchievements() } returns emptyList()
        coEvery { processor.triggerXpSync() } returns Unit

        val result = useCase(sampleResult)

        assertTrue(result.newAchievements.isEmpty())
    }

    @Test
    fun `invoke pasa el GameResult exacto al processor`() = runTest {
        coEvery { processor.recordGameAndComputeXp(any()) } returns sampleXpGain
        coEvery { processor.checkAndUnlockAchievements() } returns emptyList()
        coEvery { processor.triggerXpSync() } returns Unit

        useCase(sampleResult)

        coVerify { processor.recordGameAndComputeXp(sampleResult) }
    }

    @Test
    fun `invoke siempre llama triggerXpSync aunque no haya achievements`() = runTest {
        coEvery { processor.recordGameAndComputeXp(any()) } returns sampleXpGain
        coEvery { processor.checkAndUnlockAchievements() } returns emptyList()
        coEvery { processor.triggerXpSync() } returns Unit

        useCase(sampleResult)

        coVerify(exactly = 1) { processor.triggerXpSync() }
    }
}
