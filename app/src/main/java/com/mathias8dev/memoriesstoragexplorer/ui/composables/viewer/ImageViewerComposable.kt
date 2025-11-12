package com.mathias8dev.memoriesstoragexplorer.ui.composables.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.ui.composables.ImageLoaderComposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds


@Composable
fun ImageViewerComposable(
    modifier: Modifier = Modifier,
    model: Any?,
    onZoomChanged: ((Boolean) -> Unit)? = null
) {
    Box(modifier = modifier) {
        var zoomed by remember { mutableStateOf(false) }
        var zoomOffset by remember { mutableStateOf(Offset.Zero) }
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset(0f, 0f)) }
        var showZoomIndicator by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        val calculateOffset = { tapOffset: Offset, size: IntSize ->
            Offset(
                x = (size.width / 2 - tapOffset.x) * 2,
                y = (size.height / 2 - tapOffset.y) * 2
            )
        }

        // Notify parent about zoom state changes
        LaunchedEffect(scale, zoomed) {
            val isZoomed = scale > 1f || zoomed
            onZoomChanged?.invoke(isZoomed)
        }

        // Auto-hide zoom indicator after 2 seconds
        LaunchedEffect(key1 = showZoomIndicator) {
            if (showZoomIndicator) {
                delay(2.seconds)
                showZoomIndicator = false
            }
        }

        Column(
            modifier = Modifier
                .matchParentSize()
        ) {
            ImageLoaderComposable(
                model = model,
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { tapOffset ->
                                // Reset zoom first
                                scale = 1f
                                offset = Offset.Zero

                                // Then apply new zoom if needed
                                zoomOffset = if (zoomed) Offset.Zero else
                                    calculateOffset(tapOffset, size)
                                zoomed = !zoomed
                                showZoomIndicator = true
                            }
                        )
                    }
                    .pointerInput(scale, zoomed) {
                        // Custom gesture detection that doesn't consume horizontal swipes when not zoomed
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)

                            do {
                                val event = awaitPointerEvent()
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()

                                // Only consume gestures if zoomed or zooming
                                if (zoom != 1f || scale > 1f || zoomed) {
                                    val newScale = (scale * zoom).coerceIn(0.5f, 5f)

                                    // Only allow panning when zoomed in
                                    if (newScale > 1f) {
                                        scale = newScale
                                        offset = offset + pan
                                        zoomed = false // Disable double-tap zoom when using pinch
                                        showZoomIndicator = true

                                        // Consume the event only when zoomed
                                        event.changes.forEach { it.consume() }
                                    } else if (newScale <= 1f && scale > 1f) {
                                        // Reset to normal when zooming out completely
                                        scale = 1f
                                        offset = Offset.Zero
                                        zoomed = false

                                        event.changes.forEach { it.consume() }
                                    }
                                }
                                // If not zoomed and no zoom detected, don't consume - let pager handle swipes

                            } while (event.changes.any { it.pressed })
                        }
                    }
                    .graphicsLayer {
                        val finalScale = if (zoomed) scale * 2 else scale
                        scaleX = finalScale
                        scaleY = finalScale
                        translationX = if (zoomed) zoomOffset.x else offset.x
                        translationY = if (zoomed) zoomOffset.y else offset.y
                    }
            )
        }

        // Zoom level indicator
        AnimatedVisibility(
            visible = showZoomIndicator && (scale > 1f || zoomed),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            val zoomLevel = if (zoomed) scale * 2 else scale
            Text(
                text = "${String.format("%.1f", zoomLevel)}x",
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

/**
 * Alias for ImageViewerComposable that exposes zoom state changes
 * Used in pager to control swipe navigation
 */
@Composable
fun ZoomableImageViewerComposable(
    modifier: Modifier = Modifier,
    model: Any?,
    onZoomChanged: (Boolean) -> Unit
) {
    ImageViewerComposable(
        modifier = modifier,
        model = model,
        onZoomChanged = onZoomChanged
    )
}