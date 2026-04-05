package com.quiz.data.repository

interface SharedPreferencesRepository {
    fun getPaymentDone(): Boolean
    fun setPaymentDone(value: Boolean)
    fun getPersonalRecord(): Int
    fun setPersonalRecord(value: Int)
}
