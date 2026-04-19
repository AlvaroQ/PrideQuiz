package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.quiz.domain.reward.DailyReward
import com.quiz.domain.reward.RewardTier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import kotlin.random.Random

private val Context.dailyRewardDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "daily_reward_preferences"
)

/**
 * Gestiona la recompensa diaria del jugador.
 *
 * Reglas:
 * - Una recompensa por dia (UTC/local del dispositivo, fecha ISO).
 * - Tier generado de forma determinista por `LocalDate.toString()` + installId.
 * - Distribucion aproximada: 70% COMMON, 25% UNCOMMON, 5% RARE.
 * - Al reclamar se persiste isClaimed=true y se acreditan coins + XP
 *   a traves de CurrencyManager / ProgressionManager desde el ViewModel.
 */
class DailyRewardManager(
    private val context: Context,
    private val installIdProvider: () -> String = { "default" },
    private val clock: () -> LocalDate = { LocalDate.now() }
) {
    private val mutex = Mutex()

    companion object {
        val KEY_LAST_DATE = stringPreferencesKey("daily_reward_last_date")
        val KEY_CLAIMED_DATE = stringPreferencesKey("daily_reward_claimed_date")
        val KEY_DISMISSED_DATE = stringPreferencesKey("daily_reward_dismissed_date")
        val KEY_LAST_TIER = stringPreferencesKey("daily_reward_last_tier")
        val KEY_LAST_XP = intPreferencesKey("daily_reward_last_xp")
        val KEY_LAST_COINS = intPreferencesKey("daily_reward_last_coins")
        val KEY_LAST_GEMS = intPreferencesKey("daily_reward_last_gems")
    }

    /** Retorna la recompensa del dia actual, generandola si es nueva. */
    suspend fun getTodayReward(): DailyReward = mutex.withLock {
        val today = clock().toString()
        val prefs = context.dailyRewardDataStore.data.first()
        val storedDate = prefs[KEY_LAST_DATE]

        if (storedDate != today) {
            val generated = generateReward(today)
            context.dailyRewardDataStore.edit { p ->
                p[KEY_LAST_DATE] = today
                p[KEY_LAST_TIER] = generated.tier.name
                p[KEY_LAST_XP] = generated.xpAmount
                p[KEY_LAST_COINS] = generated.coinsAmount
                p[KEY_LAST_GEMS] = generated.gemsAmount
            }
            return@withLock generated.copy(isClaimed = false)
        }

        val tier = runCatching { RewardTier.valueOf(prefs[KEY_LAST_TIER] ?: "COMMON") }
            .getOrDefault(RewardTier.COMMON)

        DailyReward(
            date = today,
            tier = tier,
            xpAmount = prefs[KEY_LAST_XP] ?: 10,
            coinsAmount = prefs[KEY_LAST_COINS] ?: 5,
            gemsAmount = prefs[KEY_LAST_GEMS] ?: 0,
            isClaimed = prefs[KEY_CLAIMED_DATE] == today
        )
    }

    /** Flow reactivo de la recompensa del dia (regenera al cambiar la fecha). */
    fun observeTodayReward(): Flow<DailyReward?> =
        context.dailyRewardDataStore.data.map { prefs ->
            val today = clock().toString()
            if (prefs[KEY_LAST_DATE] != today) {
                null
            } else {
                val tier = runCatching { RewardTier.valueOf(prefs[KEY_LAST_TIER] ?: "COMMON") }
                    .getOrDefault(RewardTier.COMMON)
                DailyReward(
                    date = today,
                    tier = tier,
                    xpAmount = prefs[KEY_LAST_XP] ?: 10,
                    coinsAmount = prefs[KEY_LAST_COINS] ?: 5,
                    gemsAmount = prefs[KEY_LAST_GEMS] ?: 0,
                    isClaimed = prefs[KEY_CLAIMED_DATE] == today
                )
            }
        }

    /**
     * Marca la recompensa del dia como reclamada.
     * @return la DailyReward actualizada, o null si ya estaba reclamada.
     */
    suspend fun claimTodayReward(): DailyReward? = mutex.withLock {
        val today = clock().toString()
        val prefs = context.dailyRewardDataStore.data.first()

        val current = if (prefs[KEY_LAST_DATE] != today) {
            val generated = generateReward(today)
            context.dailyRewardDataStore.edit { p ->
                p[KEY_LAST_DATE] = today
                p[KEY_LAST_TIER] = generated.tier.name
                p[KEY_LAST_XP] = generated.xpAmount
                p[KEY_LAST_COINS] = generated.coinsAmount
                p[KEY_LAST_GEMS] = generated.gemsAmount
            }
            generated.copy(isClaimed = false)
        } else {
            val tier = runCatching { RewardTier.valueOf(prefs[KEY_LAST_TIER] ?: "COMMON") }
                .getOrDefault(RewardTier.COMMON)

            DailyReward(
                date = today,
                tier = tier,
                xpAmount = prefs[KEY_LAST_XP] ?: 10,
                coinsAmount = prefs[KEY_LAST_COINS] ?: 5,
                gemsAmount = prefs[KEY_LAST_GEMS] ?: 0,
                isClaimed = prefs[KEY_CLAIMED_DATE] == today
            )
        }

        if (current.isClaimed) return@withLock null

        context.dailyRewardDataStore.edit { p ->
            p[KEY_CLAIMED_DATE] = current.date
        }
        current.copy(isClaimed = true)
    }

    internal fun generateReward(date: String): DailyReward =
        generateDailyReward(date = date, installId = installIdProvider())

    /**
     * Indica si la recompensa del dia actual ha sido ocultada (swipe-to-dismiss).
     * El reset es automatico: al cambiar la fecha, el valor deja de coincidir con hoy.
     */
    suspend fun isDismissedToday(): Boolean {
        val today = clock().toString()
        val prefs = context.dailyRewardDataStore.data.first()
        return prefs[KEY_DISMISSED_DATE] == today
    }

    /** Flow reactivo del estado "oculta hoy". */
    fun observeIsDismissedToday(): Flow<Boolean> =
        context.dailyRewardDataStore.data.map { prefs ->
            prefs[KEY_DISMISSED_DATE] == clock().toString()
        }

    /** Persiste la fecha de hoy como "oculta" — se re-expone automaticamente manana. */
    suspend fun dismissTodayReward() {
        val today = clock().toString()
        context.dailyRewardDataStore.edit { p ->
            p[KEY_DISMISSED_DATE] = today
        }
    }
}

/**
 * Genera deterministicamente la recompensa del dia a partir de la fecha y el
 * installId. Pura (sin Context, sin DataStore): testeable directamente.
 *
 * Distribucion de tiers: 70% COMMON, 25% UNCOMMON, 5% RARE.
 */
internal fun generateDailyReward(date: String, installId: String): DailyReward {
    val seed = date.hashCode().toLong() xor installId.hashCode().toLong()
    val rng = Random(seed)
    val roll = rng.nextInt(100)

    val tier = when {
        roll < 70 -> RewardTier.COMMON
        roll < 95 -> RewardTier.UNCOMMON
        else -> RewardTier.RARE
    }

    val (xp, coins, gems) = when (tier) {
        RewardTier.COMMON -> Triple(
            rng.nextInt(10, 26),       // 10-25 XP
            rng.nextInt(5, 16),         // 5-15 coins
            0
        )
        RewardTier.UNCOMMON -> Triple(
            rng.nextInt(30, 61),       // 30-60 XP
            rng.nextInt(25, 51),        // 25-50 coins
            0
        )
        RewardTier.RARE -> Triple(
            rng.nextInt(75, 151),      // 75-150 XP
            rng.nextInt(75, 151),       // 75-150 coins
            1                            // + 1 gema
        )
    }

    return DailyReward(
        date = date,
        tier = tier,
        xpAmount = xp,
        coinsAmount = coins,
        gemsAmount = gems,
        isClaimed = false
    )
}
