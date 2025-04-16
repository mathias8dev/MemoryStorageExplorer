package com.mathias8dev.memoriesstoragexplorer.domain.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.storage.StorageManager
import androidx.annotation.StringRes
import com.mathias8dev.memoriesstoragexplorer.BuildConfig
import com.mathias8dev.memoriesstoragexplorer.domain.models.SemanticVersion
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.GetFileNameUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.absolutePathOrNull
import kotlinx.coroutines.runBlocking

object Utils {
    val appPlayStoreDownloadUrl: String
        get() = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"

    val appCurrentVersion: String
        get() = BuildConfig.VERSION_NAME.split("-")[0]

    val appCurrentVersionSemantic: SemanticVersion
        get() = SemanticVersion.from(appCurrentVersion)!!

    fun hasInternetConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // For 29 api or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                    ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        }
        // For below 29 api
        else {
            @Suppress("DEPRECATION")
            if (connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnectedOrConnecting) {
                return true
            }
        }
        return false
    }


    fun hasRemovableSdCard(context: Context = koinGet<Context>()): Boolean {
        return getStorageManager(context).storageVolumes.any { it.isRemovable }
    }

    fun getStorageManager(context: Context = koinGet<Context>()): StorageManager {
        return context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    }

    fun getStorageVolumes(context: Context = koinGet<Context>()): List<String> {
        return getStorageManager(context)
            .storageVolumes.mapNotNull {
                it.absolutePathOrNull()
            }
    }

    fun getFileName(path: String): String {
        val getFileNameUseCase = koinGet<GetFileNameUseCase>()
        return runBlocking { getFileNameUseCase(path) }
    }

    fun getStringRes(context: Context = koinGet(), @StringRes resId: Int): String {
        return context.getString(resId)
    }

}