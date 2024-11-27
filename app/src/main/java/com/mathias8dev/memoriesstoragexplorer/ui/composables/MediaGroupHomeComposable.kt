package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.info.AudioFilesInfoUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.info.DocumentFilesInfoUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.info.ImageFilesInfoUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.info.InstalledApplicationsSizeUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.info.InternalStorageFilesInfoUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.info.VideoFilesInfoUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.utils.koinInject
import com.mathias8dev.memoriesstoragexplorer.ui.utils.asFileReadableSize


@Composable
fun MediaGroupHomeComposable(
    modifier: Modifier = Modifier,
    onMediaGroupClick: (MediaGroup) -> Unit
) {

    suspend fun getInfo(mediaGroup: MediaGroup): String? {
        val useCase = when (mediaGroup) {
            MediaGroup.InternalStorage -> {
                val infoUseCase by koinInject<InternalStorageFilesInfoUseCase>()
                infoUseCase::invoke
            }

            MediaGroup.Image -> {
                val infoUseCase by koinInject<ImageFilesInfoUseCase>()
                infoUseCase::invoke
            }

            MediaGroup.App -> {
                val infoUseCase by koinInject<InstalledApplicationsSizeUseCase>()
                infoUseCase::invoke
            }

            MediaGroup.Video -> {
                val infoUseCase by koinInject<VideoFilesInfoUseCase>()
                infoUseCase::invoke
            }

            MediaGroup.Audio -> {
                val infoUseCase by koinInject<AudioFilesInfoUseCase>()
                infoUseCase::invoke
            }

            MediaGroup.Document -> {
                val infoUseCase by koinInject<DocumentFilesInfoUseCase>()
                infoUseCase::invoke
            }

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
            MediaGroupComposable(
                iconRes = it.iconRes,
                colorHex = it.colorHex,
                title = it.title,
                subTitle = { getInfo(it) },
                onClick = {
                    onMediaGroupClick(it)
                }
            )
        }
    }
}