package com.mathias8dev.memoriesstoragexplorer.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.mathias8dev.memoriesstoragexplorer.ui.utils.dpToPx
import com.mathias8dev.memoriesstoragexplorer.ui.utils.pxToDp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs


@Composable
fun <T> ContextMenuComposable(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onExpandedChange: (updatedExpanded: Boolean) -> Unit,
    onActionClicked: (index: Int, clickedAction: T) -> Unit,
    actions: List<T>,
    actionToString: (item: T) -> String = { it.toString() },
    actionHolder: @Composable (onClick: () -> Unit) -> Unit,
    onDrawActionMenuItem: @Composable (item: T) -> Unit = {
        Text(
            modifier = Modifier
                .wrapContentWidth()
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 72.dp),
            text = actionToString(it),
        )
    }
) {

    var holderSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    var popupHeight by remember {
        mutableIntStateOf(0)
    }

    // Get the screen height based on the orientation
    val screenHeightDp = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
        LocalConfiguration.current.screenHeightDp
    } else {
        LocalConfiguration.current.screenWidthDp
    }
    val topPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding().value
    val availableHeight = screenHeightDp - topPadding


    val screenHeightPx = availableHeight.dp.dpToPx()

    var holderPositionInWindow: Offset by remember {
        mutableStateOf(Offset(0F, 0F))
    }

    val spacingBetweenPopupAndHolder = 6.dp.dpToPx().toInt()

    val updatedExpanded by rememberUpdatedState(expanded)


    var dismissAlreadyHandled by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    Timber.d("The holder position in window is $holderPositionInWindow")

    Timber.d("The screen height is $screenHeightPx")


    val popupMaxHeightPx = 300.dp.dpToPx()

    val popupPositionIsBottom by remember(
        holderPositionInWindow,
        screenHeightPx,
        popupMaxHeightPx,
        spacingBetweenPopupAndHolder
    ) {
        derivedStateOf {
            screenHeightPx - holderPositionInWindow.y > popupMaxHeightPx + spacingBetweenPopupAndHolder
        }
    }




    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .onGloballyPositioned {
                    holderSize = it.size
                    holderPositionInWindow = it.positionInWindow()
                }
        ) {

            actionHolder {
                if (!dismissAlreadyHandled) {
                    if (!updatedExpanded) {
                        onExpandedChange(true)
                    }
                }

                Timber.d("The position in window is $holderPositionInWindow")

            }

        }

        if (updatedExpanded) {
            Popup(
                onDismissRequest = {
                    onDismissRequest()
                    dismissAlreadyHandled = true
                    coroutineScope.launch {
                        delay(200)
                        dismissAlreadyHandled = false
                    }
                },
                offset = IntOffset(
                    x = 0,
                    y = if (popupPositionIsBottom) {
                        holderSize.height + spacingBetweenPopupAndHolder
                    } else {
                        -popupHeight
                    }
                ),
                properties = PopupProperties(
                    focusable = true,
                ),
            ) {
                Timber.d("This value is ${(abs(holderPositionInWindow.y - spacingBetweenPopupAndHolder)).pxToDp()}")
                Card(
                    modifier = Modifier
                        .wrapContentWidth()
                        .heightIn(
                            max = if (popupPositionIsBottom) (screenHeightPx - holderPositionInWindow.y - holderSize.height - spacingBetweenPopupAndHolder).pxToDp()
                            else abs(holderPositionInWindow.y - spacingBetweenPopupAndHolder)
                                .pxToDp()

                        )
                        .onGloballyPositioned {
                            popupHeight = it.size.height
                        },
                    shape = shape,
                    colors = colors,
                    elevation = elevation
                ) {

                    LazyColumn(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(vertical = 8.dp)
                    ) {

                        itemsIndexed(items = actions, key = { index, _ -> index }) { index, item ->
                            ActionMenuItem(
                                modifier = Modifier,
                                onClick = { onActionClicked(index, item) }
                            ) {
                                onDrawActionMenuItem(item)
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun ActionMenuItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {

    Box(
        modifier = Modifier
            .clickable { onClick() }
            .then(modifier)
    ) {
        content()
    }
}


@Composable
fun ContextMenuComposable(
    modifier: Modifier = Modifier,
    menuModifier: Modifier = Modifier,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onExpandedChange: (updatedExpanded: Boolean) -> Unit,
    actionHolder: @Composable (onClick: () -> Unit) -> Unit,
    onDrawMenu: @Composable ColumnScope.() -> Unit
) {

    var holderSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    var popupHeight by remember {
        mutableIntStateOf(0)
    }

    val screenHeightDp = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
        LocalConfiguration.current.screenHeightDp
    } else {
        LocalConfiguration.current.screenWidthDp
    }
    val topPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding().value
    val availableHeight = screenHeightDp - topPadding


    val screenHeightPx = availableHeight.dp.dpToPx()

    var holderPositionInWindow: Offset by remember {
        mutableStateOf(Offset(0F, 0F))
    }

    val spacingBetweenPopupAndHolder = 6.dp.dpToPx().toInt()

    val updatedExpanded by rememberUpdatedState(expanded)


    var dismissAlreadyHandled by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    Timber.d("The holder position in window is $holderPositionInWindow")


    val popupMaxHeightPx = 300.dp.dpToPx()

    val popupPositionIsBottom by remember(
        holderPositionInWindow,
        screenHeightPx,
        popupMaxHeightPx,
        spacingBetweenPopupAndHolder
    ) {
        derivedStateOf {
            screenHeightPx - holderPositionInWindow.y > popupMaxHeightPx + spacingBetweenPopupAndHolder
        }
    }




    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .onGloballyPositioned {
                    holderSize = it.size
                    holderPositionInWindow = it.positionInWindow()
                }
        ) {

            actionHolder {
                if (!dismissAlreadyHandled) {
                    if (!updatedExpanded) {
                        onExpandedChange(true)
                    }
                }

                Timber.d("The position in window is $holderPositionInWindow")

            }

        }

        if (updatedExpanded) {

            Popup(
                onDismissRequest = {
                    onDismissRequest()
                    dismissAlreadyHandled = true
                    coroutineScope.launch {
                        delay(200)
                        dismissAlreadyHandled = false
                    }
                },
                offset = IntOffset(
                    x = 0,
                    y = if (popupPositionIsBottom) {
                        holderSize.height + spacingBetweenPopupAndHolder
                    } else {
                        -popupHeight
                    }
                ),
                properties = PopupProperties(
                    focusable = true,
                )
            ) {
                Timber.d("This value is ${(abs(holderPositionInWindow.y - spacingBetweenPopupAndHolder)).pxToDp()}")
                Box(
                    modifier = menuModifier
                        .wrapContentWidth()
                        .heightIn(
                            max = if (popupPositionIsBottom) (screenHeightPx - holderPositionInWindow.y - holderSize.height - spacingBetweenPopupAndHolder).pxToDp()
                            else abs(holderPositionInWindow.y - spacingBetweenPopupAndHolder)
                                .pxToDp()

                        )
                        .onGloballyPositioned {
                            popupHeight = it.size.height
                        }
                ) {

                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(vertical = 8.dp)
                    ) {
                        onDrawMenu()
                    }

                }
            }
        }
    }
}