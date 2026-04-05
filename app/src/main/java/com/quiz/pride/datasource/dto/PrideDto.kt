package com.quiz.pride.datasource.dto

import com.quiz.domain.Pride

data class PrideDto(
    var name: NameDto? = null,
    var description: NameDto? = null,
    var flag: String = ""
) {
    fun toDomain(): Pride = Pride(
        name = name?.toDomain(),
        description = description?.toDomain(),
        flag = flag
    )
}
