package com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
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
class QueryAllArchivesUseCase(private val context: Context) {
    suspend fun invoke(): List<MediaInfo> = withContext(Dispatchers.IO) {

        // URI for querying any type of file
        val archiveList = mutableListOf<MediaInfo>()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Files.getContentUri("external")
            }


        // MIME types for different archive formats
        val mimeTypes = arrayOf(
            "application/zip",
            "application/x-rar-compressed",
            "application/x-tar",
            "application/x-7z-compressed",
            "application/gzip"
        )


        // Selection criteria to match any of the MIME types
        var selection = MediaStore.Files.FileColumns.MIME_TYPE + "=?"
        for (i in 1 until mimeTypes.size) {
            selection += " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
        }


        // Convert MIME types array to selectionArgs
        val selectionArgs = mimeTypes
        val sortOrder = "${MediaStore.Files.FileColumns.DISPLAY_NAME} ASC"

        // Projection to specify the columns we are interested in
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE
        )


        // Perform the query
        val cursor: Cursor? = context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )



        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Get details of each file
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME))
                val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE))
                val filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
                val bucketName = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME))
                val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))

                val contentUri: Uri = ContentUris.withAppendedId(
                    collection,
                    id
                )

                val file = File(filePath)

                // Process the archive file details
                archiveList += MediaInfo(
                    mediaId = id,
                    name = name,
                    size = size,
                    contentUri = contentUri,
                    privateContentUri = file.toUri(),
                    bucketName = bucketName,
                    bucketPrivateContentUri = file.parentFile?.toUri(),
                    mimeTypeString = mimeType
                )
            }
            cursor.close()
        }

        archiveList
    }
}