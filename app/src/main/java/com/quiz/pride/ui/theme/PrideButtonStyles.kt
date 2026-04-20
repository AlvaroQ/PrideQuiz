package com.quiz.pride.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Identidad cromática concreta de un botón: gradiente, glow y color de contenido
 * (texto/icono). Se resuelve según el tema activo a través de [ButtonThemeVariant].
 */
data class ButtonGradient(
    val top: Color,
    val bottom: Color,
    val glow: Color,
    val contentColor: Color
) {
    val colors: List<Color> get() = listOf(top, bottom)
}

/**
 * Par de variantes (light / dark) de un mismo botón. Usa [current] dentro de
 * cualquier `@Composable` para obtener la variante adecuada al tema activo.
 */
data class ButtonThemeVariant(
    val light: ButtonGradient,
    val dark: ButtonGradient
) {
    @Composable
    @ReadOnlyComposable
    fun current(): ButtonGradient =
        if (MaterialTheme.colorScheme.background.luminance() < 0.5f) dark else light
}

/**
 * Catálogo central de identidades cromáticas de botones.
 *
 * Para cambiar la tonalidad o el color del texto de un botón, edita la entrada
 * correspondiente aquí. Los cambios se propagan automáticamente a todas las
 * pantallas (Select, SelectGame, Result, Onboarding, etc.) y a los
 * banners/cards que reutilizan estas tonalidades.
 *
 * Cada entrada tiene dos variantes:
 * - `light`: tonos más suaves y texto oscuro para tema claro.
 * - `dark`: tonos vibrantes y texto claro para tema oscuro.
 */
object PrideButtonStyles {

    /** Botón principal "Jugar" — rosa. */
    val Start = ButtonThemeVariant(
        light = ButtonGradient(
            top = Pink400,
            bottom = Pink500,
            glow = GlowPink,
            contentColor = OffWhiteInk
        ),
        dark = ButtonGradient(
            top = Pink500,
            bottom = Pink800,
            glow = GlowPink,
            contentColor = OffWhiteInk
        )
    )

    /** Botón "Aprender" — azul/cian. */
    val Learn = ButtonThemeVariant(
        light = ButtonGradient(
            top = Color(0xFFA7E2FF),
            bottom = Color(0xFF38BDF8),
            glow = GlowBlue,
            contentColor = OffBlackInk
        ),
        dark = ButtonGradient(
            top = Color(0xFF005CB0),
            bottom = Color(0xFF003C80),
            glow = GlowBlue,
            contentColor = OffWhiteInk
        )
    )

    /** Botón "Ajustes" — violeta a rosa. */
    val Settings = ButtonThemeVariant(
        light = ButtonGradient(
            top = Color(0xFFA4E5BB),
            bottom = Color(0xFF4ADE80),
            glow = NeonGreen.copy(alpha = 0.3f),
            contentColor = OffBlackInk
        ),
        dark = ButtonGradient(
            top = Color(0xFF256C44),
            bottom = Color(0xFF00521A),
            glow = NeonGreen.copy(alpha = 0.5f),
            contentColor = OffWhiteInk
        )
    )

    /** Dificultad "Normal" — verdes. */
    val Normal = ButtonThemeVariant(
        light = ButtonGradient(
            top = Color(0xFFA4E5BB),
            bottom = Color(0xFF4ADE80),
            glow = NeonGreen.copy(alpha = 0.3f),
            contentColor = OffBlackInk
        ),
        dark = ButtonGradient(
            top = Color(0xFF256C44),
            bottom = Color(0xFF00521A),
            glow = NeonGreen.copy(alpha = 0.5f),
            contentColor = OffWhiteInk
        )
    )

    /** Dificultad "Avanzado" — rosa intenso. */
    val Advance = ButtonThemeVariant(
        light = ButtonGradient(
            top = Pink300,
            bottom = Pink400,
            glow = NeonOrange.copy(alpha = 0.3f),
            contentColor = OffBlackInk
        ),
        dark = ButtonGradient(
            top = Pink500,
            bottom = Pink800,
            glow = GlowPink,
            contentColor = OffWhiteInk
        )
    )

    /** Modo "Contrarreloj" — cian. */
    val Timed = ButtonThemeVariant(
        light = ButtonGradient(
            top = Color(0xFFA7E2FF),
            bottom = Color(0xFF38BDF8),
            glow = GlowBlue,
            contentColor = OffBlackInk
        ),
        dark = ButtonGradient(
            top = Color(0xFF005CB0),
            bottom = Color(0xFF003C80),
            glow = GlowBlue,
            contentColor = OffWhiteInk
        )
    )

    /** Botón "Valorar" — dorado/ámbar (identidad de estrellas). */
    val Rate = ButtonThemeVariant(
        light = ButtonGradient(
            top = Color(0xFFFFE4A0),
            bottom = Color(0xFFFBBF24),
            glow = NeonOrange.copy(alpha = 0.3f),
            contentColor = OffBlackInk
        ),
        dark = ButtonGradient(
            top = Color(0xFF8A5A00),
            bottom = Color(0xFF5C3A00),
            glow = NeonOrange.copy(alpha = 0.5f),
            contentColor = OffWhiteInk
        )
    )
}
