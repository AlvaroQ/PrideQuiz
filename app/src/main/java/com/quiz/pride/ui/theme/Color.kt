package com.quiz.pride.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================
// PRIDE RAINBOW COLORS - Main Palette
// ============================================

// Classic Pride Rainbow
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
val NeonYellow = Color(0xFFFFFF00)
val NeonOrange = Color(0xFFFF9100)

// ============================================
// GRADIENT PAIRS - For beautiful transitions
// ============================================

// Rainbow Gradient Stops (full spectrum)
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
val GradientPrimaryEnd = Color(0xFFEC4899)

// Vibrant Background Gradient
val GradientBackgroundStart = Color(0xFF8B5CF6)  // Vivid Purple
val GradientBackgroundMid = Color(0xFFEC4899)     // Pink
val GradientBackgroundMidWarm = Color(0xFFF472B6) // Warm Pink (transicion suave)
val GradientBackgroundEnd = Color(0xFFF97316)     // Orange

// Card Gradients - Vibrant Menu Cards
val StartGradientTop = Color(0xFFFF6B6B)      // Coral Red
val StartGradientBottom = Color(0xFFFF8E53)   // Orange

val LearnGradientTop = Color(0xFF4FACFE)      // Sky Blue
val LearnGradientBottom = Color(0xFF00F2FE)   // Cyan

val SettingsGradientTop = Color(0xFFA855F7)   // Purple
val SettingsGradientBottom = Color(0xFFEC4899) // Pink

// Difficulty Gradients
val NormalGradientTop = Color(0xFF4ADE80)     // Green
val NormalGradientBottom = Color(0xFF22C55E)

val AdvanceGradientTop = Color(0xFFFBBF24)    // Yellow
val AdvanceGradientBottom = Color(0xFFF59E0B) // Amber

val TimedGradientTop = Color(0xFF06B6D4)      // Cyan
val TimedGradientBottom = Color(0xFF0891B2)   // Dark Cyan

// Game Screen Gradient — Warm midnight (NOT cold indigo)
val GradientGameTop = Color(0xFF13111F)       // Warm midnight
val GradientGameBottom = Color(0xFF1B1830)    // Warm midnight plum

// Position/Ranking Gradient
val GradientPositionTop = Color(0xFFD946EF)   // Fuchsia
val GradientPositionBottom = Color(0xFF8B5CF6) // Violet

// Points Display Gradient
val GradientPointsTop = Color(0xFFFDE68A)     // Yellow light
val GradientPointsBottom = Color(0xFFFBBF24)  // Yellow

// ============================================
// UI COLORS
// ============================================

// Response Feedback
val ResponseCorrect = Color(0xFF22C55E)       // Vibrant Green
val ResponseFail = Color(0xFFEF4444)          // Vibrant Red

// Basic Colors
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val LightGray = Color(0xFFF3F4F6)
val DarkGray = Color(0xFF6B7280)

// Text Colors
val TextOnDark = Color(0xFFFFFFFF)
val TextOnLight = Color(0xFF1F2937)
val TextMuted = Color(0xFF9CA3AF)

// ============================================
// DARK THEME COLORS
// ============================================

val DarkBackground = Color(0xFF0F0F23)        // Deep dark blue
val DarkSurface = Color(0xFF1A1A2E)           // Slightly lighter
val DarkSurfaceVariant = Color(0xFF252542)    // Card background
val DarkSurfaceElevated = Color(0xFF2D2D4A)   // Elevated surfaces

// ============================================
// LIGHT THEME COLORS
// ============================================

val LightBackground = Color(0xFFFAFAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF3F4F6)

// ============================================
// SPECIAL EFFECTS
// ============================================

val GlassWhite = Color(0x33FFFFFF)            // 20% white
val GlassDark = Color(0x33000000)             // 20% black
val Shimmer = Color(0x66FFFFFF)               // For shimmer effects
val Overlay = Color(0x80000000)               // 50% black overlay

// Glow Colors (for neon effects)
val GlowPink = Color(0x40EC4899)
val GlowPurple = Color(0x40A855F7)
val GlowBlue = Color(0x404FACFE)

// Premium glass effects
val GlassHighlight = Color(0x1AFFFFFF)    // 10% white for glass top highlight
val GlassShadow = Color(0x0D000000)       // 5% black for glass bottom
val PremiumGlow = Color(0x33B24BF3)       // Purple glow for question frame shadow

// ============================================
// SPECTRUM THEME — "Fiesta + Prisma + Aurora"
// ============================================

// Warm atmospheric surfaces (plum/wine undertone, NOT cold blue)
val SpectrumSurface = Color(0xFF1E1A35)           // Warm glass surface
val SpectrumSurfaceElevated = Color(0xFF262245)   // Elevated warm
val SpectrumSurfaceLight = Color(0xFFF8F6FF)      // Light mode: barely lavender
val SpectrumSurfaceLightElevated = Color(0xFFF0ECFA) // Light mode: gentle violet tint

// Iridescent border palette — pride spectrum for prismatic edges
val IridescentColors = listOf(
    Color(0xFFFF6B9D),  // Pink
    Color(0xFFA855F7),  // Violet
    Color(0xFF60A5FA),  // Blue
    Color(0xFF34D399),  // Emerald
    Color(0xFFFBBF24),  // Amber
    Color(0xFFFF6B6B)   // Coral
)

// Rainbow progress fill — celebratory, the ONE place full spectrum appears
val RainbowProgressFill = listOf(
    Color(0xFFEF4444),  // Red
    Color(0xFFF97316),  // Orange
    Color(0xFFFBBF24),  // Yellow
    Color(0xFF22C55E),  // Green
    Color(0xFF3B82F6),  // Blue
    Color(0xFF8B5CF6),  // Violet
    Color(0xFFEC4899)   // Pink (loops back for continuity)
)

// Button chromatic identities — warm tints, each button owns a color
val ButtonAccentA = Color(0xFFFF6B9D)     // Rose
val ButtonAccentB = Color(0xFF60A5FA)     // Sky
val ButtonAccentC = Color(0xFFA855F7)     // Violet
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
val TopBarSurface = Color(0xCC1E1A35)     // 80% warm surface
val TopBarSurfaceLight = Color(0xCCF8F6FF) // 80% light surface

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
// Primary: amarillo brillante sobre negro — ratio ~18:1
val HighContrastDarkPrimary = Color(0xFFFFD700)
val HighContrastDarkOnPrimary = Color(0xFF000000)
val HighContrastDarkPrimaryContainer = Color(0xFF1A1500)
val HighContrastDarkOnPrimaryContainer = Color(0xFFFFD700)
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

// Alto contraste — tema claro
// Fondo blanco puro, texto negro puro — ratio > 21:1
val HighContrastLightBackground = Color(0xFFFFFFFF)
val HighContrastLightSurface = Color(0xFFFFFFFF)
val HighContrastLightSurfaceVariant = Color(0xFFF0F0F0)
val HighContrastLightOnBackground = Color(0xFF000000)
val HighContrastLightOnSurface = Color(0xFF000000)
// Primary: purpura intenso sobre blanco — ratio ~8:1
val HighContrastLightPrimary = Color(0xFF5B009E)
val HighContrastLightOnPrimary = Color(0xFFFFFFFF)
val HighContrastLightPrimaryContainer = Color(0xFFEDD9FF)
val HighContrastLightOnPrimaryContainer = Color(0xFF2B0050)
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
