package com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.info.GetSizeFromPathUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File


@Factory
class QueryMediaListFromPathUseCase(
    private val context: Context,
    private val getSizeFromPathUseCase: GetSizeFromPathUseCase
) {

    suspend fun invoke(path: String): List<MediaInfo> = withContext(Dispatchers.IO) {
        val rootFile = File(path)
        val mediaList = mutableListOf<MediaInfo>()

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
        // Query only direct children (files or folders)
        val selection = "(${MediaStore.Files.FileColumns.DATA} LIKE ?) AND " +
                "(${MediaStore.Files.FileColumns.DATA} NOT LIKE ?) AND " +
                "(${MediaStore.Files.FileColumns.MIME_TYPE} IS NOT NULL OR ${MediaStore.Files.FileColumns.MIME_TYPE} IS NULL)"
        val selectionArgs = arrayOf("${rootFile.absolutePath}/%", "${rootFile.absolutePath}/%/%")

        val query = context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            null
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


                mediaList += MediaInfo(
                    mediaId = id,
                    name = name,
                    size = if (file.isFile) size else getSizeFromPathUseCase.invoke(file.absolutePath),
                    contentUri = contentUri,
                    privateContentUri = file.toUri(),
                    bucketName = bucketName,
                    bucketPrivateContentUri = file.parentFile?.toUri(),
                    mimeTypeString = mimeType
                )
            }
        }

        mediaList

    }
}