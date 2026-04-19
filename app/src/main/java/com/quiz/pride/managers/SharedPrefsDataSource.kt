@file:Suppress("DEPRECATION")

package com.quiz.pride.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.quiz.data.datasource.SharedPreferencesLocalDataSource
import timber.log.Timber

/*
 * NOTA sobre @file:Suppress("DEPRECATION"):
 *
 * `EncryptedSharedPreferences` y `MasterKey` estan marcados como @Deprecated en
 * androidx.security:security-crypto:1.1.0 ya que Google aun NO ha publicado un
 * reemplazo oficial equivalente. Las APIs siguen funcionando y son seguras.
 *
 * Se mantiene esta capa porque `PAYMENT_DONE` almacena el estado de compra IAP
 * y el cifrado aplicativo aporta defensa en profundidad frente a Android FBE.
 * Eliminar el cifrado obligaria a los usuarios existentes a hacer "Restore
 * purchases" para recuperar su estado, lo cual es una decision de producto.
 *
 * Revisar cuando Google publique el reemplazo oficial recomendado.
 */
open class SharedPrefsDataSource(context: Context) : SharedPreferencesLocalDataSource {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)

    private val encryptedPreferences: SharedPreferences? = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "pride_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Timber.e(e, "Failed to create EncryptedSharedPreferences, falling back to regular")
        FirebaseCrashlytics.getInstance().recordException(e)
        null
    }

    private val securePrefs: SharedPreferences
        get() = encryptedPreferences ?: sharedPreferences

    init {
        migratePaymentDoneToEncrypted()
    }

    private fun migratePaymentDoneToEncrypted() {
        if (encryptedPreferences == null) return
        val oldValue = sharedPreferences.getBoolean(PAYMENT_DONE, false)
        if (oldValue && !encryptedPreferences.contains(PAYMENT_DONE)) {
            encryptedPreferences.edit { putBoolean(PAYMENT_DONE, true) }
            sharedPreferences.edit { remove(PAYMENT_DONE) }
        }
    }

    override fun getPaymentDone(): Boolean = securePrefs.getBoolean(PAYMENT_DONE, false)

    override fun setPaymentDone(value: Boolean) = securePrefs.edit { putBoolean(PAYMENT_DONE, value) }

    override fun getPersonalRecord(gameMode: String): Int {
        if (gameMode.isEmpty()) return sharedPreferences.getInt(RECORD_PERSONAL, 0)

        val key = "${RECORD_PERSONAL}_$gameMode"
        val value = sharedPreferences.getInt(key, 0)

        if (value == 0) {
            val legacyValue = sharedPreferences.getInt(RECORD_PERSONAL, 0)
            if (legacyValue > 0) {
                sharedPreferences.edit { putInt(key, legacyValue) }
                return legacyValue
            }
        }
        return value
    }

    override fun setPersonalRecord(value: Int, gameMode: String) {
        val key = if (gameMode.isEmpty()) RECORD_PERSONAL else "${RECORD_PERSONAL}_$gameMode"
        sharedPreferences.edit { putInt(key, value) }
    }

    companion object {
        const val PAYMENT_DONE = "payment_done"
        const val RECORD_PERSONAL = "personal_record"
    }
}
