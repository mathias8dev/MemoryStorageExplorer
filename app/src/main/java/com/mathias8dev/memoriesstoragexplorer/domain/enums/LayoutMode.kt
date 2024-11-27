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
    COMPACT(nameRes = R.string.compact),
    COLUMNED(nameRes = R.string.columned),
    DETAILED(nameRes = R.string.detailed),
    WRAPPED(nameRes = R.string.wrapped),
    MINIMAL(nameRes = R.string.minimal),
    GRID(nameRes = R.string.grid),
    GALLERY(nameRes = R.string.gallery),
}