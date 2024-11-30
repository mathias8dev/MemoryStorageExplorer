package com.mathias8dev.memoriesstoragexplorer.ui.activities.basicTextEditor.components

import android.net.Uri
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.mathias8dev.memoriesstoragexplorer.ui.activities.basicTextEditor.readTextFromUri

@Composable
fun BasicTextEditorComposable(
    modifier: Modifier = Modifier,
    onTextChange: ((String) -> Unit)? = null,
    model: Uri
) {

    val localContext = LocalContext.current
    var content by rememberSaveable { mutableStateOf("") }


    LaunchedEffect(model) {
        content = readTextFromUri(localContext, model)
    }

    BasicTextField(
        value = content,
        onValueChange = {
            content = it
            onTextChange?.invoke(it)
        },
        modifier = modifier,
        textStyle = androidx.compose.ui.text.TextStyle.Default.copy(textAlign = TextAlign.Start)
    )
}

