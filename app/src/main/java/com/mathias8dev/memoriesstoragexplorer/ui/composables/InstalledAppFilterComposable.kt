package com.mathias8dev.memoriesstoragexplorer.ui.composables

import android.os.Parcelable
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.parcelize.Parcelize


@Parcelize
enum class InstalledAppFilter : Parcelable {
    USER,
    SYSTEM,
    ALL
}

@Composable
fun InstalledAppFilterComposable(
    modifier: Modifier = Modifier,
    filter: InstalledAppFilter,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Text(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .then(modifier),
        fontSize = 14.sp,
        text = if (filter == InstalledAppFilter.ALL) "All" else if (filter == InstalledAppFilter.SYSTEM) "System" else "User",
        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    )
}