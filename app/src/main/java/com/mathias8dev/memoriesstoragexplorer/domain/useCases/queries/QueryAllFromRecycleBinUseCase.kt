package com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import timber.log.Timber
import java.io.File

@Factory
class QueryAllFromRecycleBinUseCase(private val context: Context) {
    suspend fun invoke(): List<MediaInfo> = withContext(Dispatchers.IO) {

        val recycleBinMediaList = mutableListOf<MediaInfo>()

        // Define your recycle bin folder (modify as per your app's logic)
        val recycleBinFolderPath = Environment.getExternalStorageDirectory().absolutePath + "/.recycle"
        Timber.d("Recycle bin folder path: $recycleBinFolderPath")
        Timber.d("Context external cache dir: ${context.getExternalFilesDir(null)}")
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

        // Query only files in the recycle bin folder
        val selection = "${MediaStore.Files.FileColumns.DATA} LIKE ? AND ${MediaStore.Files.FileColumns.MIME_TYPE} IS NOT NULL"
        val selectionArgs = arrayOf("$recycleBinFolderPath/%")

        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        val query = context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val size = cursor.getLong(sizeColumn)
                val filePath = cursor.getString(dataColumn)
                val bucketName = cursor.getStringOrNull(bucketNameColumn)
                val mimeType = cursor.getString(mimeTypeColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    collection,
                    id
                )

                val file = File(filePath)
                Timber.d("The file path is $filePath")

                recycleBinMediaList += MediaInfo(
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
        }

        recycleBinMediaList
    }
}
