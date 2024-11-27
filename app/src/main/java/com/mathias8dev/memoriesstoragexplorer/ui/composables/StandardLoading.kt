package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mathias8dev.memoriesstoragexplorer.ui.utils.toPx


@Composable
fun StandardLoading(modifier: Modifier = Modifier) {

    val primaryColor = MaterialTheme.colorScheme.primary
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        CircularProgressIndicatorRoundedRotating(
            modifier = Modifier.size(size = 42.dp),
            color = primaryColor,
            strokeWidth = 8.dp
        )
        Canvas(
            modifier = Modifier.size(50.dp)
        ) {
            val strokeWidth = 1.dp.toPx()
            val circleRadius = (size.width - strokeWidth) / 2
            drawCircle(
                color = primaryColor,
                radius = circleRadius,
                style = Stroke(strokeWidth)
            )
        }
    }
}

@Composable
internal fun CircularProgressIndicatorRoundedRotating(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 8.dp,
    progress: Float = 0.5f
) {
    val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

    val infiniteTransition = rememberInfiniteTransition(
        label = "CircularProgressIndicatorRoundedRotatingTransitionLabel"
    )
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CircularProgressIndicatorRoundedRotatingAnimationLabel"
    )

    Canvas(modifier = modifier) {
        rotate(rotation.value) {
            val startAngle = -90f
            val sweepAngle = 360 * progress

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = stroke
            )
        }
    }
}

