package com.mutazyounes.prayerathan.audio

import android.media.AudioDeviceInfo

internal object AthanOutput {
    fun preferredType(availableTypes: IntArray): Int? {
        return availableTypes.firstOrNull { it == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
    }
}
