package com.quiz.pride.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.common.collect.ImmutableList
import com.quiz.pride.BuildConfig
import com.quiz.pride.R
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.ui.components.PrideTopAppBar
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

private const val REMOVE_AD = "remove_ad"

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMoreApps: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val isDynamicColorsEnabled by viewModel.isDynamicColorsEnabled.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    // Billing Client setup
    var billingClient by remember { mutableStateOf<BillingClient?>(null) }
    var isBillingReady by remember { mutableStateOf(false) }

    val purchasesUpdatedListener = remember {
        PurchasesUpdatedListener { billingResult, purchases ->
            when {
                billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null -> {
                    for (purchase in purchases) {
                        when (purchase.purchaseState) {
                            Purchase.PurchaseState.PURCHASED -> {
                                AnalyticsManager.analyticsScreenViewed("billing_purchase_ok")
                                viewModel.onPurchaseComplete()
                                // Acknowledge purchase
                                billingClient?.let { client ->
                                    val params = AcknowledgePurchaseParams.newBuilder()
                                        .setPurchaseToken(purchase.purchaseToken)
                                        .build()
                                    client.acknowledgePurchase(params) { _ -> }
                                }
                            }
                            Purchase.PurchaseState.PENDING -> {
                                AnalyticsManager.analyticsScreenViewed("billing_purchase_pending")
                            }
                            else -> {
                                AnalyticsManager.analyticsScreenViewed("billing_purchase_unspecified")
                            }
                        }
                    }
                }
                billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED -> {
                    AnalyticsManager.analyticsScreenViewed("billing_purchase_canceled")
                    viewModel.onPurchaseCancelled()
                }
                billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    AnalyticsManager.analyticsScreenViewed("billing_already_purchase")
                    viewModel.onPurchaseComplete()
                }
                else -> {
                    AnalyticsManager.analyticsScreenViewed("billing_purchase_error")
                    viewModel.onPurchaseError()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .setListener(purchasesUpdatedListener)
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isBillingReady = billingResult.responseCode == BillingClient.BillingResponseCode.OK
            }

            override fun onBillingServiceDisconnected() {
                isBillingReady = false
            }
        })

        onDispose {
            billingClient?.endConnection()
        }
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                SettingsEvent.LaunchBillingFlow -> {
                    if (isBillingReady && billingClient != null && activity != null) {
                        launchBillingFlow(billingClient!!, activity)
                    }
                }
                SettingsEvent.PurchaseSuccess -> {
                    Toast.makeText(context, R.string.purchase_success, Toast.LENGTH_SHORT).show()
                }
                SettingsEvent.PurchaseError -> {
                    Toast.makeText(context, R.string.purchase_error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            PrideTopAppBar(
                title = stringResource(R.string.settings),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Section
            SettingsSectionHeader(title = stringResource(R.string.settings_appearance))

            SettingsCard {
                SettingsSwitchItem(
                    icon = Icons.Default.BrightnessMedium,
                    title = stringResource(R.string.settings_dark_mode),
                    subtitle = stringResource(R.string.settings_dark_mode_desc),
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsSwitchItem(
                        icon = Icons.Default.ColorLens,
                        title = stringResource(R.string.settings_dynamic_colors),
                        subtitle = stringResource(R.string.settings_dynamic_colors_desc),
                        checked = isDynamicColorsEnabled,
                        onCheckedChange = { viewModel.setDynamicColorsEnabled(it) }
                    )
                }
            }

            // Sound Section
            SettingsSectionHeader(title = stringResource(R.string.settings_sound))

            SettingsCard {
                SettingsSwitchItem(
                    icon = Icons.Default.VolumeUp,
                    title = stringResource(R.string.settings_sound_effects),
                    subtitle = stringResource(R.string.settings_sound_effects_desc),
                    checked = isSoundEnabled,
                    onCheckedChange = { viewModel.setSoundEnabled(it) }
                )
            }

            // Actions Section
            SettingsSectionHeader(title = stringResource(R.string.settings_actions))

            SettingsCard {
                // Remove Ads
                if (uiState.showAds) {
                    SettingsClickableItem(
                        icon = Icons.Default.RemoveCircleOutline,
                        title = stringResource(R.string.settings_remove_ads),
                        subtitle = stringResource(R.string.settings_remove_ads_desc),
                        onClick = { viewModel.onRemoveAdsClick() }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // Rate App
                SettingsClickableItem(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.settings_rate_app),
                    subtitle = stringResource(R.string.settings_rate_app_desc),
                    onClick = {
                        AnalyticsManager.analyticsClicked(AnalyticsManager.BTN_RATE)
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("market://details?id=${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Share
                SettingsClickableItem(
                    icon = Icons.Default.Share,
                    title = stringResource(R.string.share),
                    subtitle = stringResource(R.string.settings_share_desc),
                    onClick = {
                        AnalyticsManager.analyticsClicked(AnalyticsManager.BTN_SHARE)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
                            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_message, 0))
                        }
                        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share)))
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // More Apps
                SettingsClickableItem(
                    icon = Icons.Default.MoreHoriz,
                    title = stringResource(R.string.more_apps),
                    subtitle = stringResource(R.string.settings_more_apps_desc),
                    onClick = onNavigateToMoreApps
                )
            }

            // About Section
            SettingsSectionHeader(title = stringResource(R.string.settings_about))

            SettingsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${stringResource(R.string.settings_version)} ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun launchBillingFlow(billingClient: BillingClient, activity: Activity) {
    val productList = ImmutableList.of(
        QueryProductDetailsParams.Product.newBuilder()
            .setProductId(REMOVE_AD)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
    )

    val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
        .setProductList(productList)
        .build()

    billingClient.queryProductDetailsAsync(queryProductDetailsParams) { _, result ->
        val productDetailsList = result.productDetailsList
        for (productDetails in productDetailsList) {
            if (productDetails.productId == REMOVE_AD) {
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()
                billingClient.launchBillingFlow(activity, flowParams)
                break
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        content()
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
