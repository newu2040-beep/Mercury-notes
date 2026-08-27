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
import com.example.data.preferences.FontPreset
import com.example.data.preferences.NoteFontSize
import com.example.data.preferences.PastelThemePreset
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.TranslucencyLevel

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
    val tertiaryAccent: Color,
    val accentGradient: Brush,
    val buttonGradient: Brush,
    val heroGradient: Brush,
    val moltenSheenGradient: Brush,
    val specularReflection: Brush,
    val auraBlobColors: List<Color>,
    val glowColor: Color,
    val searchBarBackground: Color,
    val dividerColor: Color,
    val isLiquidGlass: Boolean,
    val translucencyLevel: TranslucencyLevel
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
    val tertiary: Color,
    val canvasStart: Color,
    val canvasEnd: Color,
    val cardDark: Color,
    val cardLight: Color
)

fun getGlassColors(
    isDark: Boolean,
    preset: PastelThemePreset,
    liquidGlass: Boolean,
    reduceTransparency: Boolean,
    translucencyLevel: TranslucencyLevel = if (reduceTransparency) TranslucencyLevel.OPAQUE else TranslucencyLevel.FROSTED
): MercuryGlassColors {
    val set = when (preset) {
        PastelThemePreset.MERCURY -> ThemeColorSet(
            MercuryViolet, MercuryBlue, MercuryPink,
            if (isDark) DarkCanvasStart else LightCanvasStart,
            if (isDark) DarkCanvasEnd else LightCanvasEnd,
            DarkGlassCard, LightGlassCard
        )
        PastelThemePreset.LAVENDER -> ThemeColorSet(
            Color(0xFFA78BFA), Color(0xFFC084FC), Color(0xFF818CF8),
            if (isDark) Color(0xFF0F0E1E) else Color(0xFFFAF7FF),
            if (isDark) Color(0xFF19162E) else Color(0xFFF3EDFD),
            Color(0xB31E1B38), Color(0xF2FDFBFF)
        )
        PastelThemePreset.PEACH -> ThemeColorSet(
            Color(0xFFFB923C), Color(0xFFF472B6), Color(0xFFFBBF24),
            if (isDark) Color(0xFF1C110C) else Color(0xFFFFFBF7),
            if (isDark) Color(0xFF2B1912) else Color(0xFFFFF1E6),
            Color(0xB3331E17), Color(0xF2FFFDFC)
        )
        PastelThemePreset.MINT -> ThemeColorSet(
            Color(0xFF34D399), Color(0xFF2DD4BF), Color(0xFF38BDF8),
            if (isDark) Color(0xFF091712) else Color(0xFFF4FBF7),
            if (isDark) Color(0xFF10261E) else Color(0xFFE6F7EF),
            Color(0xB3142B23), Color(0xF2FBFFFD)
        )
        PastelThemePreset.ROSE -> ThemeColorSet(
            Color(0xFFF472B6), Color(0xFFFB7185), Color(0xFFA855F7),
            if (isDark) Color(0xFF1A0E15) else Color(0xFFFFF7FA),
            if (isDark) Color(0xFF2A1521) else Color(0xFFFFECF3),
            Color(0xB3331A29), Color(0xF2FFFDFC)
        )
        PastelThemePreset.OCEAN -> ThemeColorSet(
            Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFF34D399),
            if (isDark) Color(0xFF09131C) else Color(0xFFF5FAFF),
            if (isDark) Color(0xFF0E1D2D) else Color(0xFFE8F3FF),
            Color(0xB313273B), Color(0xF2FBFEFF)
        )
        PastelThemePreset.LIQUID_OPAL -> ThemeColorSet(
            Color(0xFF818CF8), Color(0xFFF472B6), Color(0xFF38BDF8),
            if (isDark) Color(0xFF121422) else Color(0xFFF9FAFF),
            if (isDark) Color(0xFF1E2135) else Color(0xFFEEF2FF),
            Color(0xB322253E), Color(0xF2FFFFFF)
        )
        PastelThemePreset.MIDNIGHT -> ThemeColorSet(
            Color(0xFFC084FC), Color(0xFF60A5FA), Color(0xFFF472B6),
            if (isDark) Color(0xFF050508) else Color(0xFFF8FAFC),
            if (isDark) Color(0xFF0C0C14) else Color(0xFFEFF2F6),
            Color(0xCC0E0E18), Color(0xF2FFFFFF)
        )
    }

    val primaryColor = set.primary
    val secondaryColor = set.secondary
    val tertiaryColor = set.tertiary
    val cStart = set.canvasStart
    val cEnd = set.canvasEnd
    val cDark = set.cardDark
    val cLight = set.cardLight

    val effectiveTranslucency = if (reduceTransparency) TranslucencyLevel.OPAQUE else translucencyLevel

    val accentGradient = Brush.linearGradient(
        colors = listOf(primaryColor, secondaryColor, tertiaryColor)
    )

    val buttonGradient = Brush.linearGradient(
        colors = listOf(primaryColor, secondaryColor)
    )

    val heroGradient = Brush.horizontalGradient(
        colors = if (isDark) {
            listOf(primaryColor.copy(alpha = 0.28f), secondaryColor.copy(alpha = 0.22f), tertiaryColor.copy(alpha = 0.16f))
        } else {
            listOf(primaryColor.copy(alpha = 0.22f), secondaryColor.copy(alpha = 0.18f), tertiaryColor.copy(alpha = 0.12f))
        }
    )

    val moltenSheenGradient = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color(0x35FFFFFF),
                primaryColor.copy(alpha = 0.45f),
                secondaryColor.copy(alpha = 0.35f),
                Color(0x15FFFFFF)
            )
        } else {
            listOf(
                Color(0x80FFFFFF),
                primaryColor.copy(alpha = 0.28f),
                secondaryColor.copy(alpha = 0.20f),
                Color(0x40FFFFFF)
            )
        }
    )

    val specularReflection = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(Color(0x30FFFFFF), Color(0x06FFFFFF), Color.Transparent)
        } else {
            listOf(Color(0x85FFFFFF), Color(0x20FFFFFF), Color.Transparent)
        }
    )

    val auraBlobColors = listOf(
        primaryColor.copy(alpha = if (isDark) 0.35f else 0.22f),
        secondaryColor.copy(alpha = if (isDark) 0.30f else 0.18f),
        tertiaryColor.copy(alpha = if (isDark) 0.25f else 0.15f)
    )

    return if (isDark) {
        val cardBg = when (effectiveTranslucency) {
            TranslucencyLevel.CRYSTAL -> cDark.copy(alpha = 0.38f)
            TranslucencyLevel.FROSTED -> cDark.copy(alpha = 0.68f)
            TranslucencyLevel.SOFT -> cDark.copy(alpha = 0.86f)
            TranslucencyLevel.OPAQUE -> Color(0xFF141926)
        }
        val elevatedBg = when (effectiveTranslucency) {
            TranslucencyLevel.CRYSTAL -> cDark.copy(alpha = 0.52f)
            TranslucencyLevel.FROSTED -> cDark.copy(alpha = 0.82f)
            TranslucencyLevel.SOFT -> cDark.copy(alpha = 0.92f)
            TranslucencyLevel.OPAQUE -> Color(0xFF1E2538)
        }
        val borderAlpha = if (liquidGlass) effectiveTranslucency.glassBorderAlpha else 0.20f
        val border = if (liquidGlass) primaryColor.copy(alpha = borderAlpha) else Color(0x22FFFFFF)
        val borderElevated = if (liquidGlass) primaryColor.copy(alpha = (borderAlpha + 0.15f).coerceAtMost(0.6f)) else Color(0x30FFFFFF)

        MercuryGlassColors(
            isDark = true,
            canvasBackground = Brush.verticalGradient(listOf(cStart, Color(0xFF090C16), cEnd)),
            canvasSolid = cStart,
            cardBackground = cardBg,
            cardBackgroundElevated = elevatedBg,
            cardBorder = border,
            cardBorderElevated = borderElevated,
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            textMuted = Color(0xFF64748B),
            bottomNavBackground = when (effectiveTranslucency) {
                TranslucencyLevel.CRYSTAL -> Color(0x750E1322)
                TranslucencyLevel.FROSTED -> Color(0xC00E1322)
                TranslucencyLevel.SOFT -> Color(0xE80E1322)
                TranslucencyLevel.OPAQUE -> Color(0xFF0F1422)
            },
            bottomNavBorder = border,
            primaryAccent = primaryColor,
            secondaryAccent = secondaryColor,
            tertiaryAccent = tertiaryColor,
            accentGradient = accentGradient,
            buttonGradient = buttonGradient,
            heroGradient = heroGradient,
            moltenSheenGradient = moltenSheenGradient,
            specularReflection = specularReflection,
            auraBlobColors = auraBlobColors,
            glowColor = primaryColor.copy(alpha = 0.35f),
            searchBarBackground = when (effectiveTranslucency) {
                TranslucencyLevel.CRYSTAL -> Color(0x351E2538)
                TranslucencyLevel.FROSTED -> Color(0x601E2538)
                TranslucencyLevel.SOFT -> Color(0x881E2538)
                TranslucencyLevel.OPAQUE -> Color(0xFF1E2538)
            },
            dividerColor = Color(0x14FFFFFF),
            isLiquidGlass = liquidGlass,
            translucencyLevel = effectiveTranslucency
        )
    } else {
        val cardBg = when (effectiveTranslucency) {
            TranslucencyLevel.CRYSTAL -> cLight.copy(alpha = 0.48f)
            TranslucencyLevel.FROSTED -> cLight.copy(alpha = 0.75f)
            TranslucencyLevel.SOFT -> cLight.copy(alpha = 0.90f)
            TranslucencyLevel.OPAQUE -> Color(0xFFFFFFFF)
        }
        val elevatedBg = when (effectiveTranslucency) {
            TranslucencyLevel.CRYSTAL -> Color(0x88FFFFFF)
            TranslucencyLevel.FROSTED -> Color(0xD0FFFFFF)
            TranslucencyLevel.SOFT -> Color(0xF4FFFFFF)
            TranslucencyLevel.OPAQUE -> Color(0xFFFFFFFF)
        }
        val borderAlpha = if (liquidGlass) (effectiveTranslucency.glassBorderAlpha * 0.8f) else 0.15f
        val border = if (liquidGlass) primaryColor.copy(alpha = borderAlpha) else Color(0x18000000)
        val borderElevated = if (liquidGlass) primaryColor.copy(alpha = (borderAlpha + 0.15f).coerceAtMost(0.5f)) else Color(0x22000000)

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
            bottomNavBackground = when (effectiveTranslucency) {
                TranslucencyLevel.CRYSTAL -> Color(0x85FFFFFF)
                TranslucencyLevel.FROSTED -> Color(0xD8FFFFFF)
                TranslucencyLevel.SOFT -> Color(0xF4FFFFFF)
                TranslucencyLevel.OPAQUE -> Color(0xFFFFFFFF)
            },
            bottomNavBorder = border,
            primaryAccent = primaryColor,
            secondaryAccent = secondaryColor,
            tertiaryAccent = tertiaryColor,
            accentGradient = accentGradient,
            buttonGradient = buttonGradient,
            heroGradient = heroGradient,
            moltenSheenGradient = moltenSheenGradient,
            specularReflection = specularReflection,
            auraBlobColors = auraBlobColors,
            glowColor = primaryColor.copy(alpha = 0.22f),
            searchBarBackground = when (effectiveTranslucency) {
                TranslucencyLevel.CRYSTAL -> Color(0x50E2E8F0)
                TranslucencyLevel.FROSTED -> Color(0x85E2E8F0)
                TranslucencyLevel.SOFT -> Color(0xB5E2E8F0)
                TranslucencyLevel.OPAQUE -> Color(0xFFF1F5F9)
            },
            dividerColor = Color(0x0F000000),
            isLiquidGlass = liquidGlass,
            translucencyLevel = effectiveTranslucency
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
    fontPreset: FontPreset = FontPreset.DEFAULT,
    customFontPath: String? = null,
    reduceTransparency: Boolean = false,
    translucencyLevel: TranslucencyLevel = TranslucencyLevel.FROSTED,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val glassColors = remember(isDark, pastelTheme, liquidGlassEnabled, reduceTransparency, translucencyLevel) {
        getGlassColors(
            isDark = isDark,
            preset = pastelTheme,
            liquidGlass = liquidGlassEnabled,
            reduceTransparency = reduceTransparency,
            translucencyLevel = translucencyLevel
        )
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = glassColors.primaryAccent,
            secondary = glassColors.secondaryAccent,
            tertiary = glassColors.tertiaryAccent,
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
            tertiary = glassColors.tertiaryAccent,
            background = glassColors.canvasSolid,
            surface = glassColors.cardBackground,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = glassColors.textPrimary,
            onSurface = glassColors.textPrimary
        )
    }

    val fontScale = fontSize.scale
    val fontFamily = remember(fontPreset, customFontPath) {
        resolveAppFontFamily(fontPreset, customFontPath)
    }
    val scaledTypography = remember(fontScale, fontFamily) {
        createScaledTypography(scale = fontScale, customFontFamily = fontFamily)
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
