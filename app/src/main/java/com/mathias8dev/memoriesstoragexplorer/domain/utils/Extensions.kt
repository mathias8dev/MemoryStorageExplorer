package com.mathias8dev.memoriesstoragexplorer.domain.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


fun Context.findActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("No activity found")
}


fun <T> T?.otherwise(value: T): T = this ?: value
inline fun <T> T?.otherwise(block: () -> T): T = this ?: block()

fun <T, V> MutableMap<T, V>.removeIf(predicate: (T, V) -> Boolean) {
    val iterator = entries.iterator()
    while (iterator.hasNext()) {
        val entry = iterator.next()
        if (predicate(entry.key, entry.value)) {
            iterator.remove()
        }
    }
}

fun <T, V> MutableMap<T, V>.forEachValue(action: (V) -> Unit) {
    values.forEach(action)
}

fun Long.toLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), zoneId)


inline fun <T> Iterable<T>.sumOf(selector: (T) -> Float): Float {
    var sum: Float = 0.toFloat()
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

fun <T> T.toJson(gson: Gson = Gson()): String {
    return if (this is String || this is CharSequence) this.toString()
    else gson.toJson(this)
}


inline fun <reified T> Any.convert(gson: Gson = Gson()): T {
    return gson.fromJson(gson.toJson(this), object : TypeToken<T>() {}.type)
}
