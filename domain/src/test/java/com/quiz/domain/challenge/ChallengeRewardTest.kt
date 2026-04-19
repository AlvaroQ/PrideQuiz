package com.quiz.domain.challenge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de la logica pura de ChallengeReward.
 *
 * ChallengeReward es un data class puro con logica en el companion object.
 * No requiere mocks ni dependencias de Android.
 */
class ChallengeRewardTest {

    // =========================================================
    // forDifficulty — Recompensas por dificultad
    // =========================================================

    @Test
    fun `forDifficulty EASY retorna 25 XP y 5 monedas`() {
        val reward = ChallengeReward.forDifficulty(ChallengeDifficulty.EASY)

        assertEquals(25, reward.xp)
        assertEquals(5, reward.coins)
    }

    @Test
    fun `forDifficulty MEDIUM retorna 50 XP y 10 monedas`() {
        val reward = ChallengeReward.forDifficulty(ChallengeDifficulty.MEDIUM)

        assertEquals(50, reward.xp)
        assertEquals(10, reward.coins)
    }

    @Test
    fun `forDifficulty HARD retorna 100 XP y 20 monedas`() {
        val reward = ChallengeReward.forDifficulty(ChallengeDifficulty.HARD)

        assertEquals(100, reward.xp)
        assertEquals(20, reward.coins)
    }

    @Test
    fun `forDifficulty WEEKLY retorna 200 XP y 50 monedas`() {
        val reward = ChallengeReward.forDifficulty(ChallengeDifficulty.WEEKLY)

        assertEquals(200, reward.xp)
        assertEquals(50, reward.coins)
    }

    @Test
    fun `recompensas XP escalan con la dificultad`() {
        val easy = ChallengeReward.forDifficulty(ChallengeDifficulty.EASY)
        val medium = ChallengeReward.forDifficulty(ChallengeDifficulty.MEDIUM)
        val hard = ChallengeReward.forDifficulty(ChallengeDifficulty.HARD)
        val weekly = ChallengeReward.forDifficulty(ChallengeDifficulty.WEEKLY)

        assertTrue(easy.xp < medium.xp)
        assertTrue(medium.xp < hard.xp)
        assertTrue(hard.xp < weekly.xp)
    }

    @Test
    fun `recompensas de monedas escalan con la dificultad`() {
        val easy = ChallengeReward.forDifficulty(ChallengeDifficulty.EASY)
        val medium = ChallengeReward.forDifficulty(ChallengeDifficulty.MEDIUM)
        val hard = ChallengeReward.forDifficulty(ChallengeDifficulty.HARD)
        val weekly = ChallengeReward.forDifficulty(ChallengeDifficulty.WEEKLY)

        assertTrue(easy.coins < medium.coins)
        assertTrue(medium.coins < hard.coins)
        assertTrue(hard.coins < weekly.coins)
    }

    @Test
    fun `todas las recompensas tienen XP positivo`() {
        ChallengeDifficulty.entries.forEach { dificultad ->
            val reward = ChallengeReward.forDifficulty(dificultad)
            assertTrue("XP debe ser positivo para $dificultad", reward.xp > 0)
        }
    }

    @Test
    fun `todas las recompensas tienen monedas positivas`() {
        ChallengeDifficulty.entries.forEach { dificultad ->
            val reward = ChallengeReward.forDifficulty(dificultad)
            assertTrue("Coins debe ser positivo para $dificultad", reward.coins > 0)
        }
    }

    // =========================================================
    // ALL_DAILY_COMPLETE_BONUS
    // =========================================================

    @Test
    fun `ALL_DAILY_COMPLETE_BONUS retorna 75 XP y 15 monedas`() {
        val bonus = ChallengeReward.ALL_DAILY_COMPLETE_BONUS

        assertEquals(75, bonus.xp)
        assertEquals(15, bonus.coins)
    }

    @Test
    fun `ALL_DAILY_COMPLETE_BONUS tiene XP mayor a la recompensa EASY`() {
        val bonus = ChallengeReward.ALL_DAILY_COMPLETE_BONUS
        val easy = ChallengeReward.forDifficulty(ChallengeDifficulty.EASY)

        assertTrue(bonus.xp > easy.xp)
    }

    @Test
    fun `ALL_DAILY_COMPLETE_BONUS tiene XP menor a la recompensa HARD`() {
        val bonus = ChallengeReward.ALL_DAILY_COMPLETE_BONUS
        val hard = ChallengeReward.forDifficulty(ChallengeDifficulty.HARD)

        assertTrue(bonus.xp < hard.xp)
    }

    // =========================================================
    // Integridad del enum ChallengeDifficulty
    // =========================================================

    @Test
    fun `ChallengeDifficulty tiene exactamente 4 valores`() {
        assertEquals(4, ChallengeDifficulty.entries.size)
    }

    @Test
    fun `forDifficulty cubre todos los valores del enum sin lanzar excepcion`() {
        // Si se agrega una dificultad al enum sin actualizar forDifficulty, este test falla
        ChallengeDifficulty.entries.forEach { dificultad ->
            val reward = ChallengeReward.forDifficulty(dificultad)
            assertTrue(reward.xp > 0)
        }
    }
}
