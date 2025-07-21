package com.quiz.pride.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.quiz.data.datasource.SharedPreferencesLocalDataSource

open class SharedPrefsDataSource (context: Context): SharedPreferencesLocalDataSource {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override var paymentDone: Boolean
        get() = sharedPreferences.getBoolean(PAYMENT_DONE, false)
        set(value) = sharedPreferences.edit { putBoolean(PAYMENT_DONE, value) }

    override var personalRecord: Int
        get() = sharedPreferences.getInt(RECORD_PERSONAL, 0)
        set(value) = sharedPreferences.edit { putInt(RECORD_PERSONAL, value) }

    companion object {
        const val PAYMENT_DONE = "payment_done"
        const val RECORD_PERSONAL = "personal_record"
    }
}