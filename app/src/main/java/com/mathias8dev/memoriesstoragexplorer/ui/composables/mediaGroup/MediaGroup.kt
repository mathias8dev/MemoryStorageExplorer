package com.mathias8dev.memoriesstoragexplorer.ui.composables.mediaGroup

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mathias8dev.memoriesstoragexplorer.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class MediaGroup(
    @StringRes val titleRes: Int? = null,
    @DrawableRes val iconRes: Int? = null,
    val colorHex: Long? = null,
    val path: String
) : Parcelable {

    Home(
        iconRes = R.drawable.ic_favorite,
        colorHex = 0XFF1E6091,
        titleRes = R.string.home,
        path = "/data/user/0/cssm/home"
    ),

    IntExtSlot(
        iconRes = R.drawable.ic_stay_primary_portrait,
        colorHex = 0xFFFCA311,
        titleRes = R.string.int_ext_slot,
        path = "content://cssm/int_ext_slot"
    ),

    Root(
        iconRes = R.drawable.ic_tag,
        colorHex = 0xFF2A9D8F,
        titleRes = R.string.root,
        path = "/"
    ),

    Audio(
        iconRes = R.drawable.ic_music_note,
        colorHex = 0xFF003049,
        titleRes = R.string.audio,
        path = "content://cssm/audio"
    ),

    Video(
        iconRes = R.drawable.ic_play,
        colorHex = 0xFFFF0054,
        titleRes = R.string.video,
        path = "content://cssm/video"
    ),

    Image(
        iconRes = R.drawable.ic_outline_image,
        colorHex = 0xFF2A9D8F,
        titleRes = R.string.image,
        path = "content://cssm/image"
    ),

    Apk(
        iconRes = R.drawable.ic_android_robot,
        colorHex = 0XFF386641,
        titleRes = R.string.apk,
        path = "content://cssm/apk"
    ),

    Archive(
        iconRes = R.drawable.ic_archive_folder,
        colorHex = 0xFF2A9D8F,
        titleRes = R.string.archive,
        path = "content://cssm/archive"
    ),

    Document(
        iconRes = R.drawable.ic_document,
        colorHex = 0xFFF77F00,
        titleRes = R.string.document,
        path = "content://cssm/document"
    ),

    App(
        iconRes = R.drawable.ic_view_mode,
        colorHex = 0xFF2A9D8F,
        titleRes = R.string.apps,
        path = "content://cssm/app"
    ),

    RecentFiles(
        iconRes = R.drawable.ic_recent,
        colorHex = 0xFFD62828,
        titleRes = R.string.recent_files,
        path = "content://cssm/recent"
    ),

    AllFiles(
        iconRes = R.drawable.ic_insert_drive_file,
        colorHex = 0xFF0077B6,
        titleRes = R.string.all_files,
        path = "content://cssm/all"
    ),

    RecycleBin(
        iconRes = R.drawable.ic_delete,
        colorHex = 0xFFD62828,
        titleRes = R.string.recycle_bin,
        path = "content://cssm/trash"
    );

    companion object {
        fun fromPath(path: String?): MediaGroup? {
            return entries.firstOrNull { it.path == path }
        }

        val homeList = listOf(
            IntExtSlot,
            App,
            Audio,
            Document,
            Image,
            Video
        )
    }


    override fun toString(): String {
        return "MediaGroup(title='$titleRes', path='$path')"
    }
}

