package com.quiz.pride.application

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import okio.Path.Companion.toOkioPath
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.quiz.pride.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PrideApp : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        initializeKoin()
        // Firebase Auth se inicializa en MainActivity con callbacks completos
        // (Crashlytics userId, Analytics uid). No duplicar aqui.
        // MobileAds NO se inicializa aqui — el consentimiento UMP debe resolverse primero.
        // La inicializacion se delega a MainActivity despues de mostrar el formulario de consentimiento.
        configureAdMobRequestConfiguration()
    }

    /**
     * Singleton ImageLoader compartido por toda la app.
     * - Memory cache: 25% de la memoria disponible
     * - Disk cache: 50 MB en el directorio de cache interno
     * - crossfade: transicion suave al cargar imagenes
     * Coil lo invoca una unica vez y reutiliza la instancia.
     */
    override fun newImageLoader(context: android.content.Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(50L * 1024 * 1024) // 50 MB
                    .build()
            }
            .crossfade(true)
            .build()
    }

    /**
     * Configura la solicitud global de AdMob (test devices en DEBUG).
     * Es seguro llamar antes del consentimiento porque no solicita anuncios.
     * La inicializacion real de MobileAds ocurre en MainActivity
     * DESPUES de que el consentimiento GDPR sea resuelto via ConsentManager.
     */
    fun configureAdMobRequestConfiguration() {
        if (BuildConfig.DEBUG) {
            val testDeviceIds = listOf("87E31DEF5BA1FA89F463E054E4451C23")
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)
        }
    }

    /**
     * Inicializa MobileAds. Debe llamarse SOLO despues de que el consentimiento
     * GDPR haya sido resuelto (desde MainActivity via ConsentManager).
     */
    fun initializeMobileAds() {
        MobileAds.initialize(this)
    }

    private fun initializeKoin() {
        startKoin {
            androidContext(this@PrideApp)
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            modules(managerModule, dataSourceModule, repositoryModule, useCaseModule, viewModelModule)
        }
    }

}