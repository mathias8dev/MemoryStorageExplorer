package com.mathias8dev.memoriesstoragexplorer.domain.useCases.info

import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory

@Factory
class AudioFilesInfoUseCase(private val context: Context) {
    suspend fun invoke(): Pair<Int, Long> = withContext(Dispatchers.IO) {
        val audioMediaListUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var totalSize = 0L
        var totalCount = 0

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.SIZE
        )

        context.contentResolver.query(audioMediaListUri, projection, null, null, null)?.use { cursor ->
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            while (cursor.moveToNext()) {
                totalCount++
                totalSize += cursor.getLong(sizeColumn)
            }
        }

        Pair(totalCount, totalSize)
    }
}
