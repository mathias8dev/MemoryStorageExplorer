package com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File


@Factory
class SaveTextToUriUseCase(private val context: Context) {

    suspend fun invoke(uri: Uri, content: String): Boolean = withContext(Dispatchers.IO) {
        val file = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
                File(filePath)
            } else {
                null
            }
        }

        if (file != null) {
            runCatching {
                file.writeText(content)
                true
            }.getOrElse {
                false
            }
        } else {
            false
        }
    }
}