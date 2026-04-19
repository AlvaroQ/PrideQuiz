package com.quiz.domain.cosmetics

data class PlayerCosmetics(
    val unlockedIds: Set<String> = emptySet(),
    val equippedFrame: String = "frame_default",
    val equippedTitle: String = "title_default",
    val equippedCardTheme: String = "card_default",
    val equippedCelebration: String = "celebration_default",
    val equippedAppIcon: String = ""
)
