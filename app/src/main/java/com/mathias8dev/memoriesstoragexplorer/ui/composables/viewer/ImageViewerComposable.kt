package com.mathias8dev.memoriesstoragexplorer.ui.composables.viewer

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import com.mathias8dev.memoriesstoragexplorer.ui.composables.ImageLoaderComposable
import kotlinx.coroutines.launch


@Composable
fun ImageViewerComposable(
    modifier: Modifier = Modifier,
    model: Any?
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .matchParentSize()
        ) {
            var zoomed by remember { mutableStateOf(false) }
            var zoomOffset by remember { mutableStateOf(Offset.Zero) }
            val calculateOffset = { tapOffset: Offset, size: IntSize ->
                Offset(
                    x = (size.width / 2 - tapOffset.x) * 2,
                    y = (size.height / 2 - tapOffset.y) * 2
                )
            }
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset(0f, 0f)) }
            val coroutineScope = rememberCoroutineScope()

            ImageLoaderComposable(
                model = model,
                modifier = Modifier
                    .pointerInput(Unit) {
                        coroutineScope.launch {
                            launch {
                                detectTapGestures(
                                    onDoubleTap = { tapOffset ->
                                        zoomOffset = if (zoomed) Offset.Zero else
                                            calculateOffset(tapOffset, size)
                                        zoomed = !zoomed
                                    }
                                )
                            }
                            launch {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale *= zoom
                                    scale = scale.coerceAtLeast(0.5f)
                                    offset = if (scale == 1f) Offset.Zero else offset + pan
                                }
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = if (zoomed) scale * 2 else scale
                        scaleY = if (zoomed) scale * 2 else scale
                        translationX = if (zoomed) zoomOffset.x else offset.x
                        translationY = if (zoomed) zoomOffset.y else offset.y
                    }
            )

        }
    }

}