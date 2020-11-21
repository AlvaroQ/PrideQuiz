package com.quiz.data.repository

import com.quiz.data.datasource.SharedPreferencesLocalDataSource


class SharedPreferencesRepository(private val sharedPreferencesLocalDataSource: SharedPreferencesLocalDataSource)  {

    var paymentDone: Boolean
        get() = sharedPreferencesLocalDataSource.paymentDone
        set(value) {
            sharedPreferencesLocalDataSource.paymentDone = value
        }

    var personalRecord: Int
        get() = sharedPreferencesLocalDataSource.personalRecord
        set(value) {
            sharedPreferencesLocalDataSource.personalRecord = value
        }
}