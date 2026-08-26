package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.data.preferences.ThemeMode
import com.example.ui.components.FilterChipPill
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingGradientButton
import com.example.ui.theme.MercuryBlue
import com.example.ui.theme.MercuryPink
import com.example.ui.theme.MercuryPrimaryGradient
import com.example.ui.theme.MercuryTheme
import com.example.ui.theme.MercuryViolet
import com.example.ui.viewmodel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: NotesViewModel,
    onNoteClick: (Long) -> Unit,
    onNewNoteClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = MercuryTheme.glass
    val notes by viewModel.homeDisplayNotes.collectAsStateWithLifecycle()
    val folders by viewModel.allFolders.collectAsStateWithLifecycle()
    val selectedFolderId by viewModel.selectedFolderId.collectAsStateWithLifecycle()
    val heroDismissed by viewModel.heroDismissed.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    val pinnedNotes = remember(notes) { notes.filter { it.isPinned } }
    val regularNotes = remember(notes) { notes.filter { !it.isPinned } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(glass.canvasBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header: Logo, Title, Search button, Theme toggle
            item {
                HomeHeader(
                    themeMode = themeMode,
                    onToggleTheme = {
                        val nextTheme = when (themeMode) {
                            ThemeMode.LIGHT -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.LIGHT
                            ThemeMode.SYSTEM -> if (glass.isDark) ThemeMode.LIGHT else ThemeMode.DARK
                        }
                        viewModel.setThemeMode(nextTheme)
                    },
                    onSearchClick = onSearchClick
                )
            }

            // Hero Card ("Good Ideas Live Here" or "Welcome Back")
            if (!heroDismissed) {
                item {
                    HomeHeroCard(
                        notesCount = notes.size,
                        onDismiss = { viewModel.dismissHero() },
                        onQuickNote = onNewNoteClick
                    )
                }
            }

            // Folder Filter Pills
            item {
                FolderFilterRow(
                    folders = folders,
                    selectedFolderId = selectedFolderId,
                    onSelectFolder = { viewModel.setSelectedFolder(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Section: Pinned Notes (if any)
            if (pinnedNotes.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Pinned",
                        icon = Icons.Default.PushPin,
                        count = pinnedNotes.size
                    )
                }

                items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                    NoteCardItem(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        onTogglePin = { viewModel.togglePin(note) },
                        onToggleFavorite = { viewModel.toggleFavorite(note) },
                        onDelete = { viewModel.moveToTrash(note.id) },
                        onDuplicate = { viewModel.duplicateNote(note) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Section: All / Recent Notes
            item {
                SectionHeader(
                    title = if (selectedFolderId != null) {
                        folders.find { it.id == selectedFolderId }?.name ?: "Notes"
                    } else "All Notes",
                    count = notes.size
                )
            }

            if (notes.isEmpty()) {
                item {
                    EmptyNotesState(
                        isFiltered = selectedFolderId != null,
                        onCreateNote = onNewNoteClick
                    )
                }
            } else {
                items(regularNotes, key = { "note_${it.id}" }) { note ->
                    NoteCardItem(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        onTogglePin = { viewModel.togglePin(note) },
                        onToggleFavorite = { viewModel.toggleFavorite(note) },
                        onDelete = { viewModel.moveToTrash(note.id) },
                        onDuplicate = { viewModel.duplicateNote(note) }
                    )
                }
            }
        }

        // Floating Glowing New Note Button
        GlowingGradientButton(
            text = "New Note",
            icon = Icons.Default.Add,
            onClick = onNewNoteClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 28.dp)
        )
    }
}

@Composable
fun HomeHeader(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onSearchClick: () -> Unit
) {
    val glass = MercuryTheme.glass

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon Logo
            Image(
                painter = painterResource(id = R.drawable.mercurynotes_icon),
                contentDescription = "Mercurynotes Logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Mercurynotes",
                    style = MaterialTheme.typography.titleLarge,
                    color = glass.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Capture Ideas • Organize Life",
                    style = MaterialTheme.typography.labelSmall,
                    color = glass.textSecondary
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search shortcut icon
            GlassCard(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                backgroundColor = glass.searchBarBackground,
                onClick = onSearchClick
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = glass.textPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center)
                )
            }

            // Quick Theme switch
            GlassCard(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                backgroundColor = glass.searchBarBackground,
                onClick = onToggleTheme
            ) {
                Icon(
                    imageVector = if (glass.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Switch Theme",
                    tint = if (glass.isDark) Color(0xFFFBBF24) else MercuryViolet,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun HomeHeroCard(
    notesCount: Int,
    onDismiss: () -> Unit,
    onQuickNote: () -> Unit
) {
    val glass = MercuryTheme.glass

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(26.dp),
        backgroundColor = if (glass.isDark) Color(0xCC131A2D) else Color(0xF5FFFFFF),
        borderColor = if (glass.isDark) Color(0x338B5CF6) else Color(0x403B82F6),
        borderWidth = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = if (glass.isDark) {
                            listOf(Color(0x334F46E5), Color(0x1F7C3AED), Color(0x1ADB2777))
                        } else {
                            listOf(Color(0x243B82F6), Color(0x1A8B5CF6), Color(0x14EC4899))
                        }
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Good Ideas Live Here",
                        style = MaterialTheme.typography.titleMedium,
                        color = glass.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You have $notesCount active ${if (notesCount == 1) "note" else "notes"}. Tap + to jot down new thoughts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = glass.textSecondary,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MercuryPrimaryGradient)
                            .clickable(onClick = onQuickNote)
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Quick Note",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.hero_feather_card),
                        contentDescription = "Feather illustration",
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = glass.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun FolderFilterRow(
    folders: List<FolderEntity>,
    selectedFolderId: Long?,
    onSelectFolder: (Long?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            FilterChipPill(
                label = "All Notes",
                isSelected = selectedFolderId == null,
                onClick = { onSelectFolder(null) }
            )
        }

        items(folders, key = { it.id }) { folder ->
            FilterChipPill(
                label = folder.name,
                isSelected = selectedFolderId == folder.id,
                onClick = { onSelectFolder(folder.id) },
                badgeColor = Color(folder.color)
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    count: Int? = null
) {
    val glass = MercuryTheme.glass

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MercuryViolet,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = glass.textSecondary,
            fontWeight = FontWeight.Bold
        )

        if (count != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "($count)",
                style = MaterialTheme.typography.labelSmall,
                color = glass.textMuted
            )
        }
    }
}

@Composable
fun NoteCardItem(
    note: NoteEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    val glass = MercuryTheme.glass
    var menuExpanded by remember { mutableStateOf(false) }

    val formattedDate = remember(note.updatedAt) {
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        sdf.format(Date(note.updatedAt))
    }

    val cardBg = if (note.colorTag != 0L) {
        Color(note.colorTag).copy(alpha = if (glass.isDark) 0.35f else 0.45f)
    } else {
        glass.cardBackground
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = cardBg,
        borderColor = if (note.isPinned) MercuryViolet.copy(alpha = 0.5f) else glass.cardBorder,
        borderWidth = if (note.isPinned) 1.5.dp else 1.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Title + Pin/Favorite indicators + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title.ifBlank { "Untitled Note" },
                    style = MaterialTheme.typography.titleMedium,
                    color = glass.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MercuryViolet,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (note.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = MercuryPink,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More actions",
                                tint = glass.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(glass.cardBackgroundElevated)
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (note.isPinned) "Unpin Note" else "Pin Note") },
                                onClick = {
                                    menuExpanded = false
                                    onTogglePin()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (note.isPinned) Icons.Outlined.PushPin else Icons.Default.PushPin,
                                        contentDescription = null
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (note.isFavorite) "Remove Favorite" else "Add to Favorites") },
                                onClick = {
                                    menuExpanded = false
                                    onToggleFavorite()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (note.isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                                        contentDescription = null
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Duplicate Note") },
                                onClick = {
                                    menuExpanded = false
                                    onDuplicate()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Delete Note", color = Color(0xFFEF4444)) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Note Content Snippet
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = glass.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Date & Folder badge & Image attachment tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Folder pill tag
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(note.folderColor).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(note.folderColor))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = note.folderName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(note.folderColor)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!note.imageUri.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Has image",
                            tint = glass.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = glass.textMuted
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyNotesState(
    isFiltered: Boolean,
    onCreateNote: () -> Unit
) {
    val glass = MercuryTheme.glass

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(glass.searchBarBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MercuryViolet,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isFiltered) "No notes in this folder" else "No notes created yet",
            style = MaterialTheme.typography.titleMedium,
            color = glass.textPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isFiltered) "Create a note in this folder to get started" else "Tap below to capture your first idea in Mercurynotes",
            style = MaterialTheme.typography.bodyMedium,
            color = glass.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        GlowingGradientButton(
            text = "Create Note",
            onClick = onCreateNote
        )
    }
}
