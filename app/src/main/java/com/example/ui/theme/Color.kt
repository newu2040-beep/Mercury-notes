package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Mercury Brand Accents
val MercuryBlue = Color(0xFF3B82F6)
val MercuryViolet = Color(0xFF8B5CF6)
val MercuryPink = Color(0xFFEC4899)
val MercuryCyan = Color(0xFF06B6D4)
val MercuryTeal = Color(0xFF10B981)
val MercuryAmber = Color(0xFFF59E0B)
val MercuryRose = Color(0xFFF43F5E)

// Gradient Brushes
val MercuryPrimaryGradient = Brush.linearGradient(
    colors = listOf(MercuryBlue, MercuryViolet, MercuryPink)
)

val MercuryGlassGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x333B82F6),
        Color(0x228B5CF6),
        Color(0x11EC4899)
    )
)

val MercuryHeroGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF4F46E5),
        Color(0xFF7C3AED),
        Color(0xFFDB2777)
    )
)

// Dark Theme Glassmorphism
val DarkCanvasStart = Color(0xFF090D16)
val DarkCanvasEnd = Color(0xFF0E1322)
val DarkGlassCard = Color(0xB3151D30)
val DarkGlassCardElevated = Color(0xD91B243B)
val DarkGlassBorder = Color(0x2EFFFFFF)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)

// Light Theme Glassmorphism
val LightCanvasStart = Color(0xFFF8FAFF)
val LightCanvasEnd = Color(0xFFEEF2F9)
val LightGlassCard = Color(0xE6FFFFFF)
val LightGlassCardElevated = Color(0xF2FFFFFF)
val LightGlassBorder = Color(0x3394A3B8)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF94A3B8)

// Accent Tint Options for Notes
val NoteTintOptions = listOf(
    0L to ("Default" to Color.Transparent),
    0xFFFFE4E6.toLong() to ("Rose" to Color(0xFFF43F5E)),
    0xFFEDE9FE.toLong() to ("Violet" to Color(0xFF8B5CF6)),
    0xFFE0F2FE.toLong() to ("Sky" to Color(0xFF0284C7)),
    0xFFD1FAE5.toLong() to ("Emerald" to Color(0xFF10B981)),
    0xFFFEF3C7.toLong() to ("Amber" to Color(0xFFD97706))
)
