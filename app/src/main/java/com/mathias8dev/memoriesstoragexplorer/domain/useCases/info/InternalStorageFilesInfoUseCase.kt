package com.mathias8dev.memoriesstoragexplorer.domain.useCases.info

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory

@Factory
class InternalStorageFilesInfoUseCase(private val context: Context) {
    suspend fun invoke(): Pair<Int, Long> = withContext(Dispatchers.IO) {
        var totalSize = 0L
        var totalCount = 0

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Files.getContentUri("external")
            }

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.SIZE,
        )

        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " IS NOT NULL"

        val query = context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            null
        )

        query?.use { cursor ->
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

            while (cursor.moveToNext()) {
                totalCount++
                totalSize += cursor.getLong(sizeColumn)
            }
        }

        Pair(totalCount, totalSize)
    }
}
