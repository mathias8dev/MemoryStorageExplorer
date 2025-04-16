package com.mathias8dev.memoriesstoragexplorer.ui.composables.mediaGroup

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.info.AudioFilesInfoUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.info.DocumentFilesInfoUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.info.ImageFilesInfoUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.info.InstalledApplicationsSizeUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.info.VideoFilesInfoUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.utils.koinGet
import com.mathias8dev.memoriesstoragexplorer.ui.utils.asFileReadableSize


@Composable
fun MediaGroupHomeComposable(
    modifier: Modifier = Modifier,
    onMediaGroupClick: (path: String) -> Unit
) {

    suspend fun getInfo(mediaGroup: MediaGroup): String? {
        val useCase = when (mediaGroup) {
            MediaGroup.Image -> koinGet<ImageFilesInfoUseCase>()::invoke
            MediaGroup.App -> koinGet<InstalledApplicationsSizeUseCase>()::invoke
            MediaGroup.Video -> koinGet<VideoFilesInfoUseCase>()::invoke
            MediaGroup.Audio -> koinGet<AudioFilesInfoUseCase>()::invoke
            MediaGroup.Document -> koinGet<DocumentFilesInfoUseCase>()::invoke

            else -> null
        }

        return useCase?.let { func ->
            val (count, size) = func.invoke()
            "${size.asFileReadableSize()} | $count"
        }

    }

    Column(
        modifier = modifier
    ) {

        MediaGroup.homeList.forEach {
            if (it == MediaGroup.IntExtSlot) {
                IntExtSlotComposable(usePathSubtitle = false) { path ->
                    onMediaGroupClick(path)
                }
            } else {
                MediaGroupComposable(
                    iconRes = it.iconRes,
                    colorHex = it.colorHex,
                    title = stringResource(it.titleRes!!),
                    subTitle = { getInfo(it) },
                    onClick = {
                        onMediaGroupClick(it.path)
                    }
                )
            }

        }
    }
}