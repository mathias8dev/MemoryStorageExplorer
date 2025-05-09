package com.mathias8dev.memoriesstoragexplorer.ui.composables.autoGrow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun <T> AutoGrowTabs(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    tabs: List<T>,
    tabToString: (T) -> String = { it.toString() },
    tabToKey: ((Int, T) -> Any)? = null,
    onRemoveTabAt: (Int) -> Unit,
    onTabClicked: (Int) -> Unit,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onBackground,
    onRenderTab: @Composable RowScope.(index: Int, tab: T) -> Unit = { index, tab ->
        Text(
            modifier = Modifier
                .clickable { onTabClicked(index) }
                .wrapContentSize(),
            text = tabToString(tab),
            fontSize = 14.sp,
            color = if (selectedIndex == index) selectedColor
            else unselectedColor
        )
    },
) {

    val listState = rememberLazyListState()
    val selectedIndexState by rememberUpdatedState(selectedIndex)

    LaunchedEffect(selectedIndexState) {
        listState.animateScrollToItem(selectedIndexState)
    }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        state = listState
    ) {
        itemsIndexed(items = tabs, key = tabToKey) { index, item ->
            Row(
                modifier = Modifier
                    .drawBehind {
                        if (index == selectedIndexState) {
                            drawLine(
                                color = selectedColor,
                                start = Offset(0f, size.height),
                                end = Offset(size.width - 10, size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                onRenderTab(index, item)

                if (tabs.size > 1) {
                    IconButton(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(20.dp),
                        onClick = {
                            onRemoveTabAt(index)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}