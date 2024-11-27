package com.mathias8dev.memoriesstoragexplorer.ui.activities.htmlViewer.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


@Composable
fun FullscreenDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val updatedShow by rememberUpdatedState(newValue = show)

    if (updatedShow) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
            content = content
        )
    }
}