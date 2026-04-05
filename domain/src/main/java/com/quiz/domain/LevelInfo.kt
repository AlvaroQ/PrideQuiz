package com.quiz.domain

data class LevelInfo(
    val level: Int,
    val title: String,
    val totalXp: Long,
    val xpInCurrentLevel: Long,
    val xpNeededForNextLevel: Long,
    val progressPercent: Float
)
