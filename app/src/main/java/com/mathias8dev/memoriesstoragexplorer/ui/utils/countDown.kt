package com.mathias8dev.memoriesstoragexplorer.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun countDown(
    initialValue: Long,
    countDownTo: Long = 0,
    step: Long = 1,
    delayMillis: Long = 1000,
    onFinished: (() -> Unit)? = null,
): State<Long> {

    return produceState(initialValue, delayMillis, step) {
        while (value > 0) {
            delay(delayMillis)
            value = (value - step).coerceAtLeast(countDownTo)
        }
        onFinished?.invoke()
    }
}


inline fun justCountDown(
    initialValue: Long,
    countDownTo: Long = 0,
    step: Long = 1,
    delayMillis: Long = 1000,
    crossinline onFinished: () -> Unit,
) {

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    coroutineScope.launch {
        var value = initialValue
        while (value > 0) {
            delay(delayMillis)
            value = (value - step).coerceAtLeast(countDownTo)
        }
        onFinished()
    }

}

suspend fun suspendCountDown(
    initialValue: Long,
    countDownTo: Long = 0,
    step: Long = 1,
    delayMillis: Long = 1000,
    onFinished: () -> Unit,
) = coroutineScope {


    var value = initialValue
    while (value > 0) {
        delay(delayMillis)
        value = (value - step).coerceAtLeast(countDownTo)
    }
    onFinished()

}