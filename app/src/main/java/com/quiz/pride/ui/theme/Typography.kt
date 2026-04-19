package com.quiz.pride.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.quiz.pride.R

// Pride font families
private val PrideDisplayFontFamily = FontFamily(
    Font(R.font.dynapuff_condensed_medium, FontWeight.Medium),
    Font(R.font.dynapuff_condensed_bold, FontWeight.Bold)
)

private val PrideHeadlineFontFamily = FontFamily(
    Font(R.font.dynapuff_semicondensed_regular, FontWeight.Normal),
    Font(R.font.dynapuff_semicondensed_medium, FontWeight.Medium),
    Font(R.font.dynapuff_semicondensed_semibold, FontWeight.SemiBold),
    Font(R.font.dynapuff_semicondensed_bold, FontWeight.Bold)
)

private val PrideBodyFontFamily = FontFamily(
    Font(R.font.dynapuff_regular, FontWeight.Normal),
    Font(R.font.dynapuff_medium, FontWeight.Medium),
    Font(R.font.dynapuff_semibold, FontWeight.SemiBold),
    Font(R.font.dynapuff_bold, FontWeight.Bold)
)

private val PrideLabelFontFamily = FontFamily(
    Font(R.font.dynapuff_condensed_regular, FontWeight.Normal),
    Font(R.font.dynapuff_condensed_medium, FontWeight.Medium),
    Font(R.font.dynapuff_condensed_semibold, FontWeight.SemiBold),
    Font(R.font.dynapuff_condensed_bold, FontWeight.Bold)
)

// Typography
val PrideTypography = Typography(
    // Display styles - hero titles / key celebratory numbers
    displayLarge = TextStyle(
        fontFamily = PrideDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    displayMedium = TextStyle(
        fontFamily = PrideDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    displaySmall = TextStyle(
        fontFamily = PrideDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),

    // Headline styles - screen and section headings
    headlineLarge = TextStyle(
        fontFamily = PrideHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PrideHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PrideHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),

    // Title styles - cards, tiles and highlighted labels
    titleLarge = TextStyle(
        fontFamily = PrideHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PrideHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PrideHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),

    // Body styles - descriptive and supporting copy
    bodyLarge = TextStyle(
        fontFamily = PrideBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PrideBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PrideBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),

    // Label styles - buttons, chips, badges and compact UI text
    labelLarge = TextStyle(
        fontFamily = PrideLabelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PrideLabelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PrideLabelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

// Tipografia de texto grande: +4sp en cada estilo para mayor accesibilidad
val PrideLargeTypography = Typography(
    // Display styles
    displayLarge = TextStyle(
        fontFamily = PrideDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    displayMedium = TextStyle(
        fontFamily = PrideDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    displaySmall = TextStyle(
        fontFamily = PrideDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),

    // Headline styles
    headlineLarge = TextStyle(
        fontFamily = PrideHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PrideHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PrideHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),

    // Title styles
    titleLarge = TextStyle(
        fontFamily = PrideHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PrideHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PrideHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),

    // Body styles
    bodyLarge = TextStyle(
        fontFamily = PrideBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PrideBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PrideBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),

    // Label styles
    labelLarge = TextStyle(
        fontFamily = PrideLabelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PrideLabelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PrideLabelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    )
)
