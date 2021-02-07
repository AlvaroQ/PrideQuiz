package com.quiz.domain

data class User(
    var name: String? = "",
    var points: String? = "",
    var score: Int? = 0,
    var userImage: String? = "",
    var timestamp: Long? = 0L
)