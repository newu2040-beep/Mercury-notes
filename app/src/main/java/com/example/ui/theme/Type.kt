package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun createScaledTypography(scale: Float = 1.0f, customFontFamily: FontFamily = FontFamily.SansSerif): Typography {
    val s = scale.coerceIn(0.75f, 1.6f)
    return Typography(
        displayLarge = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (34 * s).sp,
            lineHeight = (40 * s).sp,
            letterSpacing = (-0.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (28 * s).sp,
            lineHeight = (34 * s).sp,
            letterSpacing = (-0.3).sp
        ),
        titleLarge = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (22 * s).sp,
            lineHeight = (28 * s).sp,
            letterSpacing = (-0.2).sp
        ),
        titleMedium = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (18 * s).sp,
            lineHeight = (24 * s).sp,
            letterSpacing = (-0.1).sp
        ),
        titleSmall = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (15 * s).sp,
            lineHeight = (20 * s).sp,
            letterSpacing = 0.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * s).sp,
            lineHeight = (24 * s).sp,
            letterSpacing = 0.2.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * s).sp,
            lineHeight = (16 * s).sp,
            letterSpacing = 0.3.sp
        ),
        labelLarge = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * s).sp,
            lineHeight = (16 * s).sp,
            letterSpacing = 0.4.sp
        ),
        labelSmall = TextStyle(
            fontFamily = customFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (11 * s).sp,
            lineHeight = (14 * s).sp,
            letterSpacing = 0.5.sp
        )
    )
}

val Typography = createScaledTypography(1.0f)
