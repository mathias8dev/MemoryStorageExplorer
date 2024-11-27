package com.mathias8dev.memoriesstoragexplorer.domain.useCases.info

import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory

@Factory
class DocumentFilesInfoUseCase(private val context: Context) {
    suspend fun invoke(): Pair<Int, Long> = withContext(Dispatchers.IO) {
        val documentsMediaListUri = MediaStore.Files.getContentUri("external")
        var totalSize = 0L
        var totalCount = 0

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'application/%'"

        context.contentResolver.query(documentsMediaListUri, projection, selection, null, null)?.use { cursor ->
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

            while (cursor.moveToNext()) {
                totalCount++
                totalSize += cursor.getLong(sizeColumn)
            }
        }

        Pair(totalCount, totalSize)
    }
}
