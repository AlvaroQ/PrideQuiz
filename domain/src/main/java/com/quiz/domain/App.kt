package com.quiz.domain

data class App(
    val image: String = "",
    val localeName: Name? = null,
    val localeDescription: Name? = null,
    val url: String = "",
    val priority: Int = 0
)