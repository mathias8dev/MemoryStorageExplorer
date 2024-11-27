package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.domain.utils.otherwise

data class SelectedPathView(
    val path: String,
    val name: String,
    val foldersCount: Int?,
    val filesCount: Int?,
)

@Composable
fun ActionMenuCurrentPath(
    modifier: Modifier = Modifier,
    view: SelectedPathView,
    selectedDiskOverview: SelectedDiskOverview? = null,
    contentOtherwise: @Composable (ColumnScope.() -> Unit)? = null
) {

    val state by rememberUpdatedState(view)

    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    val rotationDegrees by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "RotationDegreesAnimationLabel"
    )

    ContextMenuComposable(
        modifier = modifier,
        menuModifier = Modifier
            .shadow(8.dp, RoundedCornerShape(4.dp))
            .background(color = dialogBackgroundColor(), RoundedCornerShape(4.dp))
            .wrapContentSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        expanded = expanded,
        onDismissRequest = { expanded = false },
        onExpandedChange = { expanded = it },
        actionHolder = { onClick ->
            Row(
                modifier = Modifier.clickable { onClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = state.name,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                        )
                        if (state.filesCount != null || state.foldersCount != null) {
                            Text(
                                text = when {
                                    state.filesCount != null && state.foldersCount != null -> "${state.foldersCount} folders, ${state.filesCount} files"
                                    state.filesCount != null -> "${state.filesCount} files"
                                    else -> "${state.foldersCount} folders"
                                },
                                fontSize = 12.sp,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
                selectedDiskOverview?.let {
                    Icon(
                        modifier = Modifier.rotate(rotationDegrees),
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                    )
                }
            }
        },
        onDrawMenu = {
            selectedDiskOverview?.let {
                SelectedDiskOverviewComposable(
                    selectedDiskOverview = it,
                )
            }.otherwise {
                contentOtherwise?.invoke(this)
            }
        }
    )
}