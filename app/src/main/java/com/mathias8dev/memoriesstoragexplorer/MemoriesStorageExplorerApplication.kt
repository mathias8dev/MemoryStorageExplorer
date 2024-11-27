package com.mathias8dev.memoriesstoragexplorer

import android.app.Application
import com.mathias8dev.memoriesstoragexplorer.injection.MiscModule
import com.mathias8dev.memoriesstoragexplorer.injection.StorageModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule
import org.koin.ksp.generated.module
import timber.log.Timber

class MemoriesStorageExplorerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        startKoin {
            androidContext(this@MemoriesStorageExplorerApplication)
            modules(
                defaultModule,
                StorageModule().module,
                MiscModule().module,
            )
        }
    }
}