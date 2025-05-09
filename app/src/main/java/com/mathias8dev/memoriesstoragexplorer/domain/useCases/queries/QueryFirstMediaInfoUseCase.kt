package com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.domain.utils.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import timber.log.Timber
import java.io.File


@Factory
class QueryFirstMediaInfoUseCase(private val context: Context) {

    suspend fun invoke(directoryPath: String, mimeTypeArg: String = "image/%"): MediaInfo? = withContext(Dispatchers.IO) {

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Files.getContentUri("external")
            }

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val selection = "(${MediaStore.Files.FileColumns.DATA} LIKE ?) AND " +
                "(${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?)"

        val selectionArgs = arrayOf(
            "$directoryPath/%",
            mimeTypeArg
        )

        Timber.d("The query is $selection")
        Timber.d("The selctionArgs is ${selectionArgs.toJson()}")

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        var mediaInfo: MediaInfo? = null


        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val dataUriColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val size = cursor.getLong(sizeColumn)
                val mimeType = cursor.getString(mimeTypeColumn)
                val bucketName = cursor.getString(bucketNameColumn)

                val contentUri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                val file = File(cursor.getString(dataUriColumn))


                mediaInfo = MediaInfo(
                    mediaId = id,
                    contentUri = contentUri,
                    privateContentUri = file.toUri(),
                    bucketPrivateContentUri = file.parentFile?.toUri(),
                    size = size,
                    name = name,
                    bucketName = bucketName,
                    mimeTypeString = mimeType,
                )

                Timber.d("The mediaInfo is $mediaInfo")
            }
        }

        mediaInfo
    }

}

private fun getDirectoryPathFromUri(contentResolver: ContentResolver, uri: Uri): String? {
    // For content URIs
    if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val filePath = cursor.getString(pathIndex)
                return filePath.substringBeforeLast('/')
            }
        }
    }

    // For file URIs
    if (uri.scheme == ContentResolver.SCHEME_FILE) {
        val path = uri.path
        if (path != null) {
            return path.substringBeforeLast('/')
        }
    }

    return null
}