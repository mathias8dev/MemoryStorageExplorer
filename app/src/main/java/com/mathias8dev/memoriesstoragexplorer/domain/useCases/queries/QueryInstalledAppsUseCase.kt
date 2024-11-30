package com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries

import android.content.Context
import androidx.core.net.toUri
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import timber.log.Timber
import java.io.File


@Factory
class QueryInstalledAppsUseCase(private val context: Context) {

    suspend fun invoke(): List<MediaInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledApplications(0) // Retrieve all installed apps
        val appMediaInfoList = mutableListOf<MediaInfo>()

        for (app in installedApps) {
            try {
                // App name
                val appName = packageManager.getApplicationLabel(app).toString()

                // APK file
                val apkFile = File(app.sourceDir)


                // Add to MediaInfo list
                appMediaInfoList += MediaInfo(
                    mediaId = apkFile.hashCode().toLong(), // Unique ID using APK file's hash
                    name = appName,
                    size = apkFile.length(), // APK size
                    contentUri = apkFile.toUri(), // URI pointing to the APK
                    privateContentUri = apkFile.toUri(), // Same as above
                    bucketName = "Installed Apps", // Group name (optional)
                    bucketPrivateContentUri = null,
                    mimeTypeString = "application/vnd.android.package-archive"
                )
            } catch (e: Exception) {
                // Log and skip this app if there's an issue
                Timber.w("Failed to process app: ${app.packageName}, Error: ${e.message}")
            }
        }

        appMediaInfoList
    }
}