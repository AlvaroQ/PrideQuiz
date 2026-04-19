package com.quiz.pride.managers

import com.quiz.domain.challenge.ChallengeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de integridad de ChallengeAppConfig.
 *
 * ChallengeAppConfig es un object Kotlin puro sin dependencias de Android.
 * Se verifica que los templates esten correctamente configurados: targets positivos,
 * descripciones no vacias, sin tipos duplicados por dificultad y parametros validos.
 */
class ChallengeAppConfigTest {

    // =========================================================
    // Templates EASY
    // =========================================================

    @Test
    fun `easyTemplates no esta vacio`() {
        assertTrue(ChallengeAppConfig.easyTemplates.isNotEmpty())
    }

    @Test
    fun `easyTemplates tienen targets base positivos`() {
        ChallengeAppConfig.easyTemplates.forEach { template ->
            assertTrue(
                "Template EASY '${template.type}' tiene baseTarget=${template.baseTarget} (debe ser > 0)",
                template.baseTarget > 0
            )
        }
    }

    @Test
    fun `easyTemplates tienen descriptionKey no vacias`() {
        ChallengeAppConfig.easyTemplates.forEach { template ->
            assertTrue(
                "Template EASY '${template.type}' tiene descriptionKey vacia",
                template.descriptionKey.isNotBlank()
            )
        }
    }

    @Test
    fun `easyTemplates no tienen tipos duplicados`() {
        val tipos = ChallengeAppConfig.easyTemplates.map { it.type }
        val tiposUnicos = tipos.toSet()

        assertEquals(
            "EASY tiene tipos duplicados: ${tipos.groupBy { it }.filter { it.value.size > 1 }.keys}",
            tiposUnicos.size,
            tipos.size
        )
    }

    @Test
    fun `easyTemplates incluye GAMES_PLAYED`() {
        val tiposEasy = ChallengeAppConfig.easyTemplates.map { it.type }
        assertTrue(ChallengeType.GAMES_PLAYED in tiposEasy)
    }

    @Test
    fun `easyTemplates tiene objetivo de 1 partida para GAMES_PLAYED`() {
        val template = ChallengeAppConfig.easyTemplates.find { it.type == ChallengeType.GAMES_PLAYED }
        assertFalse(template == null)
        assertEquals(1, template!!.baseTarget)
    }

    // =========================================================
    // Templates MEDIUM
    // =========================================================

    @Test
    fun `mediumTemplates no esta vacio`() {
        assertTrue(ChallengeAppConfig.mediumTemplates.isNotEmpty())
    }

    @Test
    fun `mediumTemplates tienen targets base positivos`() {
        ChallengeAppConfig.mediumTemplates.forEach { template ->
            assertTrue(
                "Template MEDIUM '${template.type}' tiene baseTarget=${template.baseTarget} (debe ser > 0)",
                template.baseTarget > 0
            )
        }
    }

    @Test
    fun `mediumTemplates tienen descriptionKey no vacias`() {
        ChallengeAppConfig.mediumTemplates.forEach { template ->
            assertTrue(
                "Template MEDIUM '${template.type}' tiene descriptionKey vacia",
                template.descriptionKey.isNotBlank()
            )
        }
    }

    @Test
    fun `mediumTemplates no tienen tipos duplicados`() {
        val tipos = ChallengeAppConfig.mediumTemplates.map { it.type }
        val tiposUnicos = tipos.toSet()

        assertEquals(
            "MEDIUM tiene tipos duplicados: ${tipos.groupBy { it }.filter { it.value.size > 1 }.keys}",
            tiposUnicos.size,
            tipos.size
        )
    }

    @Test
    fun `mediumTemplates incluye PLAY_MODE`() {
        val tiposMedium = ChallengeAppConfig.mediumTemplates.map { it.type }
        assertTrue(ChallengeType.PLAY_MODE in tiposMedium)
    }

    @Test
    fun `mediumTemplates PLAY_MODE tiene extraParam no vacio`() {
        val playModeTemplate = ChallengeAppConfig.mediumTemplates.find {
            it.type == ChallengeType.PLAY_MODE
        }
        assertFalse("PLAY_MODE no existe en mediumTemplates", playModeTemplate == null)
        assertTrue(
            "PLAY_MODE tiene extraParam vacio: '${playModeTemplate!!.extraParam}'",
            playModeTemplate.extraParam.isNotBlank()
        )
    }

    @Test
    fun `mediumTemplates PLAY_MODE extraParam es un modo de juego valido`() {
        val playModeTemplate = ChallengeAppConfig.mediumTemplates.find {
            it.type == ChallengeType.PLAY_MODE
        }!!

        assertTrue(
            "extraParam '${playModeTemplate.extraParam}' no esta en availableGameModes",
            playModeTemplate.extraParam in ChallengeAppConfig.availableGameModes
        )
    }

    @Test
    fun `mediumTemplates tiene targets mayores que easy para el mismo tipo`() {
        // GAMES_PLAYED: MEDIUM debe tener un target mayor que EASY
        val easyGames = ChallengeAppConfig.easyTemplates.find { it.type == ChallengeType.GAMES_PLAYED }
        val mediumGames = ChallengeAppConfig.mediumTemplates.find { it.type == ChallengeType.GAMES_PLAYED }

        if (easyGames != null && mediumGames != null) {
            assertTrue(
                "MEDIUM GAMES_PLAYED (${mediumGames.baseTarget}) debe ser >= EASY (${easyGames.baseTarget})",
                mediumGames.baseTarget >= easyGames.baseTarget
            )
        }
    }

    // =========================================================
    // Templates HARD
    // =========================================================

    @Test
    fun `hardTemplates no esta vacio`() {
        assertTrue(ChallengeAppConfig.hardTemplates.isNotEmpty())
    }

    @Test
    fun `hardTemplates tienen targets base positivos`() {
        ChallengeAppConfig.hardTemplates.forEach { template ->
            assertTrue(
                "Template HARD '${template.type}' tiene baseTarget=${template.baseTarget} (debe ser > 0)",
                template.baseTarget > 0
            )
        }
    }

    @Test
    fun `hardTemplates tienen descriptionKey no vacias`() {
        ChallengeAppConfig.hardTemplates.forEach { template ->
            assertTrue(
                "Template HARD '${template.type}' tiene descriptionKey vacia",
                template.descriptionKey.isNotBlank()
            )
        }
    }

    @Test
    fun `hardTemplates no tienen tipos duplicados`() {
        val tipos = ChallengeAppConfig.hardTemplates.map { it.type }
        val tiposUnicos = tipos.toSet()

        assertEquals(
            "HARD tiene tipos duplicados: ${tipos.groupBy { it }.filter { it.value.size > 1 }.keys}",
            tiposUnicos.size,
            tipos.size
        )
    }

    @Test
    fun `hardTemplates incluye PERFECT_GAME`() {
        val tiposHard = ChallengeAppConfig.hardTemplates.map { it.type }
        assertTrue(ChallengeType.PERFECT_GAME in tiposHard)
    }

    @Test
    fun `hardTemplates GAMES_PLAYED tiene target mayor que MEDIUM`() {
        val mediumGames = ChallengeAppConfig.mediumTemplates.find { it.type == ChallengeType.GAMES_PLAYED }
        val hardGames = ChallengeAppConfig.hardTemplates.find { it.type == ChallengeType.GAMES_PLAYED }

        if (mediumGames != null && hardGames != null) {
            assertTrue(
                "HARD GAMES_PLAYED (${hardGames.baseTarget}) debe ser >= MEDIUM (${mediumGames.baseTarget})",
                hardGames.baseTarget >= mediumGames.baseTarget
            )
        }
    }

    // =========================================================
    // Templates WEEKLY
    // =========================================================

    @Test
    fun `weeklyTemplates no esta vacio`() {
        assertTrue(ChallengeAppConfig.weeklyTemplates.isNotEmpty())
    }

    @Test
    fun `weeklyTemplates tienen targets base positivos`() {
        ChallengeAppConfig.weeklyTemplates.forEach { template ->
            assertTrue(
                "Template WEEKLY '${template.type}' tiene baseTarget=${template.baseTarget} (debe ser > 0)",
                template.baseTarget > 0
            )
        }
    }

    @Test
    fun `weeklyTemplates tienen descriptionKey no vacias`() {
        ChallengeAppConfig.weeklyTemplates.forEach { template ->
            assertTrue(
                "Template WEEKLY '${template.type}' tiene descriptionKey vacia",
                template.descriptionKey.isNotBlank()
            )
        }
    }

    @Test
    fun `weeklyTemplates no tienen tipos duplicados`() {
        val tipos = ChallengeAppConfig.weeklyTemplates.map { it.type }
        val tiposUnicos = tipos.toSet()

        assertEquals(
            "WEEKLY tiene tipos duplicados: ${tipos.groupBy { it }.filter { it.value.size > 1 }.keys}",
            tiposUnicos.size,
            tipos.size
        )
    }

    @Test
    fun `weeklyTemplates tienen targets significativamente mayores que hard`() {
        // El objetivo semanal de GAMES_PLAYED debe ser mucho mayor que el diario hard
        val hardGames = ChallengeAppConfig.hardTemplates.find { it.type == ChallengeType.GAMES_PLAYED }
        val weeklyGames = ChallengeAppConfig.weeklyTemplates.find { it.type == ChallengeType.GAMES_PLAYED }

        if (hardGames != null && weeklyGames != null) {
            assertTrue(
                "WEEKLY GAMES_PLAYED (${weeklyGames.baseTarget}) debe ser mayor que HARD (${hardGames.baseTarget})",
                weeklyGames.baseTarget > hardGames.baseTarget
            )
        }
    }

    // =========================================================
    // availableGameModes
    // =========================================================

    @Test
    fun `availableGameModes no esta vacio`() {
        assertTrue(ChallengeAppConfig.availableGameModes.isNotEmpty())
    }

    @Test
    fun `availableGameModes contiene al menos NORMAL y TIMED`() {
        assertTrue("NORMAL no esta en availableGameModes", "NORMAL" in ChallengeAppConfig.availableGameModes)
        assertTrue("TIMED no esta en availableGameModes", "TIMED" in ChallengeAppConfig.availableGameModes)
    }

    @Test
    fun `availableGameModes no tiene modos duplicados`() {
        val modos = ChallengeAppConfig.availableGameModes
        val modosUnicos = modos.toSet()

        assertEquals(modosUnicos.size, modos.size)
    }

    @Test
    fun `availableGameModes todos los modos tienen nombre no vacio`() {
        ChallengeAppConfig.availableGameModes.forEach { modo ->
            assertTrue("Modo vacio encontrado", modo.isNotBlank())
        }
    }

    // =========================================================
    // maxQuestionsPerGame
    // =========================================================

    @Test
    fun `maxQuestionsPerGame es positivo`() {
        assertTrue(ChallengeAppConfig.maxQuestionsPerGame > 0)
    }

    @Test
    fun `maxQuestionsPerGame es 163`() {
        assertEquals(163, ChallengeAppConfig.maxQuestionsPerGame)
    }
}
