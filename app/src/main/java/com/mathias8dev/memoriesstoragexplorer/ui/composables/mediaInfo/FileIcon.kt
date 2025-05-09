package com.mathias8dev.memoriesstoragexplorer.ui.composables.mediaInfo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import com.mathias8dev.memoriesstoragexplorer.R
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries.QueryFirstMediaInfoUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.utils.data
import com.mathias8dev.memoriesstoragexplorer.ui.composables.ImageLoaderComposable
import com.mathias8dev.memoriesstoragexplorer.ui.composables.produceResourceState
import com.mathias8dev.memoriesstoragexplorer.ui.utils.isDocumentDirectory
import com.mathias8dev.memoriesstoragexplorer.ui.utils.isImageMimeType
import com.mathias8dev.memoriesstoragexplorer.ui.utils.isMusicDirectory
import com.mathias8dev.memoriesstoragexplorer.ui.utils.isVideoDirectory
import com.mathias8dev.memoriesstoragexplorer.ui.utils.mimeData
import com.mathias8dev.memoriesstoragexplorer.ui.utils.toIconResource
import org.koin.compose.koinInject


@Composable
fun FileIcon(
    modifier: Modifier = Modifier,
    iconSizeDp: Dp = 48.dp,
    mediaInfo: MediaInfo
) {

    val localContext = LocalContext.current

    val file by remember {
        derivedStateOf {
            mediaInfo.privateContentUri!!.toFile()
        }
    }

    val mimeData = remember(file) {
        file.mimeData
    }

    val iconResource = remember(file, mimeData) {
        file.toIconResource(localContext)
    }

    @Composable
    fun Thumbnail(
        model: Any,
        modifier: Modifier = Modifier,
        size: Dp = 28.dp
    ) {
        ImageLoaderComposable(
            model = model,
            contentScale = ContentScale.Fit,
            modifier = modifier
                .size(size)
        )
    }

    Box {
        Card(modifier = modifier.size(iconSizeDp)) {
            when {
                mimeData?.isImage == true || mimeData?.isVideo == true || file.isImageMimeType() -> {
                    ImageLoaderComposable(
                        model = file,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(iconSizeDp)
                    )
                }

                else -> {
                    Thumbnail(
                        modifier = Modifier
                            .padding(if (file.isDirectory) 0.dp else 4.dp),
                        model = iconResource,
                        size = iconSizeDp
                    )
                }
            }
        }

        if (file.isDirectory) {
            val getFirstImageMediaInfoUseCase = koinInject<QueryFirstMediaInfoUseCase>()
            val modelResource by produceResourceState {
                when {
                    file.isMusicDirectory() -> R.drawable.ic_music_icon
                    file.isDocumentDirectory() -> R.drawable.ic_document_next
                    file.isVideoDirectory() -> R.drawable.ic_movie_player
                    file.name.contains("android", true) -> R.drawable.ic_android
                    file.name.contains("telegram", true) -> R.drawable.img_telegram
                    file.name.contains("whatsapp", true) -> R.drawable.img_whatsapp
                    else -> {
                        getFirstImageMediaInfoUseCase.invoke(file.absolutePath)?.privateContentUri?.toFile()
                    }
                }
            }

            modelResource.data?.let {
                Thumbnail(
                    model = it,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 3.dp, y = 2.dp)
                )
            }

        }
    }
}