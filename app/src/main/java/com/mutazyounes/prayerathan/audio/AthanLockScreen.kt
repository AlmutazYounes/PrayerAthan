package com.mutazyounes.prayerathan.audio

internal object AthanLockScreen {
    const val CHANNEL_ID = "athan_playback_alarm"
    const val EXTRA_OVER_LOCK = "athan_over_lock"

    fun usesFullScreen(liveAthan: Boolean): Boolean = liveAthan
}
