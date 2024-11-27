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
class QueryAllApksUseCase(private val context: Context) {

    suspend fun invoke(): List<MediaInfo> = withContext(Dispatchers.IO) {
        // URI for querying any type of file
        val apkList = mutableListOf<MediaInfo>()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Files.getContentUri("external")
            }


        // MIME types for different archive formats
        val mimeType = "application/vnd.android.package-archive"


        // Selection criteria to match any of the MIME types
        val selection = MediaStore.Files.FileColumns.MIME_TYPE + "=?"


        // Convert MIME types array to selectionArgs
        val selectionArgs = arrayOf(mimeType)
        val sortOrder = "${MediaStore.Files.FileColumns.DISPLAY_NAME} ASC"

        // Projection to specify the columns we are interested in
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME
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

                val contentUri: Uri = ContentUris.withAppendedId(
                    collection,
                    id
                )

                // Process the archive file details
                apkList += MediaInfo(
                    mediaId = id,
                    name = name,
                    size = size,
                    contentUri = contentUri,
                    privateContentUri = File(filePath).toUri(),
                    bucketName = bucketName,
                    bucketPrivateContentUri = null
                )
            }
            cursor.close()
        }

        apkList
    }
}