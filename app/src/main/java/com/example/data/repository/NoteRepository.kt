package com.example.data.repository

import com.example.data.dao.FolderDao
import com.example.data.dao.NoteDao
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao,
    private val folderDao: FolderDao
) {
    val allActiveNotes: Flow<List<NoteEntity>> = noteDao.getAllActiveNotes()
    val favoriteNotes: Flow<List<NoteEntity>> = noteDao.getFavoriteNotes()
    val deletedNotes: Flow<List<NoteEntity>> = noteDao.getDeletedNotes()
    val activeNotesCount: Flow<Int> = noteDao.getActiveNotesCount()
    val deletedNotesCount: Flow<Int> = noteDao.getDeletedNotesCount()
    val allFolders: Flow<List<FolderEntity>> = folderDao.getAllFolders()

    fun getNotesByFolder(folderId: Long): Flow<List<NoteEntity>> =
        noteDao.getNotesByFolder(folderId)

    fun getNoteById(id: Long): Flow<NoteEntity?> =
        noteDao.getNoteById(id)

    suspend fun getNoteByIdOnce(id: Long): NoteEntity? =
        noteDao.getNoteByIdOnce(id)

    fun searchNotes(query: String): Flow<List<NoteEntity>> =
        noteDao.searchNotes(query)

    suspend fun saveNote(note: NoteEntity): Long =
        noteDao.insertNote(note)

    suspend fun moveToTrash(id: Long) =
        noteDao.softDelete(id)

    suspend fun restoreNote(id: Long) =
        noteDao.restoreNote(id)

    suspend fun deletePermanently(id: Long) =
        noteDao.deletePermanently(id)

    suspend fun emptyTrash() =
        noteDao.emptyTrash()

    suspend fun deleteAllNotes() =
        noteDao.deleteAllNotes()

    suspend fun saveFolder(folder: FolderEntity): Long =
        folderDao.insertFolder(folder)

    suspend fun updateFolder(folder: FolderEntity) =
        folderDao.updateFolder(folder)

    suspend fun deleteFolder(folderId: Long, fallbackFolderId: Long, fallbackName: String, fallbackColor: Long) {
        noteDao.reassignFolderNotes(folderId, fallbackFolderId, fallbackName, fallbackColor)
        folderDao.deleteFolder(folderId)
    }
}
