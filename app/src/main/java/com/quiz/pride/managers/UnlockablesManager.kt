package com.quiz.pride.managers

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.quiz.domain.cosmetics.CosmeticCategory
import com.quiz.domain.cosmetics.CosmeticPurchaseResult
import com.quiz.domain.cosmetics.PlayerCosmetics
import com.quiz.domain.cosmetics.Unlockable
import com.quiz.domain.cosmetics.UnlockCondition
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Gestiona el inventario de cosmeticos del jugador: compras, desbloqueos y equipamiento.
 *
 * Utiliza el mismo cosmeticsDataStore que CurrencyManager para mantener
 * todo el estado cosmetico en un unico DataStore ("cosmetics_preferences").
 *
 * Las operaciones con estado compartido estan protegidas con Mutex para
 * evitar condiciones de carrera en operaciones concurrentes.
 */
class UnlockablesManager(
    private val context: Context,
    private val currencyManager: CurrencyManager,
    private val catalog: UnlockableCatalog
) {

    private val dataStore get() = context.cosmeticsDataStore
    private val mutex = Mutex()

    companion object {
        val KEY_UNLOCKED_IDS = stringSetPreferencesKey("cosmetics_unlocked_ids")
        val KEY_EQUIPPED_FRAME = stringPreferencesKey("cosmetics_equipped_frame")
        val KEY_EQUIPPED_TITLE = stringPreferencesKey("cosmetics_equipped_title")
        val KEY_EQUIPPED_CARD_THEME = stringPreferencesKey("cosmetics_equipped_card_theme")
        val KEY_EQUIPPED_CELEBRATION = stringPreferencesKey("cosmetics_equipped_celebration")
        val KEY_EQUIPPED_APP_ICON = stringPreferencesKey("cosmetics_equipped_app_icon")
    }

    /**
     * Retorna el estado cosmetico actual del jugador.
     * Los items marcados como [Unlockable.isDefault] se incluyen automaticamente
     * sin necesidad de estar en el Set persistido.
     */
    suspend fun getPlayerCosmetics(): PlayerCosmetics {
        val prefs = dataStore.data.first()
        val defaultIds = catalog.getAllItems().filter { it.isDefault }.map { it.id }.toSet()
        return PlayerCosmetics(
            unlockedIds = (prefs[KEY_UNLOCKED_IDS] ?: emptySet()) + defaultIds,
            equippedFrame = prefs[KEY_EQUIPPED_FRAME] ?: "frame_default",
            equippedTitle = prefs[KEY_EQUIPPED_TITLE] ?: "title_default",
            equippedCardTheme = prefs[KEY_EQUIPPED_CARD_THEME] ?: "card_default",
            equippedCelebration = prefs[KEY_EQUIPPED_CELEBRATION] ?: "celebration_default",
            equippedAppIcon = prefs[KEY_EQUIPPED_APP_ICON] ?: ""
        )
    }

    /**
     * Intenta comprar un item del catalogo con la moneda adecuada.
     *
     * Solo procesa items con condicion [UnlockCondition.PurchaseWithCoins] o
     * [UnlockCondition.PurchaseWithGems]. Los items desbloqueables por nivel,
     * logro, racha o desafios se gestionan via [autoUnlockIfEligible].
     *
     * @return [CosmeticPurchaseResult] con el resultado de la operacion.
     */
    suspend fun purchaseItem(itemId: String): CosmeticPurchaseResult = mutex.withLock {
        val item = catalog.getItem(itemId)
            ?: return@withLock CosmeticPurchaseResult.ConditionNotMet(UnlockCondition.Free)

        val cosmetics = getPlayerCosmetics()
        if (itemId in cosmetics.unlockedIds) {
            return@withLock CosmeticPurchaseResult.AlreadyOwned(itemId)
        }

        val balance = currencyManager.getBalance()

        return@withLock when (val condition = item.unlockCondition) {
            is UnlockCondition.PurchaseWithCoins -> {
                if (balance.coins < condition.price) {
                    CosmeticPurchaseResult.InsufficientFunds(condition.price, balance.coins, "coins")
                } else {
                    currencyManager.spendCoins(condition.price)
                    unlockItem(itemId)
                    CosmeticPurchaseResult.Success(item, currencyManager.getBalance())
                }
            }
            is UnlockCondition.PurchaseWithGems -> {
                if (balance.gems < condition.price) {
                    CosmeticPurchaseResult.InsufficientFunds(condition.price, balance.gems, "gems")
                } else {
                    currencyManager.spendGems(condition.price)
                    unlockItem(itemId)
                    CosmeticPurchaseResult.Success(item, currencyManager.getBalance())
                }
            }
            else -> {
                // Items no comprables directamente (nivel, logro, racha, desafio)
                // Se desbloquean automaticamente via autoUnlockIfEligible
                CosmeticPurchaseResult.ConditionNotMet(condition)
            }
        }
    }

    /**
     * Evalua si un item es elegible para desbloqueo automatico segun el contexto actual.
     * Se debe llamar despues de cada partida para verificar nuevas condiciones cumplidas.
     *
     * @param itemId ID del item a evaluar
     * @param currentLevel Nivel actual del jugador
     * @param unlockedAchievements Conjunto de IDs de logros desbloqueados
     * @param streakDays Dias de racha actuales
     * @param challengesCompleted Total de desafios completados
     */
    suspend fun autoUnlockIfEligible(
        itemId: String,
        currentLevel: Int,
        unlockedAchievements: Set<String>,
        streakDays: Int,
        challengesCompleted: Int
    ) = mutex.withLock {
        val item = catalog.getItem(itemId) ?: return@withLock
        val cosmetics = getPlayerCosmetics()
        if (itemId in cosmetics.unlockedIds) return@withLock

        val eligible = when (val cond = item.unlockCondition) {
            is UnlockCondition.ReachLevel -> currentLevel >= cond.level
            is UnlockCondition.CompleteAchievement -> cond.achievementId in unlockedAchievements
            is UnlockCondition.StreakMilestone -> streakDays >= cond.days
            is UnlockCondition.ChallengeCount -> challengesCompleted >= cond.count
            is UnlockCondition.Free -> true
            else -> false
        }

        if (eligible) unlockItem(itemId)
    }

    /**
     * Equipa un item en la ranura correspondiente a su categoria.
     * Requiere que el item ya este desbloqueado (no valida pertenencia al inventario).
     */
    suspend fun equipItem(itemId: String, category: CosmeticCategory) = mutex.withLock {
        dataStore.edit { prefs ->
            when (category) {
                CosmeticCategory.PROFILE_FRAME -> prefs[KEY_EQUIPPED_FRAME] = itemId
                CosmeticCategory.TITLE_BADGE -> prefs[KEY_EQUIPPED_TITLE] = itemId
                CosmeticCategory.ANSWER_CARD_THEME -> prefs[KEY_EQUIPPED_CARD_THEME] = itemId
                CosmeticCategory.CELEBRATION_ANIMATION -> prefs[KEY_EQUIPPED_CELEBRATION] = itemId
                CosmeticCategory.APP_ICON -> prefs[KEY_EQUIPPED_APP_ICON] = itemId
            }
        }
    }

    /** Retorna el catalogo completo de items disponibles. */
    fun getCatalog(): List<Unlockable> = catalog.getAllItems()

    /** Retorna los items del catalogo filtrados por categoria. */
    fun getCatalogByCategory(category: CosmeticCategory): List<Unlockable> =
        catalog.getByCategory(category)

    private suspend fun unlockItem(itemId: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_UNLOCKED_IDS] ?: emptySet()
            prefs[KEY_UNLOCKED_IDS] = current + itemId
        }
    }
}

/**
 * Contrato del catalogo de items cosmeticos.
 * La implementacion concreta (PrideCatalog) vive en app/cosmetics/.
 * Separar la interfaz facilita testing con catalogos mock.
 */
interface UnlockableCatalog {
    fun getAllItems(): List<Unlockable>
    fun getItem(id: String): Unlockable?
    fun getByCategory(category: CosmeticCategory): List<Unlockable>
}
