package com.mathias8dev.memoriesstoragexplorer.injection

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mathias8dev.memoriesstoragexplorer.data.datasource.local.datastore.serializers.AppSettingsSerializer
import com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import java.io.File


@Module
class StorageModule {


    @Single
    fun provideSharedPreferences(context: Context): SharedPreferences {
        val masterKey: MasterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "LocalDataSource",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }


    @Single
    fun provideDataStore(context: Context): DataStore<AppSettings> {
        return MultiProcessDataStoreFactory.create(
            serializer = AppSettingsSerializer(),
            produceFile = {
                File(
                    context.cacheDir,
                    AppSettingsSerializer.filename
                )
            }
        )
    }
}