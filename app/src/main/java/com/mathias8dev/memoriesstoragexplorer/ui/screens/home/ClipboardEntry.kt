package com.mathias8dev.memoriesstoragexplorer.ui.screens.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.util.UUID

@Parcelize
data class ClipboardEntry(
    val uid: String = UUID.randomUUID().toString(),
    val intent: Intent,
    val time: LocalDateTime,
    val status: Status = Status.NOT_STARTED,
    val payloads: List<ClipboardEntryPayload>
) : Parcelable {

    @Parcelize
    enum class Intent : Parcelable {
        CUT, COPY
    }

    @Parcelize
    enum class Status : Parcelable {
        STARTED, NOT_STARTED
    }
}