package com.quiz.pride.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.quiz.data.datasource.SharedPreferencesLocalDataSource

open class SharedPrefsDataSource(context: Context) : SharedPreferencesLocalDataSource {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun getPaymentDone(): Boolean = sharedPreferences.getBoolean(PAYMENT_DONE, false)

    override fun setPaymentDone(value: Boolean) = sharedPreferences.edit { putBoolean(PAYMENT_DONE, value) }

    override fun getPersonalRecord(): Int = sharedPreferences.getInt(RECORD_PERSONAL, 0)

    override fun setPersonalRecord(value: Int) = sharedPreferences.edit { putInt(RECORD_PERSONAL, value) }

    companion object {
        const val PAYMENT_DONE = "payment_done"
        const val RECORD_PERSONAL = "personal_record"
    }
}
