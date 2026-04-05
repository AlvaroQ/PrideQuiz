package com.quiz.pride.datasource.dto

import com.quiz.domain.App

data class AppDto(
    var image: String = "",
    var localeName: NameDto? = null,
    var localeDescription: NameDto? = null,
    var url: String = "",
    var priority: Int = 0
) {
    fun toDomain(): App = App(
        image = image,
        localeName = localeName?.toDomain(),
        localeDescription = localeDescription?.toDomain(),
        url = url,
        priority = priority
    )
}
