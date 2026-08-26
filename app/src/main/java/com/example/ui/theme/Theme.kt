package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.preferences.NoteFontSize
import com.example.data.preferences.PastelThemePreset
import com.example.data.preferences.ThemeMode

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
    val dividerColor: Color,
    val isLiquidGlass: Boolean
)

val LocalMercuryGlass = staticCompositionLocalOf<MercuryGlassColors> {
    error("No MercuryGlassColors provided")
}

val LocalCompactMode = compositionLocalOf { false }
val LocalNoteFontScale = compositionLocalOf { 1.0f }

object MercuryTheme {
    val glass: MercuryGlassColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMercuryGlass.current

    val isCompact: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalCompactMode.current

    val fontScale: Float
        @Composable
        @ReadOnlyComposable
        get() = LocalNoteFontScale.current
}

private data class ThemeColorSet(
    val primary: Color,
    val secondary: Color,
    val canvasStart: Color,
    val canvasEnd: Color,
    val cardDark: Color,
    val cardLight: Color
)

fun getGlassColors(
    isDark: Boolean,
    preset: PastelThemePreset,
    liquidGlass: Boolean,
    reduceTransparency: Boolean
): MercuryGlassColors {
    val set = when (preset) {
        PastelThemePreset.MERCURY -> ThemeColorSet(
            MercuryViolet, MercuryBlue,
            if (isDark) DarkCanvasStart else LightCanvasStart,
            if (isDark) DarkCanvasEnd else LightCanvasEnd,
            DarkGlassCard, LightGlassCard
        )
        PastelThemePreset.LAVENDER -> ThemeColorSet(
            Color(0xFFA78BFA), Color(0xFFC084FC),
            if (isDark) Color(0xFF0F0E1E) else Color(0xFFFAF7FF),
            if (isDark) Color(0xFF19162E) else Color(0xFFF3EDFD),
            Color(0xB31E1B38), Color(0xF2FDFBFF)
        )
        PastelThemePreset.PEACH -> ThemeColorSet(
            Color(0xFFFB923C), Color(0xFFF472B6),
            if (isDark) Color(0xFF1C110C) else Color(0xFFFFFBF7),
            if (isDark) Color(0xFF2B1912) else Color(0xFFFFF1E6),
            Color(0xB3331E17), Color(0xF2FFFDFC)
        )
        PastelThemePreset.MINT -> ThemeColorSet(
            Color(0xFF34D399), Color(0xFF2DD4BF),
            if (isDark) Color(0xFF091712) else Color(0xFFF4FBF7),
            if (isDark) Color(0xFF10261E) else Color(0xFFE6F7EF),
            Color(0xB3142B23), Color(0xF2FBFFFD)
        )
        PastelThemePreset.ROSE -> ThemeColorSet(
            Color(0xFFF472B6), Color(0xFFFB7185),
            if (isDark) Color(0xFF1A0E15) else Color(0xFFFFF7FA),
            if (isDark) Color(0xFF2A1521) else Color(0xFFFFECF3),
            Color(0xB3331A29), Color(0xF2FFFDFC)
        )
        PastelThemePreset.OCEAN -> ThemeColorSet(
            Color(0xFF38BDF8), Color(0xFF818CF8),
            if (isDark) Color(0xFF09131C) else Color(0xFFF5FAFF),
            if (isDark) Color(0xFF0E1D2D) else Color(0xFFE8F3FF),
            Color(0xB313273B), Color(0xF2FBFEFF)
        )
        PastelThemePreset.LIQUID_OPAL -> ThemeColorSet(
            Color(0xFF818CF8), Color(0xFFF472B6),
            if (isDark) Color(0xFF121422) else Color(0xFFF9FAFF),
            if (isDark) Color(0xFF1E2135) else Color(0xFFEEF2FF),
            Color(0xB322253E), Color(0xF2FFFFFF)
        )
        PastelThemePreset.MIDNIGHT -> ThemeColorSet(
            Color(0xFFC084FC), Color(0xFF60A5FA),
            if (isDark) Color(0xFF000000) else Color(0xFFF8FAFC),
            if (isDark) Color(0xFF08080C) else Color(0xFFEFF2F6),
            Color(0xCC0D0D14), Color(0xF2FFFFFF)
        )
    }

    val primaryColor = set.primary
    val secondaryColor = set.secondary
    val cStart = set.canvasStart
    val cEnd = set.canvasEnd
    val cDark = set.cardDark
    val cLight = set.cardLight

    val accentGradient = Brush.linearGradient(
        colors = listOf(primaryColor, secondaryColor, primaryColor.copy(alpha = 0.8f))
    )

    return if (isDark) {
        val cardBg = if (reduceTransparency) Color(0xFF141926) else cDark
        val elevatedBg = if (reduceTransparency) Color(0xFF1E2538) else cDark.copy(alpha = 0.85f)
        val border = if (liquidGlass) Color(0x38FFFFFF) else Color(0x1FFFFFFF)
        val borderElevated = if (liquidGlass) primaryColor.copy(alpha = 0.45f) else Color(0x30FFFFFF)

        MercuryGlassColors(
            isDark = true,
            canvasBackground = Brush.verticalGradient(listOf(cStart, Color(0xFF0B0E1A), cEnd)),
            canvasSolid = cStart,
            cardBackground = cardBg,
            cardBackgroundElevated = elevatedBg,
            cardBorder = border,
            cardBorderElevated = borderElevated,
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            textMuted = Color(0xFF64748B),
            bottomNavBackground = if (reduceTransparency) Color(0xFF0F1422) else Color(0xD90E1322),
            bottomNavBorder = border,
            primaryAccent = primaryColor,
            secondaryAccent = secondaryColor,
            accentGradient = accentGradient,
            glowColor = primaryColor.copy(alpha = 0.28f),
            searchBarBackground = if (reduceTransparency) Color(0xFF1E2538) else Color(0x551E2538),
            dividerColor = Color(0x14FFFFFF),
            isLiquidGlass = liquidGlass
        )
    } else {
        val cardBg = if (reduceTransparency) Color(0xFFFFFFFF) else cLight
        val elevatedBg = if (reduceTransparency) Color(0xFFFFFFFF) else Color(0xF2FFFFFF)
        val border = if (liquidGlass) primaryColor.copy(alpha = 0.20f) else Color(0x14000000)
        val borderElevated = if (liquidGlass) primaryColor.copy(alpha = 0.35f) else Color(0x1F000000)

        MercuryGlassColors(
            isDark = false,
            canvasBackground = Brush.verticalGradient(listOf(cStart, Color(0xFFF3F6FC), cEnd)),
            canvasSolid = cStart,
            cardBackground = cardBg,
            cardBackgroundElevated = elevatedBg,
            cardBorder = border,
            cardBorderElevated = borderElevated,
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF475569),
            textMuted = Color(0xFF94A3B8),
            bottomNavBackground = if (reduceTransparency) Color(0xFFFFFFFF) else Color(0xE6FFFFFF),
            bottomNavBorder = border,
            primaryAccent = primaryColor,
            secondaryAccent = secondaryColor,
            accentGradient = accentGradient,
            glowColor = primaryColor.copy(alpha = 0.18f),
            searchBarBackground = if (reduceTransparency) Color(0xFFF1F5F9) else Color(0x73E2E8F0),
            dividerColor = Color(0x0F000000),
            isLiquidGlass = liquidGlass
        )
    }
}

@Composable
fun MercurynotesTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    pastelTheme: PastelThemePreset = PastelThemePreset.MERCURY,
    liquidGlassEnabled: Boolean = true,
    compactMode: Boolean = false,
    fontSize: NoteFontSize = NoteFontSize.MEDIUM,
    reduceTransparency: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val glassColors = remember(isDark, pastelTheme, liquidGlassEnabled, reduceTransparency) {
        getGlassColors(
            isDark = isDark,
            preset = pastelTheme,
            liquidGlass = liquidGlassEnabled,
            reduceTransparency = reduceTransparency
        )
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = glassColors.primaryAccent,
            secondary = glassColors.secondaryAccent,
            background = glassColors.canvasSolid,
            surface = glassColors.cardBackground,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = glassColors.textPrimary,
            onSurface = glassColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = glassColors.primaryAccent,
            secondary = glassColors.secondaryAccent,
            background = glassColors.canvasSolid,
            surface = glassColors.cardBackground,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = glassColors.textPrimary,
            onSurface = glassColors.textPrimary
        )
    }

    val fontScale = fontSize.scale
    val scaledTypography = remember(fontScale) {
        createScaledTypography(fontScale)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalMercuryGlass provides glassColors,
        LocalCompactMode provides compactMode,
        LocalNoteFontScale provides fontScale
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = scaledTypography,
            content = content
        )
    }
}
