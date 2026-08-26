package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val color: Long,
    val iconName: String = "Folder",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
