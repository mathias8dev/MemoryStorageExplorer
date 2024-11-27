package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mathias8dev.memoriesstoragexplorer.data.event.BroadcastEvent
import com.mathias8dev.memoriesstoragexplorer.data.event.EventBus
import com.mathias8dev.memoriesstoragexplorer.domain.FilterQuery
import com.mathias8dev.memoriesstoragexplorer.domain.enums.AddMode
import com.mathias8dev.memoriesstoragexplorer.domain.enums.LayoutMode
import com.mathias8dev.memoriesstoragexplorer.domain.enums.SortMode
import kotlinx.coroutines.launch


@Composable
fun MediaListScreenLayout(
    currentQuery: String? = null,
    onNavigateToMediaGroup: (MediaGroup) -> Unit,
    onFilter: (queries: List<FilterQuery>) -> Unit,
    onSort: (SortMode) -> Unit,
    onReload: () -> Unit,
    onUpdateLayout: (LayoutMode) -> Unit,
    onAdd: (AddMode) -> Unit,
    onSelectAll: () -> Unit,
    title: @Composable () -> Unit = {
        Text("Accueil")
    },
    content: @Composable (PaddingValues, DrawerState) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    var showBottomActions by rememberSaveable {
        mutableStateOf(true)
    }


    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                showBottomActions = consumed.y > 0 || (showBottomActions && consumed.y == 0.0F)
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(Unit) {
        EventBus.subscribe<BroadcastEvent.HideShowBottomActionsEvent> {
            showBottomActions = it.action
        }
    }


    NavigationDrawerLayout(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        title = title,
        drawerContent = { drawerState ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                MediaGroup.entries.forEach {
                    MediaGroupComposable(
                        iconRes = it.iconRes,
                        colorHex = it.colorHex,
                        title = it.title,
                        subTitle = it.path,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                onNavigateToMediaGroup(it)
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(visible = showBottomActions) {
                BottomActionsComposable(
                    searchTerm = currentQuery,
                    onFilter = onFilter,
                    onUpdateLayout = onUpdateLayout,
                    onReload = onReload,
                    onSort = onSort,
                    onSelectAll = onSelectAll,
                    onAdd = onAdd
                )
            }
        },
        content = content
    )
}