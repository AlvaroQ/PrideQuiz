package com.quiz.data.repository

interface SharedPreferencesRepository {
    fun getPaymentDone(): Boolean
    fun setPaymentDone(value: Boolean)
    // gameMode: identifica el modo de juego para el record personal.
    // String vacio usa la clave legacy "personal_record" para compatibilidad.
    fun getPersonalRecord(gameMode: String = ""): Int
    fun setPersonalRecord(value: Int, gameMode: String = "")
}
