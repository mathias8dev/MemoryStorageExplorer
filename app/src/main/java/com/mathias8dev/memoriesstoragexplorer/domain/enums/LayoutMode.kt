package com.mathias8dev.memoriesstoragexplorer.domain.enums

import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.mathias8dev.memoriesstoragexplorer.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class LayoutMode(
    @DrawableRes val iconRes: Int? = null,
    val nameRes: Int,
) : Parcelable {
    COMPACT(iconRes = R.drawable.ic_view_compact, nameRes = R.string.compact),
    COLUMNED(iconRes = R.drawable.ic_view_columned, nameRes = R.string.columned),
    DETAILED(iconRes = R.drawable.ic_view_detailed, nameRes = R.string.detailed),
    WRAPPED(iconRes = R.drawable.ic_view_wrapped, nameRes = R.string.wrapped),
    MINIMAL(iconRes = R.drawable.ic_view_minimal, nameRes = R.string.minimal),
    GRID(iconRes = R.drawable.ic_view_grid, nameRes = R.string.grid),
    GALLERY(iconRes = R.drawable.ic_view_gallery, nameRes = R.string.gallery), ;

    companion object {
        fun withoutGallery() = entries.filter { it != GALLERY }
    }
}