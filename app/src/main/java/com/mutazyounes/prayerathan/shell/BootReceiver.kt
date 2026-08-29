package com.mutazyounes.prayerathan.shell

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mutazyounes.prayerathan.PrayerAthanApp

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            return
        }
        val app = context.applicationContext as PrayerAthanApp
        val now = app.wallClock.now()
        app.athanController.schedule(app.prayerEngine.day(now), now)
    }
}
