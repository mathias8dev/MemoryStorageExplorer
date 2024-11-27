package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.R
import com.mathias8dev.memoriesstoragexplorer.domain.utils.toLocalDateTime
import com.mathias8dev.memoriesstoragexplorer.ui.utils.isImageMimeType
import com.mathias8dev.memoriesstoragexplorer.ui.utils.isMusicDirectory
import com.mathias8dev.memoriesstoragexplorer.ui.utils.mimeData
import com.mathias8dev.memoriesstoragexplorer.ui.utils.toFileFormat
import com.mathias8dev.memoriesstoragexplorer.ui.utils.toIconResource
import com.mathias8dev.memoriesstoragexplorer.ui.utils.toReadableSize
import java.io.File


@Composable
fun FileDisplay(
    modifier: Modifier = Modifier,
    file: File,
    size: DpSize = DpSize(32.dp, 32.dp),
    showDetails: Boolean = true,
    innerPaddingValues: PaddingValues = PaddingValues(0.dp),
) {


    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.Transparent
            )
            .padding(innerPaddingValues),
        verticalAlignment = Alignment.Top
    ) {
        Card(modifier = Modifier.size(size)) {
            FileImage(
                file = file,
                size = size
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .padding(horizontal = 8.dp)
                .width(0.dp)
                .weight(1F),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {

            Text(
                text = file.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 16.sp
            )

            AnimatedVisibility(showDetails) {
                file.parent?.let { parent ->
                    Text(
                        text = parent,
                        fontSize = 14.sp,
                        lineHeight = 14.sp
                    )
                }
                Text(
                    text = file.lastModified().toLocalDateTime().toFileFormat(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 12.sp
                )
                Text(
                    text = file.toReadableSize(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
fun FileImage(
    modifier: Modifier = Modifier,
    size: DpSize = DpSize(32.dp, 32.dp),
    file: File,
) {

    val localContext = LocalContext.current

    val mimeData = remember(file) {
        file.mimeData
    }

    val iconResource = remember(file, mimeData) {
        file.toIconResource(localContext)
    }


    Box(modifier = modifier) {
        when {
            mimeData?.isImage == true || mimeData?.isVideo == true || file.isImageMimeType() -> {
                ImageLoaderComposable(
                    model = file,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(size)
                )
            }

            else -> {
                ImageLoaderComposable(
                    model = iconResource,
                    modifier = Modifier
                        .padding(if (file.isDirectory) 0.dp else 4.dp)
                        .size(size)
                )
            }
        }

        if (file.isMusicDirectory()) {
            Image(
                painter = painterResource(R.drawable.ic_music_icon),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 3.dp, y = 2.dp)
                    .size(24.dp)
            )
        }
    }
}


@Preview
@Composable
private fun FileDisplayPreview() {
    FileDisplay(
        file = File("/storage/emulated/0/Download/"),
    )
}