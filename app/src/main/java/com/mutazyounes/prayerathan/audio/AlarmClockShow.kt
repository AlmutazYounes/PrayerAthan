package com.mutazyounes.prayerathan.audio

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock

/**
 * Show intent for [android.app.AlarmManager.setAlarmClock].
 * Opens the device Clock / Alarms UI, not Athan. Tapping the status-bar
 * alarm chip must not launch this app.
 */
internal fun alarmClockShowPendingIntent(context: Context, requestCode: Int): PendingIntent {
    val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return PendingIntent.getActivity(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
