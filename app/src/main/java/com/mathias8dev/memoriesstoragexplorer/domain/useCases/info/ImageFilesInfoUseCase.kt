package com.mathias8dev.memoriesstoragexplorer.domain.useCases.info

import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory

@Factory
class ImageFilesInfoUseCase(private val context: Context) {
    suspend fun invoke(): Pair<Int, Long> = withContext(Dispatchers.IO) {
        val imagesMediaListUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        var totalSize = 0L
        var totalCount = 0

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.SIZE
        )

        context.contentResolver.query(imagesMediaListUri, projection, null, null, null)?.use { cursor ->
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {
                totalCount++
                totalSize += cursor.getLong(sizeColumn)
            }
        }

        Pair(totalCount, totalSize)
    }
}
