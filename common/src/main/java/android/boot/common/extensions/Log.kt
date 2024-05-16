package android.boot.common.extensions

import android.util.Log

fun Any?.i(tag: String = "_Log", prefix: String = "") {
    if (this is Collection<*>) {
        val value = this.joinToString(separator = if (this.size < 20) "\n" else ",")
        Log.i(tag, "${prefix}collection:[$value]")
    }
    Log.i(tag, "$prefix$this")
}

fun Collection<*>?.i(tag: String = "_common_log") {
    (this?.joinToString(",", prefix = "[", postfix = "]") ?: "[null]").i(tag)
}

fun Map<*, *>?.i(tag: String = "_common_log") {
    this?.toList()?.i(tag)
}

fun Any?.i(tag: String = "_common_log") {
    Log.i(tag, "$this")
}