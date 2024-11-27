package com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File


@Factory
class QueryAllAudiosUseCase(private val context: Context) {
    suspend fun invoke(): List<MediaInfo> = withContext(Dispatchers.IO) {
        val audioList = mutableListOf<MediaInfo>()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.BUCKET_DISPLAY_NAME
        )


// Display audio in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val query = context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )
        query?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dataUriColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val bucketName = cursor.getStringOrNull(bucketNameColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val file = File(cursor.getString(dataUriColumn))

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                audioList += MediaInfo(
                    mediaId = id,
                    contentUri = contentUri,
                    privateContentUri = file.toUri(),
                    name = name,
                    size = size,
                    bucketName = bucketName,
                    bucketPrivateContentUri = file.parentFile?.toUri(),
                )
            }
        }

        audioList
    }
}