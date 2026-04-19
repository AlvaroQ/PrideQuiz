package com.quiz.pride.ui.shop

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.quiz.domain.cosmetics.CosmeticCategory
import com.quiz.domain.cosmetics.CosmeticPurchaseResult
import com.quiz.domain.cosmetics.CurrencyBalance
import com.quiz.domain.cosmetics.PlayerCosmetics
import com.quiz.domain.cosmetics.Unlockable
import com.quiz.domain.cosmetics.UnlockCondition
import com.quiz.pride.common.ComposeViewModel
import com.quiz.pride.managers.CurrencyManager
import com.quiz.pride.managers.UnlockablesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Representa un item del catalogo con estado contextual del jugador.
 */
@Immutable
data class ShopItem(
    val unlockable: Unlockable,
    val isOwned: Boolean,
    val isEquipped: Boolean
)

@Immutable
data class ShopUiState(
    val isLoading: Boolean = true,
    val balance: CurrencyBalance = CurrencyBalance(),
    val selectedCategory: CosmeticCategory = CosmeticCategory.PROFILE_FRAME,
    val items: List<ShopItem> = emptyList(),
    val playerCosmetics: PlayerCosmetics = PlayerCosmetics(),
    val purchaseMessage: String? = null,
    val showPurchaseConfirmDialog: Boolean = false,
    val pendingPurchaseItem: ShopItem? = null
)

class ShopViewModel(
    private val unlockablesManager: UnlockablesManager,
    private val currencyManager: CurrencyManager
) : ComposeViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    init {
        loadShopData()
        observeBalance()
    }

    private fun loadShopData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val balance = currencyManager.getBalance()
            val playerCosmetics = unlockablesManager.getPlayerCosmetics()
            val items = buildShopItems(
                category = _uiState.value.selectedCategory,
                playerCosmetics = playerCosmetics
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    balance = balance,
                    playerCosmetics = playerCosmetics,
                    items = items
                )
            }
        }
    }

    /**
     * Observa el balance de moneda en tiempo real via Flow.
     * Actualiza el estado de UI cada vez que coins o gems cambian.
     */
    private fun observeBalance() {
        viewModelScope.launch {
            currencyManager.observeBalance().collect { balance ->
                _uiState.update { it.copy(balance = balance) }
            }
        }
    }

    /**
     * Cambia la categoria seleccionada y recarga los items filtrados.
     */
    fun onCategorySelected(category: CosmeticCategory) {
        viewModelScope.launch {
            val playerCosmetics = _uiState.value.playerCosmetics
            val items = buildShopItems(category, playerCosmetics)
            _uiState.update {
                it.copy(
                    selectedCategory = category,
                    items = items
                )
            }
        }
    }

    /**
     * Muestra el dialogo de confirmacion de compra para el item seleccionado.
     */
    fun onItemTapped(shopItem: ShopItem) {
        if (shopItem.isOwned) {
            // Si ya es propio, equipar/desequipar directamente
            onEquipItem(shopItem)
        } else {
            // Mostrar dialogo de confirmacion de compra
            val isPurchasable = shopItem.unlockable.unlockCondition is UnlockCondition.PurchaseWithCoins ||
                shopItem.unlockable.unlockCondition is UnlockCondition.PurchaseWithGems
            if (isPurchasable) {
                _uiState.update {
                    it.copy(
                        showPurchaseConfirmDialog = true,
                        pendingPurchaseItem = shopItem
                    )
                }
            }
        }
    }

    /**
     * Cancela el dialogo de confirmacion de compra.
     */
    fun onPurchaseDismissed() {
        _uiState.update {
            it.copy(
                showPurchaseConfirmDialog = false,
                pendingPurchaseItem = null
            )
        }
    }

    /**
     * Confirma y ejecuta la compra del item pendiente.
     */
    fun onPurchaseConfirmed() {
        val item = _uiState.value.pendingPurchaseItem ?: return
        _uiState.update { it.copy(showPurchaseConfirmDialog = false, pendingPurchaseItem = null) }
        purchaseItem(item.unlockable.id)
    }

    /**
     * Intenta comprar el item con la moneda requerida.
     * Actualiza el estado con el mensaje de resultado.
     */
    private fun purchaseItem(itemId: String) {
        viewModelScope.launch {
            val result = unlockablesManager.purchaseItem(itemId)
            val message = when (result) {
                is CosmeticPurchaseResult.Success -> "Obteniste: ${result.item.name}"
                is CosmeticPurchaseResult.InsufficientFunds -> {
                    val currency = if (result.currency == "coins") "monedas" else "gemas"
                    "Faltan ${result.needed - result.have} $currency"
                }
                is CosmeticPurchaseResult.AlreadyOwned -> "Ya tenes este item"
                is CosmeticPurchaseResult.ConditionNotMet -> "Condicion no cumplida aun"
            }

            // Actualizar cosmeticos y lista de items despues de compra exitosa
            val updatedCosmetics = unlockablesManager.getPlayerCosmetics()
            val updatedItems = buildShopItems(_uiState.value.selectedCategory, updatedCosmetics)

            _uiState.update {
                it.copy(
                    playerCosmetics = updatedCosmetics,
                    items = updatedItems,
                    purchaseMessage = message
                )
            }
        }
    }

    /**
     * Equipa o desequipa un item ya desbloqueado.
     * Si el item ya esta equipado, se equipa el item por defecto de la misma categoria.
     */
    fun onEquipItem(shopItem: ShopItem) {
        if (!shopItem.isOwned) return

        viewModelScope.launch {
            val category = shopItem.unlockable.category
            val currentEquipped = getEquippedId(category, _uiState.value.playerCosmetics)

            val newEquipId = if (currentEquipped == shopItem.unlockable.id) {
                // Desequipar: volver al default de la categoria
                getDefaultIdForCategory(category)
            } else {
                shopItem.unlockable.id
            }

            unlockablesManager.equipItem(newEquipId, category)

            val updatedCosmetics = unlockablesManager.getPlayerCosmetics()
            val updatedItems = buildShopItems(category, updatedCosmetics)

            _uiState.update {
                it.copy(
                    playerCosmetics = updatedCosmetics,
                    items = updatedItems
                )
            }
        }
    }

    /**
     * Descarta el mensaje de compra (snackbar/toast).
     */
    fun onPurchaseMessageDismissed() {
        _uiState.update { it.copy(purchaseMessage = null) }
    }

    /**
     * Construye la lista de ShopItem para una categoria con estado del jugador.
     */
    private fun buildShopItems(
        category: CosmeticCategory,
        playerCosmetics: PlayerCosmetics
    ): List<ShopItem> {
        return unlockablesManager.getCatalogByCategory(category).map { unlockable ->
            val isOwned = unlockable.id in playerCosmetics.unlockedIds
            val equippedId = getEquippedId(category, playerCosmetics)
            ShopItem(
                unlockable = unlockable,
                isOwned = isOwned,
                isEquipped = unlockable.id == equippedId
            )
        }
    }

    private fun getEquippedId(category: CosmeticCategory, cosmetics: PlayerCosmetics): String {
        return when (category) {
            CosmeticCategory.PROFILE_FRAME -> cosmetics.equippedFrame
            CosmeticCategory.TITLE_BADGE -> cosmetics.equippedTitle
            CosmeticCategory.ANSWER_CARD_THEME -> cosmetics.equippedCardTheme
            CosmeticCategory.CELEBRATION_ANIMATION -> cosmetics.equippedCelebration
            CosmeticCategory.APP_ICON -> cosmetics.equippedAppIcon
        }
    }

    private fun getDefaultIdForCategory(category: CosmeticCategory): String {
        return when (category) {
            CosmeticCategory.PROFILE_FRAME -> "frame_default"
            CosmeticCategory.TITLE_BADGE -> "title_default"
            CosmeticCategory.ANSWER_CARD_THEME -> "card_default"
            CosmeticCategory.CELEBRATION_ANIMATION -> "celebration_default"
            CosmeticCategory.APP_ICON -> ""
        }
    }
}
