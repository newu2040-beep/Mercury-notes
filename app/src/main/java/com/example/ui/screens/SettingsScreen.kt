package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.preferences.FontPreset
import com.example.data.preferences.NoteFontSize
import com.example.data.preferences.PastelThemePreset
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.TranslucencyLevel
import com.example.ui.components.GlassCard
import com.example.ui.components.MoltenGlassCard
import com.example.ui.components.SegmentedThemeControl
import com.example.ui.theme.MercuryBlue
import com.example.ui.theme.MercuryPink
import com.example.ui.theme.MercuryTheme
import com.example.ui.theme.MercuryViolet
import com.example.ui.viewmodel.NotesViewModel
import com.example.util.BiometricAuthHelper
import com.example.util.FileImporter

@Composable
fun SettingsScreen(
    viewModel: NotesViewModel,
    onOpenTrash: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val glass = MercuryTheme.glass
    val isCompact = MercuryTheme.isCompact
    val fontScale = MercuryTheme.fontScale

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val pastelTheme by viewModel.pastelTheme.collectAsStateWithLifecycle()
    val liquidGlassEnabled by viewModel.liquidGlassEnabled.collectAsStateWithLifecycle()
    val compactMode by viewModel.compactMode.collectAsStateWithLifecycle()
    val autoSave by viewModel.autoSave.collectAsStateWithLifecycle()
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val fontPreset by viewModel.fontPreset.collectAsStateWithLifecycle()
    val customFontDisplayName by viewModel.customFontDisplayName.collectAsStateWithLifecycle()
    val reduceTransparency by viewModel.reduceTransparency.collectAsStateWithLifecycle()
    val translucencyLevel by viewModel.translucencyLevel.collectAsStateWithLifecycle()
    val ambientBackdropGlow by viewModel.ambientBackdropGlow.collectAsStateWithLifecycle()
    val highRefreshRateEnabled by viewModel.highRefreshRateEnabled.collectAsStateWithLifecycle()
    val reduceMotion by viewModel.reduceMotion.collectAsStateWithLifecycle()
    val biometricLock by viewModel.biometricLockEnabled.collectAsStateWithLifecycle()
    val deletedCount by viewModel.deletedNotesCount.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPermissionsDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }

    // Universal File Picker Launcher for Settings (Notes, Docs, etc.)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val imported = FileImporter.parseImportedUri(context, uri)
            viewModel.importNoteData(imported) { newId ->
                Toast.makeText(context, "Imported as new note: ${imported.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Dedicated Font File Picker Launcher (.ttf, .otf)
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importFontFromUri(uri) { success, result ->
                if (success) {
                    Toast.makeText(context, "Applied custom font: $result", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Font import error: $result", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Permission request launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        if (allGranted) {
            Toast.makeText(context, "All requested permissions granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permissions updated", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(glass.canvasBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = if (isCompact) 12.dp else 16.dp,
                end = if (isCompact) 12.dp else 16.dp,
                top = if (isCompact) 12.dp else 20.dp,
                bottom = 120.dp
            )
        ) {
            // Header
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.displayMedium,
                    color = glass.textPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Pastel Themes Palette Selector
            item {
                SettingsSectionHeader("Pastel Color Themes")

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = glass.cardBackground
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Choose Visual Theme",
                            style = MaterialTheme.typography.titleSmall,
                            color = glass.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Curated pastel palettes with frosted glass lighting",
                            style = MaterialTheme.typography.bodySmall,
                            color = glass.textMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(PastelThemePreset.values()) { preset ->
                                val isSelected = pastelTheme == preset
                                val (color1, color2) = getThemeSwatchColors(preset)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { viewModel.setPastelTheme(preset) }
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Brush.linearGradient(listOf(color1, color2)))
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) MercuryViolet else glass.cardBorder,
                                                shape = RoundedCornerShape(14.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = preset.displayName.replace("Pastel ", "").replace("Liquid Glass ", ""),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) glass.textPrimary else glass.textMuted,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Appearance & Glass Effects
            item {
                SettingsSectionHeader("Appearance & Glass Effects")

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = glass.cardBackground
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        // Theme Mode Light/Dark/System
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                            Text(
                                text = "Light & Dark Appearance",
                                style = MaterialTheme.typography.titleSmall,
                                color = glass.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            SegmentedThemeControl(
                                selectedOption = when (themeMode) {
                                    ThemeMode.LIGHT -> 0
                                    ThemeMode.DARK -> 1
                                    ThemeMode.SYSTEM -> 2
                                },
                                options = listOf("Light", "Dark", "System"),
                                onSelect = { index ->
                                    val mode = when (index) {
                                        0 -> ThemeMode.LIGHT
                                        1 -> ThemeMode.DARK
                                        else -> ThemeMode.SYSTEM
                                    }
                                    viewModel.setThemeMode(mode)
                                }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = glass.dividerColor)

                        // True Liquid Glass Effect Toggle
                        SettingsToggleRow(
                            icon = Icons.Default.Opacity,
                            iconColor = MercuryViolet,
                            title = "True Liquid Glass Effect",
                            subtitle = "Dynamic chromatic refraction & specular lighting",
                            checked = liquidGlassEnabled,
                            onCheckedChange = { viewModel.setLiquidGlassEnabled(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = glass.dividerColor)

                        // Compact Mode for Small Displays
                        SettingsToggleRow(
                            icon = Icons.Default.StayCurrentPortrait,
                            iconColor = MercuryBlue,
                            title = "Compact Mode",
                            subtitle = "Optimized padding & density for smaller displays",
                            checked = compactMode,
                            onCheckedChange = { viewModel.setCompactMode(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = glass.dividerColor)

                        val displayModes = remember(context) {
                            val display = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                context.display
                            } else {
                                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
                                @Suppress("DEPRECATION")
                                windowManager.defaultDisplay
                            }
                            display?.supportedModes?.map { it.refreshRate.toInt() }?.distinct()?.sorted() ?: emptyList()
                        }
                        val maxRate = displayModes.maxOrNull() ?: 60
                        val supportedRatesStr = displayModes.joinToString(", ") { "${it}Hz" }

                        SettingsToggleRow(
                            icon = Icons.Default.AutoAwesome,
                            iconColor = Color(0xFFF59E0B),
                            title = "High Refresh Rate",
                            subtitle = if (displayModes.size > 1) "Smooth UI at ${maxRate}Hz (Supported: $supportedRatesStr)" else "Requires > 60Hz display support",
                            checked = highRefreshRateEnabled,
                            onCheckedChange = { viewModel.setHighRefreshRateEnabled(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = glass.dividerColor)

                        SettingsToggleRow(
                            icon = Icons.Default.Animation,
                            iconColor = MercuryPink,
                            title = "Reduce Transparency",
                            subtitle = "Use solid background cards for maximum contrast",
                            checked = reduceTransparency,
                            onCheckedChange = { viewModel.setReduceTransparency(it) }
                        )

                        if (!reduceTransparency) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = glass.dividerColor)

                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                Text(
                                    text = "Glass Translucency Level",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = glass.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                SegmentedThemeControl(
                                    selectedOption = when (translucencyLevel) {
                                        TranslucencyLevel.CRYSTAL -> 0
                                        TranslucencyLevel.FROSTED -> 1
                                        TranslucencyLevel.SOFT -> 2
                                        TranslucencyLevel.OPAQUE -> 3
                                    },
                                    options = listOf("Crystal", "Frosted", "Soft", "Opaque"),
                                    onSelect = { index ->
                                        val level = when (index) {
                                            0 -> TranslucencyLevel.CRYSTAL
                                            1 -> TranslucencyLevel.FROSTED
                                            2 -> TranslucencyLevel.SOFT
                                            else -> TranslucencyLevel.OPAQUE
                                        }
                                        viewModel.setTranslucencyLevel(level)
                                    }
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = glass.dividerColor)

                            SettingsToggleRow(
                                icon = Icons.Default.ColorLens,
                                iconColor = MercuryBlue,
                                title = "Ambient Backdrop Glow",
                                subtitle = "Floating aura light blobs behind the UI",
                                checked = ambientBackdropGlow,
                                onCheckedChange = { viewModel.setAmbientBackdropGlow(it) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Typography, Custom Font & Dynamic Scale
            item {
                SettingsSectionHeader("Typography & App Font")

                MoltenGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = glass.cardBackground
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Font Family / Typeface Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TextFields, contentDescription = null, tint = glass.primaryAccent)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "App Font Style",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = glass.textPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = when {
                                            fontPreset == FontPreset.CUSTOM && !customFontDisplayName.isNullOrBlank() -> "Custom: $customFontDisplayName"
                                            else -> fontPreset.displayName
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = glass.secondaryAccent,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            if (fontPreset != FontPreset.DEFAULT) {
                                TextButton(
                                    onClick = { viewModel.resetToDefaultFont() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Reset", color = glass.secondaryAccent, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        SegmentedThemeControl(
                            selectedOption = when (fontPreset) {
                                FontPreset.DEFAULT -> 0
                                FontPreset.SERIF -> 1
                                FontPreset.MONOSPACE -> 2
                                FontPreset.CURSIVE -> 3
                                FontPreset.CUSTOM -> -1
                            },
                            options = listOf("Default", "Serif", "Mono", "Script"),
                            onSelect = { index ->
                                val selected = when (index) {
                                    0 -> FontPreset.DEFAULT
                                    1 -> FontPreset.SERIF
                                    2 -> FontPreset.MONOSPACE
                                    else -> FontPreset.CURSIVE
                                }
                                viewModel.setFontPreset(selected)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Font Importer Button (.ttf / .otf)
                        Button(
                            onClick = { fontPickerLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = glass.searchBarBackground,
                                contentColor = glass.textPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, glass.cardBorder)
                        ) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = glass.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (fontPreset == FontPreset.CUSTOM && !customFontDisplayName.isNullOrBlank())
                                    "Change Font (Current: $customFontDisplayName)"
                                else
                                    "Import Font (.ttf, .otf) & Set as App Font",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = glass.dividerColor)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Font Scaling
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FormatSize, contentDescription = null, tint = glass.primaryAccent)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Dynamic Font Scaling",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = glass.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = fontSize.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = glass.primaryAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        SegmentedThemeControl(
                            selectedOption = when (fontSize) {
                                NoteFontSize.SMALL -> 0
                                NoteFontSize.MEDIUM -> 1
                                NoteFontSize.LARGE -> 2
                                NoteFontSize.EXTRA_LARGE -> 3
                            },
                            options = listOf("Small", "Medium", "Large", "XL"),
                            onSelect = { index ->
                                val size = when (index) {
                                    0 -> NoteFontSize.SMALL
                                    1 -> NoteFontSize.MEDIUM
                                    2 -> NoteFontSize.LARGE
                                    else -> NoteFontSize.EXTRA_LARGE
                                }
                                viewModel.setFontSize(size)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Live Preview Box showing the scaled and styled font
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(glass.searchBarBackground)
                                .border(1.dp, glass.cardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Preview: The quick brown fox jumps over the lazy dog. 12345",
                                style = MaterialTheme.typography.bodyMedium,
                                color = glass.textPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Security, Biometrics & PIN Lock
            item {
                SettingsSectionHeader("Security & System Access")

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = glass.cardBackground
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsToggleRow(
                            icon = Icons.Default.Fingerprint,
                            iconColor = Color(0xFF10B981),
                            title = "Native Biometric & Face Lock",
                            subtitle = "Hardware face unlock, fingerprint or device PIN",
                            checked = biometricLock,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    val activity = context as? FragmentActivity
                                    if (activity != null && BiometricAuthHelper.isBiometricAvailable(context)) {
                                        BiometricAuthHelper.promptBiometricUnlock(
                                            activity = activity,
                                            title = "Set Up Biometric Lock",
                                            subtitle = "Verify Face or Fingerprint to enable",
                                            onSuccess = {
                                                viewModel.setBiometricLock(true)
                                                Toast.makeText(context, "Biometric Lock Enabled!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                showPinDialog = true
                                            }
                                        )
                                    } else {
                                        showPinDialog = true
                                    }
                                } else {
                                    viewModel.setBiometricLock(false)
                                }
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = glass.dividerColor)

                        SettingsNavRow(
                            icon = Icons.Default.Security,
                            iconColor = MercuryViolet,
                            title = "App Permissions Manager",
                            subtitle = "Notifications, Gallery, Mic & Camera full access",
                            onClick = { showPermissionsDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = glass.dividerColor)

                        SettingsToggleRow(
                            icon = Icons.Default.Save,
                            iconColor = MercuryBlue,
                            title = "Auto Save Notes",
                            subtitle = "Instant background persistence while typing",
                            checked = autoSave,
                            onCheckedChange = { viewModel.setAutoSave(it) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Universal File Import, Export & Data Management
            item {
                SettingsSectionHeader("Data & Document Management")

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = glass.cardBackground
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsNavRow(
                            icon = Icons.Default.AttachFile,
                            iconColor = MercuryViolet,
                            title = "Import Files, Fonts & Documents",
                            subtitle = "Supports PDF, TXT, CSV, JSON, ZIP, TTF/OTF, Images",
                            onClick = { filePickerLauncher.launch("*/*") }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = glass.dividerColor)

                        SettingsNavRow(
                            icon = Icons.Default.Delete,
                            iconColor = Color(0xFFEF4444),
                            title = "Recently Deleted (Trash)",
                            badge = if (deletedCount > 0) "$deletedCount" else null,
                            onClick = onOpenTrash
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = glass.dividerColor)

                        SettingsNavRow(
                            icon = Icons.Default.DeleteSweep,
                            iconColor = Color(0xFFEF4444),
                            title = "Clear Sample Data / Reset Database",
                            subtitle = "Wipe demo notes and start with clean real data",
                            onClick = { showClearDataDialog = true }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // About & Developer Credits
            item {
                SettingsSectionHeader("About & Credits")

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = glass.cardBackground
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsNavRow(
                            icon = Icons.Default.Info,
                            iconColor = MercuryPink,
                            title = "About Mercurynotes",
                            subtitle = "Version 1.0.0 • Architecture & Features",
                            onClick = { showAboutDialog = true }
                        )
                    }
                }

                // Footer branding
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Made with ❤️ by Rahul Shah",
                        style = MaterialTheme.typography.titleSmall,
                        color = MercuryViolet,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mercurynotes • Liquid Glass Note Experience",
                        style = MaterialTheme.typography.labelSmall,
                        color = glass.textMuted
                    )
                }
            }
        }
    }

    // Permissions Dialog
    if (showPermissionsDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionsDialog = false },
            title = { Text("App Permissions & Full Access", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Mercurynotes uses system permissions to give you full capability:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = glass.textSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    PermissionItemRow(Icons.Default.Notifications, MercuryViolet, "Notifications", "Real-time task reminders and auto-save updates")
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionItemRow(Icons.Default.PhotoLibrary, MercuryBlue, "Photos & Gallery", "Attach photos and media directly to your notes")
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionItemRow(Icons.Default.Mic, MercuryPink, "Microphone", "Voice dictation and hands-free speech-to-text")
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionItemRow(Icons.Default.CameraAlt, Color(0xFF10B981), "Camera", "Capture documents and receipts into notes")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionsDialog = false
                        val permissionsList = mutableListOf(
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.CAMERA
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionsList.add(Manifest.permission.POST_NOTIFICATIONS)
                            permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        permissionsLauncher.launch(permissionsList.toTypedArray())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MercuryViolet)
                ) {
                    Text("Grant Full Access")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Set PIN dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set App Lock PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter a 4-digit PIN to lock Mercurynotes:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        singleLine = true,
                        placeholder = { Text("1234") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_setup_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pin = if (pinInput.isNotBlank()) pinInput else "1234"
                        viewModel.setBiometricLock(true, pin)
                        showPinDialog = false
                        Toast.makeText(context, "App Lock enabled with PIN!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MercuryViolet)
                ) {
                    Text("Enable PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Data confirmation dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Notes?", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
            text = {
                Text("This will remove all sample notes from the database, giving you a clean slate to add your own real notes and files.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllNotes()
                        showClearDataDialog = false
                        Toast.makeText(context, "Database cleared! Ready for real notes.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // About Dialog with Developer Credit
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.mercurynotes_logo_1787768712898),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Mercurynotes", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Made with ❤️ by Rahul Shah",
                        fontWeight = FontWeight.Bold,
                        color = MercuryViolet,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A modern, liquid glass note-taking powerhouse crafted for maximum fluidity and productivity.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = glass.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• True Liquid Glass Chromatic Themes\n• Real-Time Voice Typing\n• Native Biometric & Face Lock\n• Universal File Import (PDF, TXT, CSV, JSON, Fonts)\n• Multi-Format Export (PDF, CSV, TXT, Markdown, JSON)\n• Compact Layout Mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = glass.textMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Version 1.0.0 (Release 2026)",
                        style = MaterialTheme.typography.labelSmall,
                        color = glass.textMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MercuryViolet)
                ) {
                    Text("Awesome")
                }
            }
        )
    }
}

@Composable
fun PermissionItemRow(icon: ImageVector, iconColor: Color, title: String, description: String) {
    val glass = MercuryTheme.glass
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = glass.textPrimary)
            Text(description, style = MaterialTheme.typography.bodySmall, color = glass.textMuted)
        }
    }
}

fun getThemeSwatchColors(preset: PastelThemePreset): Pair<Color, Color> {
    return when (preset) {
        PastelThemePreset.MERCURY -> Color(0xFF8B5CF6) to Color(0xFF3B82F6)
        PastelThemePreset.LAVENDER -> Color(0xFFA78BFA) to Color(0xFFC084FC)
        PastelThemePreset.PEACH -> Color(0xFFFB923C) to Color(0xFFF472B6)
        PastelThemePreset.MINT -> Color(0xFF34D399) to Color(0xFF2DD4BF)
        PastelThemePreset.ROSE -> Color(0xFFF472B6) to Color(0xFFFB7185)
        PastelThemePreset.OCEAN -> Color(0xFF38BDF8) to Color(0xFF818CF8)
        PastelThemePreset.LIQUID_OPAL -> Color(0xFF818CF8) to Color(0xFFF472B6)
        PastelThemePreset.MIDNIGHT -> Color(0xFFC084FC) to Color(0xFF1E293B)
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    val glass = MercuryTheme.glass
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = glass.textSecondary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
    )
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val glass = MercuryTheme.glass

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = glass.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = glass.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = glass.primaryAccent,
                uncheckedThumbColor = glass.textMuted,
                uncheckedTrackColor = glass.searchBarBackground
            )
        )
    }
}

@Composable
fun SettingsNavRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    onClick: () -> Unit
) {
    val glass = MercuryTheme.glass

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = glass.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = glass.textSecondary
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = glass.textMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
