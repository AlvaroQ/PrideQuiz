package com.quiz.domain

data class User(
    val uid: String = "",
    val name: String = "",
    val score: Int = 0,
    val userImage: String = "",
    val timestamp: Long = 0L
)
