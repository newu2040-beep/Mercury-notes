package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FolderEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingGradientButton
import com.example.ui.theme.MercuryBlue
import com.example.ui.theme.MercuryPink
import com.example.ui.theme.MercuryPrimaryGradient
import com.example.ui.theme.MercuryTheme
import com.example.ui.theme.MercuryViolet
import com.example.ui.viewmodel.NotesViewModel

@Composable
fun FoldersScreen(
    viewModel: NotesViewModel,
    onFolderSelected: (Long) -> Unit,
    onNewNoteInFolder: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = MercuryTheme.glass
    val folders by viewModel.allFolders.collectAsStateWithLifecycle()
    val allNotes by viewModel.allActiveNotes.collectAsStateWithLifecycle()
    val activeCount by viewModel.activeNotesCount.collectAsStateWithLifecycle()
    val deletedCount by viewModel.deletedNotesCount.collectAsStateWithLifecycle()

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderToEdit by remember { mutableStateOf<FolderEntity?>(null) }
    var folderToDelete by remember { mutableStateOf<FolderEntity?>(null) }

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Folders",
                            style = MaterialTheme.typography.displayMedium,
                            color = glass.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${folders.size} collections • $activeCount notes",
                            style = MaterialTheme.typography.labelMedium,
                            color = glass.textSecondary
                        )
                    }

                    GlassCard(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        backgroundColor = glass.searchBarBackground,
                        onClick = { showCreateFolderDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "New Folder",
                            tint = glass.primaryAccent,
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }

            // Stats Overview Row
            item {
                StatsOverviewSection(
                    totalNotes = activeCount,
                    pinnedNotes = allNotes.count { it.isPinned },
                    favorites = allNotes.count { it.isFavorite },
                    trashCount = deletedCount
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Folder Cards List
            item {
                Text(
                    text = "Collections",
                    style = MaterialTheme.typography.titleSmall,
                    color = glass.textSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            items(folders, key = { it.id }) { folder ->
                val noteCountInFolder = remember(allNotes, folder.id) {
                    allNotes.count { it.folderId == folder.id }
                }

                FolderCardItem(
                    folder = folder,
                    noteCount = noteCountInFolder,
                    onClick = { onFolderSelected(folder.id) },
                    onEdit = { folderToEdit = folder },
                    onDelete = { folderToDelete = folder }
                )
            }
        }

        // Floating Action Button to create folder
        GlowingGradientButton(
            text = "New Folder",
            icon = Icons.Default.Add,
            onClick = { showCreateFolderDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 28.dp),
            testTag = "create_folder_fab"
        )
    }

    // Create / Edit Folder Dialog
    if (showCreateFolderDialog || folderToEdit != null) {
        val isEditing = folderToEdit != null
        var folderName by remember { mutableStateOf(folderToEdit?.name ?: "") }
        var selectedColor by remember {
            mutableStateOf(folderToEdit?.color ?: 0xFF8A5CF6)
        }

        val availableColors = listOf(
            0xFF8A5CF6, // Violet
            0xFF3B82F6, // Blue
            0xFF06B6D4, // Cyan
            0xFF10B981, // Teal
            0xFFF59E0B, // Amber
            0xFFEC4899, // Pink
            0xFFEF4444  // Red
        )

        AlertDialog(
            onDismissRequest = {
                showCreateFolderDialog = false
                folderToEdit = null
            },
            title = {
                Text(if (isEditing) "Edit Folder" else "New Folder", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        label = { Text("Folder Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("folder_name_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select Color", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        availableColors.forEach { colorVal ->
                            val isSelected = selectedColor == colorVal
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorVal))
                                    .clickable { selectedColor = colorVal },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (folderName.isNotBlank()) {
                            if (isEditing && folderToEdit != null) {
                                viewModel.updateFolder(
                                    folderToEdit!!.copy(
                                        name = folderName.trim(),
                                        color = selectedColor
                                    )
                                )
                            } else {
                                viewModel.createFolder(
                                    name = folderName.trim(),
                                    color = selectedColor
                                )
                            }
                            showCreateFolderDialog = false
                            folderToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = glass.primaryAccent)
                ) {
                    Text(if (isEditing) "Save" else "Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateFolderDialog = false
                        folderToEdit = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (folderToDelete != null) {
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text("Delete Folder?") },
            text = {
                Text("Notes inside \"${folderToDelete?.name}\" will be moved to the Personal folder.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        folderToDelete?.let { viewModel.deleteFolder(it.id) }
                        folderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatsOverviewSection(
    totalNotes: Int,
    pinnedNotes: Int,
    favorites: Int,
    trashCount: Int
) {
    val glass = MercuryTheme.glass

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatPillCard(
            modifier = Modifier.weight(1f),
            title = "All Notes",
            value = totalNotes.toString(),
            color = glass.primaryAccent
        )
        StatPillCard(
            modifier = Modifier.weight(1f),
            title = "Pinned",
            value = pinnedNotes.toString(),
            color = glass.secondaryAccent
        )
        StatPillCard(
            modifier = Modifier.weight(1f),
            title = "Favorites",
            value = favorites.toString(),
            color = glass.secondaryAccent
        )
    }
}

@Composable
fun StatPillCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val glass = MercuryTheme.glass

    GlassCard(
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = glass.cardBackground,
        borderColor = glass.cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = glass.textSecondary
            )
        }
    }
}

@Composable
fun FolderCardItem(
    folder: FolderEntity,
    noteCount: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val glass = MercuryTheme.glass
    var menuOpen by remember { mutableStateOf(false) }

    val iconVector: ImageVector = when (folder.name.lowercase()) {
        "work" -> Icons.Default.Work
        "personal" -> Icons.Default.Person
        "ideas" -> Icons.Default.Lightbulb
        "journal" -> Icons.Default.Bookmark
        else -> Icons.Default.Folder
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("folder_card_${folder.name}"),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = glass.cardBackground,
        borderColor = glass.cardBorder,
        borderWidth = 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Folder Color Icon Box
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(folder.color).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = Color(folder.color),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = glass.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$noteCount ${if (noteCount == 1) "note" else "notes"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = glass.textSecondary
                    )
                }
            }

            // Options menu (if not default)
            Box {
                IconButton(
                    onClick = { menuOpen = true },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Folder options",
                        tint = glass.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    modifier = Modifier.background(glass.cardBackgroundElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename / Edit") },
                        onClick = {
                            menuOpen = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )

                    if (!folder.isDefault) {
                        DropdownMenuItem(
                            text = { Text("Delete Folder", color = Color(0xFFEF4444)) },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                            }
                        )
                    }
                }
            }
        }
    }
}
