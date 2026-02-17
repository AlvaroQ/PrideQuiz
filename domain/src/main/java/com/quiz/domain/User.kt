package com.quiz.domain

data class User(
    val name: String = "",
    val points: String = "",
    val score: Int = 0,
    val userImage: String = "",
    val timestamp: Long = 0L
)