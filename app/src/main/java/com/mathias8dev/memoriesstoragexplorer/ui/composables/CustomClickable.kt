package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier


@Composable
fun CustomClickable(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    indication: Indication? = null,
    interactionSource: MutableInteractionSource = rememberInteractionSource(),
    content: @Composable () -> Unit
) {
    Box(modifier) {
        content()
        onClick?.let {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        onClick = it,
                        interactionSource = interactionSource,
                        indication = indication
                    )
            ) {}
        }
    }
}

@Composable
fun rememberInteractionSource(): MutableInteractionSource {
    return remember { MutableInteractionSource() }
}