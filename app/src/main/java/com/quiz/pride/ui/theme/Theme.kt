package com.quiz.pride.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Vibrant Pride Light Color Scheme
private val PrideLightColorScheme = lightColorScheme(
    // Primary - Vibrant Pink/Magenta with good contrast
    primary = Color(0xFFDB2777),
    onPrimary = White,
    primaryContainer = Color(0xFFFCE7F3),
    onPrimaryContainer = Color(0xFF831843),

    // Secondary - Electric Purple with good contrast
    secondary = Color(0xFF7C3AED),
    onSecondary = White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF4C1D95),

    // Tertiary - Vibrant Cyan with good contrast
    tertiary = Color(0xFF0891B2),
    onTertiary = White,
    tertiaryContainer = Color(0xFFCFFAFE),
    onTertiaryContainer = Color(0xFF164E63),

    // Background & Surface
    background = LightBackground,
    onBackground = TextOnLight,
    surface = LightSurface,
    onSurface = TextOnLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = DarkGray,

    // Error
    error = ResponseFail,
    onError = White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),

    // Outline
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB),

    // Inverse
    inverseSurface = Color(0xFF1F2937),
    inverseOnSurface = White,
    inversePrimary = Color(0xFFF9A8D4),

    scrim = Overlay
)

// Vibrant Pride Dark Color Scheme
private val PrideDarkColorScheme = darkColorScheme(
    // Primary - Deeper Pink for dark mode
    primary = Color(0xFFBE185D),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF831843),
    onPrimaryContainer = Color(0xFFFCE7F3),

    // Secondary - Deeper Purple for dark mode
    secondary = Color(0xFF7C3AED),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF5B21B6),
    onSecondaryContainer = Color(0xFFEDE9FE),

    // Tertiary - Deeper Cyan for dark mode
    tertiary = Color(0xFF0891B2),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF164E63),
    onTertiaryContainer = Color(0xFFCFFAFE),

    // Background & Surface - Deep colors for vibrancy
    background = DarkBackground,
    onBackground = TextOnDark,
    surface = DarkSurface,
    onSurface = TextOnDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD1D5DB),

    // Error
    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFEE2E2),

    // Outline
    outline = Color(0xFF4B5563),
    outlineVariant = Color(0xFF374151),

    // Inverse
    inverseSurface = Color(0xFFF3F4F6),
    inverseOnSurface = Color(0xFF1F2937),
    inversePrimary = Color(0xFFEC4899),

    scrim = Color(0xCC000000)
)

@Composable
fun PrideQuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled by default to show Pride colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic colors are available on Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> PrideDarkColorScheme
        else -> PrideLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PrideTypography,
        shapes = PrideShapes,
        content = content
    )
}
