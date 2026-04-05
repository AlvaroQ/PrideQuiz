package com.quiz.domain

data class XpGainResult(
    val xpGained: Long,
    val totalXp: Long,
    val oldLevel: Int,
    val newLevel: Int,
    val leveledUp: Boolean
)
