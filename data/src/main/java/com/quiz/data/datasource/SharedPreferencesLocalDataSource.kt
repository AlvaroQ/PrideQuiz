package com.quiz.data.datasource

interface SharedPreferencesLocalDataSource {

    fun getPaymentDone(): Boolean
    fun setPaymentDone(value: Boolean)
    // gameMode: identifica el modo de juego para el record personal.
    // String vacio usa la clave legacy "personal_record" para compatibilidad con versiones anteriores.
    fun getPersonalRecord(gameMode: String = ""): Int
    fun setPersonalRecord(value: Int, gameMode: String = "")
}
