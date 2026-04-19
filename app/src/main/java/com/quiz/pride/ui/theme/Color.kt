package com.quiz.pride.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================
// PINK SCALE — Paleta maestra rosa (HSL derivada, hue ~335deg)
// De muy claro (50) a muy oscuro (950). Todas las pantallas y componentes
// consumen estos tonos via ColorScheme o variables nombradas abajo.
// ============================================

val Pink50  = Color(0xFFFFF1F5)   // Rosa casi blanco (light background)
val Pink100 = Color(0xFFFCE7F3)   // Rosa muy claro (surface variant light)
val Pink200 = Color(0xFFFBCFE8)   // Rosa suave
val Pink300 = Color(0xFFF9A8D4)   // Rosa medio claro
val Pink400 = Color(0xFFEC4899)   // Rosa vibrante
val Pink500 = Color(0xFFDB2777)   // Rosa saturado (primary light)
val Pink600 = Color(0xFFBE185D)   // Rosa profundo (primary dark container)
val Pink700 = Color(0xFF9D174D)   // Rosa oscuro
val Pink800 = Color(0xFF831843)   // Rosa muy oscuro
val Pink900 = Color(0xFF500724)   // Rosa casi negro (surface variant dark)
val Pink950 = Color(0xFF2E0616)   // Rosa tinte negro (surface dark)

// Dark backgrounds — negro con tinte rosa (S bajo, L muy bajo)
val DarkPinkBackground = Color(0xFF1A0812)   // Fondo dark theme (casi negro tintado rosa)
val DarkPinkSurface    = Color(0xFF241019)   // Surface elevada
val DarkPinkSurfaceVariant = Color(0xFF2E1624) // Cards, diálogos
val DarkPinkSurfaceElevated = Color(0xFF3A1C2D) // Máxima elevación

// Dark backgrounds — negro con tinte morado (dark theme principal)
val DarkPurpleBackground = Color(0xFF130A1F)     // Fondo dark theme (casi negro tintado violeta)
val DarkPurpleSurface = Color(0xFF1D1030)        // Surface elevada
val DarkPurpleSurfaceVariant = Color(0xFF261640) // Cards, diálogos
val DarkPurpleSurfaceElevated = Color(0xFF311C52) // Máxima elevación

// Text on pink surfaces
val PinkTextOnDark  = Color(0xFFFCE7F3)   // Blanco rosa (no blanco puro) sobre DarkPinkBackground
val PinkTextOnLight = Color(0xFF1F0812)   // Casi negro rosa sobre Pink50
val PurpleTextOnDark = Color(0xFFEDE9FE)  // Blanco violeta sobre DarkPurpleBackground

// Tintes rosa para outlines / inverse
val PinkOutlineLight = Color(0xFFE5BDCE)        // Rosa suave — outline light theme
val PinkOutlineVariantLight = Color(0xFFF3E2EB) // Rosa muy suave — outlineVariant light theme
val PinkInverseLight = Color(0xFFF9A8D4)        // Rosa pastel — inversePrimary light theme

// ============================================
// PURPLE SCALE — Paleta maestra morada (identidad dark theme, secondary light)
// De muy claro (50) a muy oscuro (950). Alineada a escala tipo Tailwind violet.
// ============================================

val Purple50  = Color(0xFFF5F3FF)   // Violeta casi blanco
val Purple100 = Color(0xFFEDE9FE)   // Violeta muy claro (secondaryContainer light, onPrimaryContainer dark)
val Purple200 = Color(0xFFDDD6FE)   // Violeta suave (onSurfaceVariant dark)
val Purple300 = Color(0xFFC4B5FD)   // Violeta claro (inversePrimary dark)
val Purple400 = Color(0xFF61558A)   // Violeta vibrante
val Purple500 = Color(0xFF8B5CF6)   // Violeta saturado
val Purple600 = Color(0xFFC9A9FF)   // Violeta profundo (primary dark, secondary light)
val Purple700 = Color(0xFF6D28D9)   // Morado oscuro
val Purple800 = Color(0xFF5B21B6)   // Morado muy oscuro (primaryContainer dark, secondaryContainer light)
val Purple900 = Color(0xFF4C1D95)   // Morado casi negro (onSecondaryContainer light)
val Purple950 = Color(0xFF2E1065)   // Morado tinte negro (onSecondary dark)

// Tintes morados para outlines
val PurpleOutlineDark = Color(0xFF6B4A8E)        // Morado grisáceo — outline dark theme
val PurpleOutlineVariantDark = Color(0xFF3E2A5E) // Morado oscuro grisáceo — outlineVariant dark theme

// ============================================
// CYAN / TEAL SCALE — tertiary en ambos temas
// ============================================

val Cyan100 = Color(0xFFCFFAFE)   // Cian muy claro (tertiaryContainer light, onTertiaryContainer dark)
val Cyan300 = Color(0xFF5EEAD4)   // Cian suave (tertiary dark)
val Cyan600 = Color(0xFF0891B2)   // Cian profundo (tertiary light)
val Cyan900 = Color(0xFF164E63)   // Cian oscuro (onTertiaryContainer light, tertiaryContainer dark)
val Cyan950 = Color(0xFF042F2E)   // Cian casi negro (onTertiary dark)

// ============================================
// RED SCALE — errores y feedback
// ============================================

val Red100 = Color(0xFFFEE2E2)    // Rojo muy claro (errorContainer light, onErrorContainer dark)
val Red400 = Color(0xFFF87171)    // Rojo suave (error dark)
val Red900 = Color(0xFF991B1B)    // Rojo oscuro (onErrorContainer light, errorContainer dark)
val Red950 = Color(0xFF450A0A)    // Rojo casi negro (onError dark)

// ============================================
// PRIDE RAINBOW COLORS — Bandera oficial (NO TOCAR)
// Se preserva PrideYellow porque es parte de la bandera de 6 franjas.
// Solo se usa en `RainbowColors` y `RainbowProgressFill`.
// ============================================

val PrideRed = Color(0xFFE40303)
val PrideOrange = Color(0xFFFF8C00)
val PrideYellow = Color(0xFFFFED00)
val PrideGreen = Color(0xFF008026)
val PrideBlue = Color(0xFF24408E)
val PridePurple = Color(0xFF732982)

// Progress Pride Colors (additional)
val PridePink = Color(0xFFFFAFC8)
val PrideLightBlue = Color(0xFF74D7EE)
val PrideBrown = Color(0xFF613915)
val PrideBlack = Color(0xFF000000)
val PrideWhite = Color(0xFFFFFFFF)

// Vibrant Neon Variants
val NeonPink = Color(0xFFFF6B9D)
val NeonPurple = Color(0xFFB24BF3)
val NeonBlue = Color(0xFF4FC3F7)
val NeonGreen = Color(0xFF69F0AE)
// Antes amarillo (#FFFF00) — ahora rosa caliente para flama/racha dentro de la paleta rosa.
// Se conserva el nombre por compatibilidad con llamantes existentes (streak, flame, accent).
val NeonYellow = Color(0xFFFF4D94)
val NeonOrange = Color(0xFFFF9100)

// ============================================
// GRADIENT PAIRS - For beautiful transitions
// ============================================

// Rainbow Gradient Stops (full spectrum — bandera oficial)
val RainbowColors = listOf(
    PrideRed,
    PrideOrange,
    PrideYellow,
    PrideGreen,
    PrideBlue,
    PridePurple
)

// Main App Gradient (Purple to Pink)
val GradientPrimaryStart = Color(0xFF667EEA)
val GradientPrimaryEnd = Pink400

// Vibrant Background Gradient — DARK theme (identidad principal)
val GradientBackgroundStart = Purple500          // Vivid Purple
val GradientBackgroundMid = Pink400
val GradientBackgroundMidWarm = Color(0xFF001357)
val GradientBackgroundEnd = Pink500

// Background Gradient — LIGHT theme (variante)
val GradientBackgroundStartLight = Purple100
val GradientBackgroundMidLight = Pink100
val GradientBackgroundMidWarmLight = Pink200
val GradientBackgroundEndLight = Pink300

// Game Screen Gradient — tinte morado profundo (alineado con dark theme)
val GradientGameTop = DarkPurpleBackground
val GradientGameBottom = DarkPurpleSurface

// Position/Ranking Gradient
val GradientPositionTop = Color(0xFFD946EF)   // Fuchsia (fuera de la escala Purple)
val GradientPositionBottom = Purple500        // Violet

// Points Display Gradient — rosa claro a rosa saturado (antes amarillo)
val GradientPointsTop = Pink200               // Rosa suave
val GradientPointsBottom = Pink400            // Rosa vibrante

// ============================================
// UI COLORS
// ============================================

// Response Feedback
val ResponseCorrect = Color(0xFF22C55E)       // Vibrant Green
val ResponseFail = Color(0xFFEF4444)          // Vibrant Red (fuera de la escala Red — más saturado)

// Basic Colors
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val LightGray = Color(0xFFF3F4F6)
val DarkGray = Color(0xFF6B7280)

// Text Colors
val TextOnDark = PurpleTextOnDark
val TextOnLight = PinkTextOnLight
val TextMuted = Color(0xFF9CA3AF)

// ============================================
// DARK THEME COLORS (tinte morado — identidad principal dark)
// ============================================

val DarkBackground = DarkPurpleBackground
val DarkSurface = DarkPurpleSurface
val DarkSurfaceVariant = DarkPurpleSurfaceVariant
val DarkSurfaceElevated = DarkPurpleSurfaceElevated

// ============================================
// LIGHT THEME COLORS (tinte rosa)
// ============================================

val LightBackground = Pink50
val LightSurface = White
val LightSurfaceVariant = Pink100

// ============================================
// RANK COLORS — Podio (oro/plata/bronce, convención universal)
// ============================================

val RankGold   = Color(0xFFFFD700)   // 1er puesto — oro
val RankGoldGlow   = Color(0x80FFD700)
val RankSilver = Color(0xFFE8E8E8)   // 2do puesto — plata
val RankSilverGlow = Color(0x80E8E8E8)
val RankBronze = Color(0xFFE8943A)   // 3er puesto — bronce
val RankBronzeGlow = Color(0x99E8943A)

// ============================================
// SPECIAL EFFECTS
// ============================================

val GlassWhite = Color(0x33FFFFFF)            // 20% white
val GlassDark = Color(0x33000000)             // 20% black
val Shimmer = Color(0x66FFFFFF)               // For shimmer effects
val Overlay = Color(0x80000000)               // 50% black overlay

// Glow Colors (for neon effects) — 25% alpha sobre Pink400/Purple400/Blue
val GlowPink = Color(0x80EC4899)              // Pink400 a 25%
val GlowPurple = Color(0x40A855F7)            // Purple400 a 25%
val GlowBlue = Color(0x404FACFE)

// Premium glass effects
val GlassHighlight = Color(0x1AFFFFFF)    // 10% white for glass top highlight
val GlassShadow = Color(0x0D000000)       // 5% black for glass bottom
val PremiumGlow = Color(0x33B24BF3)       // Purple glow for question frame shadow

// ============================================
// SPECTRUM THEME — "Fiesta + Prisma + Aurora" (tintes rosa)
// ============================================

// Warm atmospheric surfaces (tinte morado profundo — dark / rosa claro — light)
val SpectrumSurface = DarkPurpleSurface                  // Warm glass surface
val SpectrumSurfaceElevated = DarkPurpleSurfaceElevated  // Elevated warm
val SpectrumSurfaceLight = Pink50                        // Light mode: rosa blanquísimo
val SpectrumSurfaceLightElevated = Pink100               // Light mode: rosa claro

// Iridescent border palette — pride spectrum for prismatic edges
// Se sustituye el ámbar (#FBBF24) por rosa profundo para coherencia.
val IridescentColors = listOf(
    Color(0xFFFF6B9D),  // Pink
    Purple400,          // Violet
    Color(0xFF60A5FA),  // Blue
    Color(0xFF34D399),  // Emerald
    Pink500,            // Rosa saturado (antes amber)
    Color(0xFFFF6B6B)   // Coral
)

// Rainbow progress fill — celebratory, the ONE place full spectrum appears
val RainbowProgressFill = listOf(
    Color(0xFFEF4444),  // Red
    Color(0xFFF97316),  // Orange
    Color(0xFFFBBF24),  // Yellow (se preserva — es rainbow)
    Color(0xFF22C55E),  // Green
    Color(0xFF3B82F6),  // Blue
    Purple500,          // Violet
    Pink400             // Pink (loops back for continuity)
)

// Button chromatic identities — warm tints, each button owns a color
val ButtonAccentA = Color(0xFFFF6B9D)     // Rose
val ButtonAccentB = Color(0xFF60A5FA)     // Sky
val ButtonAccentC = Purple400             // Violet
val ButtonAccentD = Color(0xFF34D399)     // Emerald

// Button surface tints — very subtle, atmospheric
val ButtonTintA = Color(0x12FF6B9D)       // 7% rose
val ButtonTintB = Color(0x1260A5FA)       // 7% sky
val ButtonTintC = Color(0x12A855F7)       // 7% violet
val ButtonTintD = Color(0x1234D399)       // 7% emerald

// Aurora glow washes — wide, soft, atmospheric (for background orbs)
val AuroraWashPink = Color(0x25EC4899)    // 15% pink
val AuroraWashViolet = Color(0x20A855F7)  // 12% violet
val AuroraWashBlue = Color(0x1860A5FA)    // 10% blue

// Top bar — warm translucent
val TopBarSurface = Color(0xCC1D1030)          // 80% DarkPurpleSurface
val TopBarSurfaceLight = Color(0xCCFFF1F5)     // 80% Pink50

// ============================================
// HIGH CONTRAST COLORS — WCAG AAA (7:1 ratio)
// ============================================

// Alto contraste — tema oscuro
// Fondo negro puro, texto blanco puro — ratio > 21:1
val HighContrastDarkBackground = Color(0xFF000000)
val HighContrastDarkSurface = Color(0xFF0A0A0A)
val HighContrastDarkSurfaceVariant = Color(0xFF141414)
val HighContrastDarkOnBackground = Color(0xFFFFFFFF)
val HighContrastDarkOnSurface = Color(0xFFFFFFFF)
// Primary: morado intenso sobre negro (identidad dark HC)
val HighContrastDarkPrimary = Color(0xFF2A0B42)
val HighContrastDarkOnPrimary = Color(0xFF000000)
val HighContrastDarkPrimaryContainer = Color(0xFF3B0A1F)
val HighContrastDarkOnPrimaryContainer = Color(0xFF2B0050)
// Secondary: cian brillante sobre negro — ratio ~15:1
val HighContrastDarkSecondary = Color(0xFF00FFFF)
val HighContrastDarkOnSecondary = Color(0xFF000000)
val HighContrastDarkSecondaryContainer = Color(0xFF001A1A)
val HighContrastDarkOnSecondaryContainer = Color(0xFF00FFFF)
// Tertiary: verde lima brillante sobre negro — ratio ~14:1
val HighContrastDarkTertiary = Color(0xFF39FF14)
val HighContrastDarkOnTertiary = Color(0xFF000000)
val HighContrastDarkTertiaryContainer = Color(0xFF041400)
val HighContrastDarkOnTertiaryContainer = Color(0xFF39FF14)
// Error, outline y miscelánea — tinte rojo claro brillante sobre negro
val HighContrastDarkError = Color(0xFFFF6B6B)
val HighContrastDarkErrorContainer = Color(0xFF2A0000)
val HighContrastDarkOutlineVariant = Color(0xFFAAAAAA)

// Alto contraste — tema claro
// Fondo blanco (tinte rosa) con texto negro — ratio > 20:1
val HighContrastLightBackground = Color(0xFFFFF5F8)
val HighContrastLightSurface = Color(0xFFFFFFFF)
val HighContrastLightSurfaceVariant = Color(0xFFFCE7F0)
val HighContrastLightOnBackground = Color(0xFF000000)
val HighContrastLightOnSurface = Color(0xFF000000)
// Primary: rosa brillante sobre blanco (identidad light HC)
val HighContrastLightPrimary = Color(0xFFE56BAE)
val HighContrastLightOnPrimary = Color(0xFFFFFFFF)
val HighContrastLightPrimaryContainer = Color(0xFFEDD9FF)
val HighContrastLightOnPrimaryContainer = Color(0xFFFF66B5)
// Secondary: azul oscuro sobre blanco — ratio ~10:1
val HighContrastLightSecondary = Color(0xFF0000CD)
val HighContrastLightOnSecondary = Color(0xFFFFFFFF)
val HighContrastLightSecondaryContainer = Color(0xFFD6D6FF)
val HighContrastLightOnSecondaryContainer = Color(0xFF00006B)
// Tertiary: verde oscuro sobre blanco — ratio ~9:1
val HighContrastLightTertiary = Color(0xFF005A00)
val HighContrastLightOnTertiary = Color(0xFFFFFFFF)
val HighContrastLightTertiaryContainer = Color(0xFFCCF2CC)
val HighContrastLightOnTertiaryContainer = Color(0xFF002800)
// Error, outline y miscelánea — rojo oscuro sobre blanco
val HighContrastLightError = Color(0xFF8B0000)
val HighContrastLightErrorContainer = Color(0xFFFFDDDD)
val HighContrastLightOnErrorContainer = Color(0xFF4A0000)
val HighContrastLightOutlineVariant = Color(0xFF555555)

// ============================================
// SCRIM — overlay para modales/dropdowns
// ============================================

val ScrimDark = Color(0xCC000000)   // 80% negro — dark theme scrim
