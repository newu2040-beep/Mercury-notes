package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class NoteFontSize(val displayName: String, val scale: Float) {
    SMALL("Small", 0.9f),
    MEDIUM("Standard", 1.0f),
    LARGE("Large", 1.15f)
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

    var reduceTransparency: Boolean
        get() = prefs.getBoolean("reduce_transparency", false)
        set(value) = prefs.edit().putBoolean("reduce_transparency", value).apply()

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
