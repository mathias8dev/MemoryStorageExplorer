package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mathias8dev.memoriesstoragexplorer.domain.utils.otherwise
import kotlinx.coroutines.launch


data class NavigationItem(
    val title: String,
    @DrawableRes val selectedIcon: Int?,
    @DrawableRes val unselectedIcon: Int?,
    val badgeCount: Int? = null
)


@Composable
internal fun DefaultNavigationItemRenderer(
    selected: Boolean,
    item: NavigationItem,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(text = item.title) },
        selected = selected,
        onClick = onClick,
        icon = {
            if (item.selectedIcon != null || item.unselectedIcon != null) {
                Icon(
                    painter = if (selected) painterResource(id = item.selectedIcon.otherwise(item.unselectedIcon)!!)
                    else painterResource(id = item.unselectedIcon.otherwise(item.selectedIcon)!!),
                    contentDescription = item.title
                )
            }
        },
        badge = {  // Show Badge
            item.badgeCount?.let {
                Text(text = item.badgeCount.toString())
            }
        },
        modifier = Modifier
            .padding(NavigationDrawerItemDefaults.ItemPadding) //padding between items
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerLayout(
    items: List<NavigationItem>,
    onRenderItem: @Composable (
        selected: Boolean,
        item: NavigationItem,
        onClick: () -> Unit,
    ) -> Unit = { selected, item, onClick ->
        DefaultNavigationItemRenderer(
            selected = selected,
            item = item,
            onClick = onClick
        )
    },
    title: @Composable () -> Unit,
    bottomBar: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {


    var selectedItemIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.8F),
            ) {
                Spacer(modifier = Modifier.height(16.dp)) //space (margin) from top
                items.forEachIndexed { index, item ->
                    onRenderItem(
                        index == selectedItemIndex,
                        item
                    ) {
                        selectedItemIndex = index
                        scope.launch {
                            drawerState.close()
                        }

                    }
                }
            }
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = title,
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Column {
                    bottomBar?.invoke(this)
                }
            },
            content = content
        )
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerLayout(
    modifier: Modifier = Modifier,
    drawerContent: @Composable ColumnScope.(DrawerState) -> Unit,
    bottomBar: @Composable (ColumnScope.() -> Unit)? = null,
    title: @Composable () -> Unit,
    content: @Composable (PaddingValues, DrawerState) -> Unit
) {


    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()



    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.8F),
                content = {
                    drawerContent(drawerState)
                }
            )
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = title,
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Column {
                    bottomBar?.invoke(this)
                }
            },
            content = {
                content(it, drawerState)
            }
        )
    }

}