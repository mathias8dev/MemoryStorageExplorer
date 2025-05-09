package com.mathias8dev.memoriesstoragexplorer.ui.composables.autoGrow

import androidx.compose.runtime.mutableStateListOf
import com.mathias8dev.memoriesstoragexplorer.domain.utils.RememberCoroutineScopeOwner
import com.mathias8dev.memoriesstoragexplorer.domain.utils.RememberCoroutineScopeProvider
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory


@Factory
class AutoGrowTabController : RememberCoroutineScopeProvider by RememberCoroutineScopeOwner() {

    private val _tabIndex: MutableStateFlow<Int> = MutableStateFlow(0)
    val tabIndex: StateFlow<Int> = _tabIndex
    private val _tabs = mutableStateListOf(
        AutoGrowTab("Generic 0"),
    )
    val tabs: List<AutoGrowTab> = _tabs


    suspend fun updateTabIndexBasedOnSwipe(isSwipeToLeft: Boolean = false) = coroutineScope {
        _tabIndex.value = when (isSwipeToLeft) {
            true -> _tabIndex.value.minus(1).coerceAtLeast(0)
            false -> _tabIndex.value.plus(1)
        }
        if (_tabIndex.value > tabs.size - 1) {
            _tabs.add(
                AutoGrowTab(
                    title = "Generic ${tabs.size}"
                )
            )
        }
    }

    suspend fun updateTabIndex(i: Int) = coroutineScope {
        _tabIndex.value = i
    }

    suspend fun updateTabNameAt(index: Int = tabIndex.value, title: String) = coroutineScope {
        coroutineScope.launch {
            _tabs[index] = AutoGrowTab(title = title, id = _tabs[index].id)
        }
    }

    suspend fun removeTabAt(index: Int) = coroutineScope {
        coroutineScope.launch {
            _tabs.removeAt(index)
            if (index <= tabIndex.value)
                _tabIndex.value = _tabIndex.value.minus(1).coerceAtLeast(0)
        }
    }
}