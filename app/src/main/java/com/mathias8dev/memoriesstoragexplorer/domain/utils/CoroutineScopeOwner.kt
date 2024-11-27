package com.mathias8dev.memoriesstoragexplorer.domain.utils


import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.Closeable


interface CoroutineScopeProvider : Closeable {
    val coroutineScope: CoroutineScope

    companion object {
        val Default = CoroutineScopeOwner()
    }
}

open class CoroutineScopeOwner(dispatcher: CoroutineDispatcher = Dispatchers.IO) : CoroutineScopeProvider {
    private val context = SupervisorJob() + dispatcher
    private var scope: CloseableCoroutineScope? = CloseableCoroutineScope(context)

    override val coroutineScope: CoroutineScope
        get() {
            val newScope = CloseableCoroutineScope(context)
            if (scope == null) {
                scope = newScope
            }
            return newScope
        }

    override fun close() {
        scope?.cancel()
        scope = null
    }
}

