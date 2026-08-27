package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class PastelThemePreset(val id: String, val displayName: String) {
    MERCURY("mercury", "Classic Mercury"),
    LAVENDER("lavender", "Pastel Lavender"),
    PEACH("peach", "Pastel Peach"),
    MINT("mint", "Pastel Mint"),
    ROSE("rose", "Pastel Sakura"),
    OCEAN("ocean", "Pastel Ocean"),
    LIQUID_OPAL("opal", "Liquid Glass Opal"),
    MIDNIGHT("midnight", "Midnight Obsidian")
}

enum class NoteFontSize(val displayName: String, val scale: Float) {
    SMALL("Small", 0.85f),
    MEDIUM("Standard", 1.0f),
    LARGE("Large", 1.20f),
    EXTRA_LARGE("Extra Large", 1.35f)
}

enum class FontPreset(val id: String, val displayName: String) {
    DEFAULT("default", "System Default (Sans)"),
    SERIF("serif", "Elegant Serif"),
    MONOSPACE("monospace", "Modern Monospace"),
    CURSIVE("cursive", "Handwritten / Script"),
    CUSTOM("custom", "Imported Custom Font")
}

enum class TranslucencyLevel(
    val id: String,
    val displayName: String,
    val darkCardAlpha: Float,
    val lightCardAlpha: Float,
    val navAlpha: Float,
    val glassBorderAlpha: Float,
    val description: String
) {
    CRYSTAL("crystal", "Crystal (Ultra)", 0.42f, 0.52f, 0.65f, 0.45f, "Ultra-translucent glass with deep ambient light bleed-through"),
    FROSTED("frosted", "Frosted Glass", 0.70f, 0.78f, 0.85f, 0.35f, "Balanced frosted acrylic glass with shimmering specular sheen"),
    SOFT("soft", "Soft Glass", 0.85f, 0.90f, 0.93f, 0.25f, "Subtle translucent depth for high readability"),
    OPAQUE("opaque", "Solid Opaque", 1.00f, 1.00f, 1.00f, 0.15f, "Solid high-contrast opaque surfaces with no background bleed")
}

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mercury_prefs", Context.MODE_PRIVATE)

    var themeMode: ThemeMode
        get() {
            val value = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
            return try {
                ThemeMode.valueOf(value)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }
        set(value) = prefs.edit().putString("theme_mode", value.name).apply()

    var pastelTheme: PastelThemePreset
        get() {
            val value = prefs.getString("pastel_theme", PastelThemePreset.MERCURY.name) ?: PastelThemePreset.MERCURY.name
            return try {
                PastelThemePreset.valueOf(value)
            } catch (e: Exception) {
                PastelThemePreset.MERCURY
            }
        }
        set(value) = prefs.edit().putString("pastel_theme", value.name).apply()

    var liquidGlassEnabled: Boolean
        get() = prefs.getBoolean("liquid_glass_enabled", true)
        set(value) = prefs.edit().putBoolean("liquid_glass_enabled", value).apply()

    var compactMode: Boolean
        get() = prefs.getBoolean("compact_mode", false)
        set(value) = prefs.edit().putBoolean("compact_mode", value).apply()

    var autoSaveEnabled: Boolean
        get() = prefs.getBoolean("auto_save", true)
        set(value) = prefs.edit().putBoolean("auto_save", value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean("notifications", true)
        set(value) = prefs.edit().putBoolean("notifications", value).apply()

    var fontSize: NoteFontSize
        get() {
            val value = prefs.getString("font_size", NoteFontSize.MEDIUM.name) ?: NoteFontSize.MEDIUM.name
            return try {
                NoteFontSize.valueOf(value)
            } catch (e: Exception) {
                NoteFontSize.MEDIUM
            }
        }
        set(value) = prefs.edit().putString("font_size", value.name).apply()

    var fontPreset: FontPreset
        get() {
            val value = prefs.getString("font_preset", FontPreset.DEFAULT.name) ?: FontPreset.DEFAULT.name
            return try {
                FontPreset.valueOf(value)
            } catch (e: Exception) {
                FontPreset.DEFAULT
            }
        }
        set(value) = prefs.edit().putString("font_preset", value.name).apply()

    var customFontPath: String?
        get() = prefs.getString("custom_font_path", null)
        set(value) = prefs.edit().putString("custom_font_path", value).apply()

    var customFontDisplayName: String?
        get() = prefs.getString("custom_font_display_name", null)
        set(value) = prefs.edit().putString("custom_font_display_name", value).apply()

    var customFontName: String?
        get() = prefs.getString("custom_font_name", null)
        set(value) = prefs.edit().putString("custom_font_name", value).apply()

    var reduceTransparency: Boolean
        get() = prefs.getBoolean("reduce_transparency", false)
        set(value) = prefs.edit().putBoolean("reduce_transparency", value).apply()

    var translucencyLevel: TranslucencyLevel
        get() {
            val value = prefs.getString("translucency_level", TranslucencyLevel.FROSTED.name) ?: TranslucencyLevel.FROSTED.name
            return try {
                TranslucencyLevel.valueOf(value)
            } catch (e: Exception) {
                TranslucencyLevel.FROSTED
            }
        }
        set(value) = prefs.edit().putString("translucency_level", value.name).apply()

    var ambientBackdropGlow: Boolean
        get() = prefs.getBoolean("ambient_backdrop_glow", true)
        set(value) = prefs.edit().putBoolean("ambient_backdrop_glow", value).apply()

    var highRefreshRateEnabled: Boolean
        get() = prefs.getBoolean("high_refresh_rate", false)
        set(value) = prefs.edit().putBoolean("high_refresh_rate", value).apply()

    var reduceMotion: Boolean
        get() = prefs.getBoolean("reduce_motion", false)
        set(value) = prefs.edit().putBoolean("reduce_motion", value).apply()

    var biometricLockEnabled: Boolean
        get() = prefs.getBoolean("biometric_lock", false)
        set(value) = prefs.edit().putBoolean("biometric_lock", value).apply()

    var appPinCode: String
        get() = prefs.getString("app_pin_code", "1234") ?: "1234"
        set(value) = prefs.edit().putString("app_pin_code", value).apply()

    var heroDismissed: Boolean
        get() = prefs.getBoolean("hero_dismissed", false)
        set(value) = prefs.edit().putBoolean("hero_dismissed", value).apply()
}
