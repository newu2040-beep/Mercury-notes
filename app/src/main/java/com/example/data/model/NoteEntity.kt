package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val folderId: Long = 1L,
    val folderName: String = "Personal",
    val folderColor: Long = 0xFF8A5CF6,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val colorTag: Long = 0L, // 0 = default, otherwise custom color
    val imageUri: String? = null,
    val checklistJson: String? = null,
    val tags: String = "",
    val isLocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
