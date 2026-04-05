package com.quiz.pride.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Composable reutilizable que renderiza un banner de AdMob (320x50).
 * Maneja el ciclo de vida del AdView via DisposableEffect.
 *
 * El adUnitId ya viene con el ID correcto segun el buildType (debug/release)
 * gracias a los resValue configurados en build.gradle.
 *
 * @param adUnitId ID de la unidad de anuncio. Usar R.string.BANNER_*
 * @param modifier Modifier para personalizar el contenedor
 */
@Composable
fun BannerAdView(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Crear el AdView una sola vez y retenerlo entre recomposiciones
    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            this.adUnitId = adUnitId
        }
    }

    // Destruir el AdView cuando el composable sale de la composicion
    DisposableEffect(adView) {
        adView.loadAd(AdRequest.Builder().build())
        onDispose {
            adView.destroy()
        }
    }

    // Pausar/reanudar el AdView segun el ciclo de vida de la Activity
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                Lifecycle.Event.ON_RESUME -> adView.resume()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { adView },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
