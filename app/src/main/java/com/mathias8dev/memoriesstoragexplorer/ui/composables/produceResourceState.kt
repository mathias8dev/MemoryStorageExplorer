package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.mathias8dev.memoriesstoragexplorer.domain.utils.Resource


@Composable
fun <T> produceResourceState(
    key1: Any? = null,
    initialState: Resource<T> = Resource.Idle(),
    executeImmediately: Boolean = true,
    producer: suspend () -> T,
): State<Resource<T>> {
    return produceState(initialValue = initialState, key1 = key1) {
        if (!executeImmediately && initialState is Resource.Idle<T>) {
            return@produceState
        }

        try {
            value = Resource.Loading()
            val result = producer.invoke()
            value = Resource.Success(result)
        } catch (e: Exception) {
            value = Resource.Error(
                message = e.localizedMessage ?: "Unknown error occurred",
                cause = e
            )
        }
    }
}