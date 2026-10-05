package com.mutazyounes.prayerathan.shell

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build

/**
 * Live battery percent from sticky [Intent.ACTION_BATTERY_CHANGED].
 * Display only. No charger control.
 */
object BatteryMonitor {
    fun percentFrom(intent: Intent?): Int {
        if (intent == null) return -1
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return -1
        return ((level * 100f) / scale).toInt().coerceIn(0, 100)
    }

    fun stickyPercent(context: Context): Int {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val sticky = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(null, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(null, filter)
        }
        return percentFrom(sticky)
    }

    fun register(
        context: Context,
        onPercent: (Int) -> Unit,
    ): BroadcastReceiver {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                onPercent(percentFrom(intent))
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }
        onPercent(stickyPercent(context))
        return receiver
    }
}
