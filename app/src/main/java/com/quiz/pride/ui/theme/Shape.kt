@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.quiz.pride.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val PrideShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    largeIncreased = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Custom shapes for specific components
val CardShape = RoundedCornerShape(16.dp)
val ButtonShape = RoundedCornerShape(16.dp)
val DialogShape = RoundedCornerShape(24.dp)
val AnswerButtonShape = RoundedCornerShape(16.dp)
val TopBarShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
