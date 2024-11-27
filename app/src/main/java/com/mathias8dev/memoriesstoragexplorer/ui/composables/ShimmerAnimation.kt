package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


internal val ShimmerColorShades = listOf(
    Color.LightGray.copy(0.7f),
    Color.White.copy(0.5f),
    Color.LightGray.copy(0.7f)
)


@Composable
fun ShimmerAnimation(modifier: Modifier) {
    Row(
        modifier = Modifier
            .then(modifier)
            .background(brush = shimmerAnimation())
    ) {}
}


@Composable
fun shimmerAnimation(): Brush {
    val transition = rememberInfiniteTransition(label = "ShimmerTransitionLabel")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), label = "ShimmerAnimationLabel"
    )

    return remember(translateAnim) {
        Brush.linearGradient(
            colors = ShimmerColorShades,
            start = Offset(10f, 10f),
            end = Offset(translateAnim, translateAnim)
        )
    }
}