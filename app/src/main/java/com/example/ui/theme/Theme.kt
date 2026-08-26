package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.data.preferences.ThemeMode

@Immutable
data class MercuryGlassColors(
    val isDark: Boolean,
    val canvasBackground: Brush,
    val canvasSolid: Color,
    val cardBackground: Color,
    val cardBackgroundElevated: Color,
    val cardBorder: Color,
    val cardBorderElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val bottomNavBackground: Color,
    val bottomNavBorder: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val accentGradient: Brush,
    val glowColor: Color,
    val searchBarBackground: Color,
    val dividerColor: Color
)

val LocalMercuryGlass = staticCompositionLocalOf<MercuryGlassColors> {
    error("No MercuryGlassColors provided")
}

val DarkMercuryGlass = MercuryGlassColors(
    isDark = true,
    canvasBackground = Brush.verticalGradient(
        colors = listOf(DarkCanvasStart, Color(0xFF0C101D), DarkCanvasEnd)
    ),
    canvasSolid = DarkCanvasStart,
    cardBackground = DarkGlassCard,
    cardBackgroundElevated = DarkGlassCardElevated,
    cardBorder = DarkGlassBorder,
    cardBorderElevated = Color(0x40FFFFFF),
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    bottomNavBackground = Color(0xD90E1424),
    bottomNavBorder = Color(0x2EFFFFFF),
    primaryAccent = MercuryViolet,
    secondaryAccent = MercuryBlue,
    accentGradient = MercuryPrimaryGradient,
    glowColor = Color(0x338B5CF6),
    searchBarBackground = Color(0x801E293B),
    dividerColor = Color(0x1FFFFFFF)
)

val LightMercuryGlass = MercuryGlassColors(
    isDark = false,
    canvasBackground = Brush.verticalGradient(
        colors = listOf(LightCanvasStart, Color(0xFFF3F6FC), LightCanvasEnd)
    ),
    canvasSolid = LightCanvasStart,
    cardBackground = LightGlassCard,
    cardBackgroundElevated = LightGlassCardElevated,
    cardBorder = LightGlassBorder,
    cardBorderElevated = Color(0x4D3B82F6),
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    bottomNavBackground = Color(0xE6FFFFFF),
    bottomNavBorder = Color(0x2994A3B8),
    primaryAccent = MercuryViolet,
    secondaryAccent = MercuryBlue,
    accentGradient = MercuryPrimaryGradient,
    glowColor = Color(0x203B82F6),
    searchBarBackground = Color(0xE6FFFFFF),
    dividerColor = Color(0x140F172A)
)

private val M3DarkColorScheme = darkColorScheme(
    primary = MercuryViolet,
    onPrimary = Color.White,
    secondary = MercuryBlue,
    onSecondary = Color.White,
    tertiary = MercuryPink,
    background = DarkCanvasStart,
    surface = DarkGlassCard,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary
)

private val M3LightColorScheme = lightColorScheme(
    primary = MercuryViolet,
    onPrimary = Color.White,
    secondary = MercuryBlue,
    onSecondary = Color.White,
    tertiary = MercuryPink,
    background = LightCanvasStart,
    surface = LightGlassCard,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary
)

object MercuryTheme {
    val glass: MercuryGlassColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMercuryGlass.current
}

@Composable
fun MercurynotesTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    reduceTransparency: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    val glassColors = if (isDark) {
        if (reduceTransparency) {
            DarkMercuryGlass.copy(
                cardBackground = Color(0xFF161E30),
                cardBackgroundElevated = Color(0xFF1E2840),
                bottomNavBackground = Color(0xFF111728)
            )
        } else {
            DarkMercuryGlass
        }
    } else {
        if (reduceTransparency) {
            LightMercuryGlass.copy(
                cardBackground = Color(0xFFFFFFFF),
                cardBackgroundElevated = Color(0xFFFFFFFF),
                bottomNavBackground = Color(0xFFFFFFFF)
            )
        } else {
            LightMercuryGlass
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    val colorScheme = if (isDark) M3DarkColorScheme else M3LightColorScheme

    CompositionLocalProvider(
        LocalMercuryGlass provides glassColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
