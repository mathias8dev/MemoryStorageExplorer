package com.mathias8dev.memoriesstoragexplorer.domain.useCases.info

import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory

@Factory
class VideoFilesInfoUseCase(private val context: Context) {
    suspend fun invoke(): Pair<Int, Long> = withContext(Dispatchers.IO) {
        val videosMediaListUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        var totalSize = 0L
        var totalCount = 0

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.SIZE
        )

        context.contentResolver.query(videosMediaListUri, projection, null, null, null)?.use { cursor ->
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (cursor.moveToNext()) {
                totalCount++
                totalSize += cursor.getLong(sizeColumn)
            }
        }

        Pair(totalCount, totalSize)
    }
}
