package com.quiz.pride.datasource

import com.quiz.data.datasource.GameResultProcessorDataSource
import com.quiz.domain.Achievement
import com.quiz.domain.GameResult
import com.quiz.domain.StreakCheckResult
import com.quiz.domain.XpGainResult
import com.quiz.domain.challenge.ChallengeCompletionResult
import com.quiz.domain.challenge.ChallengeEvent
import com.quiz.pride.managers.AchievementManager
import com.quiz.pride.managers.CurrencyManager
import com.quiz.pride.managers.DailyChallengeManager
import com.quiz.pride.managers.GameStatsManager
import com.quiz.pride.managers.ProgressionManager
import com.quiz.pride.managers.StreakManager
import com.quiz.pride.managers.XpSyncManager

/**
 * Implementacion concreta de GameResultProcessorDataSource.
 *
 * Delega en los managers de la capa app/ para ejecutar la logica de
 * estadisticas, logros, racha diaria, desafios y sincronizacion.
 * Esta clase vive en app/ porque los managers necesitan Context y DataStore.
 *
 * Flujo de procesamiento al finalizar una partida:
 * 1. Leer multiplicador de racha ANTES de calcular XP (para no leerlo dos veces)
 * 2. Registrar estadisticas y calcular XP base de la partida
 * 3. Aplicar XP adicional por multiplicador de racha (si > 1.0x)
 * 4. Procesar racha diaria y acreditar XP bonus del ciclo
 * 5. Procesar evento GameCompleted en el sistema de desafios diarios
 * 6. Acreditar XP ganado por desafios completados
 * 7. Verificar y desbloquear logros
 * 8. Acreditar moneda virtual (coins por partida + desafios + ciclo de racha; gems por hitos)
 *
 * Registrada en Koin como single<GameResultProcessorDataSource> { GameResultProcessorImpl(...) }
 */
class GameResultProcessorImpl(
    private val gameStatsManager: GameStatsManager,
    private val achievementManager: AchievementManager,
    private val xpSyncManager: XpSyncManager,
    private val streakManager: StreakManager,
    private val progressionManager: ProgressionManager,
    private val dailyChallengeManager: DailyChallengeManager,
    private val currencyManager: CurrencyManager
) : GameResultProcessorDataSource {

    /** Estado de racha del ultimo procesamiento, para retornarlo al use case. */
    @Volatile
    private var lastStreakResult: StreakCheckResult? = null

    /** XP de racha acreditado en el ultimo procesamiento. */
    @Volatile
    private var lastStreakXpBonus: Int = 0

    /** Resultado de desafios del ultimo procesamiento. */
    @Volatile
    private var lastChallengeResult: ChallengeCompletionResult? = null

    /** Coins ganados en el ultimo procesamiento. */
    @Volatile
    private var lastCoinsEarned: Int = 0

    /** Gems ganadas en el ultimo procesamiento. */
    @Volatile
    private var lastGemsEarned: Int = 0

    override suspend fun recordGameAndComputeXp(result: GameResult): XpGainResult {
        // 1. Leer multiplicador de racha actual ANTES de calcular XP de partida
        val streakMultiplier = streakManager.getStreakMultiplier()

        // 2. Registrar estadisticas y calcular XP base
        val xpGainResult = gameStatsManager.recordGameResult(result)

        // 3. Aplicar XP adicional por multiplicador de racha (solo si hay bonus)
        //    Formula: XP extra = XP_base * (multiplier - 1.0)
        //    Ejemplo: 100 XP con 1.2x -> 20 XP extra adicionales
        val multiplierXpBonus = if (streakMultiplier > 1.0f) {
            (xpGainResult.xpGained * (streakMultiplier - 1.0f)).toInt()
        } else {
            0
        }

        // 4. Procesar racha diaria
        val streakResult = streakManager.onGameCompleted()
        val cycleXpBonus = when (streakResult) {
            is StreakCheckResult.AlreadyPlayedToday -> 0
            is StreakCheckResult.StreakContinued -> streakResult.reward.xpBonus
            is StreakCheckResult.StreakSavedByFreeze -> streakResult.reward.xpBonus
            is StreakCheckResult.StreakBroken -> streakResult.reward.xpBonus
            is StreakCheckResult.NewStreak -> streakResult.reward.xpBonus
        }

        // Acreditar XP de racha (multiplicador + ciclo diario)
        val totalStreakXp = multiplierXpBonus + cycleXpBonus
        if (totalStreakXp > 0) {
            progressionManager.addXp(totalStreakXp.toLong())
        }

        // 5. Procesar evento GameCompleted en el sistema de desafios diarios
        val playerLevel = progressionManager.calculateLevel(xpGainResult.totalXp)
        val isPerfect = result.completedAllQuestions && result.correctAnswers == result.totalQuestions
        val challengeEvent = ChallengeEvent.GameCompleted(
            gameMode = result.gameMode.name,
            score = result.correctAnswers,
            bestStreak = result.bestStreak,
            isPerfect = isPerfect,
            completedAll = result.completedAllQuestions,
            correctAnswers = result.correctAnswers,
            totalQuestions = result.totalQuestions
        )
        val challengeResult = dailyChallengeManager.processEvent(challengeEvent, playerLevel)

        // 6. Acreditar XP ganado por desafios completados
        if (challengeResult.totalXpEarned > 0) {
            progressionManager.addXp(challengeResult.totalXpEarned.toLong())
        }

        // 8. Acreditar moneda virtual
        // Coins: base por partida + coins de desafios completados + coins del ciclo de racha
        var coinsEarned = 5 // base fija por completar cualquier partida
        if (challengeResult.totalCoinsEarned > 0) {
            coinsEarned += challengeResult.totalCoinsEarned
        }
        val streakCoins = when (streakResult) {
            is StreakCheckResult.StreakContinued -> streakCycleCoins(streakResult.reward.cycleDay)
            is StreakCheckResult.NewStreak -> streakCycleCoins(1)
            is StreakCheckResult.StreakSavedByFreeze -> streakCycleCoins(streakResult.reward.cycleDay)
            else -> 0
        }
        coinsEarned += streakCoins
        currencyManager.earnCoins(coinsEarned, "game_completion")

        // Gems: solo en hitos de racha significativos (30, 90, 365 dias consecutivos)
        var gemsEarned = 0
        if (streakResult is StreakCheckResult.StreakContinued && streakResult.reward.isMilestone) {
            gemsEarned = when (streakResult.newState.currentStreak) {
                30 -> 5
                90 -> 15
                365 -> 50
                else -> 0
            }
            if (gemsEarned > 0) {
                currencyManager.earnGems(gemsEarned, "streak_milestone")
            }
        }

        // Guardar en estado temporal para retornarlo al use case
        lastStreakResult = streakResult
        lastStreakXpBonus = totalStreakXp
        lastChallengeResult = challengeResult
        lastCoinsEarned = coinsEarned
        lastGemsEarned = gemsEarned

        return xpGainResult
    }

    /**
     * Coins otorgados segun el dia del ciclo de racha (ciclo de 7 dias).
     * Dia 7 es el mas recompensado para incentivar la constancia semanal.
     */
    private fun streakCycleCoins(cycleDay: Int): Int = when (cycleDay) {
        1 -> 5
        2 -> 5
        3 -> 10
        4 -> 10
        5 -> 15
        6 -> 15
        7 -> 25
        else -> 5
    }

    override suspend fun checkAndUnlockAchievements(): List<Achievement> {
        return achievementManager.checkAndUnlockAchievements()
    }

    override suspend fun triggerXpSync() {
        xpSyncManager.triggerSync()
    }

    /**
     * Retorna el resultado de racha del ultimo procesamiento.
     * El use case lo incluye en ProcessedGameResult para que la UI pueda reaccionar.
     */
    override fun getLastStreakResult(): StreakCheckResult? = lastStreakResult

    /** Retorna el XP total de racha acreditado en el ultimo procesamiento. */
    override fun getLastStreakXpBonus(): Int = lastStreakXpBonus

    /**
     * Retorna el resultado de desafios diarios del ultimo procesamiento.
     * Incluye desafios completados y XP/coins obtenidos.
     */
    override fun getLastChallengeResult(): ChallengeCompletionResult? = lastChallengeResult

    /** Retorna el total de coins acreditados en el ultimo procesamiento. */
    override fun getLastCoinsEarned(): Int = lastCoinsEarned

    /** Retorna el total de gems acreditadas en el ultimo procesamiento. */
    override fun getLastGemsEarned(): Int = lastGemsEarned
}
