package com.quiz.data.repository

import com.quiz.data.datasource.SharedPreferencesLocalDataSource

class SharedPreferencesRepositoryImpl(
    private val sharedPreferencesLocalDataSource: SharedPreferencesLocalDataSource
) : SharedPreferencesRepository {

    override fun getPaymentDone(): Boolean = sharedPreferencesLocalDataSource.getPaymentDone()

    override fun setPaymentDone(value: Boolean) = sharedPreferencesLocalDataSource.setPaymentDone(value)

    override fun getPersonalRecord(): Int = sharedPreferencesLocalDataSource.getPersonalRecord()

    override fun setPersonalRecord(value: Int) = sharedPreferencesLocalDataSource.setPersonalRecord(value)
}
