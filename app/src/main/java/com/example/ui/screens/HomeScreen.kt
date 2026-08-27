package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.components.MoltenGlassCard
import com.example.ui.components.MoltenGlassPanel
import com.example.ui.theme.MercuryTheme
import com.example.ui.viewmodel.NotesViewModel
import com.example.util.FileImporter
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
    val context = LocalContext.current
    val glass = MercuryTheme.glass
    val isCompact = MercuryTheme.isCompact
    val fontScale = MercuryTheme.fontScale

    val notes by viewModel.homeDisplayNotes.collectAsStateWithLifecycle()
    val folders by viewModel.allFolders.collectAsStateWithLifecycle()
    val selectedFolderId by viewModel.selectedFolderId.collectAsStateWithLifecycle()
    val heroDismissed by viewModel.heroDismissed.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    val pinnedNotes = remember(notes) { notes.filter { it.isPinned } }
    val regularNotes = remember(notes) { notes.filter { !it.isPinned } }

    // Universal File Picker (PDF, TXT, CSV, JSON, ZIP, Fonts, Images)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val imported = FileImporter.parseImportedUri(context, uri)
            viewModel.importNoteData(imported) { newId ->
                Toast.makeText(context, "Imported \"${imported.title}\"", Toast.LENGTH_SHORT).show()
                onNoteClick(newId)
            }
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
                start = if (isCompact) 10.dp else 16.dp,
                end = if (isCompact) 10.dp else 16.dp,
                top = if (isCompact) 8.dp else 16.dp,
                bottom = 110.dp
            )
        ) {
            // Header: Logo, Title, Quick Actions (Import, Search, Theme toggle)
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
                    onSearchClick = onSearchClick,
                    onImportClick = { filePickerLauncher.launch("*/*") }
                )
            }

            // Hero Card ("Good Ideas Live Here")
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
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Section: Pinned Notes (if any)
            if (pinnedNotes.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Pinned Notes",
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
                    Spacer(modifier = Modifier.height(14.dp))
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
                        onCreateNote = onNewNoteClick,
                        onImportFile = { filePickerLauncher.launch("*/*") }
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
                .padding(end = if (isCompact) 14.dp else 20.dp, bottom = 24.dp)
        )
    }
}

@Composable
fun HomeHeader(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onSearchClick: () -> Unit,
    onImportClick: () -> Unit
) {
    val glass = MercuryTheme.glass
    val isCompact = MercuryTheme.isCompact

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon Logo
            Image(
                painter = painterResource(id = R.drawable.mercurynotes_logo_1787768712898),
                contentDescription = "Mercurynotes Logo",
                modifier = Modifier
                    .size(if (isCompact) 36.dp else 42.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "Mercurynotes",
                    style = MaterialTheme.typography.titleLarge,
                    color = glass.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isCompact) 18.sp else 22.sp
                )
                Text(
                    text = "Think. Write. Organize.",
                    style = MaterialTheme.typography.labelSmall,
                    color = glass.textSecondary,
                    fontSize = if (isCompact) 10.sp else 12.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Universal Import Button
            GlassCard(
                modifier = Modifier.size(if (isCompact) 36.dp else 40.dp),
                shape = CircleShape,
                backgroundColor = glass.searchBarBackground,
                onClick = onImportClick
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Import Document / Font",
                    tint = MercuryTheme.glass.secondaryAccent,
                    modifier = Modifier
                        .size(if (isCompact) 18.dp else 20.dp)
                        .align(Alignment.Center)
                )
            }

            // Search shortcut icon
            GlassCard(
                modifier = Modifier.size(if (isCompact) 36.dp else 40.dp),
                shape = CircleShape,
                backgroundColor = glass.searchBarBackground,
                onClick = onSearchClick
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = glass.textPrimary,
                    modifier = Modifier
                        .size(if (isCompact) 18.dp else 20.dp)
                        .align(Alignment.Center)
                )
            }

            // Quick Theme switch
            GlassCard(
                modifier = Modifier.size(if (isCompact) 36.dp else 40.dp),
                shape = CircleShape,
                backgroundColor = glass.searchBarBackground,
                onClick = onToggleTheme
            ) {
                Icon(
                    imageVector = if (glass.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Switch Theme",
                    tint = if (glass.isDark) Color(0xFFFBBF24) else glass.primaryAccent,
                    modifier = Modifier
                        .size(if (isCompact) 18.dp else 20.dp)
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
    val isCompact = MercuryTheme.isCompact

    MoltenGlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(glass.heroGradient)
                .padding(if (isCompact) 14.dp else 18.dp)
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
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "You have $notesCount active ${if (notesCount == 1) "note" else "notes"}. Tap Quick Note or use Voice to jot down thoughts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = glass.textSecondary,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(glass.buttonGradient)
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

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier.size(if (isCompact) 56.dp else 68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.hero_feather_card),
                        contentDescription = "Feather illustration",
                        modifier = Modifier
                            .size(if (isCompact) 56.dp else 68.dp)
                            .clip(RoundedCornerShape(14.dp)),
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
                    modifier = Modifier.size(15.dp)
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
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
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = glass.primaryAccent,
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
    val isCompact = MercuryTheme.isCompact
    val fontScale = MercuryTheme.fontScale
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

    MoltenGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isCompact) 4.dp else 5.dp)
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(18.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .padding(if (isCompact) 12.dp else 15.dp)
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
                    fontSize = (16 * fontScale).sp,
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
                            tint = glass.primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (note.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = glass.secondaryAccent,
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
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = (14 * fontScale).sp,
                    color = glass.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

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
    onCreateNote: () -> Unit,
    onImportFile: () -> Unit
) {
    val glass = MercuryTheme.glass

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(glass.searchBarBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = glass.primaryAccent,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isFiltered) "No notes in this folder" else "No notes found",
            style = MaterialTheme.typography.titleMedium,
            color = glass.textPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isFiltered) "Create a note in this folder to get started" else "Create your first note or import documents, PDF, TXT, CSV, or fonts",
            style = MaterialTheme.typography.bodyMedium,
            color = glass.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlowingGradientButton(
                text = "New Note",
                onClick = onCreateNote
            )
        }
    }
}
