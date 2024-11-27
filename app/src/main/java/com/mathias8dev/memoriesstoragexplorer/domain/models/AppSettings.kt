package com.mathias8dev.memoriesstoragexplorer.domain.models

import android.os.Parcelable
import androidx.compose.runtime.compositionLocalOf
import kotlinx.parcelize.Parcelize


@Parcelize
data class AppSettings(
    val selectedConfigId: Long = -1,
    val useDarkMode: Boolean = false,
    val activateFloatingMenu: Boolean = true,
    val useFloatingMenu: Boolean = true,
    val showHiddenFiles: Boolean = false,
) : Parcelable

val LocalAppSettings = compositionLocalOf { AppSettings() }