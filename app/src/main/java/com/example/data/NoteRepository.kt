package com.example.data

import com.example.model.BoardItem
import com.example.model.BoardPage
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class NoteRepository(
    private val notebookDao: NotebookDao,
    private val resourceDao: ResourceDao
) {
    val notebooks: Flow<List<NotebookEntity>> = notebookDao.observeAllNotebooks()
    val resources: Flow<List<ResourceEntity>> = resourceDao.observeAllResources()

    suspend fun saveNotebook(
        id: String,
        name: String,
        subjectLabel: String,
        pages: List<BoardPage>
    ) {
        val entity = NotebookEntity(
            id = id,
            name = name,
            updatedAt = System.currentTimeMillis(),
            pageCount = pages.size,
            subjectLabel = subjectLabel,
            pagesJson = BoardSerializer.serializePages(pages)
        )
        notebookDao.upsertNotebook(entity)
    }

    suspend fun loadNotebookPages(id: String): Pair<NotebookEntity, List<BoardPage>>? {
        val entity = notebookDao.getNotebookById(id) ?: return null
        val pages = BoardSerializer.deserializePages(entity.pagesJson)
        return entity to pages
    }

    suspend fun deleteNotebook(id: String) {
        notebookDao.deleteNotebookById(id)
    }

    suspend fun saveToResourceLibrary(title: String, category: String, item: BoardItem) {
        val entity = ResourceEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            category = category,
            createdAt = System.currentTimeMillis(),
            itemJson = BoardSerializer.serializeItem(item)
        )
        resourceDao.upsertResource(entity)
    }

    suspend fun deleteResource(id: String) {
        resourceDao.deleteResourceById(id)
    }
}
