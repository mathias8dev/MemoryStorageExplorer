package com.mathias8dev.memoriesstoragexplorer.ui.composables.contextMenu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ActionMenuItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {

    Box(
        modifier = Modifier
            .clickable { onClick() }
            .then(modifier)
    ) {
        content()
    }
}

