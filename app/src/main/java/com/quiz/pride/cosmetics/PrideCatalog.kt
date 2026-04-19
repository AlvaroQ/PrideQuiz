package com.quiz.pride.cosmetics

import com.quiz.domain.cosmetics.CosmeticCategory
import com.quiz.domain.cosmetics.CosmeticTier
import com.quiz.domain.cosmetics.Unlockable
import com.quiz.domain.cosmetics.UnlockCondition
import com.quiz.pride.managers.UnlockableCatalog

/**
 * Catalogo oficial de items cosmeticos para PrideQuiz v1.
 *
 * 17 items distribuidos en 5 categorias:
 * - PROFILE_FRAME: 5 items (1 free + 4 comprables)
 * - TITLE_BADGE: 5 items (1 free + 4 desbloqueables)
 * - ANSWER_CARD_THEME: 3 items (1 free + 2 comprables)
 * - CELEBRATION_ANIMATION: 3 items (1 free + 2 comprables)
 * - APP_ICON: 1 item (gems)
 *
 * Los items [Unlockable.isDefault] se incluyen automaticamente en el inventario
 * del jugador sin requerir persistencia en DataStore.
 */
object PrideCatalog : UnlockableCatalog {

    private val items = listOf(
        // === PROFILE FRAMES (5: 1 free + 2 coins + 2 gems) ===
        Unlockable(
            id = "frame_default",
            name = "Clasico",
            description = "Marco predeterminado",
            category = CosmeticCategory.PROFILE_FRAME,
            tier = CosmeticTier.COMMON,
            unlockCondition = UnlockCondition.Free,
            isDefault = true
        ),
        Unlockable(
            id = "frame_rainbow",
            name = "Arcoiris",
            description = "Marco con colores del arcoiris",
            category = CosmeticCategory.PROFILE_FRAME,
            tier = CosmeticTier.COMMON,
            unlockCondition = UnlockCondition.PurchaseWithCoins(100)
        ),
        Unlockable(
            id = "frame_fire",
            name = "En Llamas",
            description = "Marco con efecto de fuego",
            category = CosmeticCategory.PROFILE_FRAME,
            tier = CosmeticTier.RARE,
            unlockCondition = UnlockCondition.PurchaseWithCoins(300)
        ),
        Unlockable(
            id = "frame_diamond",
            name = "Diamante",
            description = "Marco brillante de diamante",
            category = CosmeticCategory.PROFILE_FRAME,
            tier = CosmeticTier.EPIC,
            unlockCondition = UnlockCondition.PurchaseWithGems(50)
        ),
        Unlockable(
            id = "frame_legendary",
            name = "Leyenda",
            description = "Marco dorado legendario",
            category = CosmeticCategory.PROFILE_FRAME,
            tier = CosmeticTier.LEGENDARY,
            unlockCondition = UnlockCondition.PurchaseWithGems(150)
        ),

        // === TITLE BADGES (5: 1 free + 1 coins + 1 streak + 1 challenge + 1 level) ===
        Unlockable(
            id = "title_default",
            name = "Sin Titulo",
            description = "Titulo predeterminado",
            category = CosmeticCategory.TITLE_BADGE,
            tier = CosmeticTier.COMMON,
            unlockCondition = UnlockCondition.Free,
            isDefault = true
        ),
        Unlockable(
            id = "title_curious",
            name = "Curiosx",
            description = "Para quienes siempre preguntan",
            category = CosmeticCategory.TITLE_BADGE,
            tier = CosmeticTier.COMMON,
            unlockCondition = UnlockCondition.PurchaseWithCoins(75)
        ),
        Unlockable(
            id = "title_scholar",
            name = "Eruditx",
            description = "Conocimiento sin limites",
            category = CosmeticCategory.TITLE_BADGE,
            tier = CosmeticTier.RARE,
            unlockCondition = UnlockCondition.StreakMilestone(14)
        ),
        Unlockable(
            id = "title_champion",
            name = "Campeonx",
            description = "Dominas los desafios",
            category = CosmeticCategory.TITLE_BADGE,
            tier = CosmeticTier.EPIC,
            unlockCondition = UnlockCondition.ChallengeCount(50)
        ),
        Unlockable(
            id = "title_legend",
            name = "Leyenda Viviente",
            description = "El maximo honor",
            category = CosmeticCategory.TITLE_BADGE,
            tier = CosmeticTier.LEGENDARY,
            unlockCondition = UnlockCondition.ReachLevel(50)
        ),

        // === ANSWER CARD THEMES (3: 1 free + 2 coins) ===
        Unlockable(
            id = "card_default",
            name = "Clasico",
            description = "Tema de tarjetas predeterminado",
            category = CosmeticCategory.ANSWER_CARD_THEME,
            tier = CosmeticTier.COMMON,
            unlockCondition = UnlockCondition.Free,
            isDefault = true
        ),
        Unlockable(
            id = "card_neon",
            name = "Neon",
            description = "Tarjetas con brillo neon",
            category = CosmeticCategory.ANSWER_CARD_THEME,
            tier = CosmeticTier.COMMON,
            unlockCondition = UnlockCondition.PurchaseWithCoins(150)
        ),
        Unlockable(
            id = "card_vintage",
            name = "Vintage",
            description = "Estilo retro elegante",
            category = CosmeticCategory.ANSWER_CARD_THEME,
            tier = CosmeticTier.RARE,
            unlockCondition = UnlockCondition.PurchaseWithCoins(400)
        ),

        // === CELEBRATION ANIMATIONS (3: 1 free + 2 coins) ===
        Unlockable(
            id = "celebration_default",
            name = "Confeti",
            description = "Celebracion predeterminada",
            category = CosmeticCategory.CELEBRATION_ANIMATION,
            tier = CosmeticTier.COMMON,
            unlockCondition = UnlockCondition.Free,
            isDefault = true
        ),
        Unlockable(
            id = "celebration_stars",
            name = "Estrellas",
            description = "Lluvia de estrellas doradas",
            category = CosmeticCategory.CELEBRATION_ANIMATION,
            tier = CosmeticTier.COMMON,
            unlockCondition = UnlockCondition.PurchaseWithCoins(200)
        ),
        Unlockable(
            id = "celebration_fireworks",
            name = "Fuegos Artificiales",
            description = "Explosion de colores",
            category = CosmeticCategory.CELEBRATION_ANIMATION,
            tier = CosmeticTier.RARE,
            unlockCondition = UnlockCondition.PurchaseWithCoins(500)
        ),

        // === APP ICON (1: gems) ===
        Unlockable(
            id = "icon_pride_gold",
            name = "Orgullo Dorado",
            description = "Icono exclusivo dorado",
            category = CosmeticCategory.APP_ICON,
            tier = CosmeticTier.EPIC,
            unlockCondition = UnlockCondition.PurchaseWithGems(75)
        )
    )

    override fun getAllItems(): List<Unlockable> = items

    override fun getItem(id: String): Unlockable? = items.find { it.id == id }

    override fun getByCategory(category: CosmeticCategory): List<Unlockable> =
        items.filter { it.category == category }
}
