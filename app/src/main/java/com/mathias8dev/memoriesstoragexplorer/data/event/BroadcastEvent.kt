package com.mathias8dev.memoriesstoragexplorer.data.event

import com.mathias8dev.memoriesstoragexplorer.domain.FilterQuery
import com.mathias8dev.memoriesstoragexplorer.domain.enums.AddMode
import com.mathias8dev.memoriesstoragexplorer.domain.enums.LayoutMode
import com.mathias8dev.memoriesstoragexplorer.domain.enums.SortMode
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo


sealed class BroadcastEvent : Event() {

    data class HideShowBottomActionsEvent(val action: Boolean = true) : BroadcastEvent()

    data class FilterEvent(
        val queries: List<FilterQuery>
    ) : BroadcastEvent()

    data class SortEvent(
        val mode: SortMode
    ) : BroadcastEvent()

    data class LayoutEvent(
        val mode: LayoutMode
    ) : BroadcastEvent()


    data class MediaSelectedEvent(
        val selectedMedia: List<MediaInfo>
    ) : BroadcastEvent()

    data class AddEvent(val mode: AddMode) : BroadcastEvent()

    data object UnselectAllMediaEvent : BroadcastEvent()


    data object SelectAllEvent : BroadcastEvent()

    data object ReloadEvent : BroadcastEvent()

}