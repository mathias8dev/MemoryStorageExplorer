package com.mathias8dev.memoriesstoragexplorer

import androidx.lifecycle.ViewModel
import com.mathias8dev.memoriesstoragexplorer.data.repositories.AppSettingsRepository
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class SettingsProviderViewModel(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    val appSettings = appSettingsRepository.data

}