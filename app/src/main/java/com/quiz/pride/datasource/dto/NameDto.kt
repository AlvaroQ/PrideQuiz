package com.quiz.pride.datasource.dto

import com.quiz.domain.Name

data class NameDto(
    var ES: String = "",
    var EN: String = "",
    var DE: String = "",
    var IT: String = "",
    var FR: String = "",
    var PT: String = ""
) {
    fun toDomain(): Name = Name(
        ES = ES,
        EN = EN,
        DE = DE,
        IT = IT,
        FR = FR,
        PT = PT
    )
}
