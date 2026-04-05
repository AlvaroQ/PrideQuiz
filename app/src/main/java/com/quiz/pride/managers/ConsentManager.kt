package com.quiz.pride.managers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.quiz.pride.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Gestiona el consentimiento GDPR/UMP para usuarios de la EEA.
 *
 * Flujo de uso:
 * 1. Llamar [requestAndLoadConsentForm] desde MainActivity.onCreate()
 *    — solicita el estado de consentimiento y muestra el formulario si es necesario
 * 2. Una vez resuelto el consentimiento, el callback inicializa MobileAds
 * 3. Usar [canRequestAds] para verificar si se permite solicitar anuncios
 * 4. En DEBUG: [resetConsent] fuerza la reaparicion del formulario para testing
 *
 * El SDK de UMP maneja internamente la frecuencia de muestra del formulario.
 * Google recomienda inicializar UMP en cada arranque de la Activity.
 */
class ConsentManager(private val context: Context) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    // Estado observable: true cuando el consentimiento fue resuelto (o no requerido)
    private val _consentResolved = MutableStateFlow(false)
    val consentResolved: StateFlow<Boolean> = _consentResolved.asStateFlow()

    // Bandera para evitar solicitudes duplicadas durante el mismo ciclo de vida
    private val isFetchingConsent = AtomicBoolean(false)

    /**
     * Solicita la actualizacion del estado de consentimiento desde la Activity y,
     * si se requiere formulario, lo carga y muestra automaticamente.
     *
     * Debe llamarse desde MainActivity.onCreate() ANTES de inicializar MobileAds.
     * Este metodo es NO BLOQUEANTE: el formulario se muestra de forma asincrona.
     *
     * Al resolverse (formulario mostrado, ya existia consentimiento, o error),
     * llama [onConsentGathered] para que MobileAds pueda inicializarse.
     */
    fun requestAndLoadConsentForm(
        activity: Activity,
        onConsentGathered: () -> Unit
    ) {
        if (!isFetchingConsent.compareAndSet(false, true)) {
            return // Ya hay una solicitud en curso — evitar duplicados
        }

        val params = buildConsentRequestParams()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Update exitoso: el SDK sabe si necesita mostrar el formulario
                loadAndShowConsentFormIfRequired(activity) {
                    isFetchingConsent.set(false)
                    _consentResolved.value = true
                    onConsentGathered()
                }
            },
            { formError ->
                Log.w(TAG, "Error al actualizar consentimiento: ${formError.message}")
                isFetchingConsent.set(false)
                // Ante error, permitir ads (comportamiento defensivo recomendado por Google)
                _consentResolved.value = true
                onConsentGathered()
            }
        )
    }

    /**
     * Retorna true si el estado de consentimiento permite solicitar anuncios.
     * Esto incluye usuarios fuera de la EEA (consentimiento no requerido)
     * y usuarios en la EEA que otorgaron consentimiento.
     */
    fun canRequestAds(): Boolean = consentInformation.canRequestAds()

    /**
     * Resetea el estado de consentimiento para forzar la reaparicion del formulario.
     * SOLO disponible en builds DEBUG para testear el flujo de consentimiento.
     * En builds release este metodo no tiene efecto.
     */
    fun resetConsent() {
        if (BuildConfig.DEBUG) {
            consentInformation.reset()
            isFetchingConsent.set(false)
            _consentResolved.value = false
            Log.d(TAG, "Consentimiento reseteado — el formulario se mostrara en el proximo inicio")
        }
    }

    // =========================================================================
    // Implementacion privada
    // =========================================================================

    private fun buildConsentRequestParams(): ConsentRequestParameters {
        val paramsBuilder = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)

        // En DEBUG: simular geografia EEA para poder testear el formulario sin estar en Europa
        if (BuildConfig.DEBUG) {
            val debugSettings = ConsentDebugSettings.Builder(context)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId(DEBUG_TEST_DEVICE_ID)
                .build()
            paramsBuilder.setConsentDebugSettings(debugSettings)
        }

        return paramsBuilder.build()
    }

    private fun loadAndShowConsentFormIfRequired(
        activity: Activity,
        onComplete: () -> Unit
    ) {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
            if (formError != null) {
                Log.w(TAG, "Error al mostrar formulario UMP: ${formError.message}")
            }
            // Formulario mostrado (o no requerido) — siempre notificar para no bloquear ads
            onComplete()
        }
    }

    companion object {
        private const val TAG = "ConsentManager"

        // Hash del dispositivo de test para simular EEA en DEBUG.
        // Debe coincidir con el test device registrado en AdMob.
        private const val DEBUG_TEST_DEVICE_ID = "87E31DEF5BA1FA89F463E054E4451C23"
    }
}
