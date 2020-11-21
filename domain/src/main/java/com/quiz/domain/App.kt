package com.quiz.domain

data class App(
    var image: String? = "",
    var localeName: Name? = null,
    var localeDescription: Name? = null,
    var url: String? = "",
    var priority: Int = 0
)