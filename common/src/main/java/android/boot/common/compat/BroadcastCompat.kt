package android.boot.common.compat

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

object BroadcastCompat {
    @JvmStatic
    fun getPendingBroadcast(context: Context, requestCode: Int, intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    @SuppressLint("InlinedApi", "UnspecifiedRegisterReceiverFlag")
    @JvmStatic
    fun registerReceiver(
        context: Context,
        broadcastReceiver: BroadcastReceiver,
        intentFilter: IntentFilter
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(broadcastReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(broadcastReceiver, intentFilter)
        }
    }
}