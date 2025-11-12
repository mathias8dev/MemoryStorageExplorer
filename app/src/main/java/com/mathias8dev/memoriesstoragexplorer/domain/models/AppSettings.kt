package com.mathias8dev.memoriesstoragexplorer.domain.models

import android.os.Parcelable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.mathias8dev.memoriesstoragexplorer.domain.enums.LayoutMode
import com.mathias8dev.memoriesstoragexplorer.domain.enums.ThemeMode
import kotlinx.parcelize.Parcelize


@Parcelize
data class AppSettings(
    val selectedConfigId: Long = -1,
    val useDarkMode: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themeSeedColor: Long = 0xFF6750A4, // Default Material 3 purple
    val activateFloatingMenu: Boolean = true,
    val useFloatingMenu: Boolean = true,
    val showHiddenFiles: Boolean = false,
    val layoutMode: LayoutMode = LayoutMode.COLUMNED,
) : Parcelable {
    val seedColor: Color get() = Color(themeSeedColor)
}

val LocalAppSettings = compositionLocalOf { AppSettings() }