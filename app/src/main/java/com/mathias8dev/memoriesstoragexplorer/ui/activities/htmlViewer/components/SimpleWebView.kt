package com.mathias8dev.memoriesstoragexplorer.ui.activities.htmlViewer.components

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView


@Composable
fun SimpleWebView(
    modifier: Modifier = Modifier,
    stringContent: String
) {
    val localContext = LocalContext.current
    val webView = remember { WebView(localContext) }

    AndroidView(
        modifier = modifier,
        factory = { webView }
    ) { view ->
        view.loadData(stringContent, "text/html", "UTF-8")
    }
}