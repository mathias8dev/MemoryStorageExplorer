package com.mathias8dev.memoriesstoragexplorer.ui.activities.htmlViewer.components


import androidx.annotation.RawRes
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieDynamicProperties
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition


@Composable
fun LottieAnimation(
    modifier: Modifier = Modifier.size(72.dp),
    @RawRes animationRes: Int,
    repeatForever: Boolean = false,
    clipSpec: LottieClipSpec? = null,
    dynamicProperties: LottieDynamicProperties? = null,
) {


    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = if (repeatForever) Int.MAX_VALUE else 1,
        clipSpec = clipSpec
    )

    LottieAnimation(
        modifier = modifier,
        composition = composition,
        dynamicProperties = dynamicProperties,
        progress = { progress },
    )
}