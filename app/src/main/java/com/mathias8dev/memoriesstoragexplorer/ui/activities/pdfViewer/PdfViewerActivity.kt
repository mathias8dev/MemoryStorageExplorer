package com.mathias8dev.memoriesstoragexplorer.ui.activities.pdfViewer

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mathias8dev.memoriesstoragexplorer.ui.theme.MemoriesStorageExplorerTheme
import com.rajat.pdfviewer.PdfRendererView
import com.rajat.pdfviewer.compose.PdfRendererViewCompose
import timber.log.Timber


class PdfViewerActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val uri = intent.data
        val type = intent.type

        setContent {
            MemoriesStorageExplorerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(text = "Preview")
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            finish()
                                        },
                                        content = {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back"
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    ) { padding ->
                        if (type != null && type.startsWith("application/pdf")) {
                            MyPdfScreenFromUri(
                                uri = uri,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding)
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "An error occured, sorry")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MyPdfScreenFromUri(
    uri: Uri?,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    PdfRendererViewCompose(
        modifier = modifier,
        uri = uri,
        lifecycleOwner = lifecycleOwner,
        statusCallBack = object : PdfRendererView.StatusCallBack {
            override fun onPdfLoadStart() {
                Timber.tag("statusCallBack").i("onPdfLoadStart")
            }

            override fun onPdfLoadProgress(
                progress: Int,
                downloadedBytes: Long,
                totalBytes: Long?
            ) {
                //Download is in progress
            }

            override fun onPdfLoadSuccess(absolutePath: String) {
                Timber.tag("statusCallBack").i("onPdfLoadSuccess")
            }

            override fun onError(error: Throwable) {
                Timber.tag("statusCallBack").i("onError")
            }

            override fun onPageChanged(currentPage: Int, totalPage: Int) {
                //Page change. Not require
            }
        }
    )
}

@Composable
fun MyPdfScreenFromUrl(url: String, modifier: Modifier = Modifier) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    PdfRendererViewCompose(
        modifier = modifier,
        url = url,
        lifecycleOwner = lifecycleOwner,
        statusCallBack = object : PdfRendererView.StatusCallBack {
            override fun onPdfLoadStart() {
                Log.i("statusCallBack", "onPdfLoadStart")
            }

            override fun onPdfLoadProgress(
                progress: Int,
                downloadedBytes: Long,
                totalBytes: Long?
            ) {
                //Download is in progress
            }

            override fun onPdfLoadSuccess(absolutePath: String) {
                Log.i("statusCallBack", "onPdfLoadSuccess")
            }

            override fun onError(error: Throwable) {
                Log.i("statusCallBack", "onError")
            }

            override fun onPageChanged(currentPage: Int, totalPage: Int) {
                //Page change. Not require
            }
        }

    )
}


