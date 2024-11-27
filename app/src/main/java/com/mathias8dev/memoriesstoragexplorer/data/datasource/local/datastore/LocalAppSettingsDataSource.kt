package com.mathias8dev.memoriesstoragexplorer.data.datasource.local.datastore

import androidx.datastore.core.DataStore
import com.mathias8dev.memoriesstoragexplorer.data.datasource.AppSettingsDataSource
import com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings
import com.mathias8dev.memoriesstoragexplorer.domain.utils.CoroutineScopeOwner
import com.mathias8dev.memoriesstoragexplorer.domain.utils.CoroutineScopeProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import timber.log.Timber
import kotlin.reflect.KProperty1


@Single
@Named("LocalAppSettingsDataSource")
class LocalAppSettingsDataSource(
    private val appSettingsStore: DataStore<AppSettings>
) : AppSettingsDataSource, CoroutineScopeProvider by CoroutineScopeOwner() {

    override val data = MutableSharedFlow<AppSettings>(replay = 3)

    init {
        coroutineScope.launch {
            appSettingsStore.data.collectLatest {
                Timber.d("OnCollect")
                data.emit(it)
            }
        }
    }

    override fun <V> onAppSettingsChanged(property: KProperty1<AppSettings, V>, updatedValue: V) {
        Timber.d("OnSettings changed")
        coroutineScope.launch {
            appSettingsStore.updateData {
                val updated = it.copy()
                updated.javaClass.declaredFields.find { field -> field.name == property.name }?.let { field ->
                    field.isAccessible = true
                    field.set(updated, updatedValue)
                }

                updated
            }
        }
    }

    override fun onReinitializeSettings() {
        coroutineScope.launch {
            appSettingsStore.updateData {
                AppSettings()
            }
        }
    }
}