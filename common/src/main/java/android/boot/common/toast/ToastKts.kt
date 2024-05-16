package android.boot.common.toast

import android.boot.common.provider.globalContext
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Toast

fun <T> T?.toast(
    duration: Int = Toast.LENGTH_SHORT,
    gravity: Int = Gravity.CENTER,
    transform: ((T?) -> CharSequence)? = { "$it" }
) {
    val text = transform?.invoke(this) ?: "$this"
    if (text.trim().isBlank()) return
    val runnable = {
        Toast.makeText(globalContext, text, duration)
            .apply { setGravity(gravity, 0, 0) }
            .show()
    }
    if (Looper.myLooper() != Looper.getMainLooper()) {
        Handler(Looper.getMainLooper()).post { runnable() }
        return
    }
    runnable()
}

fun <T> Collection<T?>?.toast(
    separator: String = ",",
    prefix: String = "",
    transform: ((T) -> CharSequence)? = { "$it" }
) {
    this?.joinToString(separator, prefix = "$prefix[", postfix = "]").toast()
}

fun <T> Array<T?>?.toast(
    separator: String = ",",
    prefix: String = "",
    transform: ((T?) -> CharSequence)? = { "$it" }
) {
    this?.joinToString(separator, prefix = "$prefix[", postfix = "]", transform = transform).toast()
}

fun a() {
    "".toast()
}