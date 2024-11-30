package com.mathias8dev.memoriesstoragexplorer.ui.screens.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.BuildConfig
import com.mathias8dev.memoriesstoragexplorer.R
import com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings
import com.mathias8dev.memoriesstoragexplorer.domain.models.LocalAppSettings
import com.mathias8dev.memoriesstoragexplorer.ui.composables.CopiableText
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination
@RootNavGraph
fun SettingsScreen(
    navigator: DestinationsNavigator
) {

    val viewModel: SettingsScreenViewModel = koinViewModel()

    val appSettings = LocalAppSettings.current
    val showConfirmDeleteAccountDialog = rememberSaveable {
        mutableStateOf(false)
    }

    var showContactDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val showConfirmDisconnectDialog = rememberSaveable {
        mutableStateOf(false)
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(Unit) {

    }


    val appbarColors by deriveColorsFromScrollFraction(scrollBehavior.state.overlappedFraction)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = if (isDarkModeActivated()) Color.Unspecified else appbarColors.containerColor,
                    titleContentColor = appbarColors.contentColor,
                    navigationIconContentColor = appbarColors.contentColor
                ),
                title = {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(R.string.settings)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigator.navigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                SettingsItemSection(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateContentSize(),
                    sectionTitle = "Look and feel"
                ) {
                    SwitchSettingItem(
                        actionTitle = "Theme",
                        actionContent = stringResource(R.string.use_dark_theme),
                        value = appSettings.useDarkMode,
                        onValueChanged = {
                            viewModel.onAppSettingsChanged(AppSettings::useDarkMode, it)
                        }
                    )


                }

                /*SettingsItemSection(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateContentSize(),
                    sectionTitle = "Autres"
                ) {
                    SwitchSettingItem(
                        actionTitle = "Fichers",
                        actionContent = "Afficher les fichiers cachés",
                        value = appSettings.showHiddenFiles,
                        onValueChanged = {
                            viewModel.onAppSettingsChanged(AppSettings::showHiddenFiles, it)
                        }
                    )

                }*/



                Spacer(
                    modifier = Modifier
                        .height(30.dp)
                        .weight(1F)
                )

                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable {
                            viewModel.onReinitializeSettings()
                        }
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    text = stringResource(R.string.reset_settings),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )


                DevelopedByMe(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 32.dp)
                )
            }

        }
    )
}


@Composable
fun CreatedByMe(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Version ${BuildConfig.VERSION_NAME}",
            color = Color(0xFF555454),
            fontSize = 12.sp
        )
        Text(
            text = "Powored by mathias8dev",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight(600),
            lineHeight = 14.sp
        )
        CopiableText(
            text = "kalipemathias.pro@gmail.com",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight(600),
            lineHeight = 12.sp
        )
    }
}

@Composable
fun DevelopedByMe(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Version ${BuildConfig.VERSION_NAME}",
            color = Color(0xFF555454),
            fontSize = 12.sp
        )
        Text(
            text = "Developed by mathias8dev",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight(600),
            lineHeight = 14.sp
        )
        CopiableText(
            text = "kalipemathias.pro@gmail.com",
            color = Color(0xFF555454),
            fontSize = 12.sp,
            fontWeight = FontWeight(600),
        )
    }
}