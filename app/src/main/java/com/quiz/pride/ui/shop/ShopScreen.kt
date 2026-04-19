package com.quiz.pride.ui.shop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quiz.pride.R
import com.quiz.domain.cosmetics.CosmeticCategory
import com.quiz.domain.cosmetics.CosmeticTier
import com.quiz.domain.cosmetics.UnlockCondition
import com.quiz.pride.ui.components.AnimatedScreenBackground
import com.quiz.pride.ui.components.CurrencyDisplay
import com.quiz.pride.ui.components.LoadingIndicator
import com.quiz.pride.ui.components.PrideTopAppBar
import com.quiz.pride.ui.theme.DarkSurfaceVariant
import com.quiz.pride.ui.theme.GlowPurple
import com.quiz.pride.ui.theme.GradientPointsBottom
import com.quiz.pride.ui.theme.GradientPointsTop
import com.quiz.pride.ui.theme.NeonBlue
import com.quiz.pride.ui.theme.NeonGreen
import com.quiz.pride.ui.theme.NeonOrange
import com.quiz.pride.ui.theme.NeonPink
import com.quiz.pride.ui.theme.NeonPurple
import com.quiz.pride.ui.theme.NeonYellow
import com.quiz.pride.ui.theme.SettingsGradientBottom
import com.quiz.pride.ui.theme.SettingsGradientTop
import com.quiz.pride.ui.theme.White
import org.koin.androidx.compose.koinViewModel

@Composable
fun ShopScreen(
    onBack: () -> Unit,
    viewModel: ShopViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensaje de compra como snackbar
    LaunchedEffect(uiState.purchaseMessage) {
        uiState.purchaseMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onPurchaseMessageDismissed()
        }
    }

    Scaffold(
        topBar = {
            ShopTopBar(
                balance = uiState.balance,
                onBack = onBack
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = DarkSurfaceVariant,
                    contentColor = White,
                    actionColor = NeonPink
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AnimatedScreenBackground(
                orbColor1 = NeonPurple,
                orbColor2 = NeonPink
            ) {
                if (uiState.isLoading) {
                    LoadingIndicator()
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Pestanas de categorias
                        CategoryTabs(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = { viewModel.onCategorySelected(it) }
                        )

                        // Grilla de items
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = uiState.items,
                                key = { it.unlockable.id }
                            ) { shopItem ->
                                ShopItemCard(
                                    shopItem = shopItem,
                                    onTap = { viewModel.onItemTapped(shopItem) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogo de confirmacion de compra
    if (uiState.showPurchaseConfirmDialog) {
        uiState.pendingPurchaseItem?.let { item ->
            PurchaseConfirmDialog(
                shopItem = item,
                balance = uiState.balance,
                onConfirm = { viewModel.onPurchaseConfirmed() },
                onDismiss = { viewModel.onPurchaseDismissed() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShopTopBar(
    balance: com.quiz.domain.cosmetics.CurrencyBalance,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.shop_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    shadow = Shadow(
                        color = NeonPurple.copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 8f
                    )
                ),
                color = White
            )
        },
        navigationIcon = {
            TextButton(onClick = onBack) {
                Text(
                    text = stringResource(R.string.shop_back),
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonPink
                )
            }
        },
        actions = {
            CurrencyDisplay(
                balance = balance,
                modifier = Modifier.padding(end = 12.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun CategoryTabs(
    selectedCategory: CosmeticCategory,
    onCategorySelected: (CosmeticCategory) -> Unit
) {
    val categories = listOf(
        CosmeticCategory.PROFILE_FRAME to "Marcos",
        CosmeticCategory.TITLE_BADGE to "Titulos",
        CosmeticCategory.ANSWER_CARD_THEME to "Tarjetas",
        CosmeticCategory.CELEBRATION_ANIMATION to "Celebraciones",
        CosmeticCategory.APP_ICON to "Icono"
    )

    val selectedIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0)

    SecondaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.Transparent,
        contentColor = NeonPurple,
        edgePadding = 8.dp,
        divider = {}
    ) {
        categories.forEachIndexed { index, (category, label) ->
            val isSelected = index == selectedIndex
            Tab(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) Brush.linearGradient(
                                listOf(SettingsGradientTop, SettingsGradientBottom)
                            ) else Brush.linearGradient(
                                listOf(
                                    DarkSurfaceVariant.copy(alpha = 0.8f),
                                    DarkSurfaceVariant.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) White else White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShopItemCard(
    shopItem: ShopItem,
    onTap: () -> Unit
) {
    val tierColor = tierColor(shopItem.unlockable.tier)
    val isInteractable = shopItem.isOwned ||
        shopItem.unlockable.unlockCondition is UnlockCondition.PurchaseWithCoins ||
        shopItem.unlockable.unlockCondition is UnlockCondition.PurchaseWithGems

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (shopItem.isEquipped) 12.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = tierColor
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable(enabled = isInteractable) { onTap() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkSurfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (shopItem.isEquipped) 2.dp else 1.dp,
                        brush = if (shopItem.isEquipped) Brush.linearGradient(
                            listOf(tierColor, tierColor.copy(alpha = 0.6f))
                        ) else Brush.linearGradient(
                            listOf(tierColor.copy(alpha = 0.3f), tierColor.copy(alpha = 0.1f))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icono emoji de la categoria
                    Text(
                        text = categoryEmoji(shopItem.unlockable.category),
                        style = MaterialTheme.typography.displaySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Nombre del item
                    Text(
                        text = shopItem.unlockable.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            shadow = Shadow(
                                color = tierColor.copy(alpha = 0.4f),
                                offset = Offset(0f, 0f),
                                blurRadius = 6f
                            )
                        ),
                        color = White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Badge de tier
                    TierBadge(tier = shopItem.unlockable.tier, color = tierColor)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Precio o condicion
                    PriceOrCondition(
                        unlockCondition = shopItem.unlockable.unlockCondition,
                        isOwned = shopItem.isOwned
                    )

                    // Indicador de equipado
                    if (shopItem.isEquipped) {
                        Spacer(modifier = Modifier.height(6.dp))
                        EquippedBadge()
                    }
                }

                // Badge "TUYO" arriba a la derecha si es propio (pero no equipado)
                if (shopItem.isOwned && !shopItem.isEquipped) {
                    OwnedBadge(
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Composable
private fun TierBadge(tier: CosmeticTier, color: Color) {
    val label = when (tier) {
        CosmeticTier.COMMON -> "Comun"
        CosmeticTier.RARE -> "Raro"
        CosmeticTier.EPIC -> "Epico"
        CosmeticTier.LEGENDARY -> "Legendario"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.18f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun PriceOrCondition(
    unlockCondition: UnlockCondition,
    isOwned: Boolean
) {
    if (isOwned) {
        Text(
            text = stringResource(R.string.shop_unlocked),
            style = MaterialTheme.typography.labelMedium,
            color = NeonGreen
        )
        return
    }

    when (unlockCondition) {
        is UnlockCondition.PurchaseWithCoins -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "\uD83E\uDE99",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unlockCondition.price.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = GradientPointsBottom,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        is UnlockCondition.PurchaseWithGems -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "\uD83D\uDC8E",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unlockCondition.price.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        is UnlockCondition.ReachLevel -> {
            Text(
                text = stringResource(R.string.shop_unlock_level, unlockCondition.level),
                style = MaterialTheme.typography.labelMedium,
                color = NeonYellow
            )
        }
        is UnlockCondition.StreakMilestone -> {
            Text(
                text = stringResource(R.string.shop_unlock_streak_days, unlockCondition.days),
                style = MaterialTheme.typography.labelMedium,
                color = NeonOrange
            )
        }
        is UnlockCondition.ChallengeCount -> {
            Text(
                text = stringResource(R.string.shop_unlock_challenges, unlockCondition.count),
                style = MaterialTheme.typography.labelMedium,
                color = NeonPink
            )
        }
        is UnlockCondition.CompleteAchievement -> {
            Text(
                text = stringResource(R.string.shop_unlock_achievement),
                style = MaterialTheme.typography.labelMedium,
                color = NeonPurple
            )
        }
        is UnlockCondition.Free -> {
            Text(
                text = stringResource(R.string.shop_free),
                style = MaterialTheme.typography.labelMedium,
                color = NeonGreen
            )
        }
    }
}

@Composable
private fun EquippedBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(SettingsGradientTop, SettingsGradientBottom)
                )
            )
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = stringResource(R.string.shop_equipped),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = White
        )
    }
}

@Composable
private fun OwnedBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(bottomStart = 8.dp))
            .background(NeonGreen.copy(alpha = 0.85f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = stringResource(R.string.shop_owned),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
    }
}

@Composable
private fun PurchaseConfirmDialog(
    shopItem: ShopItem,
    balance: com.quiz.domain.cosmetics.CurrencyBalance,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val (priceText, canAfford) = when (val cond = shopItem.unlockable.unlockCondition) {
        is UnlockCondition.PurchaseWithCoins -> {
            "\uD83E\uDE99 ${cond.price} monedas" to (balance.coins >= cond.price)
        }
        is UnlockCondition.PurchaseWithGems -> {
            "\uD83D\uDC8E ${cond.price} gemas" to (balance.gems >= cond.price)
        }
        else -> "" to false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceVariant,
        title = {
            Text(
                text = stringResource(R.string.shop_buy_item, shopItem.unlockable.name),
                style = MaterialTheme.typography.titleMedium,
                color = White
            )
        },
        text = {
            Column {
                Text(
                    text = shopItem.unlockable.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.shop_price, priceText),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (canAfford) NeonGreen else NeonOrange
                )
                if (!canAfford) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.shop_insufficient_balance),
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonOrange
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = canAfford
            ) {
                Text(
                    text = stringResource(R.string.shop_buy),
                    color = if (canAfford) NeonGreen else White.copy(alpha = 0.4f)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.dialog_cancel), color = NeonPink)
            }
        }
    )
}

// ==========================================
// Funciones auxiliares
// ==========================================

private fun tierColor(tier: CosmeticTier): Color = when (tier) {
    CosmeticTier.COMMON -> Color(0xFF9CA3AF)    // gris
    CosmeticTier.RARE -> NeonBlue               // azul
    CosmeticTier.EPIC -> NeonPurple             // purpura
    CosmeticTier.LEGENDARY -> GradientPointsBottom // dorado
}

private fun categoryEmoji(category: CosmeticCategory): String = when (category) {
    CosmeticCategory.PROFILE_FRAME -> "\uD83D\uDDBC\uFE0F"          // cuadro
    CosmeticCategory.TITLE_BADGE -> "\uD83C\uDFF7\uFE0F"            // etiqueta
    CosmeticCategory.ANSWER_CARD_THEME -> "\uD83C\uDFA8"            // paleta
    CosmeticCategory.CELEBRATION_ANIMATION -> "\uD83C\uDF89"        // fiesta
    CosmeticCategory.APP_ICON -> "\uD83D\uDCF1"                     // celular
}
