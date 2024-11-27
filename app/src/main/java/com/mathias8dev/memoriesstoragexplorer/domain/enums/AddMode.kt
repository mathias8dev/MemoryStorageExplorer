package com.mathias8dev.memoriesstoragexplorer.domain.enums

import android.os.Parcelable
import com.mathias8dev.memoriesstoragexplorer.R
import kotlinx.parcelize.Parcelize


@Parcelize
enum class AddMode(
    val nameRes: Int
): Parcelable {
    FOLDER(nameRes = R.string.folder),
    FILE(nameRes = R.string.file),
}