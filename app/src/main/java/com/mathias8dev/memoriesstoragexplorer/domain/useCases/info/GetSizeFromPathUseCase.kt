package com.mathias8dev.memoriesstoragexplorer.domain.useCases.info

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import org.koin.core.annotation.Factory
import java.io.File


@Factory
class GetSizeFromPathUseCase(private val context: Context) {

    fun invoke(path: String): Long {
        val file = File(path)
        if (file.isFile) return file.length()
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Files.getContentUri("external")
            }

        val projection = arrayOf(
            MediaStore.Files.FileColumns.SIZE,
        )

        val selection = "(${MediaStore.Files.FileColumns.DATA} LIKE ?) "
        val selectionArgs = arrayOf("${path}/%")

        val query = context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            null
        )

        var totalSize = 0L
        query?.use { cursor ->
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            while (cursor.moveToNext()) {
                val size = cursor.getLong(sizeColumn)
                totalSize += size
            }
        }

        return totalSize
    }

}