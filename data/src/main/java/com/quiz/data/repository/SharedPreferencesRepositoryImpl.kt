package com.quiz.data.repository

import com.quiz.data.datasource.SharedPreferencesLocalDataSource

class SharedPreferencesRepositoryImpl(
    private val sharedPreferencesLocalDataSource: SharedPreferencesLocalDataSource
) : SharedPreferencesRepository {

    override var paymentDone: Boolean
        get() = sharedPreferencesLocalDataSource.paymentDone
        set(value) {
            sharedPreferencesLocalDataSource.paymentDone = value
        }

    override var personalRecord: Int
        get() = sharedPreferencesLocalDataSource.personalRecord
        set(value) {
            sharedPreferencesLocalDataSource.personalRecord = value
        }
}
