package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp


@Composable
fun StandardButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(8.dp),
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = shape,
        enabled = enabled,
        onClick = onClick,
        colors = colors
    ) {
        Text(text = text)
    }
}