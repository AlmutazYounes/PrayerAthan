package com.mutazyounes.prayerathan.audio

internal object AthanVolumeKeys {
    fun stopAfterDown(repeatCount: Int, streamVolume: Int): Boolean {
        if (repeatCount > 0) return true
        return streamVolume <= 0
    }
}
