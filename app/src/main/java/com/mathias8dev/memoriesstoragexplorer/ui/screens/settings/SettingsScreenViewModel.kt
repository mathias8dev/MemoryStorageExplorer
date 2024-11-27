package com.mathias8dev.memoriesstoragexplorer.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.mathias8dev.memoriesstoragexplorer.data.repositories.AppSettingsRepository
import com.mathias8dev.memoriesstoragexplorer.domain.services.AppSettingsService
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class SettingsScreenViewModel(
    appSettingsRepository: AppSettingsRepository,
) : ViewModel(), AppSettingsService by appSettingsRepository