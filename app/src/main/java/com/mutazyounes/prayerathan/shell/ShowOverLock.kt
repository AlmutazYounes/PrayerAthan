package com.mutazyounes.prayerathan.shell

import android.app.Activity
import android.os.Build
import android.view.WindowManager

object ShowOverLock {
    @Suppress("DEPRECATION")
    fun apply(activity: Activity, show: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            activity.setShowWhenLocked(show)
            activity.setTurnScreenOn(show)
        }
        val lockFlags =
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        if (show) {
            activity.window.addFlags(lockFlags)
        } else {
            activity.window.clearFlags(lockFlags)
        }
    }
}
