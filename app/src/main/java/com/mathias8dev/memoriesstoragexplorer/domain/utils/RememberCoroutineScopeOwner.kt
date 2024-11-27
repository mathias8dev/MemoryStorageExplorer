package com.mathias8dev.memoriesstoragexplorer.domain.utils

import androidx.compose.runtime.RememberObserver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel


interface RememberCoroutineScopeProvider : CoroutineScopeProvider, RememberObserver {

    companion object {
        val Default = RememberCoroutineScopeOwner()
    }
}


class RememberCoroutineScopeOwner(dispatcher: CoroutineDispatcher = Dispatchers.IO) : CoroutineScopeOwner(dispatcher), RememberCoroutineScopeProvider {
    override fun onAbandoned() {
        coroutineScope.cancel()
    }

    override fun onForgotten() {
        coroutineScope.cancel()
    }

    override fun onRemembered() {}
}