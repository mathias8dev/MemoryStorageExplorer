package com.mathias8dev.memoriesstoragexplorer.ui.screens.home

import androidx.collection.mutableScatterMapOf
import androidx.compose.runtime.mutableIntStateOf
import timber.log.Timber


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


}


