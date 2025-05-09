package com.mathias8dev.memoriesstoragexplorer.ui.composables.mediaGroup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.R
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.GetStorageVolumesUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.info.InternalStorageFilesInfoUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.utils.onLoading
import com.mathias8dev.memoriesstoragexplorer.domain.utils.onSuccess
import com.mathias8dev.memoriesstoragexplorer.ui.composables.ShimmerAnimation
import com.mathias8dev.memoriesstoragexplorer.ui.composables.produceResourceState
import com.mathias8dev.memoriesstoragexplorer.ui.utils.asFileReadableSize
import org.koin.compose.koinInject


@Composable
fun IntExtSlotComposable(
    usePathSubtitle: Boolean = true,
    onVolumeClick: (String) -> Unit = {}
) {
    val useCase = koinInject<InternalStorageFilesInfoUseCase>()
    val storageVolumesUseCase = koinInject<GetStorageVolumesUseCase>()

    val storageVolumesOverviewResource by produceResourceState {
        storageVolumesUseCase.invoke()
    }

    Column {
        storageVolumesOverviewResource.onLoading {
            ShimmerAnimation(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }
        storageVolumesOverviewResource.onSuccess {
            it.map { overview ->
                MediaGroupComposable(
                    iconRes = R.drawable.ic_stay_primary_portrait,
                    colorHex = 0xFFFCA311,
                    title = overview.name,
                    subTitle = {
                        if (usePathSubtitle) {
                            overview.mountPoint
                        } else {
                            val (count, size) = useCase.invoke()
                            "${size.asFileReadableSize()} | $count"
                        }

                    },
                    slot = {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            progress = { overview.usedSize.toFloat() / overview.totalSize.toFloat() },
                        )

                        Text(
                            text = overview.totalSize.asFileReadableSize() + ", " + overview.freeSize.asFileReadableSize() + " Free",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )
                    },
                    onClick = { onVolumeClick(overview.mountPoint) }
                )
            }
        }
    }
}