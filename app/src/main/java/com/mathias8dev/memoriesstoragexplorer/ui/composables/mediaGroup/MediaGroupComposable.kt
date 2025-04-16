package com.mathias8dev.memoriesstoragexplorer.ui.composables.mediaGroup

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.R
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.info.InternalStorageFilesInfoUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.utils.Utils
import com.mathias8dev.memoriesstoragexplorer.ui.utils.asFileReadableSize
import org.koin.compose.koinInject

@Composable
fun MediaGroupComposable2(
    icon: @Composable (ColumnScope.() -> Unit)? = null,
    title: String,
    subTitle: @Composable (ColumnScope.() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Card {
                it()
            }
        }

        Column {
            Text(
                text = title,
                lineHeight = 18.sp
            )
            subTitle?.invoke(this)
        }
    }
}


@Composable
fun MediaGroupComposable(
    @DrawableRes iconRes: Int? = null,
    colorHex: Long? = null,
    title: String,
    subTitle: String? = null,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            colors = cardColors(
                containerColor = colorHex?.let { Color(it) } ?: Color.Unspecified
            )
        ) {
            iconRes?.let {
                Icon(
                    modifier = Modifier
                        .padding(5.dp)
                        .size(32.dp),
                    painter = painterResource(it),
                    tint = Color.White,
                    contentDescription = null
                )
            }
        }

        Column {
            Text(
                text = title,
                lineHeight = 18.sp
            )
            subTitle?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}


@Composable
fun MediaGroupComposable(
    @DrawableRes iconRes: Int? = null,
    colorHex: Long? = null,
    title: String,
    subTitle: (suspend () -> String?)? = null,
    onClick: () -> Unit = {},
) {
    val subtitle by produceState<String?>(".....") {
        value = subTitle?.invoke()
    }

    MediaGroupComposable(
        iconRes = iconRes,
        colorHex = colorHex,
        title = title,
        subTitle = subtitle,
        onClick = onClick
    )
}

@Composable
fun IntExtSlotComposable(
    usePathSubtitle: Boolean = true,
    onVolumeClick: (String) -> Unit = {}
) {
    val useCase = koinInject<InternalStorageFilesInfoUseCase>()
    Column {
        Utils.getStorageVolumes().map { path ->

            MediaGroupComposable(
                iconRes = R.drawable.ic_stay_primary_portrait,
                colorHex = 0xFFFCA311,
                title = Utils.getFileName(path),
                subTitle = {
                    if (usePathSubtitle) {
                        path
                    } else {
                        val (count, size) = useCase.invoke()
                        "${size.asFileReadableSize()} | $count"
                    }
                },
                onClick = { onVolumeClick(path) }
            )
        }
    }
}