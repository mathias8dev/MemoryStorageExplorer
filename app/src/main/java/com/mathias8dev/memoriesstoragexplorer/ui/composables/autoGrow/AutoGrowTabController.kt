package com.mathias8dev.memoriesstoragexplorer.ui.composables.autoGrow

import androidx.compose.runtime.mutableStateListOf
import com.mathias8dev.memoriesstoragexplorer.domain.utils.RememberCoroutineScopeOwner
import com.mathias8dev.memoriesstoragexplorer.domain.utils.RememberCoroutineScopeProvider
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Factory
import timber.log.Timber


@Factory
class AutoGrowTabController : RememberCoroutineScopeProvider by RememberCoroutineScopeOwner() {

    private val _tabIndex: MutableStateFlow<Int> = MutableStateFlow(0)
    val tabIndex: StateFlow<Int> = _tabIndex
    private val _tabs = mutableStateListOf(
        AutoGrowTab("Internal Storage"),
    )
    val tabs: List<AutoGrowTab> = _tabs

    // Thread safety for concurrent tab modifications
    private val tabMutex = Mutex()


    suspend fun updateTabIndexBasedOnSwipe(isSwipeToLeft: Boolean = false) = coroutineScope {
        tabMutex.withLock {
            // LOGIC FIX: Properly handle swipe boundaries and tab creation
            val newIndex = when (isSwipeToLeft) {
                true -> (_tabIndex.value - 1).coerceAtLeast(0)
                false -> (_tabIndex.value + 1).coerceAtMost(tabs.size) // Allow one beyond current size
            }

            Timber.d("updateTabIndexBasedOnSwipe: swipeLeft=$isSwipeToLeft, oldIndex=${_tabIndex.value}, newIndex=$newIndex, tabsSize=${tabs.size}")

            // If swiping to a new position beyond current tabs, create a new tab
            if (newIndex >= tabs.size) {
                _tabs.add(AutoGrowTab(title = "Internal Storage"))
                Timber.d("updateTabIndexBasedOnSwipe: Created new tab at index $newIndex")
            }

            _tabIndex.value = newIndex
        }
    }

    suspend fun updateTabIndex(i: Int) = coroutineScope {
        tabMutex.withLock {
            // LOGIC FIX: Validate index bounds
            val validIndex = i.coerceIn(0, tabs.size - 1)
            if (validIndex != i) {
                Timber.w("updateTabIndex: Index $i out of bounds, coercing to $validIndex (tabs size: ${tabs.size})")
            }
            _tabIndex.value = validIndex
            Timber.d("updateTabIndex: Set to $validIndex")
        }
    }

    suspend fun updateTabNameAt(index: Int = tabIndex.value, title: String) = coroutineScope {
        tabMutex.withLock {
            if (index !in _tabs.indices) {
                Timber.w("updateTabNameAt: Index $index out of bounds (tabs size: ${_tabs.size})")
                return@coroutineScope
            }
            _tabs[index] = AutoGrowTab(title = title, id = _tabs[index].id)
            Timber.d("updateTabNameAt: Updated tab at $index to '$title'")
        }
    }

    suspend fun removeTabAt(index: Int) = coroutineScope {
        // COROUTINE FIX: Remove nested coroutineScope.launch
        tabMutex.withLock {
            if (index !in _tabs.indices) {
                Timber.w("removeTabAt: Index $index out of bounds (tabs size: ${_tabs.size})")
                return@coroutineScope
            }

            // LOGIC FIX: Don't allow removing the last tab
            if (_tabs.size <= 1) {
                Timber.w("removeTabAt: Cannot remove last tab")
                return@coroutineScope
            }

            Timber.d("removeTabAt: Removing tab at $index (current index: ${_tabIndex.value})")
            _tabs.removeAt(index)


            // LOGIC FIX: Update current index properly
            // If we removed a tab before or at the current index, shift left
            if (index <= _tabIndex.value) {
                _tabIndex.value = (_tabIndex.value - 1).coerceIn(0, _tabs.size - 1)
                Timber.d("removeTabAt: Adjusted current index to ${_tabIndex.value}")
            }
        }
    }
}