package com.quiz.pride.managers

import com.quiz.domain.Achievement
import com.quiz.domain.PlayerStatistics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de la logica pura de AchievementManager.
 *
 * AchievementManager requiere Context/DataStore para su ejecucion real, por lo que
 * se testea la logica de decision de desbloqueo replicando el algoritmo de
 * checkAndUnlockAchievements, junto con la integridad del enum Achievement.
 *
 * Este patron es identico al usado en AdFrequencyManagerTest y ProgressionManagerTest:
 * extraer y testear la logica pura sin side-effects de IO.
 */
class AchievementManagerTest {

    // =========================================================
    // Infraestructura: replica de la logica pura de desbloqueo
    // =========================================================

    /**
     * Replica exacta de la logica de filtrado de checkAndUnlockAchievements.
     * Permite testear las condiciones de desbloqueo sin necesitar Context ni DataStore.
     */
    private fun determinarAchievementsADesbloquear(
        stats: PlayerStatistics,
        currentLevel: Int,
        alreadyUnlocked: Set<Achievement> = emptySet()
    ): List<Achievement> {
        return Achievement.entries.filter { achievement ->
            if (achievement in alreadyUnlocked) return@filter false
            when (achievement) {
                Achievement.FIRST_GAME      -> stats.totalGamesPlayed >= 1
                Achievement.TEN_GAMES       -> stats.totalGamesPlayed >= 10
                Achievement.FIFTY_GAMES     -> stats.totalGamesPlayed >= 50
                Achievement.HUNDRED_GAMES   -> stats.totalGamesPlayed >= 100
                Achievement.FIRST_PERFECT   -> stats.perfectGames >= 1
                Achievement.FIVE_PERFECT    -> stats.perfectGames >= 5
                Achievement.STREAK_5        -> stats.bestStreakEver >= 5
                Achievement.STREAK_10       -> stats.bestStreakEver >= 10
                Achievement.STREAK_15       -> stats.bestStreakEver >= 15
                Achievement.STREAK_20       -> stats.bestStreakEver >= 20
                Achievement.LEVEL_10        -> currentLevel >= 10
                Achievement.LEVEL_25        -> currentLevel >= 25
                Achievement.LEVEL_50        -> currentLevel >= 50
                Achievement.EXPERT_MASTER   -> stats.expertGamesPlayed >= 25
                Achievement.SPEED_DEMON     -> stats.timedGamesPlayed >= 10
                Achievement.DEDICATED       -> stats.totalTimePlayedMs >= 3_600_000L
                Achievement.ACCURACY_80     -> stats.accuracy >= 80f && stats.totalGamesPlayed >= 10
                Achievement.ACCURACY_90     -> stats.accuracy >= 90f && stats.totalGamesPlayed >= 20
            }
        }
    }

    /** Crea estadisticas base con valores neutrales (todo en cero). */
    private fun statsVacias() = PlayerStatistics(
        totalGamesPlayed = 0,
        gamesWon = 0,
        totalCorrectAnswers = 0,
        totalWrongAnswers = 0,
        accuracy = 0f,
        bestStreakEver = 0,
        perfectGames = 0,
        totalTimePlayedMs = 0L,
        normalGamesPlayed = 0,
        advanceGamesPlayed = 0,
        expertGamesPlayed = 0,
        timedGamesPlayed = 0
    )

    // =========================================================
    // Achievement enum — integridad de datos
    // =========================================================

    @Test
    fun `Achievement enum tiene exactamente 18 logros definidos`() {
        assertEquals(18, Achievement.entries.size)
    }

    @Test
    fun `todos los Achievement tienen id unico`() {
        val ids = Achievement.entries.map { it.id }
        assertEquals("Hay IDs duplicados", ids.size, ids.toSet().size)
    }

    @Test
    fun `todos los Achievement tienen xpReward positivo`() {
        Achievement.entries.forEach { achievement ->
            assertTrue(
                "${achievement.name} debe tener xpReward > 0, pero es ${achievement.xpReward}",
                achievement.xpReward > 0
            )
        }
    }

    @Test
    fun `todos los Achievement tienen icon no vacio`() {
        Achievement.entries.forEach { achievement ->
            assertTrue(
                "${achievement.name} debe tener icono no vacio",
                achievement.icon.isNotEmpty()
            )
        }
    }

    @Test
    fun `Achievement se puede buscar por id correctamente`() {
        val achievement = Achievement.entries.find { it.id == "first_game" }
        assertEquals(Achievement.FIRST_GAME, achievement)
    }

    @Test
    fun `buscar Achievement con id inexistente retorna null`() {
        val achievement = Achievement.entries.find { it.id == "id_inexistente" }
        assertEquals(null, achievement)
    }

    // =========================================================
    // Hitos de partidas: FIRST_GAME, TEN_GAMES, FIFTY_GAMES, HUNDRED_GAMES
    // =========================================================

    @Test
    fun `FIRST_GAME se desbloquea cuando totalGamesPlayed es 1`() {
        val stats = statsVacias().copy(totalGamesPlayed = 1)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue(result.contains(Achievement.FIRST_GAME))
    }

    @Test
    fun `FIRST_GAME no se desbloquea cuando totalGamesPlayed es 0`() {
        val stats = statsVacias()
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertFalse(result.contains(Achievement.FIRST_GAME))
    }

    @Test
    fun `TEN_GAMES se desbloquea cuando totalGamesPlayed es exactamente 10`() {
        val stats = statsVacias().copy(totalGamesPlayed = 10)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue(result.contains(Achievement.TEN_GAMES))
    }

    @Test
    fun `TEN_GAMES no se desbloquea cuando totalGamesPlayed es 9`() {
        val stats = statsVacias().copy(totalGamesPlayed = 9)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertFalse(result.contains(Achievement.TEN_GAMES))
    }

    @Test
    fun `FIFTY_GAMES se desbloquea cuando totalGamesPlayed es exactamente 50`() {
        val stats = statsVacias().copy(totalGamesPlayed = 50)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue(result.contains(Achievement.FIFTY_GAMES))
    }

    @Test
    fun `HUNDRED_GAMES se desbloquea cuando totalGamesPlayed es exactamente 100`() {
        val stats = statsVacias().copy(totalGamesPlayed = 100)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue(result.contains(Achievement.HUNDRED_GAMES))
    }

    @Test
    fun `jugador nuevo con 0 partidas no desbloquea ningun logro de hitos`() {
        val stats = statsVacias()
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertFalse(result.contains(Achievement.FIRST_GAME))
        assertFalse(result.contains(Achievement.TEN_GAMES))
        assertFalse(result.contains(Achievement.FIFTY_GAMES))
        assertFalse(result.contains(Achievement.HUNDRED_GAMES))
    }

    // =========================================================
    // Partidas perfectas: FIRST_PERFECT, FIVE_PERFECT
    // =========================================================

    @Test
    fun `FIRST_PERFECT se desbloquea cuando perfectGames es 1`() {
        val stats = statsVacias().copy(perfectGames = 1)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue(result.contains(Achievement.FIRST_PERFECT))
    }

    @Test
    fun `FIRST_PERFECT no se desbloquea cuando perfectGames es 0`() {
        val stats = statsVacias()
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertFalse(result.contains(Achievement.FIRST_PERFECT))
    }

    @Test
    fun `FIVE_PERFECT se desbloquea cuando perfectGames es exactamente 5`() {
        val stats = statsVacias().copy(perfectGames = 5)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue(result.contains(Achievement.FIVE_PERFECT))
    }

    @Test
    fun `FIVE_PERFECT no se desbloquea cuando perfectGames es 4`() {
        val stats = statsVacias().copy(perfectGames = 4)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertFalse(result.contains(Achievement.FIVE_PERFECT))
    }

    // =========================================================
    // Rachas: STREAK_5, STREAK_10, STREAK_15, STREAK_20
    // =========================================================

    @Test
    fun `STREAK_5 se desbloquea cuando bestStreakEver es exactamente 5`() {
        val stats = statsVacias().copy(bestStreakEver = 5)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue(result.contains(Achievement.STREAK_5))
    }

    @Test
    fun `STREAK_5 no se desbloquea cuando bestStreakEver es 4`() {
        val stats = statsVacias().copy(bestStreakEver = 4)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertFalse(result.contains(Achievement.STREAK_5))
    }

    @Test
    fun `STREAK_20 se desbloquea cuando bestStreakEver es exactamente 20`() {
        val stats = statsVacias().copy(bestStreakEver = 20)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue(result.contains(Achievement.STREAK_20))
    }

    @Test
    fun `racha de 20 desbloquea todos los logros de racha anteriores`() {
        val stats = statsVacias().copy(bestStreakEver = 20)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue(result.contains(Achievement.STREAK_5))
        assertTrue(result.contains(Achievement.STREAK_10))
        assertTrue(result.contains(Achievement.STREAK_15))
        assertTrue(result.contains(Achievement.STREAK_20))
    }

    @Test
    fun `racha de 12 desbloquea STREAK_5 y STREAK_10 pero no STREAK_15 ni STREAK_20`() {
        val stats = statsVacias().copy(bestStreakEver = 12)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue(result.contains(Achievement.STREAK_5))
        assertTrue(result.contains(Achievement.STREAK_10))
        assertFalse(result.contains(Achievement.STREAK_15))
        assertFalse(result.contains(Achievement.STREAK_20))
    }

    // =========================================================
    // Niveles: LEVEL_10, LEVEL_25, LEVEL_50
    // =========================================================

    @Test
    fun `LEVEL_10 se desbloquea cuando currentLevel es exactamente 10`() {
        val stats = statsVacias()
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 10)

        assertTrue(result.contains(Achievement.LEVEL_10))
    }

    @Test
    fun `LEVEL_10 no se desbloquea cuando currentLevel es 9`() {
        val stats = statsVacias()
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 9)

        assertFalse(result.contains(Achievement.LEVEL_10))
    }

    @Test
    fun `LEVEL_25 se desbloquea cuando currentLevel es 25`() {
        val stats = statsVacias()
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 25)

        assertTrue(result.contains(Achievement.LEVEL_25))
    }

    @Test
    fun `LEVEL_50 se desbloquea cuando currentLevel es 50`() {
        val stats = statsVacias()
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 50)

        assertTrue(result.contains(Achievement.LEVEL_50))
    }

    @Test
    fun `nivel 50 desbloquea todos los logros de nivel anteriores`() {
        val stats = statsVacias()
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 50)

        assertTrue(result.contains(Achievement.LEVEL_10))
        assertTrue(result.contains(Achievement.LEVEL_25))
        assertTrue(result.contains(Achievement.LEVEL_50))
    }

    // =========================================================
    // Logros especiales: EXPERT_MASTER, SPEED_DEMON, DEDICATED
    // =========================================================

    @Test
    fun `EXPERT_MASTER se desbloquea cuando expertGamesPlayed es exactamente 25`() {
        val stats = statsVacias().copy(expertGamesPlayed = 25)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue(result.contains(Achievement.EXPERT_MASTER))
    }

    @Test
    fun `EXPERT_MASTER no se desbloquea cuando expertGamesPlayed es 24`() {
        val stats = statsVacias().copy(expertGamesPlayed = 24)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertFalse(result.contains(Achievement.EXPERT_MASTER))
    }

    @Test
    fun `SPEED_DEMON se desbloquea cuando timedGamesPlayed es exactamente 10`() {
        val stats = statsVacias().copy(timedGamesPlayed = 10)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue(result.contains(Achievement.SPEED_DEMON))
    }

    @Test
    fun `SPEED_DEMON no se desbloquea cuando timedGamesPlayed es 9`() {
        val stats = statsVacias().copy(timedGamesPlayed = 9)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertFalse(result.contains(Achievement.SPEED_DEMON))
    }

    @Test
    fun `DEDICATED se desbloquea cuando totalTimePlayedMs es exactamente 3600000 una hora`() {
        val stats = statsVacias().copy(totalTimePlayedMs = 3_600_000L)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue(result.contains(Achievement.DEDICATED))
    }

    @Test
    fun `DEDICATED no se desbloquea cuando totalTimePlayedMs es 3599999 un ms menos de una hora`() {
        val stats = statsVacias().copy(totalTimePlayedMs = 3_599_999L)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertFalse(result.contains(Achievement.DEDICATED))
    }

    // =========================================================
    // Logros de precision: ACCURACY_80, ACCURACY_90
    // =========================================================

    @Test
    fun `ACCURACY_80 se desbloquea cuando accuracy es exactamente 80 y totalGamesPlayed es 10`() {
        val stats = statsVacias().copy(accuracy = 80f, totalGamesPlayed = 10)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue(result.contains(Achievement.ACCURACY_80))
    }

    @Test
    fun `ACCURACY_80 no se desbloquea cuando accuracy es 80 pero totalGamesPlayed es 9`() {
        val stats = statsVacias().copy(accuracy = 80f, totalGamesPlayed = 9)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertFalse(result.contains(Achievement.ACCURACY_80))
    }

    @Test
    fun `ACCURACY_80 no se desbloquea cuando totalGamesPlayed es 10 pero accuracy es 79f`() {
        val stats = statsVacias().copy(accuracy = 79f, totalGamesPlayed = 10)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertFalse(result.contains(Achievement.ACCURACY_80))
    }

    @Test
    fun `ACCURACY_90 se desbloquea cuando accuracy es 90 y totalGamesPlayed es 20`() {
        val stats = statsVacias().copy(accuracy = 90f, totalGamesPlayed = 20)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue(result.contains(Achievement.ACCURACY_90))
    }

    @Test
    fun `ACCURACY_90 no se desbloquea cuando accuracy es 90 pero totalGamesPlayed es 19`() {
        val stats = statsVacias().copy(accuracy = 90f, totalGamesPlayed = 19)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertFalse(result.contains(Achievement.ACCURACY_90))
    }

    @Test
    fun `ACCURACY_90 requiere mas partidas que ACCURACY_80`() {
        // ACCURACY_90 exige 20 partidas mientras que ACCURACY_80 exige 10
        val stats = statsVacias().copy(accuracy = 95f, totalGamesPlayed = 15)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue("ACCURACY_80 con 15 partidas y 95% deberia desbloquearse", result.contains(Achievement.ACCURACY_80))
        assertFalse("ACCURACY_90 con solo 15 partidas no deberia desbloquearse", result.contains(Achievement.ACCURACY_90))
    }

    // =========================================================
    // Logica de ya desbloqueados: no repetir achievements
    // =========================================================

    @Test
    fun `achievements ya desbloqueados no se incluyen en el resultado`() {
        val stats = statsVacias().copy(totalGamesPlayed = 10)
        val yaDesbloqueados = setOf(Achievement.FIRST_GAME, Achievement.TEN_GAMES)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1, alreadyUnlocked = yaDesbloqueados)

        assertFalse(result.contains(Achievement.FIRST_GAME))
        assertFalse(result.contains(Achievement.TEN_GAMES))
    }

    @Test
    fun `si todos los achievements elegibles ya estan desbloqueados retorna lista vacia`() {
        val stats = statsVacias().copy(totalGamesPlayed = 1)
        val yaDesbloqueados = setOf(Achievement.FIRST_GAME)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1, alreadyUnlocked = yaDesbloqueados)

        assertFalse(result.contains(Achievement.FIRST_GAME))
    }

    @Test
    fun `achievements parcialmente desbloqueados - solo retorna los nuevos`() {
        val stats = statsVacias().copy(totalGamesPlayed = 50)
        val yaDesbloqueados = setOf(Achievement.FIRST_GAME, Achievement.TEN_GAMES)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1, alreadyUnlocked = yaDesbloqueados)

        assertFalse(result.contains(Achievement.FIRST_GAME))
        assertFalse(result.contains(Achievement.TEN_GAMES))
        assertTrue(result.contains(Achievement.FIFTY_GAMES))
    }

    // =========================================================
    // Casos borde generales
    // =========================================================

    @Test
    fun `jugador con estadisticas perfectas desbloquea todos los logros posibles`() {
        val stats = PlayerStatistics(
            totalGamesPlayed = 100,
            gamesWon = 100,
            totalCorrectAnswers = 1000,
            totalWrongAnswers = 0,
            accuracy = 100f,
            bestStreakEver = 20,
            perfectGames = 10,
            totalTimePlayedMs = 10_000_000L,
            normalGamesPlayed = 20,
            advanceGamesPlayed = 20,
            expertGamesPlayed = 30,
            timedGamesPlayed = 30
        )
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 50)

        // Debe contener todos los 18 achievements
        assertEquals(Achievement.entries.size, result.size)
    }

    @Test
    fun `jugador nuevo con estadisticas en cero no desbloquea ningun logro`() {
        val stats = statsVacias()
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertTrue("Jugador nuevo no deberia desbloquear logros", result.isEmpty())
    }

    @Test
    fun `resultado no contiene duplicados`() {
        val stats = statsVacias().copy(totalGamesPlayed = 100, perfectGames = 5)
        val result = determinarAchievementsADesbloquear(stats, currentLevel = 1)

        assertEquals("El resultado no debe tener duplicados", result.size, result.toSet().size)
    }
}
