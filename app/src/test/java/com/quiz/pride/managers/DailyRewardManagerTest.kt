package com.quiz.pride.managers

import com.quiz.domain.reward.RewardTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de la logica pura de `generateDailyReward`.
 *
 * generateDailyReward es una top-level function sin Context ni DataStore:
 * dado un `date` y un `installId`, retorna una DailyReward determinista.
 * Eso permite testear la distribucion de tiers y los rangos de XP/coins/gems
 * sin necesidad de Robolectric ni mocks.
 */
class DailyRewardManagerTest {

    // =========================================================
    // Determinismo: misma entrada -> misma salida
    // =========================================================

    @Test
    fun `misma fecha y mismo installId generan la misma recompensa`() {
        val a = generateDailyReward(date = "2026-06-15", installId = "user-42")
        val b = generateDailyReward(date = "2026-06-15", installId = "user-42")
        assertEquals(a, b)
    }

    @Test
    fun `fechas distintas generan recompensas potencialmente distintas`() {
        // No podemos garantizar que SIEMPRE sean distintas (podrian colisionar en un
        // mismo tier con mismos valores), pero en una muestra amplia deberia haber
        // variacion.
        val rewards = (1..30).map {
            generateDailyReward(date = "2026-06-$it", installId = "user")
        }
        val uniques = rewards.toSet()
        assertTrue(
            "Se esperaba mas de una recompensa distinta en 30 fechas, hubo ${uniques.size}",
            uniques.size > 1
        )
    }

    @Test
    fun `installIds distintos generan recompensas potencialmente distintas`() {
        val a = generateDailyReward(date = "2026-06-15", installId = "user-a")
        val b = generateDailyReward(date = "2026-06-15", installId = "user-b")
        // No garantiza desigualdad siempre (podrian colisionar), pero testea que el
        // installId efectivamente se usa en la semilla.
        val manyA = (1..10).map { generateDailyReward("2026-06-$it", "user-a") }
        val manyB = (1..10).map { generateDailyReward("2026-06-$it", "user-b") }
        assertFalse(
            "installId no afecta la semilla: ambas cohortes producen exactamente lo mismo",
            manyA == manyB
        )
        // Silencia warning de valores no usados
        assertEquals(a, a); assertEquals(b, b)
    }

    // =========================================================
    // Invariantes de tier
    // =========================================================

    @Test
    fun `isClaimed siempre es false al generar`() {
        val reward = generateDailyReward("2026-06-15", "user")
        assertFalse(reward.isClaimed)
    }

    @Test
    fun `el date del reward coincide con el input`() {
        val reward = generateDailyReward("2026-03-21", "user")
        assertEquals("2026-03-21", reward.date)
    }

    @Test
    fun `tier COMMON tiene 10-25 XP, 5-15 coins y 0 gems`() {
        val commons = sampleByTier(RewardTier.COMMON, limit = 50)
        assertTrue("No se generaron suficientes COMMON", commons.isNotEmpty())
        commons.forEach { r ->
            assertTrue("xp fuera de rango: ${r.xpAmount}", r.xpAmount in 10..25)
            assertTrue("coins fuera de rango: ${r.coinsAmount}", r.coinsAmount in 5..15)
            assertEquals("gems deben ser 0", 0, r.gemsAmount)
        }
    }

    @Test
    fun `tier UNCOMMON tiene 30-60 XP, 25-50 coins y 0 gems`() {
        val uncommons = sampleByTier(RewardTier.UNCOMMON, limit = 50)
        assertTrue("No se generaron suficientes UNCOMMON", uncommons.isNotEmpty())
        uncommons.forEach { r ->
            assertTrue("xp fuera de rango: ${r.xpAmount}", r.xpAmount in 30..60)
            assertTrue("coins fuera de rango: ${r.coinsAmount}", r.coinsAmount in 25..50)
            assertEquals("gems deben ser 0", 0, r.gemsAmount)
        }
    }

    @Test
    fun `tier RARE tiene 75-150 XP, 75-150 coins y 1 gem`() {
        val rares = sampleByTier(RewardTier.RARE, limit = 50)
        assertTrue("No se generaron suficientes RARE", rares.isNotEmpty())
        rares.forEach { r ->
            assertTrue("xp fuera de rango: ${r.xpAmount}", r.xpAmount in 75..150)
            assertTrue("coins fuera de rango: ${r.coinsAmount}", r.coinsAmount in 75..150)
            assertEquals("gems deben ser 1", 1, r.gemsAmount)
        }
    }

    // =========================================================
    // Distribucion aproximada (sanity check, no estricto)
    // =========================================================

    @Test
    fun `la distribucion sobre 1000 muestras respeta aproximadamente 70-25-5`() {
        val dist = (1..1000).map {
            generateDailyReward(date = "2026-01-$it", installId = "user-$it").tier
        }.groupingBy { it }.eachCount()

        val common = dist[RewardTier.COMMON] ?: 0
        val uncommon = dist[RewardTier.UNCOMMON] ?: 0
        val rare = dist[RewardTier.RARE] ?: 0

        // Tolerancia amplia porque son solo 1000 muestras; el objetivo es detectar
        // si alguien rompe el algoritmo (e.g. sesga todo a un solo tier).
        assertTrue("COMMON debe ser el tier mayoritario (~70%): $common", common in 600..800)
        assertTrue("UNCOMMON debe ser ~25%: $uncommon", uncommon in 180..320)
        assertTrue("RARE debe ser ~5%: $rare", rare in 20..100)
    }

    // =========================================================
    // Helpers
    // =========================================================

    private fun sampleByTier(tier: RewardTier, limit: Int): List<com.quiz.domain.reward.DailyReward> {
        val collected = mutableListOf<com.quiz.domain.reward.DailyReward>()
        var i = 1
        while (collected.size < limit && i < 10000) {
            val reward = generateDailyReward(date = "2026-01-$i", installId = "user-$i")
            if (reward.tier == tier) collected.add(reward)
            i++
        }
        return collected
    }
}
