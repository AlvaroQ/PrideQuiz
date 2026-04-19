package com.quiz.pride.managers

import com.quiz.domain.reward.MysteryRewardType
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Tests de la logica del sorteo de `openBox`.
 *
 * Para el contador (onGameCompleted / consumeBox / isBoxReady) se requeriria
 * DataStore instrumentado. Aqui cubrimos la parte determinista del sorteo
 * inyectando un Random seedeado; el contador se valida indirectamente por el
 * ResultViewModelTest que verifica que onGameCompleted se llama una vez por
 * partida.
 */
class MysteryBoxManagerTest {

    private val anyContext: android.content.Context = mockk(relaxed = true)

    // =========================================================
    // openBox — distribucion por tipo
    // =========================================================

    @Test
    fun `openBox con seed 0 produce resultado determinista`() {
        val rng1 = Random(0)
        val rng2 = Random(0)
        val manager1 = MysteryBoxManager(context = anyContext, rng = rng1)
        val manager2 = MysteryBoxManager(context = anyContext, rng = rng2)

        val a = manager1.openBox()
        val b = manager2.openBox()
        assertEquals(a, b)
    }

    @Test
    fun `openBox con canGrantFreezeToken=false nunca devuelve FREEZE_TOKEN`() {
        // Con 200 muestras deberia haber caido al menos una vez en el rango
        // >= 85 (15% de probabilidad natural), que normalmente seria FREEZE_TOKEN
        // pero con canGrantFreezeToken=false debe reconvertirse a COINS_BONUS.
        val manager = MysteryBoxManager(context = anyContext, rng = Random(42))
        val rewards = (1..200).map { manager.openBox(canGrantFreezeToken = false) }
        assertTrue(rewards.none { it.type == MysteryRewardType.FREEZE_TOKEN })
    }

    @Test
    fun `COINS_BONUS tiene 30-100 coins, 0 XP y 0 freezeTokens`() {
        val manager = MysteryBoxManager(context = anyContext, rng = Random(1))
        val coinsRewards = (1..300)
            .map { manager.openBox() }
            .filter { it.type == MysteryRewardType.COINS_BONUS }

        assertTrue("No se generaron COINS_BONUS suficientes", coinsRewards.isNotEmpty())
        coinsRewards.forEach { r ->
            assertTrue("coins fuera de rango: ${r.coinsAmount}", r.coinsAmount in 30..100)
            assertEquals(0, r.xpAmount)
            assertEquals(0, r.freezeTokens)
        }
    }

    @Test
    fun `XP_BONUS tiene 50-200 XP, 0 coins y 0 freezeTokens`() {
        val manager = MysteryBoxManager(context = anyContext, rng = Random(7))
        val xpRewards = (1..300)
            .map { manager.openBox() }
            .filter { it.type == MysteryRewardType.XP_BONUS }

        assertTrue("No se generaron XP_BONUS suficientes", xpRewards.isNotEmpty())
        xpRewards.forEach { r ->
            assertTrue("xp fuera de rango: ${r.xpAmount}", r.xpAmount in 50..200)
            assertEquals(0, r.coinsAmount)
            assertEquals(0, r.freezeTokens)
        }
    }

    @Test
    fun `FREEZE_TOKEN tiene freezeTokens=1 y 0 XP_y_coins`() {
        val manager = MysteryBoxManager(context = anyContext, rng = Random(3))
        val freezeRewards = (1..300)
            .map { manager.openBox(canGrantFreezeToken = true) }
            .filter { it.type == MysteryRewardType.FREEZE_TOKEN }

        assertTrue("No se generaron FREEZE_TOKEN suficientes", freezeRewards.isNotEmpty())
        freezeRewards.forEach { r ->
            assertEquals(1, r.freezeTokens)
            assertEquals(0, r.xpAmount)
            assertEquals(0, r.coinsAmount)
        }
    }

    @Test
    fun `distribucion sobre 1000 muestras es aproximadamente 50-35-15`() {
        val manager = MysteryBoxManager(context = anyContext, rng = Random(123))
        val dist = (1..1000)
            .map { manager.openBox(canGrantFreezeToken = true).type }
            .groupingBy { it }.eachCount()

        val coins = dist[MysteryRewardType.COINS_BONUS] ?: 0
        val xp = dist[MysteryRewardType.XP_BONUS] ?: 0
        val freeze = dist[MysteryRewardType.FREEZE_TOKEN] ?: 0

        // Tolerancia: detecta si alguien rompe el algoritmo (sesgo a un solo tipo).
        assertTrue("COINS_BONUS ~50%: $coins", coins in 400..600)
        assertTrue("XP_BONUS ~35%: $xp", xp in 260..440)
        assertTrue("FREEZE_TOKEN ~15%: $freeze", freeze in 80..220)
    }

    @Test
    fun `dos mananagers con seeds distintos producen distribuciones distintas`() {
        val m1 = MysteryBoxManager(context = anyContext, rng = Random(1))
        val m2 = MysteryBoxManager(context = anyContext, rng = Random(2))

        val seq1 = (1..20).map { m1.openBox() }
        val seq2 = (1..20).map { m2.openBox() }
        assertNotEquals(seq1, seq2)
    }
}
