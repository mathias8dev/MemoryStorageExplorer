package com.mathias8dev.memoriesstoragexplorer.ui.screens.home

import androidx.collection.mutableScatterMapOf
import androidx.compose.runtime.mutableIntStateOf
import timber.log.Timber


class BackStackHolder {

    private val currentPosition = mutableIntStateOf(0)

    // The key is the tab index
    private val stack = mutableScatterMapOf<Int, List<BackStackEntry>>()

    private val lastPoppedEntries = mutableScatterMapOf<Int, BackStackEntry>()

    // Thread safety lock for concurrent access
    private val lock = Any()

    fun setCurrentPosition(position: Int) {
        currentPosition.intValue = position
        Timber.d("setCurrentPosition: $position")
    }

    fun addAt(position: Int = currentPosition.intValue, entry: BackStackEntry) {
        synchronized(lock) {
            stack[position] = stack.getOrElse(position) { emptyList() } + entry
            Timber.d("addAt: $position - $entry")
        }
    }

    fun stackSizeAt(position: Int = currentPosition.intValue): Int {
        synchronized(lock) {
            return stack[position]?.size ?: 0
        }
    }

    fun isStackEmptyAt(position: Int = currentPosition.intValue): Boolean {
        synchronized(lock) {
            return stack[position].isNullOrEmpty()
        }
    }

    fun getAt(position: Int = currentPosition.intValue): BackStackEntry? {
        synchronized(lock) {
            val entry = stack.getOrElse(position) { emptyList() }.lastOrNull()
            Timber.d("getAt: $position - $entry")
            return entry
        }
    }

    fun removeAt(position: Int = currentPosition.intValue) {
        // LOGIC FIX: Don't create empty stack entries where none existed
        synchronized(lock) {
            val currentStack = stack[position]
            if (currentStack.isNullOrEmpty()) {
                Timber.d("removeAt: Stack is empty at position $position, nothing to remove")
                return
            }

            val last = currentStack.lastOrNull()
            stack[position] = currentStack.dropLast(1)
            last?.let { lastPoppedEntries[position] = it }
            Timber.d("removeAt: $position - removed $last")
        }
    }

    fun updateLastEntryAt(position: Int = currentPosition.intValue, entry: BackStackEntry) {
        // LOGIC FIX: Validate that stack is not empty before updating
        synchronized(lock) {
            val currentStack = stack.getOrElse(position) { emptyList() }
            if (currentStack.isEmpty()) {
                Timber.w("updateLastEntryAt: Cannot update empty stack at position $position, adding instead")
                addAt(position, entry)
                return
            }
            stack[position] = currentStack.dropLast(1) + entry
            Timber.d("updateLastEntryAt: $position - updated to $entry")
        }
    }

    fun clear() {
        // MEMORY LEAK FIX: Clear both stack and lastPoppedEntries
        synchronized(lock) {
            stack.clear()
            lastPoppedEntries.clear()
            Timber.d("clear: Cleared all stacks and last popped entries")
        }
    }

    fun lastPoppedAt(position: Int = currentPosition.intValue): BackStackEntry? {
        synchronized(lock) {
            val entry = lastPoppedEntries[position]
            Timber.d("lastPoppedAt: $position - $entry")
            return entry
        }
    }


}


