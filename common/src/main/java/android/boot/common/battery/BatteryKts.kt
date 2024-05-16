package android.boot.common.battery

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.boot.common.extensions.i
import android.boot.common.provider.globalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
    globalContext.registerReceiver(null, filter)
}

data class BatteryStatus(
    val level: Float,
    val isCharging: Boolean
)


fun batteryStatusFlow() = flow {
    "observeFlow ".i("_battery")
    while (true) {
        delay(10_000)
        emit(getBatteryStatus())
    }
}

fun getBatteryStatus(): BatteryStatus {
    val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
    val batteryPct: Float? = batteryStatus?.let { intent ->
        val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        level * 100 / scale.toFloat()
    }
    return BatteryStatus(batteryPct ?: 0f, isCharging).also {
        it.i("_battery")
    }
}