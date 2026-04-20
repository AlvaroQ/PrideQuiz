package com.quiz.pride.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.crashlytics
import com.quiz.pride.application.PrideApp
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.ConsentManager
import com.quiz.pride.managers.NetworkManager
import com.quiz.pride.managers.NetworkState
import com.quiz.pride.managers.OnboardingPreferences
import com.quiz.pride.managers.ThemeManager
import com.quiz.pride.navigation.OnboardingRoute
import com.quiz.pride.navigation.PrideNavGraph
import com.quiz.pride.navigation.SelectRoute
import com.quiz.pride.ui.components.OfflineBanner
import com.quiz.pride.ui.theme.PrideQuizTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val themeManager: ThemeManager by inject()
    private val analyticsManager: AnalyticsManager by inject()
    private val networkManager: NetworkManager by inject()
    private val consentManager: ConsentManager by inject()
    private val onboardingPreferences: OnboardingPreferences by inject()

    // Bandera para evitar mostrar el formulario de consentimiento mas de una vez por ciclo
    private var consentFormShownThisSession = false

    // Session tracking para analytics de engagement
    private var sessionStartTime = 0L
    private var gamesPlayedInSession = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        sessionStartTime = System.currentTimeMillis()

        // Initialize Analytics first with current user or anonymous
        initializeAnalytics()

        // Firebase Anonymous Authentication (will update Analytics uid when ready)
        initializeFirebaseAuth()

        // Iniciar flujo de consentimiento GDPR/UMP
        // El formulario se muestra si el usuario esta en la EEA y no ha dado consentimiento.
        // MobileAds.initialize() se llama DESPUES de que el consentimiento sea resuelto.
        initializeConsentAndAds()

        setContent {
            PrideAppRoot(
                themeManager = themeManager,
                networkManager = networkManager
            )
        }
    }

    /**
     * Composable raiz que encapsula los collectors de tema y red.
     * Al extraerlo aqui, los cambios de tema solo recomponen este wrapper
     * y no el contenido completo de la Activity.
     */
    @Composable
    private fun PrideAppRoot(
        themeManager: ThemeManager,
        networkManager: NetworkManager
    ) {
        // Collect theme state.
        // initialValue usa el tema del sistema como fallback para el primer frame,
        // evitando el FOUC (light -> dark) cuando DataStore aun no ha emitido.
        val systemIsDark = isSystemInDarkTheme()
        val isDarkMode by themeManager.isDarkMode.collectAsStateWithLifecycle(initialValue = systemIsDark)
        val isDynamicColors by themeManager.isDynamicColorsEnabled.collectAsStateWithLifecycle(initialValue = true)
        val isOnboardingCompleted by onboardingPreferences.isOnboardingCompleted.collectAsStateWithLifecycle(initialValue = true)
        // Collect accessibility settings
        val isHighContrast by themeManager.isHighContrastEnabled.collectAsStateWithLifecycle(initialValue = false)
        val isLargeText by themeManager.isLargeTextEnabled.collectAsStateWithLifecycle(initialValue = false)
        val coroutineScope = rememberCoroutineScope()

        // Espera al primer emit de DataStore antes de renderizar el NavGraph para
        // eliminar el FOUC cuando la preferencia guardada difiere del tema del sistema.
        // Mientras no este listo, el Surface raiz se muestra en el color correcto
        // y se confunde con la transicion desde el splash del sistema.
        var prefsReady by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            themeManager.isDarkMode.first()
            prefsReady = true
        }

        // Collect network state para el banner global
        val networkState by networkManager.networkState
            .collectAsStateWithLifecycle()

        // Determinar destino inicial segun el estado del onboarding
        val startDestination: Any = if (isOnboardingCompleted) {
            SelectRoute
        } else {
            OnboardingRoute
        }

        // Status bar + navigation bar adaptativas: iconos se ajustan segun tema.
        // Dark mode => fondo oscuro => iconos claros (isAppearanceLightStatusBars=false).
        // Light mode => fondo claro => iconos oscuros (isAppearanceLightStatusBars=true).
        val view = LocalView.current
        if (!view.isInEditMode) {
            DisposableEffect(isDarkMode) {
                val window = (view.context as Activity).window
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDarkMode
                controller.isAppearanceLightNavigationBars = !isDarkMode
                onDispose {}
            }
        }

        PrideQuizTheme(
            darkTheme = isDarkMode,
            dynamicColor = isDynamicColors,
            highContrast = isHighContrast,
            largeText = isLargeText
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                // Solo renderizar el NavGraph cuando las prefs esten listas,
                // para evitar cualquier flash de tema durante el arranque.
                if (prefsReady) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberNavController()
                        PrideNavGraph(
                            navController = navController,
                            startDestination = startDestination,
                            onOnboardingComplete = {
                                coroutineScope.launch {
                                    onboardingPreferences.setOnboardingCompleted(true)
                                }
                            }
                        )

                        // Banner offline global: visible en todas las pantallas
                        OfflineBanner(
                            isOffline = networkState is NetworkState.Unavailable,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }

    /**
     * Inicia el flujo UMP/GDPR:
     * 1. Solicita la actualizacion del estado de consentimiento
     * 2. Si se requiere formulario, lo muestra al usuario
     * 3. Al resolverse (con o sin consentimiento), inicializa MobileAds
     *
     * Este flujo es NO BLOQUEANTE: la UI de la app carga inmediatamente.
     * Los ads solo apareceran una vez que MobileAds este inicializado.
     */
    private fun initializeConsentAndAds() {
        if (consentFormShownThisSession) return
        consentFormShownThisSession = true

        consentManager.requestAndLoadConsentForm(
            activity = this,
            onConsentGathered = {
                // El consentimiento fue resuelto — inicializar MobileAds ahora
                (application as? PrideApp)?.initializeMobileAds()
            }
        )
    }

    override fun onStop() {
        super.onStop()
        // Enviar engagement de sesion cuando el usuario sale de la app
        if (sessionStartTime > 0) {
            val sessionDuration = System.currentTimeMillis() - sessionStartTime
            analyticsManager.analyticsSessionInfo(gamesPlayedInSession, sessionDuration)
        }
    }

    /** Llamar desde navigation cuando se inicia una partida para conteo de sesion */
    fun incrementGamesPlayed() {
        gamesPlayedInSession++
    }

    private fun initializeAnalytics() {
        // Initialize with current user or anonymous to prevent crashes
        val currentUid = Firebase.auth.currentUser?.uid ?: "anonymous"
        analyticsManager.uid = currentUid
    }

    private fun initializeFirebaseAuth() {
        val auth = Firebase.auth
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener { result ->
                    result.user?.uid?.let { uid ->
                        Firebase.crashlytics.setUserId(uid)
                        // Update Analytics with real uid
                        analyticsManager.uid = uid
                    }
                }
                .addOnFailureListener { exception ->
                    Firebase.crashlytics.recordException(exception)
                }
        } else {
            auth.currentUser?.uid?.let { uid ->
                Firebase.crashlytics.setUserId(uid)
            }
        }
    }
}
