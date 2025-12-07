package com.quiz.pride.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val PrideShapes = Shapes(
    // Extra small - for small chips, badges
    extraSmall = RoundedCornerShape(4.dp),

    // Small - for small cards, buttons
    small = RoundedCornerShape(8.dp),

    // Medium - for cards, dialogs
    medium = RoundedCornerShape(16.dp),

    // Large - for bottom sheets, large cards
    large = RoundedCornerShape(24.dp),

    // Extra large - for full screen dialogs
    extraLarge = RoundedCornerShape(32.dp)
)

// Custom shapes for specific components
val CardShape = RoundedCornerShape(16.dp)
val ButtonShape = RoundedCornerShape(16.dp)
val DialogShape = RoundedCornerShape(24.dp)
val AnswerButtonShape = RoundedCornerShape(16.dp)
val TopBarShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
