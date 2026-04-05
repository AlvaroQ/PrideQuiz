package com.quiz.data.datasource

interface SharedPreferencesLocalDataSource {

    fun getPaymentDone(): Boolean
    fun setPaymentDone(value: Boolean)
    fun getPersonalRecord(): Int
    fun setPersonalRecord(value: Int)
}
