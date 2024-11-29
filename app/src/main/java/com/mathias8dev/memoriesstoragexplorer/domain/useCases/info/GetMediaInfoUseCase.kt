package com.mathias8dev.memoriesstoragexplorer.domain.useCases.info

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File


@Factory
class GetMediaInfoUseCase(private val context: Context) {

    suspend fun invoke(uri: Uri): MediaInfo = withContext(Dispatchers.IO) {
        var mediaInfo = MediaInfo(contentUri = uri)

        context.contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                val name = it.getString(it.getColumnIndexOrThrow("_display_name"))
                val size = it.getLong(it.getColumnIndexOrThrow("_size"))
                val filePath = it.getString(it.getColumnIndexOrThrow("_data"))
                val file = File(filePath)
                mediaInfo = mediaInfo.copy(
                    name = name,
                    size = size,
                    privateContentUri = file.toUri(),
                    bucketPrivateContentUri = file.parentFile?.toUri(),
                )
            }
        }

        mediaInfo
    }

}