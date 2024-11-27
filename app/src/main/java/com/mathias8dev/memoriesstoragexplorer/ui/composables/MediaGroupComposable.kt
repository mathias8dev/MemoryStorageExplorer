package com.mathias8dev.memoriesstoragexplorer.ui.composables

import android.os.Parcelable
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
import kotlinx.parcelize.Parcelize


@Parcelize
enum class MediaGroup(
    val title: String,
    @DrawableRes val iconRes: Int? = null,
    val colorHex: Long? = null,
    val path: String,
) : Parcelable {

    Home(
        iconRes = R.drawable.ic_favorite,
        colorHex = 0XFF1E6091,
        title = "Home",
        path = "/data/user/0/cssm/home"
    ),

    InternalStorage(
        iconRes = R.drawable.ic_stay_primary_portrait,
        colorHex = 0xFFFCA311,
        title = "Internal storage",
        path = "/storage/emulated/0"
    ),

    Root(
        iconRes = R.drawable.ic_tag,
        colorHex = 0xFF2A9D8F,
        title = "Root",
        path = "/"
    ),

    Audio(
        iconRes = R.drawable.ic_music_note,
        colorHex = 0xFF003049,
        title = "Audio",
        path = "content://cssm/audio"
    ),

    Video(
        iconRes = R.drawable.ic_play,
        colorHex = 0xFFFF0054,
        title = "Video",
        path = "content://cssm/video"
    ),

    Image(
        iconRes = R.drawable.ic_outline_image,
        colorHex = 0xFF2A9D8F,
        title = "Image",
        path = "content://cssm/image"
    ),

    Apk(
        iconRes = R.drawable.ic_android_robot,
        colorHex = 0XFF386641,
        title = "Apk",
        path = "content://cssm/apk"
    ),

    Archive(
        iconRes = R.drawable.ic_archive_folder,
        colorHex = 0xFF2A9D8F,
        title = "Archive",
        path = "content://cssm/archive"
    ),

    Document(
        iconRes = R.drawable.ic_document,
        colorHex = 0xFFF77F00,
        title = "Document",
        path = "content://cssm/document"
    ),

    App(
        iconRes = R.drawable.ic_view_mode,
        colorHex = 0xFF2A9D8F,
        title = "Apps",
        path = "content://cssm/app"
    ),

    RecentFiles(
        iconRes = R.drawable.ic_recent,
        colorHex = 0xFFD62828,
        title = "Recent files",
        path = "content://cssm/recent"
    ),


    AllFiles(
        iconRes = R.drawable.ic_insert_drive_file,
        colorHex = 0xFF0077B6,
        title = "All Files",
        path = "content://cssm/all"
    ),

    RecycleBin(
        iconRes = R.drawable.ic_delete,
        colorHex = 0xFFD62828,
        title = "Recycle bin",
        path = "content://cssm/trash"
    ), ;

    override fun toString(): String {
        return "MediaGroup(title='$title', path='$path')"
    }

    companion object {
        fun fromPath(path: String?): MediaGroup? {
            return entries.firstOrNull { it.path == path }
        }

        val homeList = listOf(
            InternalStorage,
            App,
            Audio,
            Document,
            Image,
            Video,
        )
    }

}


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