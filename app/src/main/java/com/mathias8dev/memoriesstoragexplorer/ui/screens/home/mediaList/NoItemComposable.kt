package com.mathias8dev.memoriesstoragexplorer.ui.screens.home.mediaList

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp


@Composable
fun NoItemComposable(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val theme = MaterialTheme.colorScheme
        Canvas(modifier = Modifier.size(72.dp)) {
            drawRoundRect(
                color = theme.surfaceVariant,
                cornerRadius = CornerRadius(8.dp.toPx()),
                style = Stroke(8.dp.toPx())
            )
        }

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "No item",
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}