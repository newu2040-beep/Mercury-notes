package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Bookmark
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.util.ExportFormat
import com.example.util.FileImporter
import com.example.util.NotesExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    noteId: Long?,
    viewModel: NotesViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val glass = MercuryTheme.glass
    val isCompact = MercuryTheme.isCompact
    val fontScale = MercuryTheme.fontScale
    val folders by viewModel.allFolders.collectAsStateWithLifecycle()
    val autoSaveEnabled by viewModel.autoSave.collectAsStateWithLifecycle()

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
    var attachedDocName by remember { mutableStateOf<String?>(null) }
    var isChecklistMode by remember { mutableStateOf(false) }
    val checklistItems = remember { mutableStateListOf<ChecklistItem>() }
    var newChecklistText by remember { mutableStateOf("") }
    var lastSavedAt by remember { mutableStateOf(System.currentTimeMillis()) }

    var folderDropdownOpen by remember { mutableStateOf(false) }
    var moreMenuOpen by remember { mutableStateOf(false) }
    var colorPickerOpen by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var isVoiceListening by remember { mutableStateOf(false) }

    // Voice recognition launcher
    val voiceRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenText: String? = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                val currentText = contentValue.text
                val separator = if (currentText.isBlank() || currentText.endsWith("\n") || currentText.endsWith(" ")) "" else " "
                val updatedText = currentText + separator + spokenText
                contentValue = TextFieldValue(updatedText, TextRange(updatedText.length))
                Toast.makeText(context, "Voice typed: \"$spokenText\"", Toast.LENGTH_SHORT).show()
            }
        }
        isVoiceListening = false
    }

    // Photo/Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
            Toast.makeText(context, "Image attached", Toast.LENGTH_SHORT).show()
        }
    }

    // Universal File Picker launcher (PDF, TXT, CSV, JSON, ZIP, TTF)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val imported = FileImporter.parseImportedUri(context, uri)
            if (title.isBlank()) {
                title = imported.title
            }
            if (imported.content.isNotBlank()) {
                val separator = if (contentValue.text.isBlank()) "" else "\n\n"
                val combined = contentValue.text + separator + imported.content
                contentValue = TextFieldValue(combined, TextRange(combined.length))
            }
            if (imported.checklists.isNotEmpty()) {
                isChecklistMode = true
                checklistItems.addAll(imported.checklists)
            }
            if (imported.imageUri != null) {
                imageUri = imported.imageUri
            }
            attachedDocName = imported.attachedFileName
            Toast.makeText(context, "Imported: ${imported.attachedFileName ?: "File"}", Toast.LENGTH_SHORT).show()
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

    fun triggerVoiceTyping() {
        try {
            isVoiceListening = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now to type note...")
            }
            voiceRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            isVoiceListening = false
            Toast.makeText(context, "Voice typing not available on this device", Toast.LENGTH_SHORT).show()
        }
    }

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
            tags = if (attachedDocName != null) "attachment" else "",
            updatedAt = System.currentTimeMillis()
        )

        viewModel.saveNote(note) { newId ->
            currentNoteId = newId
            lastSavedAt = System.currentTimeMillis()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (autoSaveEnabled) {
                performSave()
            }
        }
    }

    LaunchedEffect(title, contentValue.text, selectedFolder, isFavorite, isPinned, colorTag, imageUri, checklistItems.size) {
        if (autoSaveEnabled && (title.isNotBlank() || contentValue.text.isNotBlank() || checklistItems.isNotEmpty())) {
            kotlinx.coroutines.delay(800)
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

    val baseFontSize = (16 * fontScale).sp
    val titleFontSize = (26 * fontScale).sp

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
                onVoiceClick = { triggerVoiceTyping() },
                onShareClick = { showExportSheet = true },
                onMoreClick = { moreMenuOpen = true }
            )

            // Folder Dropdown Menu
            Box(modifier = Modifier.padding(start = if (isCompact) 40.dp else 60.dp)) {
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
                                    Text(folder.name, color = glass.textPrimary)
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
            Box(modifier = Modifier.align(Alignment.End).padding(end = 16.dp)) {
                DropdownMenu(
                    expanded = moreMenuOpen,
                    onDismissRequest = { moreMenuOpen = false },
                    modifier = Modifier.background(glass.cardBackgroundElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("Color Tint", color = glass.textPrimary) },
                        onClick = {
                            moreMenuOpen = false
                            colorPickerOpen = !colorPickerOpen
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = MercuryViolet)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(if (isPinned) "Unpin Note" else "Pin Note", color = glass.textPrimary) },
                        onClick = {
                            moreMenuOpen = false
                            isPinned = !isPinned
                            performSave()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = glass.textSecondary)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(if (isLocked) "Unlock Note" else "Lock Note (Biometric)", color = glass.textPrimary) },
                        onClick = {
                            moreMenuOpen = false
                            isLocked = !isLocked
                            performSave()
                            Toast.makeText(context, if (isLocked) "Note locked" else "Note unlocked", Toast.LENGTH_SHORT).show()
                        },
                        leadingIcon = {
                            Icon(if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock, contentDescription = null, tint = MercuryPink)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Import Document / Font", color = glass.textPrimary) },
                        onClick = {
                            moreMenuOpen = false
                            filePickerLauncher.launch("*/*")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = MercuryTheme.glass.secondaryAccent)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Export Note (PDF, CSV, TXT, JSON)", color = glass.textPrimary) },
                        onClick = {
                            moreMenuOpen = false
                            showExportSheet = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Share, contentDescription = null, tint = MercuryViolet)
                        }
                    )

                    if (currentNoteId > 0L) {
                        DropdownMenuItem(
                            text = { Text("Move to Trash", color = Color(0xFFEF4444)) },
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
                        .padding(horizontal = if (isCompact) 12.dp else 20.dp, vertical = 6.dp),
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
                contentPadding = PaddingValues(horizontal = if (isCompact) 12.dp else 20.dp, vertical = 8.dp)
            ) {
                // Attached Image (if any)
                if (!imageUri.isNullOrBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isCompact) 180.dp else 220.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, glass.cardBorder, RoundedCornerShape(18.dp))
                        ) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Attached image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

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

                // Attached Document File Tag (if imported)
                if (attachedDocName != null) {
                    item {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(glass.cardBackgroundElevated)
                                .border(1.dp, glass.cardBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = null,
                                tint = MercuryViolet,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = attachedDocName ?: "File",
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
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
                            fontSize = titleFontSize,
                            fontWeight = FontWeight.Bold
                        ),
                        cursorBrush = SolidColor(glass.primaryAccent),
                        decorationBox = { innerTextField ->
                            if (title.isEmpty()) {
                                Text(
                                    text = "Title",
                                    color = glass.textMuted,
                                    fontSize = titleFontSize,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            innerTextField()
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
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
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add item",
                                tint = MercuryViolet,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
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
                            .height(if (contentValue.text.length < 200) 280.dp else 600.dp)
                            .testTag("note_content_input"),
                        textStyle = TextStyle(
                            color = glass.textPrimary,
                            fontSize = baseFontSize,
                            lineHeight = (baseFontSize.value * 1.55f).sp
                        ),
                        cursorBrush = SolidColor(glass.primaryAccent),
                        decorationBox = { innerTextField ->
                            if (contentValue.text.isEmpty() && checklistItems.isEmpty()) {
                                Text(
                                    text = "Start writing your notes, thoughts, or voice dictations here...",
                                    color = glass.textMuted,
                                    fontSize = baseFontSize
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            // Floating Frosted Formatting Toolbar (Fully responsive with Voice typing, File Import, Lists, Formatting)
            FloatingFormattingToolbar(
                isListening = isVoiceListening,
                onBold = {
                    val text = contentValue.text
                    val sel = contentValue.selection
                    val newText = if (sel.collapsed) {
                        text.substring(0, sel.start) + "**Bold**" + text.substring(sel.end)
                    } else {
                        text.substring(0, sel.start) + "**" + text.substring(sel.start, sel.end) + "**" + text.substring(sel.end)
                    }
                    contentValue = TextFieldValue(newText, TextRange(sel.start + 2))
                },
                onItalic = {
                    val text = contentValue.text
                    val sel = contentValue.selection
                    val newText = if (sel.collapsed) {
                        text.substring(0, sel.start) + "*Italic*" + text.substring(sel.end)
                    } else {
                        text.substring(0, sel.start) + "*" + text.substring(sel.start, sel.end) + "*" + text.substring(sel.end)
                    }
                    contentValue = TextFieldValue(newText, TextRange(sel.start + 1))
                },
                onUnderline = {
                    val text = contentValue.text
                    val sel = contentValue.selection
                    val newText = if (sel.collapsed) {
                        text.substring(0, sel.start) + "<u>Underline</u>" + text.substring(sel.end)
                    } else {
                        text.substring(0, sel.start) + "<u>" + text.substring(sel.start, sel.end) + "</u>" + text.substring(sel.end)
                    }
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
                        checklistItems.add(ChecklistItem(text = "First task"))
                    }
                },
                onAttachImage = {
                    imagePickerLauncher.launch("image/*")
                },
                onAttachFile = {
                    filePickerLauncher.launch("*/*")
                },
                onVoiceTyping = {
                    triggerVoiceTyping()
                },
                onToggleColorPicker = {
                    colorPickerOpen = !colorPickerOpen
                }
            )
        }

        // Export Modal Bottom Sheet
        if (showExportSheet) {
            ModalBottomSheet(
                onDismissRequest = { showExportSheet = false },
                containerColor = glass.cardBackgroundElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Export & Share Note",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = glass.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose your preferred document or data format:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = glass.textSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val exportNoteEntity = NoteEntity(
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
                        updatedAt = lastSavedAt
                    )

                    ExportOptionCard(
                        title = "PDF Document",
                        subtitle = "High quality formatted printable PDF (.pdf)",
                        icon = Icons.Default.PictureAsPdf,
                        iconTint = Color(0xFFEF4444)
                    ) {
                        showExportSheet = false
                        NotesExporter.exportAndShare(context, exportNoteEntity, ExportFormat.PDF, checklistItems)
                    }

                    ExportOptionCard(
                        title = "Plain Text",
                        subtitle = "Universal clean text file (.txt)",
                        icon = Icons.Default.TextFields,
                        iconTint = MercuryViolet
                    ) {
                        showExportSheet = false
                        NotesExporter.exportAndShare(context, exportNoteEntity, ExportFormat.TXT, checklistItems)
                    }

                    ExportOptionCard(
                        title = "Markdown Document",
                        subtitle = "Formatted with headers & checklists (.md)",
                        icon = Icons.AutoMirrored.Filled.InsertDriveFile,
                        iconTint = MercuryTheme.glass.secondaryAccent
                    ) {
                        showExportSheet = false
                        NotesExporter.exportAndShare(context, exportNoteEntity, ExportFormat.MARKDOWN, checklistItems)
                    }

                    ExportOptionCard(
                        title = "Spreadsheet / CSV",
                        subtitle = "Tabular data structure (.csv)",
                        icon = Icons.Default.Checklist,
                        iconTint = Color(0xFF10B981)
                    ) {
                        showExportSheet = false
                        NotesExporter.exportAndShare(context, exportNoteEntity, ExportFormat.CSV, checklistItems)
                    }

                    ExportOptionCard(
                        title = "Structured JSON",
                        subtitle = "Developer format with full schema (.json)",
                        icon = Icons.Default.Share,
                        iconTint = Color(0xFFF59E0B)
                    ) {
                        showExportSheet = false
                        NotesExporter.exportAndShare(context, exportNoteEntity, ExportFormat.JSON, checklistItems)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ExportOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    val glass = MercuryTheme.glass

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        backgroundColor = glass.searchBarBackground,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = glass.textPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = glass.textMuted
                )
            }
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
    onVoiceClick: () -> Unit,
    onShareClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val glass = MercuryTheme.glass
    val isCompact = MercuryTheme.isCompact

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isCompact) 10.dp else 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            GlassCard(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                backgroundColor = glass.searchBarBackground,
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = glass.textPrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Folder Pill Tag
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(selectedFolder.color).copy(alpha = 0.15f))
                    .border(1.dp, Color(selectedFolder.color).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable(onClick = onFolderClick)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(selectedFolder.color))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedFolder.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(selectedFolder.color)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Voice Typing Fast Action Button
            GlassCard(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                backgroundColor = glass.searchBarBackground,
                onClick = onVoiceClick
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Dictate",
                    tint = MercuryViolet,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.Center)
                )
            }

            // Share / Export Button
            GlassCard(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                backgroundColor = glass.searchBarBackground,
                onClick = onShareClick
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = glass.textPrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.Center)
                )
            }

            // Favorite Button
            GlassCard(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                backgroundColor = glass.searchBarBackground,
                onClick = onToggleFavorite
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MercuryPink else glass.textSecondary,
                    modifier = Modifier
                        .size(17.dp)
                        .align(Alignment.Center)
                )
            }

            // More Options
            GlassCard(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                backgroundColor = glass.searchBarBackground,
                onClick = onMoreClick
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = glass.textPrimary,
                    modifier = Modifier
                        .size(17.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun FloatingFormattingToolbar(
    isListening: Boolean,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onBulletList: () -> Unit,
    onNumberedList: () -> Unit,
    onToggleChecklist: () -> Unit,
    onAttachImage: () -> Unit,
    onAttachFile: () -> Unit,
    onVoiceTyping: () -> Unit,
    onToggleColorPicker: () -> Unit
) {
    val glass = MercuryTheme.glass
    val isCompact = MercuryTheme.isCompact

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isCompact) 8.dp else 14.dp, vertical = 6.dp)
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        backgroundColor = glass.bottomNavBackground,
        borderColor = if (isListening) MercuryPink else glass.bottomNavBorder,
        borderWidth = if (isListening) 2.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Mic Dictation Button
            IconButton(
                onClick = onVoiceTyping,
                modifier = Modifier
                    .size(36.dp)
                    .scale(micScale)
                    .clip(CircleShape)
                    .background(if (isListening) MercuryPink.copy(alpha = 0.25f) else Color.Transparent)
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice Dictate",
                    tint = if (isListening) MercuryPink else MercuryViolet,
                    modifier = Modifier.size(20.dp)
                )
            }

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
            IconButton(onClick = onAttachFile, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.AttachFile, contentDescription = "Attach File/Doc/Font", tint = MercuryTheme.glass.secondaryAccent, modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = onToggleColorPicker, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ColorLens, contentDescription = "Color tint", tint = MercuryPink, modifier = Modifier.size(19.dp))
            }
        }
    }
}
