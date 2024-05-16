package android.boot.common.kvstore

import android.os.Parcelable
import com.tencent.mmkv.MMKV
import android.boot.common.provider.globalContext
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <reified T> kv() = MMKVDelegate(T::class.java)

inline fun <reified T : Parcelable> parcelableKv() = MMKVParcelableDelegate(T::class.java)

@Volatile
private var initialized = false

class MMKVDelegate<T>(private val clazz: Class<T>) : ReadWriteProperty<Any?, T?> {
    private val kv by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        if (!initialized) MMKV.initialize(globalContext).also { initialized = true }
        MMKV.defaultMMKV()
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>) = when (clazz) {
        java.lang.Boolean::class.java -> kv.decodeBool(property.name, false) as T
        java.lang.Integer::class.java -> kv.decodeInt(property.name, 0) as T
        java.lang.Float::class.java -> kv.decodeFloat(property.name, 0f) as T
        java.lang.Long::class.java -> kv.decodeLong(property.name, 0) as T
        java.lang.Double::class.java -> kv.decodeDouble(property.name, 0.0) as T
        java.lang.String::class.java -> (kv.decodeString(property.name) ?: "") as T
        else -> throw IllegalAccessException("Failed to get unsupported k-v storage type:${clazz.simpleName}")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        value?.let {
            when (clazz) {
                java.lang.Boolean::class.java -> kv.encode(property.name, it as Boolean)
                java.lang.Integer::class.java -> kv.encode(property.name, it as Int)
                java.lang.Float::class.java -> kv.encode(property.name, it as Float)
                java.lang.Long::class.java -> kv.encode(property.name, it as Long)
                java.lang.Double::class.java -> kv.encode(property.name, it as Double)
                java.lang.String::class.java -> kv.encode(property.name, it as String)
                else -> throw IllegalAccessException("Failed to store unsupported k-v storage type:${clazz.simpleName}")
            }
        }
    }
}

class MMKVParcelableDelegate<T : Parcelable>(private val clazz: Class<T>) :
    ReadWriteProperty<Any?, T?> {
    private val kv by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        if (!initialized) MMKV.initialize(globalContext).also { initialized = true }
        MMKV.defaultMMKV()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>) =
        kv.decodeParcelable(property.name, clazz)


    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        kv.encode(property.name, value)
    }
}