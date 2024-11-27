package com.mathias8dev.memoriesstoragexplorer.data.datasource

import com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.reflect.KProperty1

interface AppSettingsDataSource {

    val data: MutableSharedFlow<AppSettings>
    fun <V> onAppSettingsChanged(property: KProperty1<AppSettings, V>, updatedValue: V)

    fun onReinitializeSettings()
}