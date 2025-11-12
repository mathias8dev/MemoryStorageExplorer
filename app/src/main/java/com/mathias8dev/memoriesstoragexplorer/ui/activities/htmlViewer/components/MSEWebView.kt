package com.mathias8dev.memoriesstoragexplorer.ui.activities.htmlViewer.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mathias8dev.memoriesstoragexplorer.R
import com.mathias8dev.memoriesstoragexplorer.domain.utils.Utils
import com.mathias8dev.memoriesstoragexplorer.domain.utils.otherwise
import timber.log.Timber
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader


@Composable
@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
fun MSEWebView(
    webUrl: String? = null,
    webData: String? = null,
    onAddJavascriptInterface: (() -> Pair<String, Any>)? = null,
    shouldOverrideUrlLoading: ((view: WebView, url: String) -> Boolean)? = null,
    onPageFinished: ((view: WebView, url: String) -> Unit)? = null,
    onGoBack: () -> Unit,
) {

    val context = LocalContext.current
    val showLoadingDialog = remember { mutableStateOf(false) }

    //.................................................
    // Remove ads
    val adServers = rememberAdServers()


    var backEnabled by rememberSaveable { mutableStateOf(false) }
    var webView: WebView? = null


    val showErrorDialog = rememberSaveable { mutableStateOf(false) }
    val showNoInternetErrorDialog = rememberSaveable { mutableStateOf(false) }


    // Adding a WebView inside AndroidView
    // with layout as full screen
    AndroidView(
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()


                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                // to verify that the client requesting your web page is actually your Android app.
                settings.userAgentString =
                    System.getProperty("http.agent") //Dalvik/2.1.0 (Linux; U; Android 11; M2012K11I Build/RKQ1.201112.002)


                // Bind JavaScript code to Android code
                onAddJavascriptInterface?.let { callback ->
                    val data = callback.invoke()
                    addJavascriptInterface(data.second, data.first)
                }




                webViewClient = object : WebViewClient() {


                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError
                    ) {
                        super.onReceivedError(view, request, error)
                        Timber.tag("WebViewPage").d("onReceivedError")
                        Timber.tag("WebViewPage").e(error.toString())
                        handleWebError(view, error.errorCode)
                    }

                    override fun onReceivedError(
                        view: WebView,
                        errorCode: Int,
                        description: String,
                        failingUrl: String
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        Timber.tag("WebViewPage").d("onReceivedError")
                        Timber.tag("WebViewPage").e(description)
                        handleWebError(view, errorCode)
                    }

                    private fun handleWebError(view: WebView, errorCode: Int) {
                        Timber.tag("WebViewPage").d("handleWebError")
                        when {
                            !Utils.hasInternetConnection(context) -> {
                                showErrorDialog.value = true
                                showNoInternetErrorDialog.value = true
                                showLoadingDialog.value = false
                            }


                            else -> {
                                showErrorDialog.value = true
                                showNoInternetErrorDialog.value = false
                                showLoadingDialog.value = false
                            }
                        }

                    }


                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        val empty = ByteArrayInputStream("".toByteArray())
                        val kk5 = adServers.toString()
                        if (kk5.contains(":::::" + request.url.host))
                            return WebResourceResponse("text/plain", "utf-8", empty)
                        return super.shouldInterceptRequest(view, request)
                    }


                    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                        showLoadingDialog.value = true
                        showNoInternetErrorDialog.value = false
                        showErrorDialog.value = false
                        backEnabled = view.canGoBack()
                    }


                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        showLoadingDialog.value = false
                        showErrorDialog.value = false
                        showNoInternetErrorDialog.value = false
                        removeElement(view!!)
                        onPageFinished?.invoke(view, url.orEmpty())
                    }


                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
                        val url = request?.url?.toString()
                        return url?.let { it1 ->
                            shouldOverrideUrlLoading?.invoke(view, it1).otherwise {
                                super.shouldOverrideUrlLoading(view, request)
                            }
                        }.otherwise {
                            super.shouldOverrideUrlLoading(view, request)
                        }
                    }
                }

                webUrl?.let { it1 -> loadUrl(it1) }.otherwise {
                    webData?.let { webData ->
                        loadData(webData, "text/html", "UTF-8")
                    }
                }
                webView = this
            }
        }, update = {
            webView = it
        }
    )




    BackHandler(enabled = backEnabled) {
        webView?.let { view ->
            removeElement(view)
            view.goBack()
        }
    }


    FullscreenDialog(
        show = showLoadingDialog.value,
        onDismiss = {
            showLoadingDialog.value = false
            if (webView?.canGoBack() == true) {
                webView?.goBack()
            } else {
                onGoBack()
            }
        }
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    animationRes = R.raw.lottie_empty
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Chargement ...",
                    textAlign = TextAlign.Center
                )

            }

        }
    }

    FullscreenDialog(
        show = showNoInternetErrorDialog.value,
        onDismiss = {
            showNoInternetErrorDialog.value = false
            if (webView?.canGoBack() == true) {
                webView?.goBack()
            } else {
                onGoBack()
            }
        }
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    animationRes = R.raw.lottie_error
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pas de connexion internet disponible.",
                    textAlign = TextAlign.Center
                )

                OutlinedButton(onClick = {
                    webView?.reload()
                }) {
                    Text(text = "Recharger")
                }

            }

        }
    }

    FullscreenDialog(
        show = showErrorDialog.value,
        onDismiss = {
            showErrorDialog.value = false
            if (webView?.canGoBack() == true) {
                webView?.goBack()
            } else {
                onGoBack()
            }
        }
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    animationRes = R.raw.lottie_error
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Une erreur est survenue.",
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        webView?.reload()
                    },
                ) {
                    Text(text = "Recharcher")
                }
            }

        }
    }


}

internal fun removeElement(webView: WebView) {
    // Safe JavaScript to hide elements by id
    val script = """
        javascript:(function() {
            const element1 = document.getElementById('blog-pager');
            if (element1) {
                element1.style.display = 'none';
            }

            const element2 = document.getElementById('cookie-law-div');
            if (element2) {
                element2.style.display = 'none';
            }
        })()
    """
    webView.loadUrl(script)
}


@Composable
internal fun rememberAdServers(): StringBuilder {
    val context = LocalContext.current
    return remember {
        val adServers = StringBuilder()

        var line: String? = ""
        val inputStream = context.resources.openRawResource(R.raw.adblockserverlist)
        val br = BufferedReader(InputStreamReader(inputStream))
        try {
            while (br.readLine().also { line = it } != null) {
                adServers.append(line)
                adServers.append("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            // RESOURCE LEAK FIX: Always close streams to prevent memory leaks
            try {
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        adServers
    }

}


