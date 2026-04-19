@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.quiz.pride.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Esquema de alto contraste oscuro — WCAG AAA (ratio minimo 7:1)
private val HighContrastDarkColorScheme = darkColorScheme(
    primary = HighContrastDarkPrimary,
    onPrimary = HighContrastDarkOnPrimary,
    primaryContainer = HighContrastDarkPrimaryContainer,
    onPrimaryContainer = HighContrastDarkOnPrimaryContainer,

    secondary = HighContrastDarkSecondary,
    onSecondary = HighContrastDarkOnSecondary,
    secondaryContainer = HighContrastDarkSecondaryContainer,
    onSecondaryContainer = HighContrastDarkOnSecondaryContainer,

    tertiary = HighContrastDarkTertiary,
    onTertiary = HighContrastDarkOnTertiary,
    tertiaryContainer = HighContrastDarkTertiaryContainer,
    onTertiaryContainer = HighContrastDarkOnTertiaryContainer,

    background = HighContrastDarkBackground,
    onBackground = HighContrastDarkOnBackground,
    surface = HighContrastDarkSurface,
    onSurface = HighContrastDarkOnSurface,
    surfaceVariant = HighContrastDarkSurfaceVariant,
    onSurfaceVariant = HighContrastDarkOnSurface,

    error = HighContrastDarkError,
    onError = Black,
    errorContainer = HighContrastDarkErrorContainer,
    onErrorContainer = HighContrastDarkError,

    outline = White,
    outlineVariant = HighContrastDarkOutlineVariant,

    inverseSurface = White,
    inverseOnSurface = Black,
    inversePrimary = HighContrastLightPrimary,

    scrim = Black
)

// Esquema de alto contraste claro — WCAG AAA (ratio minimo 7:1)
private val HighContrastLightColorScheme = lightColorScheme(
    primary = HighContrastLightPrimary,
    onPrimary = HighContrastLightOnPrimary,
    primaryContainer = HighContrastLightPrimaryContainer,
    onPrimaryContainer = HighContrastLightOnPrimaryContainer,

    secondary = HighContrastLightSecondary,
    onSecondary = HighContrastLightOnSecondary,
    secondaryContainer = HighContrastLightSecondaryContainer,
    onSecondaryContainer = HighContrastLightOnSecondaryContainer,

    tertiary = HighContrastLightTertiary,
    onTertiary = HighContrastLightOnTertiary,
    tertiaryContainer = HighContrastLightTertiaryContainer,
    onTertiaryContainer = HighContrastLightOnTertiaryContainer,

    background = HighContrastLightBackground,
    onBackground = HighContrastLightOnBackground,
    surface = HighContrastLightSurface,
    onSurface = HighContrastLightOnSurface,
    surfaceVariant = HighContrastLightSurfaceVariant,
    onSurfaceVariant = HighContrastLightOnSurface,

    error = HighContrastLightError,
    onError = White,
    errorContainer = HighContrastLightErrorContainer,
    onErrorContainer = HighContrastLightOnErrorContainer,

    outline = Black,
    outlineVariant = HighContrastLightOutlineVariant,

    inverseSurface = Black,
    inverseOnSurface = White,
    inversePrimary = HighContrastDarkPrimary,

    scrim = Overlay
)

// Pride Light Color Scheme — rosa claro y aireado (Pink50 background, Pink400 primary)
private val PrideLightColorScheme = lightColorScheme(
    // Primary - Rosa vibrante (identidad light theme)
    primary = Pink400,
    onPrimary = White,
    primaryContainer = Pink50,
    onPrimaryContainer = Pink700,

    // Secondary - Morado (Purple Scale)
    secondary = Purple600,
    onSecondary = White,
    secondaryContainer = Purple100,
    onSecondaryContainer = Purple900,

    // Tertiary - Cian (Cyan Scale)
    tertiary = Cyan600,
    onTertiary = White,
    tertiaryContainer = Cyan100,
    onTertiaryContainer = Cyan900,

    // Background & Surface (rosa claro)
    background = LightBackground,           // Pink50
    onBackground = TextOnLight,             // Casi negro rosa (contraste 17:1)
    surface = LightSurface,                 // Blanco
    onSurface = TextOnLight,
    surfaceVariant = LightSurfaceVariant,   // Pink100
    onSurfaceVariant = DarkGray,

    // Error
    error = ResponseFail,
    onError = White,
    errorContainer = Red100,
    onErrorContainer = Red900,

    // Outline
    outline = PinkOutlineLight,
    outlineVariant = PinkOutlineVariantLight,

    // Inverse
    inverseSurface = DarkPurpleSurface,
    inverseOnSurface = White,
    inversePrimary = PinkInverseLight,

    scrim = Overlay
)

// Pride Dark Color Scheme — morado (identidad principal dark)
private val PrideDarkColorScheme = darkColorScheme(
    // Primary - Morado vibrante (Purple Scale)
    primary = Purple600,
    onPrimary = White,
    primaryContainer = Purple800,
    onPrimaryContainer = Purple100,

    // Secondary - Rosa vibrante (intercambio con primary)
    secondary = Pink400,
    onSecondary = Pink900,
    secondaryContainer = Pink700,
    onSecondaryContainer = Pink100,

    // Tertiary - Cian (Cyan Scale)
    tertiary = Cyan300,
    onTertiary = Cyan950,
    tertiaryContainer = Cyan900,
    onTertiaryContainer = Cyan100,

    // Background & Surface - Morado muy oscuro
    background = DarkBackground,            // DarkPurpleBackground
    onBackground = TextOnDark,              // Blanco violeta
    surface = DarkSurface,                  // DarkPurpleSurface
    onSurface = TextOnDark,
    surfaceVariant = DarkSurfaceVariant,    // DarkPurpleSurfaceVariant
    onSurfaceVariant = Purple200,           // Violeta claro legible

    // Error
    error = Red400,
    onError = Red950,
    errorContainer = Red900,
    onErrorContainer = Red100,

    // Outline
    outline = PurpleOutlineDark,
    outlineVariant = PurpleOutlineVariantDark,

    // Inverse
    inverseSurface = Pink50,
    inverseOnSurface = DarkPurpleBackground,
    inversePrimary = Purple300,

    scrim = ScrimDark
)

@Composable
fun PrideQuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled by default to show Pride colors
    highContrast: Boolean = false,
    largeText: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Alto contraste tiene prioridad sobre dynamic colors
        highContrast && darkTheme -> HighContrastDarkColorScheme
        highContrast && !darkTheme -> HighContrastLightColorScheme
        // Dynamic colors disponibles en Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> PrideDarkColorScheme
        else -> PrideLightColorScheme
    }

    val typography = if (largeText) PrideLargeTypography else PrideTypography

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = PrideShapes,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}
