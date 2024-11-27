package com.mathias8dev.memoriesstoragexplorer.ui.screens.home

import android.Manifest
import android.os.Build
import android.os.Environment
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.mathias8dev.memoriesstoragexplorer.ui.permissionRequestUis.StoragePermissionRequestUi
import com.mathias8dev.memoriesstoragexplorer.ui.screens.mediaList.MediaListScreen
import com.mathias8dev.permissionhelper.permission.OneShotPermissionsHelper
import com.mathias8dev.permissionhelper.permission.Permission
import com.mathias8dev.permissionhelper.permission.PermissionState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
@Destination
@RootNavGraph(start = true)
fun HomeScreen(
    navigator: DestinationsNavigator
) {

    val localContext = LocalContext.current

    var showHomeScreenContent by remember {
        mutableStateOf(false)
    }


    val permissions = remember {
        mutableSetOf<Permission>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Permission(Manifest.permission.READ_MEDIA_AUDIO))
                add(Permission(Manifest.permission.READ_MEDIA_VIDEO))
                add(Permission(Manifest.permission.READ_MEDIA_IMAGES))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(Permission(Manifest.permission.MANAGE_EXTERNAL_STORAGE))
                add(Permission(Manifest.permission.QUERY_ALL_PACKAGES))
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                add(Permission(Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }
    }




    OneShotPermissionsHelper(
        permissions = permissions.toList(),
        permissionRequestUi = StoragePermissionRequestUi
    ) {
        val permissionScope = this

        LaunchedEffect(permissionScope) {
            permissionScope.launchPermissions { result ->

                if (result.all { it.second == PermissionState.Granted }) {
                    showHomeScreenContent = true
                } else {
                    navigator.popBackStack()
                }
            }

            launch {
                while (true) {
                    delay(1000)
                    if (permissionScope.getPermissionState(permissions.toList()).all { it.second == PermissionState.Granted }) {
                        showHomeScreenContent = true
                        break
                    }
                }
            }
        }
    }

    if (showHomeScreenContent) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            MediaListScreen(
                path = Environment.getExternalStorageDirectory().absolutePath,
                navigator = navigator
            )
        }
    }


}