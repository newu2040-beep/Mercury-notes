package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.ChecklistItem
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.InteractiveChecklistRow
import com.example.ui.theme.MercuryPink
import com.example.ui.theme.MercuryTheme
import com.example.ui.theme.MercuryViolet
import com.example.ui.theme.NoteTintOptions
import com.example.ui.viewmodel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditorScreen(
    noteId: Long?,
    viewModel: NotesViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val glass = MercuryTheme.glass
    val folders by viewModel.allFolders.collectAsStateWithLifecycle()
    val autoSaveEnabled by viewModel.autoSave.collectAsStateWithLifecycle()
    val fontSizePref by viewModel.fontSize.collectAsStateWithLifecycle()

    var currentNoteId by remember { mutableStateOf(noteId ?: 0L) }
    var title by remember { mutableStateOf("") }
    var contentValue by remember { mutableStateOf(TextFieldValue("")) }
    var selectedFolder by remember {
        mutableStateOf(folders.firstOrNull { it.isDefault } ?: folders.firstOrNull() ?: FolderEntity(1L, "Personal", 0xFF8A5CF6))
    }
    var isFavorite by remember { mutableStateOf(false) }
    var isPinned by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    var colorTag by remember { mutableStateOf(0L) }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var isChecklistMode by remember { mutableStateOf(false) }
    val checklistItems = remember { mutableStateListOf<ChecklistItem>() }
    var newChecklistText by remember { mutableStateOf("") }
    var lastSavedAt by remember { mutableStateOf(System.currentTimeMillis()) }

    var folderDropdownOpen by remember { mutableStateOf(false) }
    var moreMenuOpen by remember { mutableStateOf(false) }
    var colorPickerOpen by remember { mutableStateOf(false) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
        }
    }

    // Load existing note if editing
    LaunchedEffect(noteId) {
        if (noteId != null && noteId > 0L) {
            val note = viewModel.getNoteById(noteId)
            if (note != null) {
                currentNoteId = note.id
                title = note.title
                contentValue = TextFieldValue(note.content, TextRange(note.content.length))
                folders.find { it.id == note.folderId }?.let { selectedFolder = it }
                isFavorite = note.isFavorite
                isPinned = note.isPinned
                isLocked = note.isLocked
                colorTag = note.colorTag
                imageUri = note.imageUri
                lastSavedAt = note.updatedAt

                val parsedChecklist = viewModel.parseChecklistJson(note.checklistJson)
                if (parsedChecklist.isNotEmpty()) {
                    checklistItems.clear()
                    checklistItems.addAll(parsedChecklist)
                    isChecklistMode = true
                }
            }
        }
    }

    // Function to save note
    fun performSave() {
        if (title.isBlank() && contentValue.text.isBlank() && checklistItems.isEmpty() && imageUri.isNullOrBlank()) {
            return
        }
        val checklistJson = if (checklistItems.isNotEmpty()) {
            viewModel.serializeChecklist(checklistItems)
        } else null

        val note = NoteEntity(
            id = currentNoteId,
            title = title,
            content = contentValue.text,
            folderId = selectedFolder.id,
            folderName = selectedFolder.name,
            folderColor = selectedFolder.color,
            isPinned = isPinned,
            isFavorite = isFavorite,
            isLocked = isLocked,
            colorTag = colorTag,
            imageUri = imageUri,
            checklistJson = checklistJson,
            updatedAt = System.currentTimeMillis()
        )

        viewModel.saveNote(note) { newId ->
            currentNoteId = newId
            lastSavedAt = System.currentTimeMillis()
        }
    }

    // Auto-save on exit
    DisposableEffect(Unit) {
        onDispose {
            if (autoSaveEnabled) {
                performSave()
            }
        }
    }

    // Auto-save debounce effect
    LaunchedEffect(title, contentValue.text, selectedFolder, isFavorite, isPinned, colorTag, imageUri, checklistItems.size) {
        if (autoSaveEnabled && (title.isNotBlank() || contentValue.text.isNotBlank() || checklistItems.isNotEmpty())) {
            kotlinx.coroutines.delay(1000)
            performSave()
        }
    }

    val wordCount = remember(contentValue.text) {
        if (contentValue.text.isBlank()) 0
        else contentValue.text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }

    val formattedDate = remember(lastSavedAt) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(lastSavedAt))
    }

    val baseFontSize = 16.sp * fontSizePref.scale

    val canvasTint = if (colorTag != 0L) {
        Color(colorTag).copy(alpha = if (glass.isDark) 0.15f else 0.25f)
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(glass.canvasBackground)
            .background(canvasTint)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            EditorTopBar(
                onBack = {
                    performSave()
                    onBack()
                },
                selectedFolder = selectedFolder,
                onFolderClick = { folderDropdownOpen = true },
                isFavorite = isFavorite,
                onToggleFavorite = {
                    isFavorite = !isFavorite
                    performSave()
                },
                onMoreClick = { moreMenuOpen = true }
            )

            // Folder Dropdown Menu
            Box(modifier = Modifier.padding(start = 60.dp)) {
                DropdownMenu(
                    expanded = folderDropdownOpen,
                    onDismissRequest = { folderDropdownOpen = false },
                    modifier = Modifier.background(glass.cardBackgroundElevated)
                ) {
                    folders.forEach { folder ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(folder.color))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(folder.name)
                                }
                            },
                            onClick = {
                                selectedFolder = folder
                                folderDropdownOpen = false
                                performSave()
                            }
                        )
                    }
                }
            }

            // More Options Dropdown Menu
            Box(modifier = Modifier.align(Alignment.End).padding(end = 20.dp)) {
                DropdownMenu(
                    expanded = moreMenuOpen,
                    onDismissRequest = { moreMenuOpen = false },
                    modifier = Modifier.background(glass.cardBackgroundElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("Color Tint") },
                        onClick = {
                            moreMenuOpen = false
                            colorPickerOpen = !colorPickerOpen
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Palette, contentDescription = null)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(if (isPinned) "Unpin Note" else "Pin Note") },
                        onClick = {
                            moreMenuOpen = false
                            isPinned = !isPinned
                            performSave()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Bookmark, contentDescription = null)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(if (isLocked) "Unlock Note" else "Lock Note") },
                        onClick = {
                            moreMenuOpen = false
                            isLocked = !isLocked
                            performSave()
                        },
                        leadingIcon = {
                            Icon(if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock, contentDescription = null)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Share / Export Note") },
                        onClick = {
                            moreMenuOpen = false
                            shareNote(context, title, contentValue.text)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Share, contentDescription = null)
                        }
                    )

                    if (currentNoteId > 0L) {
                        DropdownMenuItem(
                            text = { Text("Delete Note", color = Color(0xFFEF4444)) },
                            onClick = {
                                moreMenuOpen = false
                                viewModel.moveToTrash(currentNoteId)
                                onBack()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                            }
                        )
                    }
                }
            }

            // Color Palette Selector Row (if toggled)
            AnimatedVisibility(visible = colorPickerOpen) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(NoteTintOptions) { (colorVal, pair) ->
                        val (_, displayColor) = pair
                        val isSelected = colorTag == colorVal

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (colorVal == 0L) glass.cardBackgroundElevated else displayColor)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MercuryViolet else glass.cardBorder,
                                    shape = CircleShape
                                )
                                .clickable {
                                    colorTag = colorVal
                                    performSave()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (colorVal == 0L) {
                                Text("∅", color = glass.textSecondary, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // Main Editor Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Attached Image (if any)
                if (!imageUri.isNullOrBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, glass.cardBorder, RoundedCornerShape(20.dp))
                        ) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Attached image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Remove Image Button
                            IconButton(
                                onClick = {
                                    imageUri = null
                                    performSave()
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x99000000))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Remove image",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Date, Word Count & Status Meta Info
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = glass.textMuted
                        )
                        Text(
                            text = "$wordCount words • ${contentValue.text.length} chars",
                            style = MaterialTheme.typography.labelSmall,
                            color = glass.textMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Note Title
                item {
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_title_input"),
                        textStyle = TextStyle(
                            color = glass.textPrimary,
                            fontSize = 26.sp * fontSizePref.scale,
                            fontWeight = FontWeight.Bold
                        ),
                        cursorBrush = SolidColor(glass.primaryAccent),
                        decorationBox = { innerTextField ->
                            if (title.isEmpty()) {
                                Text(
                                    text = "Title",
                                    color = glass.textMuted,
                                    fontSize = 26.sp * fontSizePref.scale,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            innerTextField()
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Checklist Items (if in checklist mode)
                if (isChecklistMode || checklistItems.isNotEmpty()) {
                    items(checklistItems, key = { it.id }) { item ->
                        InteractiveChecklistRow(
                            text = item.text,
                            isChecked = item.isChecked,
                            onCheckedChange = { checked ->
                                val index = checklistItems.indexOfFirst { it.id == item.id }
                                if (index != -1) {
                                    checklistItems[index] = item.copy(isChecked = checked)
                                    performSave()
                                }
                            },
                            onDelete = {
                                checklistItems.remove(item)
                                performSave()
                            }
                        )
                    }

                    // Add new checklist item input
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add item",
                                tint = MercuryViolet,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            BasicTextField(
                                value = newChecklistText,
                                onValueChange = { newChecklistText = it },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(
                                    color = glass.textPrimary,
                                    fontSize = baseFontSize
                                ),
                                cursorBrush = SolidColor(glass.primaryAccent),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    if (newChecklistText.isEmpty()) {
                                        Text(
                                            text = "Add checklist item...",
                                            color = glass.textMuted,
                                            fontSize = baseFontSize
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (newChecklistText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        checklistItems.add(ChecklistItem(text = newChecklistText.trim()))
                                        newChecklistText = ""
                                        performSave()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Save item",
                                        tint = MercuryViolet
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Note Body Text Field
                item {
                    BasicTextField(
                        value = contentValue,
                        onValueChange = { contentValue = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (contentValue.text.length < 200) 300.dp else 600.dp)
                            .testTag("note_content_input"),
                        textStyle = TextStyle(
                            color = glass.textPrimary,
                            fontSize = baseFontSize,
                            lineHeight = (baseFontSize.value * 1.5f).sp
                        ),
                        cursorBrush = SolidColor(glass.primaryAccent),
                        decorationBox = { innerTextField ->
                            if (contentValue.text.isEmpty() && checklistItems.isEmpty()) {
                                Text(
                                    text = "Start writing your thoughts, ideas, or lists here...",
                                    color = glass.textMuted,
                                    fontSize = baseFontSize
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            // Floating Frosted Formatting Toolbar
            FloatingFormattingToolbar(
                onBold = {
                    val text = contentValue.text
                    val sel = contentValue.selection
                    val newText = if (sel.collapsed) {
                        text.substring(0, sel.start) + "****" + text.substring(sel.end)
                    } else {
                        text.substring(0, sel.start) + "**" + text.substring(sel.start, sel.end) + "**" + text.substring(sel.end)
                    }
                    contentValue = TextFieldValue(newText, TextRange(sel.start + 2))
                },
                onItalic = {
                    val text = contentValue.text
                    val sel = contentValue.selection
                    val newText = if (sel.collapsed) {
                        text.substring(0, sel.start) + "**" + text.substring(sel.end)
                    } else {
                        text.substring(0, sel.start) + "*" + text.substring(sel.start, sel.end) + "*" + text.substring(sel.end)
                    }
                    contentValue = TextFieldValue(newText, TextRange(sel.start + 1))
                },
                onUnderline = {
                    val text = contentValue.text
                    val sel = contentValue.selection
                    val newText = text.substring(0, sel.start) + "<u>" + text.substring(sel.start, sel.end) + "</u>" + text.substring(sel.end)
                    contentValue = TextFieldValue(newText, TextRange(sel.start + 3))
                },
                onBulletList = {
                    val text = contentValue.text
                    val sel = contentValue.selection
                    val insert = "\n• "
                    contentValue = TextFieldValue(
                        text.substring(0, sel.start) + insert + text.substring(sel.end),
                        TextRange(sel.start + insert.length)
                    )
                },
                onNumberedList = {
                    val text = contentValue.text
                    val sel = contentValue.selection
                    val insert = "\n1. "
                    contentValue = TextFieldValue(
                        text.substring(0, sel.start) + insert + text.substring(sel.end),
                        TextRange(sel.start + insert.length)
                    )
                },
                onToggleChecklist = {
                    isChecklistMode = !isChecklistMode
                    if (isChecklistMode && checklistItems.isEmpty()) {
                        checklistItems.add(ChecklistItem(text = "Task 1"))
                    }
                },
                onAttachImage = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onToggleColorPicker = {
                    colorPickerOpen = !colorPickerOpen
                }
            )
        }
    }
}

@Composable
fun EditorTopBar(
    onBack: () -> Unit,
    selectedFolder: FolderEntity,
    onFolderClick: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onMoreClick: () -> Unit
) {
    val glass = MercuryTheme.glass

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            GlassCard(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                backgroundColor = glass.searchBarBackground,
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = glass.textPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Folder Pill Tag
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(selectedFolder.color).copy(alpha = 0.15f))
                    .border(1.dp, Color(selectedFolder.color).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable(onClick = onFolderClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(selectedFolder.color))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedFolder.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(selectedFolder.color)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Favorite Button
            GlassCard(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                backgroundColor = glass.searchBarBackground,
                onClick = onToggleFavorite
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MercuryPink else glass.textSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.Center)
                )
            }

            // More Options
            GlassCard(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                backgroundColor = glass.searchBarBackground,
                onClick = onMoreClick
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = glass.textPrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun FloatingFormattingToolbar(
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onBulletList: () -> Unit,
    onNumberedList: () -> Unit,
    onToggleChecklist: () -> Unit,
    onAttachImage: () -> Unit,
    onToggleColorPicker: () -> Unit
) {
    val glass = MercuryTheme.glass

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        backgroundColor = glass.bottomNavBackground,
        borderColor = glass.bottomNavBorder,
        borderWidth = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBold, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.FormatBold, contentDescription = "Bold", tint = glass.textPrimary, modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = onItalic, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.FormatItalic, contentDescription = "Italic", tint = glass.textPrimary, modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = onUnderline, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.FormatUnderlined, contentDescription = "Underline", tint = glass.textPrimary, modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = onBulletList, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.FormatListBulleted, contentDescription = "Bullet list", tint = glass.textPrimary, modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = onNumberedList, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.FormatListNumbered, contentDescription = "Numbered list", tint = glass.textPrimary, modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = onToggleChecklist, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Checklist, contentDescription = "Checklist", tint = MercuryViolet, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onAttachImage, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Image, contentDescription = "Attach image", tint = glass.textPrimary, modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = onToggleColorPicker, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ColorLens, contentDescription = "Color tint", tint = MercuryPink, modifier = Modifier.size(19.dp))
            }
        }
    }
}

fun shareNote(context: Context, title: String, content: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title.ifBlank { "Mercurynotes Note" })
        putExtra(Intent.EXTRA_TEXT, "${title.ifBlank { "Untitled" }}\n\n$content\n\n— Captured with Mercurynotes")
    }
    val chooser = Intent.createChooser(shareIntent, "Share Note")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}
