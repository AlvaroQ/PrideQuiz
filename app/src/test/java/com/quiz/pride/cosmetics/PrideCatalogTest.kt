package com.quiz.pride.cosmetics

import com.quiz.domain.cosmetics.CosmeticCategory
import com.quiz.domain.cosmetics.CosmeticTier
import com.quiz.domain.cosmetics.UnlockCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de integridad del catalogo de cosmeticos PrideCatalog.
 *
 * PrideCatalog es un object Kotlin puro sin dependencias de Android.
 * Se verifica la coherencia del catalogo: IDs unicos, items default por categoria,
 * condiciones de desbloqueo validas y distribucion de tiers.
 */
class PrideCatalogTest {

    private val todosLosItems = PrideCatalog.getAllItems()

    // =========================================================
    // Integridad global del catalogo
    // =========================================================

    @Test
    fun `el catalogo contiene exactamente 17 items`() {
        assertEquals(17, todosLosItems.size)
    }

    @Test
    fun `todos los items tienen IDs unicos`() {
        val ids = todosLosItems.map { it.id }
        val idsUnicos = ids.toSet()

        assertEquals(
            "Hay IDs duplicados: ${ids.groupBy { it }.filter { it.value.size > 1 }.keys}",
            idsUnicos.size,
            ids.size
        )
    }

    @Test
    fun `todos los items tienen nombre no vacio`() {
        todosLosItems.forEach { item ->
            assertTrue("Item '${item.id}' tiene nombre vacio", item.name.isNotBlank())
        }
    }

    @Test
    fun `todos los items tienen descripcion no vacia`() {
        todosLosItems.forEach { item ->
            assertTrue("Item '${item.id}' tiene descripcion vacia", item.description.isNotBlank())
        }
    }

    // =========================================================
    // Cobertura de categorias
    // =========================================================

    @Test
    fun `el catalogo tiene items en las 5 categorias`() {
        val categoriasPresentes = todosLosItems.map { it.category }.toSet()

        assertEquals(CosmeticCategory.entries.size, categoriasPresentes.size)
        CosmeticCategory.entries.forEach { categoria ->
            assertTrue("Categoria $categoria no tiene items", categoria in categoriasPresentes)
        }
    }

    @Test
    fun `PROFILE_FRAME tiene exactamente 5 items`() {
        val items = PrideCatalog.getByCategory(CosmeticCategory.PROFILE_FRAME)
        assertEquals(5, items.size)
    }

    @Test
    fun `TITLE_BADGE tiene exactamente 5 items`() {
        val items = PrideCatalog.getByCategory(CosmeticCategory.TITLE_BADGE)
        assertEquals(5, items.size)
    }

    @Test
    fun `ANSWER_CARD_THEME tiene exactamente 3 items`() {
        val items = PrideCatalog.getByCategory(CosmeticCategory.ANSWER_CARD_THEME)
        assertEquals(3, items.size)
    }

    @Test
    fun `CELEBRATION_ANIMATION tiene exactamente 3 items`() {
        val items = PrideCatalog.getByCategory(CosmeticCategory.CELEBRATION_ANIMATION)
        assertEquals(3, items.size)
    }

    @Test
    fun `APP_ICON tiene exactamente 1 item`() {
        val items = PrideCatalog.getByCategory(CosmeticCategory.APP_ICON)
        assertEquals(1, items.size)
    }

    // =========================================================
    // Items default (uno por categoria obligatorio)
    // =========================================================

    @Test
    fun `cada categoria cosmetica salvo APP_ICON tiene al menos un item default`() {
        // APP_ICON se excluye intencionalmente: el icono por defecto del launcher
        // nativo actua como default implicito y no esta representado en el catalogo.
        // Las demas categorias deben tener SI o SI un item default gratuito para que
        // cualquier jugador tenga siempre un item equipable sin coste.
        val categoriasConDefaultObligatorio = CosmeticCategory.entries - CosmeticCategory.APP_ICON

        categoriasConDefaultObligatorio.forEach { categoria ->
            val tieneDefault = PrideCatalog.getByCategory(categoria).any { it.isDefault }
            assertTrue("Categoria $categoria no tiene item default", tieneDefault)
        }
    }

    @Test
    fun `los items default tienen condicion de desbloqueo Free`() {
        val itemsDefault = todosLosItems.filter { it.isDefault }

        itemsDefault.forEach { item ->
            assertTrue(
                "Item default '${item.id}' no tiene condicion Free: ${item.unlockCondition}",
                item.unlockCondition is UnlockCondition.Free
            )
        }
    }

    @Test
    fun `hay exactamente 4 items default (uno por categoria con default)`() {
        // APP_ICON no tiene item default segun el catalogo actual
        val itemsDefault = todosLosItems.filter { it.isDefault }
        // PROFILE_FRAME, TITLE_BADGE, ANSWER_CARD_THEME, CELEBRATION_ANIMATION = 4 defaults
        assertEquals(4, itemsDefault.size)
    }

    // =========================================================
    // getItem — busqueda por ID
    // =========================================================

    @Test
    fun `getItem con ID valido retorna el item correcto`() {
        val item = PrideCatalog.getItem("frame_default")

        assertNotNull(item)
        assertEquals("frame_default", item!!.id)
        assertEquals(CosmeticCategory.PROFILE_FRAME, item.category)
    }

    @Test
    fun `getItem con ID invalido retorna null`() {
        val item = PrideCatalog.getItem("item_que_no_existe")

        assertNull(item)
    }

    @Test
    fun `getItem con string vacio retorna null`() {
        val item = PrideCatalog.getItem("")

        assertNull(item)
    }

    @Test
    fun `getItem es consistente con getAllItems para todos los IDs`() {
        todosLosItems.forEach { itemEsperado ->
            val itemEncontrado = PrideCatalog.getItem(itemEsperado.id)
            assertNotNull("getItem no encontro '${itemEsperado.id}'", itemEncontrado)
            assertEquals(itemEsperado, itemEncontrado)
        }
    }

    // =========================================================
    // getByCategory — busqueda por categoria
    // =========================================================

    @Test
    fun `getByCategory retorna solo items de la categoria solicitada`() {
        CosmeticCategory.entries.forEach { categoria ->
            val items = PrideCatalog.getByCategory(categoria)
            items.forEach { item ->
                assertEquals(
                    "Item '${item.id}' no pertenece a $categoria",
                    categoria,
                    item.category
                )
            }
        }
    }

    @Test
    fun `getByCategory no retorna items de otras categorias`() {
        val frames = PrideCatalog.getByCategory(CosmeticCategory.PROFILE_FRAME)

        frames.forEach { item ->
            assertTrue(
                "Item '${item.id}' de frame no deberia estar en TITLE_BADGE",
                item.category != CosmeticCategory.TITLE_BADGE
            )
        }
    }

    // =========================================================
    // Distribucion de tiers
    // =========================================================

    @Test
    fun `el catalogo tiene items de tier COMMON`() {
        val tieneCommon = todosLosItems.any { it.tier == CosmeticTier.COMMON }
        assertTrue(tieneCommon)
    }

    @Test
    fun `el catalogo tiene items de tier RARE`() {
        val tieneRare = todosLosItems.any { it.tier == CosmeticTier.RARE }
        assertTrue(tieneRare)
    }

    @Test
    fun `el catalogo tiene items de tier EPIC`() {
        val tieneEpic = todosLosItems.any { it.tier == CosmeticTier.EPIC }
        assertTrue(tieneEpic)
    }

    @Test
    fun `el catalogo tiene items de tier LEGENDARY`() {
        val tieneLegendary = todosLosItems.any { it.tier == CosmeticTier.LEGENDARY }
        assertTrue(tieneLegendary)
    }

    // =========================================================
    // Precios positivos en items de compra
    // =========================================================

    @Test
    fun `items con PurchaseWithCoins tienen precio mayor a 0`() {
        todosLosItems
            .filter { it.unlockCondition is UnlockCondition.PurchaseWithCoins }
            .forEach { item ->
                val precio = (item.unlockCondition as UnlockCondition.PurchaseWithCoins).price
                assertTrue("Item '${item.id}' tiene precio en monedas de $precio (debe ser > 0)", precio > 0)
            }
    }

    @Test
    fun `items con PurchaseWithGems tienen precio mayor a 0`() {
        todosLosItems
            .filter { it.unlockCondition is UnlockCondition.PurchaseWithGems }
            .forEach { item ->
                val precio = (item.unlockCondition as UnlockCondition.PurchaseWithGems).price
                assertTrue("Item '${item.id}' tiene precio en gemas de $precio (debe ser > 0)", precio > 0)
            }
    }

    // =========================================================
    // Items especificos del catalogo (smoke tests)
    // =========================================================

    @Test
    fun `frame_default existe y es Free y default`() {
        val item = PrideCatalog.getItem("frame_default")

        assertNotNull(item)
        assertTrue(item!!.isDefault)
        assertTrue(item.unlockCondition is UnlockCondition.Free)
    }

    @Test
    fun `title_legend requiere nivel 50`() {
        val item = PrideCatalog.getItem("title_legend")

        assertNotNull(item)
        val condicion = item!!.unlockCondition
        assertTrue(condicion is UnlockCondition.ReachLevel)
        assertEquals(50, (condicion as UnlockCondition.ReachLevel).level)
    }

    @Test
    fun `title_scholar requiere racha de 14 dias`() {
        val item = PrideCatalog.getItem("title_scholar")

        assertNotNull(item)
        val condicion = item!!.unlockCondition
        assertTrue(condicion is UnlockCondition.StreakMilestone)
        assertEquals(14, (condicion as UnlockCondition.StreakMilestone).days)
    }

    @Test
    fun `icon_pride_gold es el unico APP_ICON y requiere gemas`() {
        val iconItems = PrideCatalog.getByCategory(CosmeticCategory.APP_ICON)

        assertEquals(1, iconItems.size)
        assertTrue(iconItems.first().unlockCondition is UnlockCondition.PurchaseWithGems)
    }
}
