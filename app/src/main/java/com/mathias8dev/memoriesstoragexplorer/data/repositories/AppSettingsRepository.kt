package com.mathias8dev.memoriesstoragexplorer.data.repositories

import com.mathias8dev.memoriesstoragexplorer.data.datasource.AppSettingsDataSource
import com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings
import com.mathias8dev.memoriesstoragexplorer.domain.services.AppSettingsService
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import timber.log.Timber
import kotlin.reflect.KProperty1


@Single
class AppSettingsRepository(
    @Named("LocalAppSettingsDataSource")
    private val appSettingsDataSource: AppSettingsDataSource
) : AppSettingsService {

    val data = appSettingsDataSource.data

    override fun <V> onAppSettingsChanged(property: KProperty1<AppSettings, V>, updatedValue: V) {
        Timber.d("OnUpdating")
        appSettingsDataSource.onAppSettingsChanged(property, updatedValue)
    }

    override fun onReinitializeSettings() {
        appSettingsDataSource.onReinitializeSettings()
    }
}