package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.FolderDao
import com.example.data.dao.NoteDao
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [NoteEntity::class, FolderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MercuryDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile
        private var INSTANCE: MercuryDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MercuryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MercuryDatabase::class.java,
                    "mercurynotes_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.folderDao(), database.noteDao())
                    }
                }
            }
        }

        private suspend fun populateInitialData(folderDao: FolderDao, noteDao: NoteDao) {
            val defaultFolders = listOf(
                FolderEntity(id = 1L, name = "Personal", color = 0xFF8A5CF6, iconName = "Person", isDefault = true),
                FolderEntity(id = 2L, name = "Work", color = 0xFF3B82F6, iconName = "Work", isDefault = false),
                FolderEntity(id = 3L, name = "Ideas", color = 0xFFF59E0B, iconName = "Lightbulb", isDefault = false),
                FolderEntity(id = 4L, name = "Journal", color = 0xFF10B981, iconName = "Book", isDefault = false)
            )
            folderDao.insertFolders(defaultFolders)

            val now = System.currentTimeMillis()
            val sampleNotes = listOf(
                NoteEntity(
                    id = 1L,
                    title = "Design Ideas",
                    content = "A clean and minimal interface with soft glassmorphism and smooth animations. Focus on simplicity and usability.\n\n• Subtle frosted blur & glowing contours\n• Apple-inspired bold semibold typography\n• Blue, violet and soft pink gradient accents\n• Distraction-free writing experience",
                    folderId = 2L,
                    folderName = "Work",
                    folderColor = 0xFF3B82F6,
                    isPinned = true,
                    isFavorite = true,
                    tags = "design,ui,mercury",
                    createdAt = now - 3600000 * 2,
                    updatedAt = now - 1800000
                ),
                NoteEntity(
                    id = 2L,
                    title = "Project Ideas",
                    content = "Here are some exciting project concepts to explore:\n\n• AI powered note taking assistant\n• Minimalist habit & focus tracker\n• Creative photo & canvas editor\n• Offline-first knowledge repository",
                    folderId = 3L,
                    folderName = "Ideas",
                    folderColor = 0xFFF59E0B,
                    isPinned = true,
                    isFavorite = true,
                    tags = "projects,roadmap",
                    createdAt = now - 3600000 * 24,
                    updatedAt = now - 3600000 * 12
                ),
                NoteEntity(
                    id = 3L,
                    title = "Morning Routine",
                    content = "Daily energizing morning habits:\n\n1. Wake up at 6:30 AM without snooze\n2. Drink 500ml warm lemon water\n3. 15-minute mindfulness breathing\n4. Review top 3 daily focus outcomes",
                    folderId = 1L,
                    folderName = "Personal",
                    folderColor = 0xFF8A5CF6,
                    isPinned = false,
                    isFavorite = false,
                    tags = "routine,health",
                    createdAt = now - 3600000 * 48,
                    updatedAt = now - 3600000 * 20
                ),
                NoteEntity(
                    id = 4L,
                    title = "Travel Plans",
                    content = "Japan Autumn Itinerary:\n\n• Tokyo: Shibuya Sky, teamLab Planets, Akihabara\n• Kyoto: Fushimi Inari, Arashiyama Bamboo Grove\n• Osaka: Dotonbori street food tour & Castle",
                    folderId = 1L,
                    folderName = "Personal",
                    folderColor = 0xFF8A5CF6,
                    isPinned = false,
                    isFavorite = true,
                    tags = "travel,japan",
                    createdAt = now - 3600000 * 72,
                    updatedAt = now - 3600000 * 36
                ),
                NoteEntity(
                    id = 5L,
                    title = "Books to Read",
                    content = "Reading list for this season:\n\n• Atomic Habits by James Clear\n• Deep Work by Cal Newport\n• The Design of Everyday Things by Don Norman\n• Show Your Work by Austin Kleon",
                    folderId = 4L,
                    folderName = "Journal",
                    folderColor = 0xFF10B981,
                    isPinned = false,
                    isFavorite = false,
                    tags = "reading,books",
                    createdAt = now - 3600000 * 96,
                    updatedAt = now - 3600000 * 50
                )
            )

            for (note in sampleNotes) {
                noteDao.insertNote(note)
            }
        }
    }
}
