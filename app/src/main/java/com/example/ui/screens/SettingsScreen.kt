package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.preferences.NoteFontSize
import com.example.data.preferences.ThemeMode
import com.example.ui.components.GlassCard
import com.example.ui.components.SegmentedThemeControl
import com.example.ui.theme.MercuryBlue
import com.example.ui.theme.MercuryPink
import com.example.ui.theme.MercuryTheme
import com.example.ui.theme.MercuryViolet
import com.example.ui.viewmodel.NotesViewModel

@Composable
fun SettingsScreen(
    viewModel: NotesViewModel,
    onOpenTrash: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val glass = MercuryTheme.glass
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val autoSave by viewModel.autoSave.collectAsStateWithLifecycle()
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val reduceTransparency by viewModel.reduceTransparency.collectAsStateWithLifecycle()
    val reduceMotion by viewModel.reduceMotion.collectAsStateWithLifecycle()
    val biometricLock by viewModel.biometricLockEnabled.collectAsStateWithLifecycle()
    val deletedCount by viewModel.deletedNotesCount.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(glass.canvasBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.displayMedium,
                    color = glass.textPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)
                )
            }

            // Appearance Section
            item {
                SettingsSectionHeader("Appearance")

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = glass.cardBackground
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Theme Mode",
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
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Preferences Section
            item {
                SettingsSectionHeader("Preferences")

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = glass.cardBackground
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsToggleRow(
                            icon = Icons.Default.Save,
                            iconColor = MercuryViolet,
                            title = "Auto Save",
                            subtitle = "Automatically save notes while typing",
                            checked = autoSave,
                            onCheckedChange = { viewModel.setAutoSave(it) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = glass.dividerColor
                        )

                        SettingsToggleRow(
                            icon = Icons.Default.Opacity,
                            iconColor = MercuryBlue,
                            title = "Reduce Transparency",
                            subtitle = "Use solid background cards for readability",
                            checked = reduceTransparency,
                            onCheckedChange = { viewModel.setReduceTransparency(it) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = glass.dividerColor
                        )

                        SettingsToggleRow(
                            icon = Icons.Default.Animation,
                            iconColor = MercuryPink,
                            title = "Reduce Motion",
                            subtitle = "Simplify screen transitions and motion",
                            checked = reduceMotion,
                            onCheckedChange = { viewModel.setReduceMotion(it) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = glass.dividerColor
                        )

                        SettingsToggleRow(
                            icon = Icons.Default.Fingerprint,
                            iconColor = Color(0xFF10B981),
                            title = "App Lock / Biometrics",
                            subtitle = "Require PIN or biometric auth to access app",
                            checked = biometricLock,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showPinDialog = true
                                } else {
                                    viewModel.setBiometricLock(false)
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Font Size Section
            item {
                SettingsSectionHeader("Typography")

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = glass.cardBackground
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FormatSize, contentDescription = null, tint = MercuryViolet)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Editor Font Size",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = glass.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = fontSize.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MercuryViolet,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        SegmentedThemeControl(
                            selectedOption = when (fontSize) {
                                NoteFontSize.SMALL -> 0
                                NoteFontSize.MEDIUM -> 1
                                NoteFontSize.LARGE -> 2
                            },
                            options = listOf("Small", "Standard", "Large"),
                            onSelect = { index ->
                                val size = when (index) {
                                    0 -> NoteFontSize.SMALL
                                    1 -> NoteFontSize.MEDIUM
                                    else -> NoteFontSize.LARGE
                                }
                                viewModel.setFontSize(size)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Data & Storage
            item {
                SettingsSectionHeader("Data & Storage")

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = glass.cardBackground
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsNavRow(
                            icon = Icons.Default.Delete,
                            iconColor = Color(0xFFEF4444),
                            title = "Recently Deleted",
                            badge = if (deletedCount > 0) "$deletedCount" else null,
                            onClick = onOpenTrash
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = glass.dividerColor
                        )

                        SettingsNavRow(
                            icon = Icons.Default.Share,
                            iconColor = MercuryViolet,
                            title = "Export All Notes (Markdown)",
                            onClick = { viewModel.exportAllNotes(context, formatMarkdown = true) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = glass.dividerColor
                        )

                        SettingsNavRow(
                            icon = Icons.Default.Share,
                            iconColor = MercuryBlue,
                            title = "Export All Notes (Plain Text)",
                            onClick = { viewModel.exportAllNotes(context, formatMarkdown = false) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // About Section
            item {
                SettingsSectionHeader("About")

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = glass.cardBackground
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsNavRow(
                            icon = Icons.Default.Info,
                            iconColor = MercuryPink,
                            title = "About Mercurynotes",
                            subtitle = "Version 1.0.0",
                            onClick = { showAboutDialog = true }
                        )
                    }
                }

                // Footer branding
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Mercurynotes — Think. Write. Organize.",
                        style = MaterialTheme.typography.labelMedium,
                        color = glass.textMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
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
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MercuryViolet)
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.mercurynotes_icon),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Mercurynotes", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Think. Write. Organize.",
                        fontWeight = FontWeight.SemiBold,
                        color = MercuryViolet,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A premium minimalist notes application designed with frosted glass aesthetics, smooth 60 FPS performance, rich formatting, and local offline Room database storage.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = glass.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Version 1.0.0 • 2026",
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
                    Text("Close")
                }
            }
        )
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
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                checkedTrackColor = MercuryViolet,
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
