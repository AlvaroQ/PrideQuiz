package com.quiz.pride.application

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.quiz.pride.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PrideApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        initializeKoin()
        initializeFirebaseAuth()
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
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
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

    private fun initializeFirebaseAuth() {
        if (Firebase.auth.currentUser == null) {
            Firebase.auth.signInAnonymously()
        }
    }
}