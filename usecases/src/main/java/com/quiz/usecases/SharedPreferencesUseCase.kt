package com.quiz.usecases

import com.quiz.data.repository.SharedPreferencesRepository

class SetPaymentDone(private val sharedPreferencesRepository: SharedPreferencesRepository) {
    operator fun invoke(value: Boolean) {
        sharedPreferencesRepository.setPaymentDone(value)
    }
}

class GetPaymentDone(private val sharedPreferencesRepository: SharedPreferencesRepository) {
    operator fun invoke() = sharedPreferencesRepository.getPaymentDone()
}

class SetPersonalRecord(private val sharedPreferencesRepository: SharedPreferencesRepository) {
    // gameMode: modo de juego para separar records por modo ("NORMAL", "ADVANCE", "EXPERT", "TIMED").
    // String vacio usa la clave legacy "personal_record" para compatibilidad con versiones anteriores.
    operator fun invoke(value: Int, gameMode: String = "") {
        sharedPreferencesRepository.setPersonalRecord(value, gameMode)
    }
}

class GetPersonalRecord(private val sharedPreferencesRepository: SharedPreferencesRepository) {
    // gameMode: modo de juego para separar records por modo ("NORMAL", "ADVANCE", "EXPERT", "TIMED").
    // String vacio usa la clave legacy "personal_record" para compatibilidad con versiones anteriores.
    operator fun invoke(gameMode: String = "") = sharedPreferencesRepository.getPersonalRecord(gameMode)
}
