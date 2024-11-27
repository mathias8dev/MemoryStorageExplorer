package com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File


@Factory
class QueryAllDocumentsUseCase(private val context: Context) {
    suspend fun invoke(): List<MediaInfo> = withContext(Dispatchers.IO) {
        val documentList = mutableListOf<MediaInfo>()

        // Determine the URI to query based on Android version
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME
        )

        // Define the selection criteria for documents, including ePub files
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} IN (?, ?, ?, ?, ?, ?)"
        val selectionArgs = arrayOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/epub+zip"  // Added MIME type for ePub files
        )

        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        val cursor: Cursor? = context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown"
                val size = cursor.getLong(sizeColumn)
                val bucketName = cursor.getString(bucketNameColumn)
                val filePath = cursor.getString(dataColumn)

                // Build content URIs
                val contentUri: Uri = Uri.withAppendedPath(collection, id.toString())
                val file = File(filePath)

                val mediaInfo = MediaInfo(
                    mediaId = id,
                    contentUri = contentUri,
                    privateContentUri = file.toUri(),
                    name = name,
                    size = size,
                    bucketName = bucketName,
                    bucketPrivateContentUri = file.parentFile?.toUri()
                )

                documentList.add(mediaInfo)
            }
        }

        documentList
    }
}