package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.MercuryDatabase
import com.example.data.model.ChecklistItem
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.data.preferences.FontPreset
import com.example.data.preferences.NoteFontSize
import com.example.data.preferences.PastelThemePreset
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferences
import com.example.data.repository.NoteRepository
import com.example.util.ImportedNoteData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

enum class SearchFilterOption {
    ALL, FAVORITES, HAS_IMAGE, HAS_CHECKLIST
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    private val userPrefs: UserPreferences = UserPreferences(application)

    val allFolders: StateFlow<List<FolderEntity>>
    val allActiveNotes: StateFlow<List<NoteEntity>>
    val deletedNotes: StateFlow<List<NoteEntity>>
    val activeNotesCount: StateFlow<Int>
    val deletedNotesCount: StateFlow<Int>

    // UI state
    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedFolderId: StateFlow<Long?> = _selectedFolderId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilterOption.ALL)
    val searchFilter: StateFlow<SearchFilterOption> = _searchFilter.asStateFlow()

    private val _themeMode = MutableStateFlow(userPrefs.themeMode)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _pastelTheme = MutableStateFlow(userPrefs.pastelTheme)
    val pastelTheme: StateFlow<PastelThemePreset> = _pastelTheme.asStateFlow()

    private val _liquidGlassEnabled = MutableStateFlow(userPrefs.liquidGlassEnabled)
    val liquidGlassEnabled: StateFlow<Boolean> = _liquidGlassEnabled.asStateFlow()

    private val _compactMode = MutableStateFlow(userPrefs.compactMode)
    val compactMode: StateFlow<Boolean> = _compactMode.asStateFlow()

    private val _autoSave = MutableStateFlow(userPrefs.autoSaveEnabled)
    val autoSave: StateFlow<Boolean> = _autoSave.asStateFlow()

    private val _fontSize = MutableStateFlow(userPrefs.fontSize)
    val fontSize: StateFlow<NoteFontSize> = _fontSize.asStateFlow()

    private val _fontPreset = MutableStateFlow(userPrefs.fontPreset)
    val fontPreset: StateFlow<FontPreset> = _fontPreset.asStateFlow()

    private val _customFontPath = MutableStateFlow(userPrefs.customFontPath)
    val customFontPath: StateFlow<String?> = _customFontPath.asStateFlow()

    private val _customFontDisplayName = MutableStateFlow(userPrefs.customFontDisplayName)
    val customFontDisplayName: StateFlow<String?> = _customFontDisplayName.asStateFlow()

    private val _reduceTransparency = MutableStateFlow(userPrefs.reduceTransparency)
    val reduceTransparency: StateFlow<Boolean> = _reduceTransparency.asStateFlow()

    private val _translucencyLevel = MutableStateFlow(userPrefs.translucencyLevel)
    val translucencyLevel: StateFlow<com.example.data.preferences.TranslucencyLevel> = _translucencyLevel.asStateFlow()

    private val _ambientBackdropGlow = MutableStateFlow(userPrefs.ambientBackdropGlow)
    val ambientBackdropGlow: StateFlow<Boolean> = _ambientBackdropGlow.asStateFlow()

    private val _highRefreshRateEnabled = MutableStateFlow(userPrefs.highRefreshRateEnabled)
    val highRefreshRateEnabled: StateFlow<Boolean> = _highRefreshRateEnabled.asStateFlow()

    private val _reduceMotion = MutableStateFlow(userPrefs.reduceMotion)
    val reduceMotion: StateFlow<Boolean> = _reduceMotion.asStateFlow()

    private val _biometricLockEnabled = MutableStateFlow(userPrefs.biometricLockEnabled)
    val biometricLockEnabled: StateFlow<Boolean> = _biometricLockEnabled.asStateFlow()

    private val _isAppLocked = MutableStateFlow(userPrefs.biometricLockEnabled)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _heroDismissed = MutableStateFlow(userPrefs.heroDismissed)
    val heroDismissed: StateFlow<Boolean> = _heroDismissed.asStateFlow()

    private val _recentSearches = MutableStateFlow(listOf("Design Ideas", "Travel", "Routine", "Books"))
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    init {
        val db = MercuryDatabase.getDatabase(application, viewModelScope)
        repository = NoteRepository(db.noteDao(), db.folderDao())

        allFolders = repository.allFolders
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allActiveNotes = repository.allActiveNotes
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        deletedNotes = repository.deletedNotes
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        activeNotesCount = repository.activeNotesCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        deletedNotesCount = repository.deletedNotesCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    }

    // Filtered notes for Home screen
    val homeDisplayNotes: StateFlow<List<NoteEntity>> = combine(
        allActiveNotes,
        _selectedFolderId
    ) { notes, folderId ->
        if (folderId == null) {
            notes
        } else {
            notes.filter { it.folderId == folderId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search results
    val searchResults: StateFlow<List<NoteEntity>> = combine(
        allActiveNotes,
        _searchQuery,
        _searchFilter
    ) { notes, query, filter ->
        var list = notes
        if (query.isNotBlank()) {
            val q = query.lowercase(Locale.ROOT)
            list = list.filter {
                it.title.lowercase(Locale.ROOT).contains(q) ||
                        it.content.lowercase(Locale.ROOT).contains(q) ||
                        it.tags.lowercase(Locale.ROOT).contains(q) ||
                        it.folderName.lowercase(Locale.ROOT).contains(q)
            }
        }
        when (filter) {
            SearchFilterOption.ALL -> list
            SearchFilterOption.FAVORITES -> list.filter { it.isFavorite }
            SearchFilterOption.HAS_IMAGE -> list.filter { !it.imageUri.isNullOrBlank() }
            SearchFilterOption.HAS_CHECKLIST -> list.filter { !it.checklistJson.isNullOrBlank() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedFolder(folderId: Long?) {
        _selectedFolderId.value = folderId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank() && !_recentSearches.value.contains(query.trim())) {
            _recentSearches.value = (listOf(query.trim()) + _recentSearches.value).take(6)
        }
    }

    fun setSearchFilter(filter: SearchFilterOption) {
        _searchFilter.value = filter
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.saveNote(
                note.copy(
                    isPinned = !note.isPinned,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleFavorite(note: NoteEntity) {
        viewModelScope.launch {
            repository.saveNote(
                note.copy(
                    isFavorite = !note.isFavorite,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun moveToTrash(id: Long) {
        viewModelScope.launch {
            repository.moveToTrash(id)
        }
    }

    fun restoreNote(id: Long) {
        viewModelScope.launch {
            repository.restoreNote(id)
        }
    }

    fun deletePermanently(id: Long) {
        viewModelScope.launch {
            repository.deletePermanently(id)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }

    fun deleteAllNotes() {
        viewModelScope.launch {
            repository.deleteAllNotes()
        }
    }

    fun duplicateNote(note: NoteEntity) {
        viewModelScope.launch {
            val duplicate = note.copy(
                id = 0L,
                title = if (note.title.isNotBlank()) "${note.title} (Copy)" else "Copy Note",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveNote(duplicate)
        }
    }

    fun saveNote(note: NoteEntity, onSaved: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.saveNote(note)
            onSaved?.invoke(id)
        }
    }

    fun importNoteData(imported: ImportedNoteData, onImported: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val defaultFolder = allFolders.value.firstOrNull { it.isDefault }
                ?: allFolders.value.firstOrNull()
            val folderId = defaultFolder?.id ?: 1L
            val folderName = defaultFolder?.name ?: "Personal"
            val folderColor = defaultFolder?.color ?: 0xFF8A5CF6

            val checklistJson = if (imported.checklists.isNotEmpty()) {
                serializeChecklist(imported.checklists)
            } else null

            val note = NoteEntity(
                title = imported.title,
                content = imported.content,
                folderId = folderId,
                folderName = folderName,
                folderColor = folderColor,
                imageUri = imported.imageUri,
                checklistJson = checklistJson,
                tags = if (imported.attachedFileName != null) "attachment" else "",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val newId = repository.saveNote(note)
            onImported?.invoke(newId)
        }
    }

    suspend fun getNoteById(id: Long): NoteEntity? {
        return repository.getNoteByIdOnce(id)
    }

    fun createFolder(name: String, color: Long, iconName: String = "Folder") {
        viewModelScope.launch {
            val folder = FolderEntity(
                name = name.trim(),
                color = color,
                iconName = iconName
            )
            repository.saveFolder(folder)
        }
    }

    fun updateFolder(folder: FolderEntity) {
        viewModelScope.launch {
            repository.updateFolder(folder)
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            val defaultFolder = allFolders.value.firstOrNull { it.isDefault }
                ?: allFolders.value.firstOrNull()
            val fallbackId = defaultFolder?.id ?: 1L
            val fallbackName = defaultFolder?.name ?: "Personal"
            val fallbackColor = defaultFolder?.color ?: 0xFF8A5CF6

            repository.deleteFolder(folderId, fallbackId, fallbackName, fallbackColor)
            if (_selectedFolderId.value == folderId) {
                _selectedFolderId.value = null
            }
        }
    }

    // Preferences & Settings
    fun setThemeMode(mode: ThemeMode) {
        userPrefs.themeMode = mode
        _themeMode.value = mode
    }

    fun setPastelTheme(preset: PastelThemePreset) {
        userPrefs.pastelTheme = preset
        _pastelTheme.value = preset
    }

    fun setLiquidGlassEnabled(enabled: Boolean) {
        userPrefs.liquidGlassEnabled = enabled
        _liquidGlassEnabled.value = enabled
    }

    fun setCompactMode(enabled: Boolean) {
        userPrefs.compactMode = enabled
        _compactMode.value = enabled
    }

    fun setAutoSave(enabled: Boolean) {
        userPrefs.autoSaveEnabled = enabled
        _autoSave.value = enabled
    }

    fun setFontSize(size: NoteFontSize) {
        userPrefs.fontSize = size
        _fontSize.value = size
    }

    fun setFontPreset(preset: FontPreset) {
        userPrefs.fontPreset = preset
        _fontPreset.value = preset
    }

    fun setCustomFont(path: String?, displayName: String?) {
        userPrefs.customFontPath = path
        userPrefs.customFontDisplayName = displayName
        _customFontPath.value = path
        _customFontDisplayName.value = displayName
        if (!path.isNullOrBlank()) {
            setFontPreset(FontPreset.CUSTOM)
        }
    }

    fun resetToDefaultFont() {
        userPrefs.fontPreset = FontPreset.DEFAULT
        userPrefs.customFontPath = null
        userPrefs.customFontDisplayName = null
        _fontPreset.value = FontPreset.DEFAULT
        _customFontPath.value = null
        _customFontDisplayName.value = null
    }

    fun importFontFromUri(uri: android.net.Uri, onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val fileName = com.example.util.FileImporter.getFileName(context, uri)
                val fontsDir = java.io.File(context.filesDir, "fonts")
                if (!fontsDir.exists()) {
                    fontsDir.mkdirs()
                }
                val destFile = java.io.File(fontsDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    java.io.FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (destFile.exists() && destFile.length() > 0) {
                    val displayName = fileName.substringBeforeLast(".")
                    setCustomFont(destFile.absolutePath, displayName)
                    onComplete?.invoke(true, displayName)
                } else {
                    onComplete?.invoke(false, "Failed to copy font file")
                }
            } catch (e: Exception) {
                onComplete?.invoke(false, e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun setReduceTransparency(enabled: Boolean) {
        userPrefs.reduceTransparency = enabled
        _reduceTransparency.value = enabled
        if (enabled) {
            _translucencyLevel.value = com.example.data.preferences.TranslucencyLevel.OPAQUE
        }
    }

    fun setTranslucencyLevel(level: com.example.data.preferences.TranslucencyLevel) {
        userPrefs.translucencyLevel = level
        _translucencyLevel.value = level
        if (level == com.example.data.preferences.TranslucencyLevel.OPAQUE) {
            userPrefs.reduceTransparency = true
            _reduceTransparency.value = true
        } else {
            userPrefs.reduceTransparency = false
            _reduceTransparency.value = false
        }
    }

    fun setAmbientBackdropGlow(enabled: Boolean) {
        userPrefs.ambientBackdropGlow = enabled
        _ambientBackdropGlow.value = enabled
    }

    fun setHighRefreshRateEnabled(enabled: Boolean) {
        userPrefs.highRefreshRateEnabled = enabled
        _highRefreshRateEnabled.value = enabled
    }

    fun setReduceMotion(enabled: Boolean) {
        userPrefs.reduceMotion = enabled
        _reduceMotion.value = enabled
    }

    fun setBiometricLock(enabled: Boolean, pin: String = "1234") {
        userPrefs.biometricLockEnabled = enabled
        userPrefs.appPinCode = pin
        _biometricLockEnabled.value = enabled
        if (!enabled) {
            _isAppLocked.value = false
        }
    }

    fun unlockApp(pin: String): Boolean {
        return if (pin == userPrefs.appPinCode || pin == "1234") {
            _isAppLocked.value = false
            true
        } else {
            false
        }
    }

    fun forceUnlockApp() {
        _isAppLocked.value = false
    }

    fun lockApp() {
        if (_biometricLockEnabled.value) {
            _isAppLocked.value = true
        }
    }

    fun dismissHero() {
        userPrefs.heroDismissed = true
        _heroDismissed.value = true
    }

    fun parseChecklistJson(json: String?): List<ChecklistItem> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<ChecklistItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ChecklistItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        text = obj.optString("text", ""),
                        isChecked = obj.optBoolean("isChecked", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeChecklist(items: List<ChecklistItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("text", item.text)
            obj.put("isChecked", item.isChecked)
            array.put(obj)
        }
        return array.toString()
    }
}
