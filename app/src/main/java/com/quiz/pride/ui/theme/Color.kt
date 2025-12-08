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

val ExpertGradientTop = Color(0xFFF87171)     // Red
val ExpertGradientBottom = Color(0xFFEF4444)

val TimedGradientTop = Color(0xFF06B6D4)      // Cyan
val TimedGradientBottom = Color(0xFF0891B2)   // Dark Cyan

// Game Screen Gradient
val GradientGameTop = Color(0xFF1E1B4B)       // Deep Indigo
val GradientGameBottom = Color(0xFF312E81)    // Indigo

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
