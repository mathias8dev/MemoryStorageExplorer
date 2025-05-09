package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mathias8dev.memoriesstoragexplorer.R
import com.mathias8dev.memoriesstoragexplorer.domain.FilterQuery
import com.mathias8dev.memoriesstoragexplorer.domain.enums.AddMode
import com.mathias8dev.memoriesstoragexplorer.domain.enums.LayoutMode
import com.mathias8dev.memoriesstoragexplorer.domain.enums.SortMode


enum class BottomAction(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int
) {
    Search(
        iconRes = R.drawable.ic_search,
        titleRes = R.string.search
    ),
    ViewMode(
        iconRes = R.drawable.ic_view_mode,
        titleRes = R.string.view_mode
    ),
    Add(
        iconRes = R.drawable.ic_add,
        titleRes = R.string.add
    ),
    Reload(
        iconRes = R.drawable.ic_reload,
        titleRes = R.string.reload
    ),
    Sort(
        iconRes = R.drawable.ic_sort,
        titleRes = R.string.sort
    ),
    Select(
        iconRes = R.drawable.ic_select_all,
        titleRes = R.string.select_all
    ),

}


@Composable
fun BottomActionsComposable(
    searchTerm: String? = null,
    selectedSortMode: SortMode? = null,
    selectedLayoutMode: LayoutMode? = null,
    onFilter: (queries: List<FilterQuery>) -> Unit,
    onSort: (SortMode) -> Unit,
    onReload: () -> Unit,
    onUpdateLayout: (LayoutMode) -> Unit,
    onAdd: (AddMode) -> Unit,
    onSelectAll: () -> Unit,
) {
    val localContext = LocalContext.current

    var selectedAction: BottomAction? by rememberSaveable {
        mutableStateOf(null)
    }


    var termToSearch by rememberSaveable(searchTerm) {
        mutableStateOf(searchTerm.orEmpty())
    }


    val onFilterCallback = {
        onFilter(
            listOf(
                FilterQuery.SearchTerm(
                    term = termToSearch
                )
            )
        )
    }

    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        if (selectedAction == BottomAction.Search) {

            Row {
                IconButton(
                    onClick = { selectedAction = null }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = termToSearch,
                    onValueChange = {
                        termToSearch = it
                        onFilterCallback()
                    },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            selectedAction = null
                        }
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    placeholder = {
                        Text(
                            stringResource(R.string.type_to_filter)
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                selectedAction = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        } else {
            BottomActionComposable(
                action = BottomAction.Search,
                onClick = {
                    selectedAction = BottomAction.Search
                }
            )
            ContextMenuComposable(
                expanded = selectedAction == BottomAction.ViewMode,
                colors = CardDefaults.cardColors(containerColor = dialogBackgroundColor()),
                elevation = CardDefaults.elevatedCardElevation(2.dp),
                onDismissRequest = {
                    selectedAction = null
                },
                onExpandedChange = {
                    selectedAction = if (it) BottomAction.ViewMode else null
                },
                onActionClicked = { _, action ->
                    selectedAction = null
                    onUpdateLayout(action)
                },
                actions = LayoutMode.withoutGallery(),
                actionToString = { localContext.getString(it.nameRes) },
                actionHolder = { onHolderClicked ->
                    BottomActionComposable(
                        iconRes = selectedLayoutMode?.iconRes ?: BottomAction.ViewMode.iconRes,
                        titleRes = selectedLayoutMode?.nameRes ?: BottomAction.ViewMode.titleRes,
                        onClick = {
                            onHolderClicked()
                        }
                    )
                },
                onDrawActionMenuItem = {
                    Row(
                        modifier = Modifier
                            .width(200.dp)
                            .background(
                                color = if (it == selectedLayoutMode) MaterialTheme.colorScheme.primary.copy(0.4F)
                                else Color.Unspecified
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        it.iconRes?.let { iconRes ->
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(iconRes),
                                tint = MaterialTheme.colorScheme.onBackground,
                                contentDescription = stringResource(it.nameRes)
                            )
                        }
                        Text(
                            modifier = Modifier,
                            text = stringResource(it.nameRes),
                        )
                    }
                }
            )
            ContextMenuComposable(
                expanded = selectedAction == BottomAction.Add,
                colors = CardDefaults.cardColors(containerColor = dialogBackgroundColor()),
                elevation = CardDefaults.elevatedCardElevation(2.dp),
                onDismissRequest = {
                    selectedAction = null
                },
                onExpandedChange = {
                    selectedAction = if (it) BottomAction.Add else null
                },
                onActionClicked = { _, action ->
                    selectedAction = null
                    onAdd(action)
                },
                actions = AddMode.entries,
                actionToString = { localContext.getString(it.nameRes) },
                actionHolder = { onHolderClicked ->
                    BottomActionComposable(
                        action = BottomAction.Add,
                        onClick = {
                            onHolderClicked()
                        }
                    )
                },
                onDrawActionMenuItem = {
                    Text(
                        modifier = Modifier
                            .width(100.dp)
                            .padding(16.dp),
                        text = stringResource(it.nameRes),
                    )
                }
            )


            ContextMenuComposable(
                expanded = selectedAction == BottomAction.Sort,
                onDismissRequest = {
                    selectedAction = null
                },
                onExpandedChange = {
                    selectedAction = if (it) BottomAction.Sort else null
                },
                onActionClicked = { _, action ->
                    selectedAction = null
                    onSort(action)
                },
                colors = CardDefaults.cardColors(containerColor = dialogBackgroundColor()),
                elevation = CardDefaults.elevatedCardElevation(2.dp),
                actions = SortMode.entries,
                actionToString = { localContext.getString(it.nameRes) },
                actionHolder = { onHolderClicked ->
                    BottomActionComposable(
                        iconRes = selectedSortMode?.iconRes ?: BottomAction.Sort.iconRes,
                        titleRes = selectedSortMode?.nameRes ?: BottomAction.Sort.titleRes,
                        onClick = {
                            onHolderClicked()
                        }
                    )
                },
                onDrawActionMenuItem = {
                    Row(
                        modifier = Modifier
                            .width(216.dp)
                            .background(
                                color = if (it == selectedSortMode) MaterialTheme.colorScheme.primary.copy(0.4F)
                                else Color.Unspecified
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        it.iconRes?.let { iconRes ->
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(iconRes),
                                contentDescription = stringResource(it.nameRes)
                            )
                        }
                        Text(
                            modifier = Modifier,
                            text = stringResource(it.nameRes),
                        )
                    }
                }
            )
            BottomActionComposable(
                action = BottomAction.Reload,
                onClick = {
                    selectedAction = BottomAction.Reload
                    onReload()
                }
            )
            BottomActionComposable(
                action = BottomAction.Select,
                onClick = {
                    selectedAction = BottomAction.Select
                    onSelectAll()
                }
            )
        }

    }
}

@Composable
fun BottomActionComposable(
    modifier: Modifier = Modifier,
    action: BottomAction,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(action.iconRes),
            contentDescription = stringResource(action.titleRes)
        )
    }
}

@Composable
fun BottomActionComposable(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    @StringRes titleRes: Int? = null,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(iconRes),
            tint = MaterialTheme.colorScheme.onBackground,
            contentDescription = titleRes?.let { stringResource(titleRes) }
        )
    }
}