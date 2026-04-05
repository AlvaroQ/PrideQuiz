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
    operator fun invoke(value: Int) {
        sharedPreferencesRepository.setPersonalRecord(value)
    }
}

class GetPersonalRecord(private val sharedPreferencesRepository: SharedPreferencesRepository) {
    operator fun invoke() = sharedPreferencesRepository.getPersonalRecord()
}
