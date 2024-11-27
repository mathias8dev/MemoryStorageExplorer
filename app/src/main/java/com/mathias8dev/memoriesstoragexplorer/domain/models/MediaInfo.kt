package com.mathias8dev.memoriesstoragexplorer.domain.models

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toFile
import kotlinx.parcelize.Parcelize
import java.io.File


@Parcelize
data class MediaInfo(
    val mediaId: Long? = null,
    val contentUri: Uri? = null,
    val privateContentUri: Uri? = null,
    val size: Long = 0L,
    val name: String? = null,
    val bucketName: String? = null,
    val bucketPrivateContentUri: Uri? = null
) : Parcelable

fun MediaInfo.toFile(): File? = privateContentUri?.toFile()


data class InstalledApp(
    val name: String,
    val packageName: String,
    val sourceDir: String,
    val appIcon: Drawable? = null,
    val isSystemApp: Boolean = false,
)