package com.mathias8dev.memoriesstoragexplorer.ui.screens.home

import android.os.Environment
import android.os.Parcelable
import com.mathias8dev.memoriesstoragexplorer.domain.FilterQuery
import com.mathias8dev.memoriesstoragexplorer.domain.enums.SortMode
import kotlinx.parcelize.Parcelize
import java.util.UUID


@Parcelize
data class BackStackEntry(
    val uid: String = UUID.randomUUID().toString(),
    val path: String? = Environment.getExternalStorageDirectory().absolutePath,
    val filterQueries: List<FilterQuery> = emptyList(),
    val sortMode: SortMode = SortMode.NAME_AZ,
) : Parcelable {

    companion object {
        val default = BackStackEntry(
            path = Environment.getExternalStorageDirectory().absolutePath,
            filterQueries = emptyList(),
            sortMode = SortMode.NAME_AZ
        )
    }
}