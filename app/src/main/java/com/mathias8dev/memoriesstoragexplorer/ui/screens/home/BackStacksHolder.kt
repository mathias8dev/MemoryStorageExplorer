package com.mathias8dev.memoriesstoragexplorer.ui.screens.home

import androidx.collection.mutableScatterMapOf
import androidx.compose.runtime.mutableIntStateOf
import timber.log.Timber


class BackStacksHolder {

    private val currentStackPosition = mutableIntStateOf(0)

    // The key is the tab index
    private val stacks = mutableMapOf<Int, List<BackStackEntry>>()

    private val lastPoppedEntries = mutableScatterMapOf<Int, BackStackEntry>()

    // Thread safety lock for concurrent access
    private val lock = Any()

    fun setCurrentStackPosition(position: Int) {
        currentStackPosition.intValue = position
        Timber.d("setCurrentPosition: $position")
    }

    fun addEntryAt(stackPosition: Int = currentStackPosition.intValue, entry: BackStackEntry) {
        synchronized(lock) {
            stacks[stackPosition] = stacks.getOrElse(stackPosition) { emptyList() } + entry
            Timber.d("addAt: $stackPosition - $entry")
        }
    }

    fun stackSizeAt(stackPosition: Int = currentStackPosition.intValue): Int {
        synchronized(lock) {
            return stacks[stackPosition]?.size ?: 0
        }
    }

    fun isStackEmptyAt(positionPosition: Int = currentStackPosition.intValue): Boolean {
        synchronized(lock) {
            return stacks[positionPosition].isNullOrEmpty()
        }
    }


    fun getEntryAt(position: Int = currentStackPosition.intValue): BackStackEntry? {
        synchronized(lock) {
            val entry = stacks.getOrElse(position) { emptyList() }.lastOrNull()
            Timber.d("getAt: $position - $entry")
            return entry
        }
    }

    fun removeLastEntryAt(position: Int = currentStackPosition.intValue) {
        // LOGIC FIX: Don't create empty stack entries where none existed
        synchronized(lock) {
            val currentStack = stacks[position]
            if (currentStack.isNullOrEmpty()) {
                Timber.d("removeAt: Stack is empty at position $position, nothing to remove")
                return
            }

            val last = currentStack.lastOrNull()
            stacks[position] = currentStack.dropLast(1)
            last?.let { lastPoppedEntries[position] = it }
            Timber.d("removeAt: $position - removed $last")
        }
    }

    fun updateLastEntryAt(position: Int = currentStackPosition.intValue, entry: BackStackEntry) {
        // LOGIC FIX: Validate that stack is not empty before updating
        synchronized(lock) {
            val currentStack = stacks.getOrElse(position) { emptyList() }
            if (currentStack.isEmpty()) {
                Timber.w("updateLastEntryAt: Cannot update empty stack at position $position, adding instead")
                addEntryAt(position, entry)
                return
            }
            stacks[position] = currentStack.dropLast(1) + entry
            Timber.d("updateLastEntryAt: $position - updated to $entry")
        }
    }

    fun deleteStackAndShiftAt(position: Int = currentStackPosition.intValue) {
        synchronized(lock) {
            // Remove the stack at the specified position
            stacks.remove(position)
            lastPoppedEntries.remove(position)
            Timber.d("deleteStackAndShiftAt: Removed stack at position $position")

            // Shift all stacks with higher positions down by 1
            val stacksToShift = stacks.keys.filter { it > position }.sorted()

            stacksToShift.forEach { oldPosition ->
                val newPosition = oldPosition - 1
                stacks[newPosition] = stacks[oldPosition]!!
                stacks.remove(oldPosition)

                // Also shift lastPoppedEntries if they exist
                lastPoppedEntries[oldPosition]?.let { entry ->
                    lastPoppedEntries[newPosition] = entry
                    lastPoppedEntries.remove(oldPosition)
                }

                Timber.d("deleteStackAndShiftAt: Shifted stack from $oldPosition to $newPosition")
            }

            // Adjust currentStackPosition if needed
            when {
                currentStackPosition.intValue == position -> {
                    // If we deleted the current stack, move to the previous one (or 0 if we deleted position 0)
                    currentStackPosition.intValue = maxOf(0, position - 1)
                    Timber.d("deleteStackAndShiftAt: Adjusted currentStackPosition to ${currentStackPosition.intValue}")
                }

                currentStackPosition.intValue > position -> {
                    // If current position is after the deleted stack, shift it down
                    currentStackPosition.intValue -= 1
                    Timber.d("deleteStackAndShiftAt: Shifted currentStackPosition down to ${currentStackPosition.intValue}")
                }
            }
        }
    }


    fun clear() {
        // MEMORY LEAK FIX: Clear both stack and lastPoppedEntries
        synchronized(lock) {
            stacks.clear()
            lastPoppedEntries.clear()
            Timber.d("clear: Cleared all stacks and last popped entries")
        }
    }


    fun lastPoppedAt(position: Int = currentStackPosition.intValue): BackStackEntry? {
        synchronized(lock) {
            val entry = lastPoppedEntries[position]
            Timber.d("lastPoppedAt: $position - $entry")
            return entry
        }
    }


}


