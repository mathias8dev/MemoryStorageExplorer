package com.mathias8dev.memoriesstoragexplorer.ui.screens.home

import android.os.Parcelable
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class ClipboardEntryPayload(
    val uid: String = UUID.randomUUID().toString(),
    val status: Status = Status.NOT_STARTED,
    val mediaInfo: MediaInfo
) : Parcelable {

    @Parcelize
    enum class Status : Parcelable {
        STARTED, NOT_STARTED
    }
}