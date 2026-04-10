package com.quiz.pride.ui.settings

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.quiz.pride.BuildConfig
import com.quiz.pride.R
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.ui.components.BannerAdView
import com.quiz.pride.ui.components.PrideTopAppBar
import com.quiz.pride.ui.components.TrackScreenTime
import com.quiz.pride.ui.theme.PrideQuizTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMoreApps: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val prefsState by viewModel.prefsState.collectAsStateWithLifecycle()
    val analyticsManager: AnalyticsManager = koinInject()

    TrackScreenTime(AnalyticsManager.SCREEN_SETTINGS, analyticsManager)
    val context = LocalContext.current
    val activity = context as? Activity

    // Inicializar BillingClient cuando la pantalla entra en composicion y liberarlo al salir
    DisposableEffect(Unit) {
        viewModel.initBilling()
        onDispose {
            viewModel.releaseBilling()
        }
    }

    // Handle events del ViewModel
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                SettingsEvent.LaunchBillingFlow -> {
                    activity?.let { viewModel.launchBillingFlow(it) }
                }
                SettingsEvent.PurchaseSuccess -> {
                    Toast.makeText(context, R.string.purchase_success, Toast.LENGTH_SHORT).show()
                }
                SettingsEvent.PurchaseError -> {
                    Toast.makeText(context, R.string.purchase_error, Toast.LENGTH_SHORT).show()
                }
                SettingsEvent.ConsentReset -> {
                    Toast.makeText(context, "Consentimiento de anuncios reseteado", Toast.LENGTH_SHORT).show()
                }
                SettingsEvent.RestoreSuccess -> {
                    Toast.makeText(context, R.string.restore_success, Toast.LENGTH_SHORT).show()
                }
                SettingsEvent.RestoreNoPurchases -> {
                    Toast.makeText(context, R.string.restore_no_purchases, Toast.LENGTH_SHORT).show()
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
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Appearance Section
            SettingsSectionHeader(title = stringResource(R.string.settings_appearance))

            SettingsCard {
                SettingsSwitchItem(
                    icon = painterResource(R.drawable.ic_brightness_medium),
                    title = stringResource(R.string.settings_dark_mode),
                    subtitle = stringResource(R.string.settings_dark_mode_desc),
                    checked = prefsState.isDarkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsSwitchItem(
                        icon = painterResource(R.drawable.ic_color_lens),
                        title = stringResource(R.string.settings_dynamic_colors),
                        subtitle = stringResource(R.string.settings_dynamic_colors_desc),
                        checked = prefsState.isDynamicColorsEnabled,
                        onCheckedChange = { viewModel.setDynamicColorsEnabled(it) }
                    )
                }
            }

            // Sound Section
            SettingsSectionHeader(title = stringResource(R.string.settings_sound))

            SettingsCard {
                SettingsSwitchItem(
                    icon = painterResource(R.drawable.ic_volume_up),
                    title = stringResource(R.string.settings_sound_effects),
                    subtitle = stringResource(R.string.settings_sound_effects_desc),
                    checked = prefsState.isSoundEnabled,
                    onCheckedChange = { viewModel.setSoundEnabled(it) }
                )
            }

            // Accessibility Section
            SettingsSectionHeader(title = stringResource(R.string.settings_accessibility))

            SettingsCard {
                SettingsSwitchItem(
                    icon = painterResource(R.drawable.ic_contrast),
                    title = stringResource(R.string.settings_high_contrast),
                    subtitle = stringResource(R.string.settings_high_contrast_desc),
                    checked = prefsState.isHighContrastEnabled,
                    onCheckedChange = { viewModel.setHighContrastEnabled(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsSwitchItem(
                    icon = painterResource(R.drawable.ic_text_fields),
                    title = stringResource(R.string.settings_large_text),
                    subtitle = stringResource(R.string.settings_large_text_desc),
                    checked = prefsState.isLargeTextEnabled,
                    onCheckedChange = { viewModel.setLargeTextEnabled(it) }
                )
            }

            // Actions Section
            SettingsSectionHeader(title = stringResource(R.string.settings_actions))

            SettingsCard {
                // Remove Ads
                if (uiState.showAds) {
                    SettingsClickableItem(
                        icon = painterResource(R.drawable.ic_remove_circle_outline),
                        title = stringResource(R.string.settings_remove_ads),
                        subtitle = stringResource(R.string.settings_remove_ads_desc),
                        onClick = { viewModel.onRemoveAdsClick() }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Restore Purchases
                    SettingsClickableItem(
                        icon = rememberVectorPainter(Icons.Default.Refresh),
                        title = stringResource(R.string.settings_restore_purchases),
                        subtitle = stringResource(R.string.settings_restore_purchases_desc),
                        onClick = { viewModel.onRestorePurchases() }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // Rate App
                SettingsClickableItem(
                    icon = rememberVectorPainter(Icons.Default.Star),
                    title = stringResource(R.string.settings_rate_app),
                    subtitle = stringResource(R.string.settings_rate_app_desc),
                    onClick = {
                        viewModel.onRateClicked()
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
                    icon = rememberVectorPainter(Icons.Default.Share),
                    title = stringResource(R.string.share),
                    subtitle = stringResource(R.string.settings_share_desc),
                    onClick = {
                        viewModel.onShareClicked()
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
                    icon = painterResource(R.drawable.ic_more_horiz),
                    title = stringResource(R.string.more_apps),
                    subtitle = stringResource(R.string.settings_more_apps_desc),
                    onClick = onNavigateToMoreApps
                )
            }

            // Seccion DEBUG: solo visible en builds de desarrollo
            if (BuildConfig.DEBUG) {
                SettingsSectionHeader(title = "Debug")

                SettingsCard {
                    SettingsClickableItem(
                        icon = painterResource(R.drawable.ic_policy),
                        title = "Resetear consentimiento de anuncios",
                        subtitle = "Fuerza la reaparicion del formulario UMP/GDPR (solo DEBUG)",
                        onClick = { viewModel.resetAdConsent() }
                    )
                }
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
            } // cierra Column interior (scrolleable)

            // Banner publicitario al fondo (solo cuando el usuario no pago)
            if (uiState.showAds) {
                BannerAdView(
                    adUnitId = stringResource(R.string.BANNER_PREFERENCES)
                )
            }
        } // cierra Column exterior
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
    icon: Painter,
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
            painter = icon,
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
    icon: Painter,
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
                painter = icon,
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
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
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

// ============================================
// PREVIEWS
// ============================================

@Preview(showBackground = true, name = "SettingsSectionHeader - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "SettingsSectionHeader - Dark")
@Composable
private fun SettingsSectionHeaderPreview() {
    PrideQuizTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsSectionHeader(title = "Apariencia")
        }
    }
}

@Preview(showBackground = true, name = "SettingsSwitchItem - Activado - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "SettingsSwitchItem - Activado - Dark")
@Composable
private fun SettingsSwitchItemOnPreview() {
    PrideQuizTheme {
        SettingsCard {
            SettingsSwitchItem(
                icon = painterResource(R.drawable.ic_brightness_medium),
                title = "Modo oscuro",
                subtitle = "Cambia el tema de la aplicacion",
                checked = true,
                onCheckedChange = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "SettingsSwitchItem - Desactivado - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "SettingsSwitchItem - Desactivado - Dark")
@Composable
private fun SettingsSwitchItemOffPreview() {
    PrideQuizTheme {
        SettingsCard {
            SettingsSwitchItem(
                icon = painterResource(R.drawable.ic_volume_up),
                title = "Efectos de sonido",
                subtitle = "Activa o desactiva los sonidos del juego",
                checked = false,
                onCheckedChange = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "SettingsClickableItem - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "SettingsClickableItem - Dark")
@Composable
private fun SettingsClickableItemPreview() {
    PrideQuizTheme {
        SettingsCard {
            SettingsClickableItem(
                icon = rememberVectorPainter(Icons.Default.Star),
                title = "Valorar la app",
                subtitle = "Ayudanos con una resena en Google Play",
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "SettingsCard - Seccion Completa - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "SettingsCard - Seccion Completa - Dark")
@Composable
private fun SettingsCardFullSectionPreview() {
    PrideQuizTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsSectionHeader(title = "Apariencia")
            SettingsCard {
                SettingsSwitchItem(
                    icon = painterResource(R.drawable.ic_brightness_medium),
                    title = "Modo oscuro",
                    subtitle = "Cambia el tema de la aplicacion",
                    checked = true,
                    onCheckedChange = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsSwitchItem(
                    icon = painterResource(R.drawable.ic_color_lens),
                    title = "Colores dinamicos",
                    subtitle = "Usa los colores del sistema (Android 12+)",
                    checked = false,
                    onCheckedChange = {}
                )
            }
        }
    }
}
