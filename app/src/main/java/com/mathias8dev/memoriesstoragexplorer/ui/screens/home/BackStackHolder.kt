package com.mathias8dev.memoriesstoragexplorer.ui.screens.home

import android.os.Parcelable
import androidx.collection.mutableScatterMapOf
import androidx.compose.runtime.mutableIntStateOf
import com.mathias8dev.memoriesstoragexplorer.domain.FilterQuery
import com.mathias8dev.memoriesstoragexplorer.domain.enums.SortMode
import com.mathias8dev.memoriesstoragexplorer.ui.composables.MediaGroup
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.util.UUID


class BackStackHolder {

    private val currentPosition = mutableIntStateOf(0)

    // The key is the tab index
    private val stack = mutableScatterMapOf<Int, List<BackStackEntry>>()

    private val lastPoppedEntries = mutableScatterMapOf<Int, BackStackEntry>()

    fun setCurrentPosition(position: Int) {
        currentPosition.intValue = position
        Timber.d("setCurrentPosition: $position")
    }

    fun addAt(position: Int = currentPosition.intValue, entry: BackStackEntry) {
        stack[position] = stack.getOrElse(position) { emptyList() } + entry
        Timber.d("addAt: $position - $entry")
    }

    fun stackSizeAt(position: Int = currentPosition.intValue): Int {
        return stack[position]?.size ?: 0
    }

    fun isStackEmptyAt(position: Int = currentPosition.intValue): Boolean {
        return stack[position].isNullOrEmpty()
    }

    fun addOrUpdateAt(position: Int = currentPosition.intValue, entry: BackStackEntry) {
        addAt(position, entry)
    }

    fun getAt(position: Int = currentPosition.intValue): BackStackEntry? {
        val entry = stack.getOrElse(position) { emptyList() }.lastOrNull()
        Timber.d("getAt: $position - $entry")
        return entry
    }

    fun removeAt(position: Int = currentPosition.intValue) {
        val last = stack[position]?.lastOrNull()
        stack[position] = stack.getOrElse(position) { emptyList() }.dropLast(1)
        last?.let { lastPoppedEntries[position] = it }
    }

    fun updateLastEntryAt(position: Int = currentPosition.intValue, entry: BackStackEntry) {
        stack[position] = stack.getOrElse(position) { emptyList() }.dropLast(1) + entry
    }

    fun getBeforeLastAt(position: Int = currentPosition.intValue): BackStackEntry? {
        val stack = stack.getOrElse(position) { emptyList() }.toList()
        val entry = stack.dropLast(1).lastOrNull()
        Timber.d("getBeforeLastAt: $position - $entry")
        return entry
    }

    fun removeAllAt(position: Int = currentPosition.intValue) {
        stack[position] = emptyList()
    }


    fun clear() {
        stack.clear()
    }

    fun getOrAddAt(position: Int = currentPosition.intValue, backStackEntry: BackStackEntry): BackStackEntry {
        val entry = if (isStackEmptyAt(position)) {
            addAt(position, backStackEntry)
            backStackEntry
        } else {
            getAt(position)!!
        }
        Timber.d("getOrAddAt: $position - $entry")
        return entry
    }

    fun lastPoppedAt(position: Int = currentPosition.intValue): BackStackEntry? {
        val entry = lastPoppedEntries[position]
        Timber.d("lastPoppedAt: $position - $entry")
        return entry
    }

    companion object {
        val default = BackStackEntry(
            path = MediaGroup.InternalStorage.path,
            filterQueries = emptyList(),
            sortMode = SortMode.NAME_AZ
        )
    }
}


@Parcelize
data class BackStackEntry(
    val uid: String = UUID.randomUUID().toString(),
    val path: String? = MediaGroup.InternalStorage.path,
    val filterQueries: List<FilterQuery> = emptyList(),
    val sortMode: SortMode = SortMode.NAME_AZ,
) : Parcelable
