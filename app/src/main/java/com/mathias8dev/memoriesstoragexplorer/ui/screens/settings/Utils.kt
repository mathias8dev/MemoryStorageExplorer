package com.mathias8dev.memoriesstoragexplorer.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.mathias8dev.memoriesstoragexplorer.domain.models.LocalAppSettings


data class AppbarColors(
    val containerColor: Color,
    val contentColor: Color
)

@Composable
fun deriveColorsFromScrollFraction(scrollFraction: Float): State<AppbarColors> {
    val containerColor by animateColorAsState(
        targetValue = if (scrollFraction > 0.5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        label = "AppBarColorAnimationLabel"
    )

    val contentColor by animateColorAsState(
        targetValue = if (scrollFraction > 0.5) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        label = "AppBarColorAnimationLabel"
    )

    return remember(scrollFraction) {
        derivedStateOf { AppbarColors(containerColor, contentColor) }
    }
}


@Composable
fun isDarkModeActivated(): Boolean {
    return isSystemInDarkTheme() || LocalAppSettings.current.useDarkMode
}