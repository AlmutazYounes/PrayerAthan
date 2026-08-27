package com.mutazyounes.prayerathan.shell

import android.app.Activity
import android.view.WindowManager

object KeepAwake {
    fun apply(activity: Activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
