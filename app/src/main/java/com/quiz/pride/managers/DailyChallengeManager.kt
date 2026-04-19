package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.quiz.domain.challenge.ChallengeDifficulty
import com.quiz.domain.challenge.ChallengeCompletionResult
import com.quiz.domain.challenge.ChallengeEvent
import com.quiz.domain.challenge.ChallengeReward
import com.quiz.domain.challenge.ChallengeStats
import com.quiz.domain.challenge.ChallengeType
import com.quiz.domain.challenge.DailyChallenge
import com.quiz.domain.challenge.DailyChallengeState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

private val Context.challengeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "daily_challenge_preferences"
)

/**
 * Manager principal del sistema de desafios diarios.
 *
 * Responsabilidades:
 * - Generar desafios diarios de forma determinista por fecha + installId
 * - Trackear progreso durante el gameplay procesando ChallengeEvents
 * - Detectar completaciones y calcular recompensas (XP + coins)
 * - Persistir estado en DataStore separado ("daily_challenge_preferences")
 *
 * Seed formula: date.hashCode() xor installId.hashCode()
 * Garantiza: mismo usuario, mismo dia -> mismos desafios. Diferentes usuarios -> desafios distintos.
 *
 * El Mutex protege las operaciones de lectura-modificacion-escritura del estado.
 * Las funciones privadas que operan dentro del lock NO adquieren el mutex nuevamente
 * para evitar deadlocks (Kotlin Mutex NO es reentrant).
 */
class DailyChallengeManager(private val context: Context) {

    private val dataStore get() = context.challengeDataStore
    private val mutex = Mutex()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    companion object {
        private val KEY_STATE_JSON = stringPreferencesKey("challenge_state_json")
        private val KEY_STATS_TOTAL_COMPLETED = intPreferencesKey("challenge_stats_total_completed")
        private val KEY_STATS_ALL_DAILY_DAYS = intPreferencesKey("challenge_stats_all_daily_days")
        private val KEY_STATS_CURRENT_ALL_STREAK = intPreferencesKey("challenge_stats_current_all_streak")
        private val KEY_STATS_BEST_ALL_STREAK = intPreferencesKey("challenge_stats_best_all_streak")
        private val KEY_INSTALL_ID = stringPreferencesKey("challenge_install_id")

        // Separadores que no colisionan con contenido de usuario
        // Usamos secuencias de control ASCII poco comunes
        private const val SEP_FIELD = "\u0001"     // SOH (Start of Heading)
        private const val SEP_CHALLENGE = "\u0002" // STX (Start of Text)
    }

    /**
     * Retorna el estado de desafios actual, generando nuevos si es un dia nuevo.
     * Hilo-seguro mediante Mutex.
     */
    suspend fun getDailyChallengeState(playerLevel: Int = 1): DailyChallengeState = mutex.withLock {
        loadOrGenerateState(playerLevel)
    }

    /**
     * Procesa un ChallengeEvent y retorna las completaciones ocurridas.
     * Acredita XP de desafios via ProgressionManager si corresponde.
     */
    suspend fun processEvent(event: ChallengeEvent, playerLevel: Int = 1): ChallengeCompletionResult = mutex.withLock {
        val state = loadOrGenerateState(playerLevel)
        val completedBefore = state.challenges.filter { it.isCompleted }.map { it.id }.toSet()
        val weeklyWasCompleted = state.weeklyChallenge?.isCompleted == true

        val updatedChallenges = state.challenges.map { challenge ->
            if (challenge.isCompleted) challenge
            else updateChallengeProgress(challenge, event)
        }

        val updatedWeekly = state.weeklyChallenge?.let {
            if (it.isCompleted) it else updateChallengeProgress(it, event)
        }

        val newlyCompleted = updatedChallenges.filter { it.isCompleted && it.id !in completedBefore }
        val weeklyJustCompleted = updatedWeekly?.isCompleted == true && !weeklyWasCompleted

        val allDailyDone = updatedChallenges
            .filter { it.difficulty != ChallengeDifficulty.WEEKLY }
            .all { it.isCompleted }
        val allDailyJustCompleted = allDailyDone && !state.allDailyCompleted

        val updatedState = state.copy(
            challenges = updatedChallenges,
            weeklyChallenge = updatedWeekly,
            allDailyCompleted = allDailyDone
        )
        writeState(updatedState)

        // Calcular recompensas
        var totalXp = newlyCompleted.sumOf { it.reward.xp }
        var totalCoins = newlyCompleted.sumOf { it.reward.coins }
        if (weeklyJustCompleted && updatedWeekly != null) {
            totalXp += updatedWeekly.reward.xp
            totalCoins += updatedWeekly.reward.coins
        }
        if (allDailyJustCompleted) {
            totalXp += ChallengeReward.ALL_DAILY_COMPLETE_BONUS.xp
            totalCoins += ChallengeReward.ALL_DAILY_COMPLETE_BONUS.coins
        }

        // Actualizar estadisticas si hubo nuevas completaciones
        val totalNewCompletions = newlyCompleted.size + if (weeklyJustCompleted) 1 else 0
        if (totalNewCompletions > 0 || allDailyJustCompleted) {
            updateStats(totalNewCompletions, allDailyJustCompleted)
        }

        val allCompleted = newlyCompleted.toMutableList()
        if (weeklyJustCompleted && updatedWeekly != null) allCompleted.add(updatedWeekly)

        ChallengeCompletionResult(
            completedChallenges = allCompleted,
            allDailyJustCompleted = allDailyJustCompleted,
            totalXpEarned = totalXp,
            totalCoinsEarned = totalCoins
        )
    }

    /** Retorna las estadisticas acumuladas de desafios completados. */
    suspend fun getChallengeStats(): ChallengeStats {
        val prefs = dataStore.data.first()
        return ChallengeStats(
            totalCompleted = prefs[KEY_STATS_TOTAL_COMPLETED] ?: 0,
            totalAllDailyCompleteDays = prefs[KEY_STATS_ALL_DAILY_DAYS] ?: 0,
            currentAllDailyStreak = prefs[KEY_STATS_CURRENT_ALL_STREAK] ?: 0,
            bestAllDailyStreak = prefs[KEY_STATS_BEST_ALL_STREAK] ?: 0
        )
    }

    // ─── Funciones privadas (operan DENTRO del lock, no adquieren mutex) ───────

    /**
     * Lee el estado guardado o genera uno nuevo si es dia nuevo o no existe.
     * PRECONDICION: llamar dentro de mutex.withLock {}
     */
    private suspend fun loadOrGenerateState(playerLevel: Int): DailyChallengeState {
        val today = todayDate()
        val saved = readState()
        if (saved != null && saved.date == today) return saved

        val installId = getOrCreateInstallId()
        val seed = today.hashCode() xor installId.hashCode()
        val newState = generateChallenges(today, seed, playerLevel)
        writeState(newState)
        return newState
    }

    private fun generateChallenges(date: String, seed: Int, playerLevel: Int): DailyChallengeState {
        val random = java.util.Random(seed.toLong())
        // Escala lineal: 1.0x a nivel 1, ~1.8x a nivel 50
        val levelScale = 1.0f + (playerLevel - 1) * 0.016f

        fun buildChallenge(
            template: ChallengeAppConfig.ChallengeTemplate,
            difficulty: ChallengeDifficulty
        ): DailyChallenge {
            val scaledTarget = (template.baseTarget * levelScale).toInt()
                .coerceAtLeast(template.baseTarget)
            return DailyChallenge(
                id = "${date}_${difficulty.name}_${template.type.name}",
                type = template.type,
                difficulty = difficulty,
                descriptionKey = template.descriptionKey,
                targetValue = scaledTarget,
                reward = ChallengeReward.forDifficulty(difficulty),
                extraParam = template.extraParam
            )
        }

        val easy = pickTemplate(ChallengeAppConfig.easyTemplates, random)
        val medium = pickTemplate(ChallengeAppConfig.mediumTemplates, random)
        val hard = pickTemplate(ChallengeAppConfig.hardTemplates, random)

        val challenges = listOf(
            buildChallenge(easy, ChallengeDifficulty.EASY),
            buildChallenge(medium, ChallengeDifficulty.MEDIUM),
            buildChallenge(hard, ChallengeDifficulty.HARD)
        )

        // Desafio semanal: mismo para toda la semana, seed basado en inicio de semana
        val weekStart = getWeekStartDate(date)
        val weeklyRandom = java.util.Random(weekStart.hashCode().toLong())
        val weeklyTemplate = pickTemplate(ChallengeAppConfig.weeklyTemplates, weeklyRandom)
        val weeklyChallenge = DailyChallenge(
            id = "${weekStart}_WEEKLY_${weeklyTemplate.type.name}",
            type = weeklyTemplate.type,
            difficulty = ChallengeDifficulty.WEEKLY,
            descriptionKey = weeklyTemplate.descriptionKey,
            targetValue = (weeklyTemplate.baseTarget * levelScale).toInt()
                .coerceAtLeast(weeklyTemplate.baseTarget),
            reward = ChallengeReward.forDifficulty(ChallengeDifficulty.WEEKLY),
            extraParam = weeklyTemplate.extraParam
        )

        return DailyChallengeState(
            date = date,
            challenges = challenges,
            weekStartDate = weekStart,
            weeklyChallenge = weeklyChallenge
        )
    }

    private fun pickTemplate(
        templates: List<ChallengeAppConfig.ChallengeTemplate>,
        random: java.util.Random
    ): ChallengeAppConfig.ChallengeTemplate {
        return templates[abs(random.nextInt()) % templates.size]
    }

    private fun updateChallengeProgress(challenge: DailyChallenge, event: ChallengeEvent): DailyChallenge {
        val newProgress = when (challenge.type) {
            ChallengeType.SCORE_MINIMUM -> when (event) {
                is ChallengeEvent.GameCompleted ->
                    if (event.score >= challenge.targetValue) challenge.targetValue
                    else challenge.currentProgress
                else -> challenge.currentProgress
            }
            ChallengeType.CORRECT_STREAK -> when (event) {
                is ChallengeEvent.GameCompleted -> maxOf(challenge.currentProgress, event.bestStreak)
                else -> challenge.currentProgress
            }
            ChallengeType.GAMES_PLAYED -> when (event) {
                is ChallengeEvent.GameCompleted -> challenge.currentProgress + 1
                else -> challenge.currentProgress
            }
            ChallengeType.PERFECT_GAME -> when (event) {
                is ChallengeEvent.GameCompleted ->
                    if (event.isPerfect) challenge.currentProgress + 1
                    else challenge.currentProgress
                else -> challenge.currentProgress
            }
            ChallengeType.TOTAL_CORRECT -> when (event) {
                is ChallengeEvent.AnswerGiven ->
                    if (event.isCorrect) challenge.currentProgress + 1
                    else challenge.currentProgress
                // GameCompleted no incrementa: ya se conta via AnswerGiven durante la partida
                is ChallengeEvent.GameCompleted -> challenge.currentProgress
            }
            ChallengeType.SPEED_ANSWER -> when (event) {
                is ChallengeEvent.AnswerGiven ->
                    if (event.isCorrect && event.responseTimeMs < 5000L) challenge.currentProgress + 1
                    else challenge.currentProgress
                else -> challenge.currentProgress
            }
            ChallengeType.PLAY_MODE -> when (event) {
                is ChallengeEvent.GameCompleted ->
                    if (event.gameMode == challenge.extraParam) challenge.currentProgress + 1
                    else challenge.currentProgress
                else -> challenge.currentProgress
            }
            ChallengeType.CUMULATIVE_SCORE -> when (event) {
                is ChallengeEvent.GameCompleted -> challenge.currentProgress + event.score
                else -> challenge.currentProgress
            }
            ChallengeType.WIN_GAME -> when (event) {
                is ChallengeEvent.GameCompleted ->
                    if (event.completedAll) challenge.currentProgress + 1
                    else challenge.currentProgress
                else -> challenge.currentProgress
            }
            ChallengeType.STREAK_IN_GAME -> when (event) {
                is ChallengeEvent.GameCompleted -> maxOf(challenge.currentProgress, event.bestStreak)
                else -> challenge.currentProgress
            }
        }

        val completed = newProgress >= challenge.targetValue
        return challenge.copy(currentProgress = newProgress, isCompleted = completed)
    }

    // ─── Persistencia ────────────────────────────────────────────────────────

    private suspend fun readState(): DailyChallengeState? {
        val jsonStr = dataStore.data.first()[KEY_STATE_JSON] ?: return null
        return try {
            deserializeState(jsonStr)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun writeState(state: DailyChallengeState) {
        dataStore.edit { prefs ->
            prefs[KEY_STATE_JSON] = serializeState(state)
        }
    }

    private suspend fun updateStats(newCompletions: Int, allDailyJustDone: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_STATS_TOTAL_COMPLETED] = (prefs[KEY_STATS_TOTAL_COMPLETED] ?: 0) + newCompletions
            if (allDailyJustDone) {
                prefs[KEY_STATS_ALL_DAILY_DAYS] = (prefs[KEY_STATS_ALL_DAILY_DAYS] ?: 0) + 1
                val currentStreak = (prefs[KEY_STATS_CURRENT_ALL_STREAK] ?: 0) + 1
                prefs[KEY_STATS_CURRENT_ALL_STREAK] = currentStreak
                val bestStreak = prefs[KEY_STATS_BEST_ALL_STREAK] ?: 0
                if (currentStreak > bestStreak) {
                    prefs[KEY_STATS_BEST_ALL_STREAK] = currentStreak
                }
            }
        }
    }

    private suspend fun getOrCreateInstallId(): String {
        val prefs = dataStore.data.first()
        val existing = prefs[KEY_INSTALL_ID]
        if (existing != null) return existing
        val newId = java.util.UUID.randomUUID().toString()
        dataStore.edit { it[KEY_INSTALL_ID] = newId }
        return newId
    }

    private fun todayDate(): String = dateFormat.format(Calendar.getInstance().time)

    private fun getWeekStartDate(dateStr: String): String {
        val cal = Calendar.getInstance()
        cal.time = dateFormat.parse(dateStr) ?: cal.time
        // Asegurar que el primer dia de la semana sea lunes
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return dateFormat.format(cal.time)
    }

    // ─── Serializacion manual con separadores ASCII de control ───────────────
    // Se usan caracteres SOH (\u0001) y STX (\u0002) que no aparecen en texto normal

    private fun serializeState(state: DailyChallengeState): String {
        val parts = mutableListOf<String>()
        parts.add(state.date)
        parts.add(state.allDailyCompleted.toString())
        parts.add(state.allDailyBonusClaimed.toString())
        parts.add(state.weekStartDate)
        parts.add(state.challenges.size.toString())
        for (c in state.challenges) {
            parts.add(serializeChallenge(c))
        }
        val weekly = state.weeklyChallenge
        parts.add(if (weekly != null) serializeChallenge(weekly) else "null")
        return parts.joinToString(SEP_FIELD)
    }

    private fun serializeChallenge(c: DailyChallenge): String {
        return listOf(
            c.id,
            c.type.name,
            c.difficulty.name,
            c.descriptionKey,
            c.targetValue.toString(),
            c.currentProgress.toString(),
            c.isCompleted.toString(),
            c.reward.xp.toString(),
            c.reward.coins.toString(),
            c.extraParam
        ).joinToString(SEP_CHALLENGE)
    }

    private fun deserializeState(str: String): DailyChallengeState {
        val parts = str.split(SEP_FIELD)
        var idx = 0
        val date = parts[idx++]
        val allDailyCompleted = parts[idx++].toBoolean()
        val allDailyBonusClaimed = parts[idx++].toBoolean()
        val weekStartDate = parts[idx++]
        val challengeCount = parts[idx++].toInt()
        val challenges = (0 until challengeCount).map {
            deserializeChallenge(parts[idx++])
        }
        val weeklyStr = parts.getOrNull(idx)
        val weekly = if (weeklyStr != null && weeklyStr != "null") deserializeChallenge(weeklyStr) else null
        return DailyChallengeState(date, challenges, allDailyCompleted, allDailyBonusClaimed, weekStartDate, weekly)
    }

    private fun deserializeChallenge(str: String): DailyChallenge {
        val parts = str.split(SEP_CHALLENGE)
        return DailyChallenge(
            id = parts[0],
            type = ChallengeType.valueOf(parts[1]),
            difficulty = ChallengeDifficulty.valueOf(parts[2]),
            descriptionKey = parts[3],
            targetValue = parts[4].toInt(),
            currentProgress = parts[5].toInt(),
            isCompleted = parts[6].toBoolean(),
            reward = ChallengeReward(parts[7].toInt(), parts[8].toInt()),
            extraParam = parts.getOrElse(9) { "" }
        )
    }
}
