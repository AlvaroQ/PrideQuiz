package com.quiz.pride.managers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de la logica pura de AdFrequencyManager.
 *
 * AdFrequencyManager requiere Context para DataStore, por lo que se testea
 * la logica de decision pura via AdFrequencyManager.evaluateShouldShowInterstitial,
 * junto con la integridad de las constantes de configuracion.
 */
class AdFrequencyManagerTest {

    // Constantes de configuracion accesibles desde el companion object
    private val gamesBeforeFirstInterstitial = AdFrequencyManager.GAMES_BEFORE_FIRST_INTERSTITIAL
    private val gamesBetweenInterstitials = AdFrequencyManager.GAMES_BETWEEN_INTERSTITIALS
    private val maxInterstitialsPerSession = AdFrequencyManager.MAX_INTERSTITIALS_PER_SESSION
    private val cooldownMinutes = AdFrequencyManager.COOLDOWN_MINUTES
    private val sessionTimeoutMinutes = AdFrequencyManager.SESSION_TIMEOUT_MINUTES

    /**
     * Delega directamente a la funcion interna del companion object del manager.
     * De esta forma, si la logica de produccion cambia, los tests lo detectan.
     */
    private fun shouldShowInterstitial(
        gamesSinceLast: Int,
        interstitialsThisSession: Int,
        lastInterstitialTimeMs: Long,
        totalGamesPlayed: Int,
        currentTimeMs: Long = System.currentTimeMillis()
    ): Boolean = AdFrequencyManager.evaluateShouldShowInterstitial(
        gamesSinceLast = gamesSinceLast,
        interstitialsThisSession = interstitialsThisSession,
        lastInterstitialTimeMs = lastInterstitialTimeMs,
        totalGamesPlayed = totalGamesPlayed,
        currentTimeMs = currentTimeMs
    )

    // =========================================================
    // Constantes de configuracion — integridad
    // =========================================================

    @Test
    fun `GAMES_BEFORE_FIRST_INTERSTITIAL tiene valor positivo`() {
        assertTrue(AdFrequencyManager.GAMES_BEFORE_FIRST_INTERSTITIAL > 0)
    }

    @Test
    fun `GAMES_BETWEEN_INTERSTITIALS tiene valor positivo`() {
        assertTrue(AdFrequencyManager.GAMES_BETWEEN_INTERSTITIALS > 0)
    }

    @Test
    fun `MAX_INTERSTITIALS_PER_SESSION tiene valor positivo`() {
        assertTrue(AdFrequencyManager.MAX_INTERSTITIALS_PER_SESSION > 0)
    }

    @Test
    fun `COOLDOWN_MINUTES tiene valor positivo`() {
        assertTrue(AdFrequencyManager.COOLDOWN_MINUTES > 0)
    }

    @Test
    fun `SESSION_TIMEOUT_MINUTES es mayor que COOLDOWN_MINUTES`() {
        assertTrue(AdFrequencyManager.SESSION_TIMEOUT_MINUTES > AdFrequencyManager.COOLDOWN_MINUTES)
    }

    @Test
    fun `constantes tienen los valores documentados en el codigo`() {
        assertEquals(2, AdFrequencyManager.GAMES_BEFORE_FIRST_INTERSTITIAL)
        assertEquals(3, AdFrequencyManager.GAMES_BETWEEN_INTERSTITIALS)
        assertEquals(4, AdFrequencyManager.MAX_INTERSTITIALS_PER_SESSION)
        assertEquals(2, AdFrequencyManager.COOLDOWN_MINUTES)
        assertEquals(30, AdFrequencyManager.SESSION_TIMEOUT_MINUTES)
    }

    // =========================================================
    // shouldShowInterstitial — MAX_INTERSTITIALS_PER_SESSION
    // =========================================================

    @Test
    fun `shouldShowInterstitial retorna false cuando se alcanzo MAX_INTERSTITIALS_PER_SESSION`() {
        val result = shouldShowInterstitial(
            gamesSinceLast = 5,
            interstitialsThisSession = maxInterstitialsPerSession, // limite alcanzado
            lastInterstitialTimeMs = 0L,
            totalGamesPlayed = 10
        )

        assertFalse(result)
    }

    @Test
    fun `shouldShowInterstitial retorna false cuando se supero MAX_INTERSTITIALS_PER_SESSION`() {
        val result = shouldShowInterstitial(
            gamesSinceLast = 10,
            interstitialsThisSession = maxInterstitialsPerSession + 1,
            lastInterstitialTimeMs = 0L,
            totalGamesPlayed = 20
        )

        assertFalse(result)
    }

    @Test
    fun `shouldShowInterstitial no bloquea cuando interstitialsThisSession esta por debajo del maximo`() {
        // Con todas las demas condiciones cumplidas y por debajo del maximo
        val result = shouldShowInterstitial(
            gamesSinceLast = gamesBetweenInterstitials,
            interstitialsThisSession = maxInterstitialsPerSession - 1,
            lastInterstitialTimeMs = 0L,
            totalGamesPlayed = gamesBeforeFirstInterstitial
        )

        assertTrue(result)
    }

    // =========================================================
    // shouldShowInterstitial — COOLDOWN_MINUTES
    // =========================================================

    @Test
    fun `shouldShowInterstitial retorna false cuando no paso el cooldown`() {
        val now = System.currentTimeMillis()
        val cooldownMs = cooldownMinutes * 60 * 1000L
        val lastAdTime = now - (cooldownMs / 2) // Mitad del cooldown

        val result = shouldShowInterstitial(
            gamesSinceLast = gamesBetweenInterstitials,
            interstitialsThisSession = 0,
            lastInterstitialTimeMs = lastAdTime,
            totalGamesPlayed = gamesBeforeFirstInterstitial,
            currentTimeMs = now
        )

        assertFalse(result)
    }

    @Test
    fun `shouldShowInterstitial retorna true cuando ya paso el cooldown`() {
        val now = System.currentTimeMillis()
        val cooldownMs = cooldownMinutes * 60 * 1000L
        val lastAdTime = now - cooldownMs - 1000L // Cooldown + 1 segundo

        val result = shouldShowInterstitial(
            gamesSinceLast = gamesBetweenInterstitials,
            interstitialsThisSession = 0,
            lastInterstitialTimeMs = lastAdTime,
            totalGamesPlayed = gamesBeforeFirstInterstitial,
            currentTimeMs = now
        )

        assertTrue(result)
    }

    @Test
    fun `shouldShowInterstitial omite cooldown cuando lastInterstitialTime es 0 primera vez`() {
        // lastInterstitialTimeMs = 0 significa que nunca se mostro un anuncio
        val result = shouldShowInterstitial(
            gamesSinceLast = gamesBetweenInterstitials,
            interstitialsThisSession = 0,
            lastInterstitialTimeMs = 0L, // Sin anuncio previo
            totalGamesPlayed = gamesBeforeFirstInterstitial
        )

        assertTrue(result)
    }

    // =========================================================
    // shouldShowInterstitial — GAMES_BEFORE_FIRST_INTERSTITIAL
    // =========================================================

    @Test
    fun `shouldShowInterstitial retorna false cuando totalGamesPlayed es menor que GAMES_BEFORE_FIRST_INTERSTITIAL`() {
        val result = shouldShowInterstitial(
            gamesSinceLast = gamesBetweenInterstitials,
            interstitialsThisSession = 0,
            lastInterstitialTimeMs = 0L,
            totalGamesPlayed = gamesBeforeFirstInterstitial - 1
        )

        assertFalse(result)
    }

    @Test
    fun `shouldShowInterstitial retorna false en la primer partida del usuario`() {
        val result = shouldShowInterstitial(
            gamesSinceLast = 1,
            interstitialsThisSession = 0,
            lastInterstitialTimeMs = 0L,
            totalGamesPlayed = 1 // Primera partida
        )

        assertFalse(result)
    }

    @Test
    fun `shouldShowInterstitial permite mostrar anuncio cuando totalGamesPlayed alcanza el umbral`() {
        val result = shouldShowInterstitial(
            gamesSinceLast = gamesBetweenInterstitials,
            interstitialsThisSession = 0,
            lastInterstitialTimeMs = 0L,
            totalGamesPlayed = gamesBeforeFirstInterstitial // exactamente en el umbral
        )

        assertTrue(result)
    }

    // =========================================================
    // shouldShowInterstitial — GAMES_BETWEEN_INTERSTITIALS
    // =========================================================

    @Test
    fun `shouldShowInterstitial retorna false cuando gamesSinceLast esta por debajo del umbral`() {
        val result = shouldShowInterstitial(
            gamesSinceLast = gamesBetweenInterstitials - 1,
            interstitialsThisSession = 0,
            lastInterstitialTimeMs = 0L,
            totalGamesPlayed = gamesBeforeFirstInterstitial
        )

        assertFalse(result)
    }

    @Test
    fun `shouldShowInterstitial retorna true cuando gamesSinceLast alcanza exactamente GAMES_BETWEEN_INTERSTITIALS`() {
        val result = shouldShowInterstitial(
            gamesSinceLast = gamesBetweenInterstitials,
            interstitialsThisSession = 0,
            lastInterstitialTimeMs = 0L,
            totalGamesPlayed = gamesBeforeFirstInterstitial
        )

        assertTrue(result)
    }

    @Test
    fun `shouldShowInterstitial retorna true cuando gamesSinceLast supera GAMES_BETWEEN_INTERSTITIALS`() {
        val result = shouldShowInterstitial(
            gamesSinceLast = gamesBetweenInterstitials + 5,
            interstitialsThisSession = 0,
            lastInterstitialTimeMs = 0L,
            totalGamesPlayed = gamesBeforeFirstInterstitial
        )

        assertTrue(result)
    }

    // =========================================================
    // shouldShowInterstitial — Combinaciones de condiciones
    // =========================================================

    @Test
    fun `shouldShowInterstitial retorna false si alguna condicion falla aunque el resto sea true`() {
        // Todas las condiciones cumplidas excepto el maximo de sesion
        val resultMaxSession = shouldShowInterstitial(
            gamesSinceLast = gamesBetweenInterstitials,
            interstitialsThisSession = maxInterstitialsPerSession,
            lastInterstitialTimeMs = 0L,
            totalGamesPlayed = gamesBeforeFirstInterstitial
        )
        assertFalse("Debe fallar por MAX_INTERSTITIALS_PER_SESSION", resultMaxSession)

        // Todas las condiciones cumplidas excepto el cooldown
        val now = System.currentTimeMillis()
        val resultCooldown = shouldShowInterstitial(
            gamesSinceLast = gamesBetweenInterstitials,
            interstitialsThisSession = 0,
            lastInterstitialTimeMs = now - 10_000L, // 10 segundos, dentro del cooldown
            totalGamesPlayed = gamesBeforeFirstInterstitial,
            currentTimeMs = now
        )
        assertFalse("Debe fallar por COOLDOWN", resultCooldown)

        // Todas las condiciones cumplidas excepto el minimo de partidas
        val resultMinGames = shouldShowInterstitial(
            gamesSinceLast = gamesBetweenInterstitials,
            interstitialsThisSession = 0,
            lastInterstitialTimeMs = 0L,
            totalGamesPlayed = 0 // Cero partidas jugadas
        )
        assertFalse("Debe fallar por GAMES_BEFORE_FIRST_INTERSTITIAL", resultMinGames)
    }

    @Test
    fun `shouldShowInterstitial retorna true solo cuando todas las condiciones se cumplen`() {
        val now = System.currentTimeMillis()
        val cooldownMs = cooldownMinutes * 60 * 1000L

        val result = shouldShowInterstitial(
            gamesSinceLast = gamesBetweenInterstitials,
            interstitialsThisSession = maxInterstitialsPerSession - 1,
            lastInterstitialTimeMs = now - cooldownMs - 5000L, // Cooldown superado
            totalGamesPlayed = gamesBeforeFirstInterstitial + 1,
            currentTimeMs = now
        )

        assertTrue(result)
    }

}
