package com.mathias8dev.memoriesstoragexplorer.injection

import android.content.Context
import android.os.storage.StorageManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single


@Module
class MiscModule {


    @Single
    fun provideGson(): Gson {
        return GsonBuilder()
            .create()
    }

    @Single
    fun provideStorageManager(context: Context): StorageManager {
        return context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    }
}