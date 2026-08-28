package com.mutazyounes.prayerathan.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.PowerManager
import com.mutazyounes.prayerathan.engine.PrayerName

class AthanPlayer(
    private val context: Context,
) {
    private var player: MediaPlayer? = null

    fun play(prayer: PrayerName, onComplete: () -> Unit, onError: () -> Unit) {
        val store = AudioSettingsStore(context)
        if (prayer == PrayerName.SUNRISE || store.isPrayerMuted(prayer)) {
            onError()
            return
        }
        playRaw(
            AthanCatalog.rawRes(prayer, store.fajrSoundId(), store.standardSoundId()),
            onComplete,
            onError,
        )
    }

    fun playRaw(resId: Int, onComplete: () -> Unit, onError: () -> Unit) {
        stop()
        val next = MediaPlayer()
        player = next
        try {
            next.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            next.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
            val afd = context.resources.openRawResourceFd(resId)
            next.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            next.setOnCompletionListener { onComplete() }
            next.setOnErrorListener { _, _, _ ->
                onError()
                true
            }
            next.prepare()
            next.start()
        } catch (_: Exception) {
            stop()
            onError()
        }
    }

    fun stop() {
        val current = player ?: return
        player = null
        try {
            current.setOnCompletionListener(null)
            current.setOnErrorListener(null)
            if (current.isPlaying) {
                current.stop()
            }
        } catch (_: Exception) {
            // already released or not yet prepared
        }
        current.release()
    }
}
