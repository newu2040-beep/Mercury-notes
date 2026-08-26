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

// Pastel Colors
val PastelLilac = Color(0xFFA78BFA)
val PastelPeach = Color(0xFFFDBA74)
val PastelMint = Color(0xFF6EE7B7)
val PastelBlush = Color(0xFFF472B6)
val PastelSky = Color(0xFF7DD3FC)
val PastelOpal = Color(0xFFE2E8F0)
val PastelSage = Color(0xFFA7F3D0)
val PastelButter = Color(0xFFFDE68A)

// Gradient Brushes
val MercuryPrimaryGradient = Brush.linearGradient(
    colors = listOf(MercuryBlue, MercuryViolet, MercuryPink)
)

val LiquidGlassPrismGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x5538BDF8),
        Color(0x44A78BFA),
        Color(0x44F472B6),
        Color(0x556EE7B7)
    )
)

val LiquidGlassShimmerGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x22FFFFFF),
        Color(0x66FFFFFF),
        Color(0x11FFFFFF)
    )
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

// Dark Theme Base
val DarkCanvasStart = Color(0xFF090D16)
val DarkCanvasEnd = Color(0xFF0E1322)
val DarkGlassCard = Color(0xB3151D30)
val DarkGlassCardElevated = Color(0xD91B243B)
val DarkGlassBorder = Color(0x2EFFFFFF)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)

// Light Theme Base
val LightCanvasStart = Color(0xFFF8FAFF)
val LightCanvasEnd = Color(0xFFEEF2F9)
val LightGlassCard = Color(0xE6FFFFFF)
val LightGlassCardElevated = Color(0xF2FFFFFF)
val LightGlassBorder = Color(0x3394A3B8)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF94A3B8)

// Note Tint Palette
val NoteTintOptions = listOf(
    0L to ("Default" to Color.Transparent),
    0xFFFFE4E6.toLong() to ("Rose" to Color(0xFFF43F5E)),
    0xFFEDE9FE.toLong() to ("Violet" to Color(0xFF8B5CF6)),
    0xFFE0F2FE.toLong() to ("Sky" to Color(0xFF0284C7)),
    0xFFD1FAE5.toLong() to ("Emerald" to Color(0xFF10B981)),
    0xFFFEF3C7.toLong() to ("Amber" to Color(0xFFD97706)),
    0xFFFFEDD5.toLong() to ("Peach" to Color(0xFFFB923C)),
    0xFFF1F5F9.toLong() to ("Slate" to Color(0xFF64748B))
)
