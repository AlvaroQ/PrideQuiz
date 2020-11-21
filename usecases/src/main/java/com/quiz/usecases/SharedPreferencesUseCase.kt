package com.quiz.usecases

import com.quiz.data.repository.SharedPreferencesRepository

class SetPaymentDone(private val sharedPreferencesRepository: SharedPreferencesRepository) {
    operator fun invoke(value: Boolean) {
        sharedPreferencesRepository.paymentDone = value
    }
}
class GetPaymentDone(private val sharedPreferencesRepository: SharedPreferencesRepository) {
    operator fun invoke() = sharedPreferencesRepository.paymentDone
}


class SetPersonalRecord(private val sharedPreferencesRepository: SharedPreferencesRepository) {
    operator fun invoke(value: Int) {
        sharedPreferencesRepository.personalRecord = value
    }
}
class GetPersonalRecord(private val sharedPreferencesRepository: SharedPreferencesRepository) {
    operator fun invoke() = sharedPreferencesRepository.personalRecord
}
