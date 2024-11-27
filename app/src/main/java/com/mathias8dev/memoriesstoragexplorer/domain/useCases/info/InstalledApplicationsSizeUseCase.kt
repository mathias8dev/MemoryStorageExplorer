package com.mathias8dev.memoriesstoragexplorer.domain.useCases.info

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File

@Factory
class InstalledApplicationsSizeUseCase(private val context: Context) {
    suspend fun invoke(): Pair<Int, Long> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledApplications(0)
        var totalSize = 0L
        var appCount = 0

        installedApps.forEach { app ->
            val apkFile = File(app.sourceDir)
            if (apkFile.exists()) {
                appCount++
                totalSize += apkFile.length()
            }
        }

        Pair(appCount, totalSize)
    }
}
