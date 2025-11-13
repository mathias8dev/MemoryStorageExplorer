package com.mathias8dev.memoriesstoragexplorer.ui.composables.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.ui.composables.ImageLoaderComposable
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun ImageViewerComposable(
    modifier: Modifier = Modifier,
    model: Any?,
    onZoomChanged: ((Boolean) -> Unit)? = null,
    onSingleTap: (() -> Unit)? = null
) {
    Box(modifier = modifier) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var showZoomIndicator by remember { mutableStateOf(false) }
        var imageSize by remember { mutableStateOf(IntSize.Zero) }

        // Notify parent about zoom state changes
        LaunchedEffect(scale) {
            onZoomChanged?.invoke(scale > 1f)
        }

        // Auto-hide zoom indicator after 2 seconds
        LaunchedEffect(showZoomIndicator) {
            if (showZoomIndicator) {
                delay(2.seconds)
                showZoomIndicator = false
            }
        }

        ImageLoaderComposable(
            model = model,
            modifier = Modifier
                .matchParentSize()
                .onSizeChanged { size ->
                    imageSize = size
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            // Single tap - always invoke to allow topbar toggle
                            onSingleTap?.invoke()
                        },
                        onDoubleTap = { tapOffset ->
                            if (scale > 1f) {
                                // Reset zoom
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                // Zoom in to 2.5x at tap location
                                val newScale = 2.5f

                                // Calculate the offset to center the tap point
                                val offsetX = (imageSize.width / 2f - tapOffset.x) * (newScale - 1f)
                                val offsetY = (imageSize.height / 2f - tapOffset.y) * (newScale - 1f)

                                scale = newScale
                                offset = Offset(offsetX, offsetY)
                            }
                            showZoomIndicator = true
                        }
                    )
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)

                        do {
                            val event = awaitPointerEvent()
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            // Detect if this is a zoom gesture (multi-touch)
                            val isZoomGesture = zoomChange != 1f

                            if (isZoomGesture || scale > 1f) {
                                // Calculate new scale
                                val newScale = (scale * zoomChange).coerceIn(1f, 5f)
                                scale = newScale

                                if (scale > 1f) {
                                    // Apply pan with boundaries
                                    val newOffset = offset + panChange

                                    // Calculate boundaries
                                    val maxX = (imageSize.width * (scale - 1f)) / 2f
                                    val maxY = (imageSize.height * (scale - 1f)) / 2f

                                    offset = Offset(
                                        x = newOffset.x.coerceIn(-maxX, maxX),
                                        y = newOffset.y.coerceIn(-maxY, maxY)
                                    )

                                    showZoomIndicator = true

                                    // Consume the event when zoomed or zooming
                                    event.changes.forEach { it.consume() }
                                } else {
                                    // Reset offset when back to normal scale
                                    offset = Offset.Zero

                                    // Consume the event to complete the zoom out
                                    if (isZoomGesture) {
                                        event.changes.forEach { it.consume() }
                                    }
                                }
                            }
                            // If not zoomed and not a zoom gesture, don't consume - let pager handle swipes

                        } while (event.changes.any { it.pressed })
                    }
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )

        // Zoom level indicator
        AnimatedVisibility(
            visible = showZoomIndicator && scale > 1f,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Text(
                text = "${String.format("%.1f", scale)}x",
                modifier = Modifier
                    .padding(16.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}