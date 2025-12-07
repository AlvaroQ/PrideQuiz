package com.quiz.pride.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Purple700,
    onPrimary = White,
    primaryContainer = PurpleLight,
    onPrimaryContainer = TextColorDark,

    secondary = Brown,
    onSecondary = White,
    secondaryContainer = BrownLight,
    onSecondaryContainer = White,

    tertiary = PurpleLight,
    onTertiary = TextColorDark,
    tertiaryContainer = GradientPointsTop,
    onTertiaryContainer = TextColorDark,

    background = White,
    onBackground = TextColorDark,

    surface = White,
    onSurface = TextColorDark,
    surfaceVariant = GradientPointsTop,
    onSurfaceVariant = TextColorDark,

    error = ResponseFail,
    onError = White,
    errorContainer = ResponseFail.copy(alpha = 0.1f),
    onErrorContainer = ResponseFail,

    outline = LightGray,
    outlineVariant = LightGray.copy(alpha = 0.5f),

    scrim = SemiTransparent
)

private val DarkColorScheme = darkColorScheme(
    primary = PurpleLight,
    onPrimary = DarkBackground,
    primaryContainer = Purple700,
    onPrimaryContainer = White,

    secondary = BrownLight,
    onSecondary = DarkBackground,
    secondaryContainer = Brown,
    onSecondaryContainer = White,

    tertiary = GradientTop,
    onTertiary = DarkBackground,
    tertiaryContainer = Purple700,
    onTertiaryContainer = White,

    background = DarkBackground,
    onBackground = White,

    surface = DarkSurface,
    onSurface = White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = White.copy(alpha = 0.8f),

    error = ResponseFail,
    onError = White,
    errorContainer = ResponseFail.copy(alpha = 0.2f),
    onErrorContainer = ResponseFail,

    outline = LightGray.copy(alpha = 0.5f),
    outlineVariant = LightGray.copy(alpha = 0.3f),

    scrim = Black.copy(alpha = 0.6f)
)

@Composable
fun PrideQuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic colors are available on Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PrideTypography,
        shapes = PrideShapes,
        content = content
    )
}
