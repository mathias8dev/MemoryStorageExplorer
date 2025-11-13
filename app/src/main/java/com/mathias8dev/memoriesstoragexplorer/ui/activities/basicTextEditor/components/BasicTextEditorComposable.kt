package com.mathias8dev.memoriesstoragexplorer.ui.activities.basicTextEditor.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.ui.activities.basicTextEditor.readTextFromUri

@Composable
fun BasicTextEditorComposable(
    modifier: Modifier = Modifier,
    onTextChange: ((String) -> Unit)? = null,
    model: Uri
) {

    val localContext = LocalContext.current
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(model) {
        val content = readTextFromUri(localContext, model)
        textFieldValue = TextFieldValue(content)
    }

    val lineCount by remember {
        derivedStateOf {
            val text = textFieldValue.text
            if (text.isEmpty()) 1 else text.count { it == '\n' } + 1
        }
    }

    val textStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 20.sp
    )

    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Line numbers column
        Box(
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState, enabled = false)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    text = (1..lineCount).joinToString("\n"),
                    style = textStyle.copy(
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }

        // Text editor
        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onTextChange?.invoke(it.text)
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            textStyle = textStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
        )
    }
}

