package com.mathias8dev.memoriesstoragexplorer.ui.activities.basicTextEditor

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mathias8dev.memoriesstoragexplorer.SettingsProviderViewModel
import com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.SaveTextToUriUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.utils.koinInject
import com.mathias8dev.memoriesstoragexplorer.ui.activities.basicTextEditor.components.BasicTextEditorComposable
import com.mathias8dev.memoriesstoragexplorer.ui.theme.MemoriesStorageExplorerTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

class BasicTextEditorActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val uri = intent.data
        val type = intent.type

        setContent {
            val viewModel: SettingsProviderViewModel = koinViewModel()
            val appSettings by viewModel.appSettings.collectAsStateWithLifecycle(AppSettings())
            val saveTextToUriUseCase by koinInject<SaveTextToUriUseCase>()

            var updatedTextContent by remember { mutableStateOf("") }
            val coroutineScope = rememberCoroutineScope()

            MemoriesStorageExplorerTheme(
                darkTheme = appSettings.useDarkMode || isSystemInDarkTheme()
            ) {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Editor")

                                        uri?.let {
                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        Timber.d("The updated text content is $updatedTextContent")
                                                        val saved = saveTextToUriUseCase.invoke(it, updatedTextContent)
                                                        val message = if (saved) "Saved" else "Error saving"
                                                        Toast.makeText(this@BasicTextEditorActivity, message, Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Save,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    }
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

                        Box(modifier = Modifier.padding(padding)) {
                            if (type != null && type.startsWith("text/") && uri != null) {
                                BasicTextEditorComposable(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    model = uri,
                                    onTextChange = {
                                        updatedTextContent = it
                                    }
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
}