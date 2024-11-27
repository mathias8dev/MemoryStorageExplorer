package com.mathias8dev.memoriesstoragexplorer.data.event


import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlin.coroutines.coroutineContext


object EventBus : EventPublisher {
    private val _events = MutableSharedFlow<Event>(replay = 4)
    val events = _events.asSharedFlow()

    override suspend fun publish(event: Event) {
        _events.emit(event)
    }

    suspend inline fun <reified T> subscribe(crossinline onEvent: (T) -> Unit) {
        events.filterIsInstance<T>()
            .collectLatest { event ->
                coroutineContext.ensureActive()
                onEvent(event)
            }
    }
}

interface EventPublisher {
    suspend fun publish(event: Event)
}

interface EventSubscriber {
    suspend fun <T> subscribe(onEvent: (T) -> Unit)
}