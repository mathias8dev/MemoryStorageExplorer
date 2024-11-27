package com.mathias8dev.memoriesstoragexplorer.domain.services

import com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings
import kotlin.reflect.KProperty1

interface AppSettingsService {
    fun <V> onAppSettingsChanged(property: KProperty1<AppSettings, V>, updatedValue: V)

    fun onReinitializeSettings()
}