package android.boot.common.extensions

import android.boot.common.provider.globalContext
import java.util.Locale

val Int.value: String
    get() {
        return runCatching { globalContext.getString(this) }.getOrElse { "*" }
    }

fun Int.value(): String {
    return this.value
}

fun Int.format(vararg args: Any?): String {
    return String.format(Locale.CHINESE, this.value, args)
}