package com.mathias8dev.memoriesstoragexplorer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import androidx.navigation.plusAssign
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.decode.VideoFrameDecoder
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.mathias8dev.memoriesstoragexplorer.domain.clipboard.ClipboardHandler
import com.mathias8dev.memoriesstoragexplorer.domain.clipboard.ClipboardHandlerImpl
import com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings
import com.mathias8dev.memoriesstoragexplorer.domain.models.LocalAppSettings
import com.mathias8dev.memoriesstoragexplorer.ui.NavGraphs
import com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations.ClipboardExecutor
import com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations.FileOperationsAndroidService
import com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations.FileOperationsClipboardExecutorAndroidServiceImpl
import com.mathias8dev.memoriesstoragexplorer.ui.theme.MemoriesStorageExplorerTheme
import com.mathias8dev.memoriesstoragexplorer.ui.utils.copy
import com.mathias8dev.memoriesstoragexplorer.ui.utils.on
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

val LocalClipboardExecutor = compositionLocalOf<ClipboardExecutor?> {
    error("No ClipboardExecutorServiceProvider provided")
}

val LocalFileOperationsAndroidServiceProvider = compositionLocalOf<FileOperationsAndroidService?> {
    error("No FileOperationsServiceProvider provided")
}

val LocalClipboardHandler = compositionLocalOf<ClipboardHandler> {
    error("No ClipboardHandler provided")
}

class MainActivity : ComponentActivity() {

    private var fileOperationsAndroidServiceProvider: FileOperationsAndroidService? by mutableStateOf(null)
    private var clipboardExecutorProvider: ClipboardExecutor? by mutableStateOf(null)
    private val clipboardHandler by viewModel<ClipboardHandlerImpl>()


    private val fileOperationsServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as? FileOperationsClipboardExecutorAndroidServiceImpl.FileOperationsBinder
                if (binder == null) {
                    Timber.e("Service binder is not of expected type FileOperationsBinder")
                    return
                }
                fileOperationsAndroidServiceProvider = binder.service
                clipboardExecutorProvider = binder.service
                clipboardHandler.setFileOperationsAndroidService(binder.service)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                fileOperationsAndroidServiceProvider?.stopAll()
                clipboardHandler.setFileOperationsAndroidService(null)
                fileOperationsAndroidServiceProvider = null
                clipboardExecutorProvider = null
            }
        }
    }

    private val fileOperationsAndroidServiceIntent: Intent by lazy {
        Intent(this, FileOperationsClipboardExecutorAndroidServiceImpl::class.java)
    }

    override fun onStart() {
        super.onStart()

        if (fileOperationsAndroidServiceProvider == null) bindToFileOperationsAndroidService()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent) {
        lifecycleScope.launch {
            val action = intent.action
            val isCopyAction = intent.extras?.getBoolean("INTERNAL_COPY_ACTION") == true

            Timber.d("handleIncomingIntent: action=$action, isCopyAction=$isCopyAction")

            // Handle different intent actions
            when {
                // Handle SEND, SEND_MULTIPLE, or marked INTERNAL_COPY_ACTION
                isCopyAction || action == Intent.ACTION_SEND || action == Intent.ACTION_SEND_MULTIPLE -> {
                    Timber.d("Processing intent for copy action")
                    val uris = collectUrisFromIntent(intent)

                    if (uris.isNotEmpty()) {
                        Timber.d("Copying ${uris.size} URI(s) to clipboard: $uris")
                        clipboardHandler.copyToClipboard(uris)

                        // Show toast notification to user
                        val message = when {
                            uris.size == 1 -> "1 file ready to paste"
                            else -> "${uris.size} files ready to paste"
                        }
                        android.widget.Toast.makeText(
                            this@MainActivity,
                            message,
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        Timber.i("Successfully copied ${uris.size} file(s) to clipboard")
                    } else {
                        Timber.w("No URIs found in intent to copy")
                        android.widget.Toast.makeText(
                            this@MainActivity,
                            "No files found to copy",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                // Handle VIEW or EDIT actions - these might be handled by specific viewers
                action == Intent.ACTION_VIEW || action == Intent.ACTION_EDIT -> {
                    Timber.d("View/Edit action received, data=${intent.data}")
                    // These are typically handled by the specific activity (ImageViewerActivity, etc.)
                    // No need to process here unless we want to show them in file manager
                }

                else -> {
                    Timber.d("Unhandled intent action: $action")
                }
            }
        }
    }

    /**
     * Collect all URIs from an intent, handling various sources
     */
    private fun collectUrisFromIntent(intent: Intent): List<Uri> {
        val uris = mutableListOf<Uri>()

        // 1. Check for single URI in data field (used by SEND with single item)
        intent.data?.let { uri ->
            Timber.d("Found URI in intent.data: $uri")
            uris.add(uri)
        }

        // 2. Check for single URI in EXTRA_STREAM (alternative for SEND)
        @Suppress("DEPRECATION")
        val extraStreamUri = if (SDK_INT >= 33) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        }
        extraStreamUri?.let { uri ->
            Timber.d("Found URI in EXTRA_STREAM: $uri")
            if (!uris.contains(uri)) {
                uris.add(uri)
            }
        }

        // 3. Check for multiple URIs in EXTRA_STREAM (used by SEND_MULTIPLE)
        @Suppress("DEPRECATION")
        val extraStreamUris = if (SDK_INT >= 33) {
            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
        }
        extraStreamUris?.let { streamUris ->
            Timber.d("Found ${streamUris.size} URI(s) in EXTRA_STREAM array list")
            streamUris.forEach { uri ->
                if (!uris.contains(uri)) {
                    uris.add(uri)
                }
            }
        }

        // 4. Check clipData (used for multiple items, even in some SEND cases)
        intent.clipData?.let { clipData ->
            Timber.d("Found clipData with ${clipData.itemCount} item(s)")
            for (i in 0 until clipData.itemCount) {
                val uri = clipData.getItemAt(i).uri
                if (uri != null && !uris.contains(uri)) {
                    Timber.d("Adding URI from clipData[$i]: $uri")
                    uris.add(uri)
                }
            }
        }

        Timber.d("Total URIs collected: ${uris.size}")
        return uris
    }

    @OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEdgeToEdge()
        setupCoilImageLoader()
        handleIncomingIntent(intent)

        /*ViewCompat.setOnApplyWindowInsetsListener(
            this.window.decorView
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars: androidx.core.graphics.Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }*/


        setContent {
            val viewModel: SettingsProviderViewModel = koinViewModel()

            val appSettings by viewModel.appSettings.collectAsStateWithLifecycle(AppSettings())
            val navController = rememberNavController()
            val bottomSheetNavigator = rememberBottomSheetNavigator()


            navController.navigatorProvider += bottomSheetNavigator
            val navHostEngine = rememberAnimatedNavHostEngine(
                rootDefaultAnimations = RootNavGraphDefaultAnimations.ACCOMPANIST_FADING,
            )

            val snackbarHostState = remember { SnackbarHostState() }


            MemoriesStorageExplorerTheme(
                appSettings = appSettings
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                ) { innerPadding ->

                    val layoutDirection = LocalLayoutDirection.current
                    val orientationPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT


                    CompositionLocalProvider(
                        LocalSnackbarHostState provides snackbarHostState,
                        LocalClipboardExecutor provides clipboardExecutorProvider,
                        LocalFileOperationsAndroidServiceProvider provides fileOperationsAndroidServiceProvider,
                        LocalClipboardHandler provides clipboardHandler,
                        LocalAppSettings provides appSettings,
                    ) {

                        ModalBottomSheetLayout(
                            modifier = Modifier
                                .padding(innerPadding.copy(layoutDirection, top = 0.dp))
                                .imePadding()
                                .on(orientationPortrait) {
                                    displayCutoutPadding()
                                },

                            bottomSheetNavigator = bottomSheetNavigator,
                            sheetShape = RoundedCornerShape(16.dp),
                        ) {
                            DestinationsNavHost(
                                navController = navController,
                                navGraph = NavGraphs.root,
                                engine = navHostEngine
                            )
                        }
                    }

                }
            }
        }
    }


    private fun setupEdgeToEdge() {
        enableEdgeToEdge()
    }

    private fun setupCoilImageLoader() {
        fun getUnsafeClient(): OkHttpClient {
            val trustAllCerts: Array<TrustManager> = arrayOf(
                @SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
            val okHttpClientBuilder = OkHttpClient.Builder()
            okHttpClientBuilder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            okHttpClientBuilder.hostnameVerifier { _, _ -> true }
            return okHttpClientBuilder.build()
        }

        val imageLoader = ImageLoader
            .Builder(this)
            .okHttpClient { getUnsafeClient() }
            .components {
                add(SvgDecoder.Factory())
                add(VideoFrameDecoder.Factory())
                if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                else add(GifDecoder.Factory())
            }
            .build()
        Coil.setImageLoader(imageLoader)

    }

    private fun bindToFileOperationsAndroidService() {
        bindService(
            fileOperationsAndroidServiceIntent,
            fileOperationsServiceConnection,
            BIND_AUTO_CREATE
        )
    }

    private fun unbindToFileOperationsAndroidService() {
        if (fileOperationsAndroidServiceProvider != null)
            unbindService(fileOperationsServiceConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindToFileOperationsAndroidService()
    }
}
