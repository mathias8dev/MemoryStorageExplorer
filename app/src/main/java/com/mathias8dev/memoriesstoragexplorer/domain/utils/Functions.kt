package com.mathias8dev.memoriesstoragexplorer.domain.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import java.io.File


fun renameFileIfExists(file: File): File {
    var newFile = file
    var counter = 1
    val fileNameWithoutExt = file.nameWithoutExtension
    val extension = file.extension
    while (newFile.exists()) {
        val numberedFileName = if (extension.isEmpty()) {
            "$fileNameWithoutExt ($counter)"
        } else {
            "$fileNameWithoutExt ($counter).$extension"
        }
        newFile = File(file.parent, numberedFileName)
        counter++
    }
    Timber.d("File renamed to: ${newFile.name}")
    return newFile
}

inline fun <reified T> koinInject(): Lazy<T> {
    return KoinJavaComponent.inject(T::class.java)
}

inline fun <reified T> tryOrNull(block: () -> T?): T? {
    return runCatching {
        block()
    }.getOrNull()
}

fun allPermissionsIsGranted(context: Context, manifestKeys: Collection<String>): Boolean {
    return manifestKeys.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}