package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.quiz.domain.reward.MysteryReward
import com.quiz.domain.reward.MysteryRewardType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

private val Context.mysteryBoxDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mystery_box_preferences"
)

/**
 * Caja misteriosa que se desbloquea cada [GAMES_PER_BOX] partidas completadas.
 *
 * La recompensa se sortea al abrir. Tres tipos:
 *  - XP_BONUS: +50 a +200 XP
 *  - COINS_BONUS: +30 a +100 coins
 *  - FREEZE_TOKEN: +1 token (si ya tiene el maximo, cae en COINS_BONUS)
 *
 * Contrato:
 *  1. Tras cada partida, ResultViewModel llama a [onGameCompleted] — el contador
 *     avanza y queda capeado en GAMES_PER_BOX si ya esta lista.
 *  2. Cuando la UI quiera mostrar la caja, observa [observeIsBoxReady] o chequea
 *     [isBoxReady] puntualmente.
 *  3. Al mostrar el dialog, llamar [openBox] para sortear la recompensa y
 *     [consumeBox] para reiniciar el contador. Esto desacopla el contador de la
 *     entrega del premio, asi llamar [onGameCompleted] sin UI cableada no pierde
 *     la caja.
 */
class MysteryBoxManager(
    private val context: Context,
    private val rng: Random = Random.Default
) {

    private val mutex = Mutex()

    companion object {
        const val GAMES_PER_BOX = 10
        val KEY_GAMES_SINCE_BOX = intPreferencesKey("mystery_games_since_box")
    }

    /**
     * Incrementa el contador de partidas. Retorna true si la caja quedo lista para
     * abrirse. El contador se mantiene capeado en GAMES_PER_BOX hasta que la UI
     * llame a [consumeBox]; asi llamar a este metodo sin UI cableada no pierde la
     * caja pendiente (idempotente una vez alcanzado el umbral).
     */
    suspend fun onGameCompleted(): Boolean = mutex.withLock {
        val current = context.mysteryBoxDataStore.data.first()[KEY_GAMES_SINCE_BOX] ?: 0
        if (current >= GAMES_PER_BOX) return@withLock true

        val next = (current + 1).coerceAtMost(GAMES_PER_BOX)
        context.mysteryBoxDataStore.edit { it[KEY_GAMES_SINCE_BOX] = next }
        next >= GAMES_PER_BOX
    }

    /** Reinicia el contador. Debe llamarse cuando la UI entrega la caja al usuario. */
    suspend fun consumeBox() = mutex.withLock {
        context.mysteryBoxDataStore.edit { it[KEY_GAMES_SINCE_BOX] = 0 }
    }

    /** true si hay una caja pendiente de abrir. */
    suspend fun isBoxReady(): Boolean {
        val current = context.mysteryBoxDataStore.data.first()[KEY_GAMES_SINCE_BOX] ?: 0
        return current >= GAMES_PER_BOX
    }

    /** Flow reactivo para que la UI se entere cuando la caja queda lista. */
    fun observeIsBoxReady(): Flow<Boolean> =
        context.mysteryBoxDataStore.data.map { (it[KEY_GAMES_SINCE_BOX] ?: 0) >= GAMES_PER_BOX }

    /** Partidas restantes antes del proximo box. */
    suspend fun gamesUntilNextBox(): Int {
        val current = context.mysteryBoxDataStore.data.first()[KEY_GAMES_SINCE_BOX] ?: 0
        return (GAMES_PER_BOX - current).coerceAtLeast(0)
    }

    /**
     * Sortea y retorna una recompensa al abrir la caja.
     * @param canGrantFreezeToken false si el jugador ya tiene el maximo de tokens.
     */
    fun openBox(canGrantFreezeToken: Boolean = true): MysteryReward {
        val rawRoll = rng.nextInt(100)
        val finalType = when {
            rawRoll < 50 -> MysteryRewardType.COINS_BONUS
            rawRoll < 85 -> MysteryRewardType.XP_BONUS
            canGrantFreezeToken -> MysteryRewardType.FREEZE_TOKEN
            else -> MysteryRewardType.COINS_BONUS
        }

        return when (finalType) {
            MysteryRewardType.XP_BONUS -> MysteryReward(
                xpAmount = rng.nextInt(50, 201),
                type = MysteryRewardType.XP_BONUS
            )
            MysteryRewardType.COINS_BONUS -> MysteryReward(
                coinsAmount = rng.nextInt(30, 101),
                type = MysteryRewardType.COINS_BONUS
            )
            MysteryRewardType.FREEZE_TOKEN -> MysteryReward(
                freezeTokens = 1,
                type = MysteryRewardType.FREEZE_TOKEN
            )
        }
    }
}
