package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "notebooks")
data class NotebookEntity(
    @PrimaryKey val id: String,
    val name: String,
    val updatedAt: Long,
    val pageCount: Int,
    val subjectLabel: String,
    val pagesJson: String
)

@Entity(tableName = "resources")
data class ResourceEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val createdAt: Long,
    val itemJson: String
)

@Dao
interface NotebookDao {
    @Query("SELECT * FROM notebooks ORDER BY updatedAt DESC")
    fun observeAllNotebooks(): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM notebooks WHERE id = :id LIMIT 1")
    suspend fun getNotebookById(id: String): NotebookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotebook(notebook: NotebookEntity)

    @Query("DELETE FROM notebooks WHERE id = :id")
    suspend fun deleteNotebookById(id: String)
}

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resources ORDER BY createdAt DESC")
    fun observeAllResources(): Flow<List<ResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertResource(resource: ResourceEntity)

    @Query("DELETE FROM resources WHERE id = :id")
    suspend fun deleteResourceById(id: String)
}

@Database(
    entities = [NotebookEntity::class, ResourceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun notebookDao(): NotebookDao
    abstract fun resourceDao(): ResourceDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getInstance(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note10_whiteboard.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
