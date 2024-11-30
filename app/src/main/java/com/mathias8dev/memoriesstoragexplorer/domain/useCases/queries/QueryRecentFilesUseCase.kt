package com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries

import android.content.ContentUris
import android.content.Context
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
class QueryRecentFilesUseCase(private val context: Context) {
    suspend fun invoke(): List<MediaInfo> = withContext(Dispatchers.IO) {
        val recentFiles = mutableListOf<MediaInfo>()

        // Determine the URI to query based on Android version
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        // Calculate the timestamp for one month ago
        val oneMonthAgo = System.currentTimeMillis() / 1000L - (30L * 24L * 60L * 60L)

        // Define the columns to retrieve
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        // Define the selection criteria to exclude folders
        val selection = MediaStore.Files.FileColumns.DATE_MODIFIED + " >= ? AND " +
                MediaStore.Files.FileColumns.MIME_TYPE + " IS NOT NULL"
        val selectionArgs = arrayOf(oneMonthAgo.toString())

        // Sort the results by modification date
        val sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"

        // Perform the query
        val cursor = context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val size = cursor.getLong(sizeColumn)
                val filePath = cursor.getString(dataColumn)
                val name = cursor.getString(nameColumn)
                val bucketName = cursor.getStringOrNull(bucketNameColumn)
                val mimeType = cursor.getString(mimeTypeColumn)

                val contentUri = ContentUris.withAppendedId(collection, id)
                val file = File(filePath)

                recentFiles += MediaInfo(
                    mediaId = id,
                    contentUri = contentUri,
                    privateContentUri = file.toUri(),
                    name = name,
                    size = size,
                    bucketName = bucketName,
                    bucketPrivateContentUri = file.parentFile?.toUri(),
                    mimeTypeString = mimeType
                )
            }
        }

        recentFiles
    }
}