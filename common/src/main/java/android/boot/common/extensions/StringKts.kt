package android.boot.common.extensions

import java.util.Locale

fun ByteArray?.asHexString(
    separator: String = ",",
    upperCase: Boolean = true,
    prefix: String = "[",
    suffix: String = "]"
): String {
    return this?.joinToString(separator = separator, prefix = prefix, postfix = suffix) {
        String.format(Locale.CHINESE, "0x%02${if (upperCase) "X" else "x"}", it)
    } ?: "null-array"
}

fun <T> Collection<T?>?.asString(
    separator: String = ",",
    prefix: String = "[",
    suffix: String = "]",
    transform: ((T?) -> String) = { "$it" }
): String {
    return this?.joinToString(separator = separator, prefix = prefix, postfix = suffix) {
        transform(it)
    } ?: "null-collection"
}

fun <T> Set<T?>?.asString(
    separator: String = ",",
    prefix: String = "[",
    suffix: String = "]",
    transform: ((T?) -> String) = { "$it" }
): String {
    return this?.joinToString(separator = separator, prefix = prefix, postfix = suffix) {
        transform(it)
    } ?: "null-set"
}

fun <K, V> Map<K, V>?.asString(): String {
    val value = this?.entries?.joinToString(separator = ",", prefix = "{", postfix = "}") {
        "${it.key}:${it.value}"
    }
    return value ?: "null-collection"
}
