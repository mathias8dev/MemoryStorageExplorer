package com.mathias8dev.composesimplestorageexplorer.ui.mediaPlayer.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.mathias8dev.memoriesstoragexplorer.R


val roundedShape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val p1 = Path().apply {
            addOval(Rect(4f, 3f, size.width - 1, size.height - 1))
        }
        val thickness = size.height / 2.10f
        val p2 = Path().apply {
            addOval(
                Rect(
                    thickness,
                    thickness,
                    size.width - thickness,
                    size.height - thickness
                )
            )
        }
        val p3 = Path()
        p3.op(p1, p2, PathOperation.Difference)

        return Outline.Generic(p3)
    }
}

@Composable
fun AnimatedVinyl(
    modifier: Modifier = Modifier,
    isSongPlaying: Boolean = true,
    painter: Painter
) {
    var currentRotation by remember {
        mutableFloatStateOf(0f)
    }

    val rotation = remember {
        Animatable(currentRotation)
    }

    LaunchedEffect(isSongPlaying) {
        if (isSongPlaying) {
            rotation.animateTo(
                targetValue = currentRotation + 360f, animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing), repeatMode = RepeatMode.Restart
                )
            ) {
                currentRotation = value
            }
        } else {
            if (currentRotation > 0f) {
                rotation.animateTo(
                    targetValue = currentRotation + 50, animationSpec = tween(
                        1250, easing = LinearOutSlowInEasing
                    )
                ) {
                    currentRotation = value
                }
            }
        }
    }

    Vinyl(modifier = modifier, painter = painter, rotationDegrees = rotation.value)
}

@Composable
fun Vinyl(
    modifier: Modifier = Modifier,
    rotationDegrees: Float = 0f,
    painter: Painter
) {
    Box(
        modifier = modifier
            .aspectRatio(1.0f)
            .clip(roundedShape)
    ) {
        // Vinyl background
        Image(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotationDegrees),
            painter = painterResource(id = R.drawable.vinyl_background),
            contentDescription = "Vinyl Background"
        )

        // Vinyl song cover
        Image(
            modifier = Modifier
                .fillMaxSize(0.5f)
                .rotate(rotationDegrees)
                .aspectRatio(1.0f)
                .align(Alignment.Center)
                .clip(roundedShape),
            painter = painter,
            contentDescription = "Song cover"
        )
    }
}

